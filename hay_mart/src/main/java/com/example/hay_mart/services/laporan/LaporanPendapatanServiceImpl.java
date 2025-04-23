package com.example.hay_mart.services.laporan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.models.DetailPemesanan;
import com.example.hay_mart.models.LaporanPendapatan;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.enums.TipeLaporan;
import com.example.hay_mart.repositorys.LaporanPendapatanRepository;
import com.example.hay_mart.repositorys.PemesananRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Component
@RequiredArgsConstructor
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {

    private static final Logger logger = LoggerFactory.getLogger(LaporanPendapatanServiceImpl.class);

    @Autowired
    private PemesananRepository pemesananRepository;

    @Autowired
    private LaporanPendapatanRepository laporanPendapatanRepository;

    private BigDecimal hitungTotalPendapatan(List<Pemesanan> pemesananList) {
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        for (Pemesanan pemesanan : pemesananList) {
            for (DetailPemesanan detail : pemesanan.getDetails()) {
                BigDecimal hargaSatuan = BigDecimal.valueOf(detail.getHargaSatuan());
                BigDecimal jumlah = BigDecimal.valueOf(detail.getJumlah());
                totalPendapatan = totalPendapatan.add(hargaSatuan.multiply(jumlah));
            }
        }
        return totalPendapatan;
    }

    private BigDecimal hitungTotalModal(List<Pemesanan> pemesananList) {
        BigDecimal totalModal = BigDecimal.ZERO;
        for (Pemesanan pemesanan : pemesananList) {
            for (DetailPemesanan detail : pemesanan.getDetails()) {
                BigDecimal hargaModal;

                BigDecimal hargaSatuan = BigDecimal.valueOf(detail.getHargaSatuan());
                hargaModal = hargaSatuan.multiply(new BigDecimal("0.75"));
                logger.warn("Harga modal tidak ditemukan untuk produk ID: {}, menggunakan estimasi",
                        detail.getProduk() != null ? detail.getProduk().getProdukId() : "unknown");

                BigDecimal jumlah = BigDecimal.valueOf(detail.getJumlah());
                totalModal = totalModal.add(hargaModal.multiply(jumlah));
            }
        }
        return totalModal;
    }

    @Override
    @Scheduled(cron = "0 59 23 * * *")
    public void generateLaporanHarian() {
        LocalDate hariIni = LocalDate.now();
        LocalDateTime start = hariIni.atStartOfDay();
        LocalDateTime end = hariIni.atTime(LocalTime.MAX);

        logger.info("Generating laporan harian untuk tanggal: {}", hariIni);

        laporanPendapatanRepository.deleteByTanggalAndTipe(hariIni, TipeLaporan.HARIAN);

        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);
        logger.info("Jumlah pemesanan ditemukan: {}", pemesananList.size());

        if (pemesananList.isEmpty()) {
            logger.warn("Tidak ada pemesanan ditemukan untuk tanggal: {}", hariIni);
        }

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = hitungTotalModal(pemesananList);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(hariIni);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporan.setTipe(TipeLaporan.HARIAN);

        laporanPendapatanRepository.save(laporan);
        logger.info("Laporan harian berhasil dibuat: pendapatan={}, modal={}, keuntungan={}",
                totalPendapatan, totalModal, totalKeuntungan);
    }

    @Override
    @Scheduled(cron = "0 50 23 * * SUN")
    public void generateLaporanMingguan() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.with(DayOfWeek.MONDAY);
        LocalDate endDate = now;

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        logger.info("Generating laporan mingguan dari {} sampai {}", startDate, endDate);

        laporanPendapatanRepository.deleteByTanggalAndTipe(startDate, TipeLaporan.MINGGUAN);

        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);
        logger.info("Jumlah pemesanan ditemukan: {}", pemesananList.size());

        if (pemesananList.isEmpty()) {
            logger.warn("Tidak ada pemesanan ditemukan untuk minggu: {} s/d {}", startDate, endDate);
        }

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = hitungTotalModal(pemesananList);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporan.setTipe(TipeLaporan.MINGGUAN);
        laporan.setTanggalAkhir(endDate); // Tanggal akhir untuk laporan mingguan

        laporanPendapatanRepository.save(laporan);
        logger.info("Laporan mingguan berhasil dibuat: pendapatan={}, modal={}, keuntungan={}",
                totalPendapatan, totalModal, totalKeuntungan);
    }

    @Override
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateLaporanBulanan() {
        LocalDate now = LocalDate.now();
        // Mengambil bulan sebelumnya karena laporan dijalankan di awal bulan
        LocalDate bulanSebelumnya = now.minusMonths(1);
        LocalDate startDate = bulanSebelumnya.withDayOfMonth(1);
        LocalDate endDate = bulanSebelumnya.withDayOfMonth(bulanSebelumnya.lengthOfMonth());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        logger.info("Generating laporan bulanan untuk bulan: {}",
                startDate.format(DateTimeFormatter.ofPattern("MMMM yyyy")));

        laporanPendapatanRepository.deleteByTanggalAndTipe(startDate, TipeLaporan.BULANAN);

        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);
        logger.info("Jumlah pemesanan ditemukan: {}", pemesananList.size());

        if (pemesananList.isEmpty()) {
            logger.warn("Tidak ada pemesanan ditemukan untuk bulan: {} s/d {}", startDate, endDate);
        }

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = hitungTotalModal(pemesananList);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate); // Tanggal awal bulan
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporan.setTipe(TipeLaporan.BULANAN);
        laporan.setTanggalAkhir(endDate); // Tanggal akhir bulan

        laporanPendapatanRepository.save(laporan);
        logger.info("Laporan bulanan berhasil dibuat: pendapatan={}, modal={}, keuntungan={}",
                totalPendapatan, totalModal, totalKeuntungan);
    }

    @Override
    public List<LaporanPendapatanResponse> getLaporanPendapatan() {
        List<LaporanPendapatan> laporanList = laporanPendapatanRepository.findAll();
        logger.info("Mengambil {} laporan pendapatan", laporanList.size());

        return laporanList.stream()
                .map(laporan -> {
                    LaporanPendapatanResponse.LaporanPendapatanResponseBuilder builder = LaporanPendapatanResponse
                            .builder()
                            .startDate(laporan.getTanggal())
                            .pendapatan(laporan.getPendapatan())
                            .modal(laporan.getModal())
                            .keuntungan(laporan.getKeuntungan())
                            .tipe(laporan.getTipe().name());

                    if (laporan.getTanggalAkhir() != null) {
                        builder.endDate(laporan.getTanggalAkhir());
                    }

                    return builder.build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException {
        logger.info("Membuat file Excel laporan pendapatan");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LAPORAN PENDAPATAN");

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle titleStyle = createTitleStyle(workbook);
        CellStyle dateStyle = createDateStyle(workbook);
        // CellStyle numberStyle = createNumberStyle(workbook);
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle totalStyle = createTotalStyle(workbook);

        Row titleRow = sheet.createRow(0);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("LAPORAN PENDAPATAN HAY MART");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));

        Row printDateRow = sheet.createRow(1);
        Cell printDateCell = printDateRow.createCell(0);
        printDateCell
                .setCellValue("Tanggal cetak: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy")));
        printDateCell.setCellStyle(dateStyle);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));

        sheet.createRow(2);

        Row headerRow = sheet.createRow(3);
        String[] headers = { "No", "Tanggal", "Tipe Laporan", "Modal (Rp)", "Pendapatan (Rp)", "Keuntungan (Rp)" };

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        List<LaporanPendapatan> laporansList = laporanPendapatanRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        int currentIndexRow = 4;
        BigDecimal totalModal = BigDecimal.ZERO;
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalKeuntungan = BigDecimal.ZERO;

        for (int i = 0; i < laporansList.size(); i++) {
            LaporanPendapatan laporan = laporansList.get(i);
            Row bodyRow = sheet.createRow(currentIndexRow);

            // Nomor
            Cell cell = bodyRow.createCell(0);
            cell.setCellValue(i + 1);

            // Tanggal
            cell = bodyRow.createCell(1);
            String tanggalStr = laporan.getTanggal().format(formatter);
            if (laporan.getTanggalAkhir() != null && !laporan.getTipe().equals(TipeLaporan.HARIAN)) {
                tanggalStr += " s/d " + laporan.getTanggalAkhir().format(formatter);
            }
            cell.setCellValue(tanggalStr);

            // Tipe Laporan
            cell = bodyRow.createCell(2);
            switch (laporan.getTipe()) {
                case HARIAN:
                    cell.setCellValue("Harian");
                    break;
                case MINGGUAN:
                    cell.setCellValue("Mingguan");
                    break;
                case BULANAN:
                    cell.setCellValue("Bulanan");
                    break;
            }

            // Modal
            cell = bodyRow.createCell(3);
            cell.setCellValue(laporan.getModal().doubleValue());
            cell.setCellStyle(currencyStyle);
            totalModal = totalModal.add(laporan.getModal());

            // Pendapatan
            cell = bodyRow.createCell(4);
            cell.setCellValue(laporan.getPendapatan().doubleValue());
            cell.setCellStyle(currencyStyle);
            totalPendapatan = totalPendapatan.add(laporan.getPendapatan());

            // Keuntungan
            cell = bodyRow.createCell(5);
            cell.setCellValue(laporan.getKeuntungan().doubleValue());
            cell.setCellStyle(currencyStyle);
            totalKeuntungan = totalKeuntungan.add(laporan.getKeuntungan());

            currentIndexRow++;
        }

        Row totalRow = sheet.createRow(currentIndexRow + 1);

        Cell totalLabelCell = totalRow.createCell(0);
        totalLabelCell.setCellValue("TOTAL");
        totalLabelCell.setCellStyle(totalStyle);
        sheet.addMergedRegion(new CellRangeAddress(currentIndexRow + 1, currentIndexRow + 1, 0, 2));

        Cell totalModalCell = totalRow.createCell(3);
        totalModalCell.setCellValue(totalModal.doubleValue());
        totalModalCell.setCellStyle(totalStyle);

        Cell totalPendapatanCell = totalRow.createCell(4);
        totalPendapatanCell.setCellValue(totalPendapatan.doubleValue());
        totalPendapatanCell.setCellStyle(totalStyle);

        Cell totalKeuntunganCell = totalRow.createCell(5);
        totalKeuntunganCell.setCellValue(totalKeuntungan.doubleValue());
        totalKeuntunganCell.setCellStyle(totalStyle);

        // Menambahkan catatan kaki
        Row footerRow = sheet.createRow(currentIndexRow + 3);
        Cell footerCell = footerRow.createCell(0);
        footerCell.setCellValue("Laporan ini dibuat secara otomatis oleh sistem HAY MART.");
        sheet.addMergedRegion(new CellRangeAddress(currentIndexRow + 3, currentIndexRow + 3, 0, 5));

        // Set lebar kolom otomatis
        for (int i = 0; i < 6; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
            logger.info("File Excel berhasil dibuat");
        } finally {
            workbook.close();
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    // Helper methods untuk membuat cell style
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        return style;
    }

    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 18);
        font.setBold(true);
        style.setFont(font);

        return style;
    }

    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 10);
        font.setItalic(true);
        style.setFont(font);

        return style;
    }

    // private CellStyle createNumberStyle(Workbook workbook) {
    //     CellStyle style = workbook.createCellStyle();
    //     style.setBorderBottom(BorderStyle.THIN);
    //     style.setBorderTop(BorderStyle.THIN);
    //     style.setBorderLeft(BorderStyle.THIN);
    //     style.setBorderRight(BorderStyle.THIN);
    //     style.setAlignment(HorizontalAlignment.CENTER);

    //     return style;
    // }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));

        return style;
    }

    private CellStyle createTotalStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.RIGHT);

        XSSFFont font = ((XSSFWorkbook) workbook).createFont();
        font.setFontName("Arial");
        font.setBold(true);
        style.setFont(font);

        DataFormat format = workbook.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0"));

        return style;
    }
}