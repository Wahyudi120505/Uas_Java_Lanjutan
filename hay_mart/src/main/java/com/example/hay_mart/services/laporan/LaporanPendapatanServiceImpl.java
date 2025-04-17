package com.example.hay_mart.services.laporan;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
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
import com.example.hay_mart.repositorys.LaporanPendapatanRepository;
import com.example.hay_mart.repositorys.PemesananRepository;
import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {

    @Autowired
    private PemesananRepository pemesananRepository;

    @Autowired
    private LaporanPendapatanRepository laporanPendapatanRepository;

    private static final BigDecimal PERSEN_MODAL = new BigDecimal("0.85");

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

    @Override
    @Scheduled(cron = "0 59 23 * * *")
    public void generateLaporanHarian() {
        LocalDate hariIni = LocalDate.now();
        LocalDateTime start = hariIni.atStartOfDay();
        LocalDateTime end = hariIni.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggal(hariIni);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(hariIni);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporanPendapatanRepository.save(laporan);
    }

    @Override
    @Scheduled(cron = "0 50 23 * * SUN")
    public void generateLaporanMingguan() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.with(DayOfWeek.MONDAY);
        LocalDate endDate = now;

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggalBetween(startDate, endDate);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);

        laporanPendapatanRepository.save(laporan);
    }

    @Override
    @Scheduled(cron = "0 1 0 1 * *")
    public void generateLaporanBulanan() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        laporanPendapatanRepository.deleteByTanggalBetween(startDate, endDate);
        List<Pemesanan> pemesananList = pemesananRepository.findByTanggalPembelianBetween(start, end);

        BigDecimal totalPendapatan = hitungTotalPendapatan(pemesananList);
        BigDecimal totalModal = totalPendapatan.multiply(PERSEN_MODAL);
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatan laporan = new LaporanPendapatan();
        laporan.setTanggal(startDate);
        laporan.setTanggal(endDate);
        laporan.setPendapatan(totalPendapatan);
        laporan.setModal(totalModal);
        laporan.setKeuntungan(totalKeuntungan);
        laporanPendapatanRepository.save(laporan);
    }

    @Override
    public List<LaporanPendapatanResponse> getLaporanPendapatan() {
        return laporanPendapatanRepository.findAll().stream()
                .map(laporan -> LaporanPendapatanResponse.builder()
                        .startDate(laporan.getTanggal())
                        .pendapatan(laporan.getPendapatan())
                        .modal(laporan.getModal())
                        .keuntungan(laporan.getKeuntungan())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("LAPORAN PENDAPATAN");
        CellStyle headerStyle = workbook.createCellStyle();

        headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        XSSFFont headerFont = ((XSSFWorkbook) workbook).createFont();
        headerFont.setFontName("Times New Roman");
        headerFont.setFontHeightInPoints((short) 16);
        headerFont.setBold(true);
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        headerStyle.setFont(headerFont);

        Row headerRow = sheet.createRow(0);
        Cell idCell = headerRow.createCell(0);
        idCell.setCellValue("No");
        idCell.setCellStyle(headerStyle);

        Cell tanggalCell = headerRow.createCell(1);
        tanggalCell.setCellValue("Tanggal");
        tanggalCell.setCellStyle(headerStyle);

        Cell modalCell = headerRow.createCell(2);
        modalCell.setCellValue("Modal");
        modalCell.setCellStyle(headerStyle);

        Cell pendapatanCell = headerRow.createCell(3);
        pendapatanCell.setCellValue("Pendapatan");
        pendapatanCell.setCellStyle(headerStyle);

        Cell keuntunganCell = headerRow.createCell(4);
        keuntunganCell.setCellValue("Keuntungan");
        keuntunganCell.setCellStyle(headerStyle);

        List<LaporanPendapatan> laporansList = laporanPendapatanRepository.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        int currentIndexRow = 1;
        for (LaporanPendapatan laporans : laporansList) {
            Row bodyRow = sheet.createRow(currentIndexRow);

            Cell cell = bodyRow.createCell(0);
            cell.setCellValue(laporans.getLaporanPendapatanId());

            cell = bodyRow.createCell(1);
            cell.setCellValue(laporans.getTanggal().format(formatter));

            cell = bodyRow.createCell(2);
            cell.setCellValue(laporans.getModal().doubleValue());

            cell = bodyRow.createCell(3);
            cell.setCellValue(laporans.getPendapatan().doubleValue());

            cell = bodyRow.createCell(4);
            cell.setCellValue(laporans.getKeuntungan().doubleValue());

            currentIndexRow++;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            workbook.write(outputStream);
        } finally {
            workbook.close();
        }

        return new ByteArrayInputStream(outputStream.toByteArray());
    }
}