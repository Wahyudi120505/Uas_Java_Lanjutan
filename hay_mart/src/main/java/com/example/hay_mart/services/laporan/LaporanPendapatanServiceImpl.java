package com.example.hay_mart.services.laporan;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xddf.usermodel.XDDFColor;
import org.apache.poi.xddf.usermodel.XDDFShapeProperties;
import org.apache.poi.xddf.usermodel.XDDFSolidFillProperties;
import org.apache.poi.xddf.usermodel.chart.*;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.repositorys.PemesananRepository;
import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
@SuppressWarnings("deprecation")
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {
    @Autowired
    private PemesananRepository pemesananRepository;

    @Override
    public List<LaporanPendapatanResponse> generateLaporanHarian() {
        LocalDate hariIni = LocalDate.now();
    
        List<Pemesanan> semuaPemesanan = pemesananRepository.findAll();
    
        List<Pemesanan> pemesananHariIni = semuaPemesanan.stream()
                .filter(p -> p.getTanggalPembelian().toLocalDate().equals(hariIni))
                .collect(Collectors.toList());
    
        System.out.println("Jumlah pemesanan hari ini (" + hariIni + "): " + pemesananHariIni.size());
    
        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        
        if (pemesananHariIni.isEmpty()) {
            System.out.println("Tidak ada pemesanan untuk hari ini.");
            return null;
        }
    
        BigDecimal persenModal = new BigDecimal("0.85");
    
        DecimalFormat indonesianFormat = new DecimalFormat("#,###.00");
        indonesianFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(Locale.forLanguageTag("id-ID")));
    
        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalModal = BigDecimal.ZERO;
    
        for (Pemesanan pemesanan : pemesananHariIni) {
            BigDecimal harga = BigDecimal.valueOf(pemesanan.getTotalHarga());
            totalPendapatan = totalPendapatan.add(harga);
            totalModal = totalModal.add(harga.multiply(persenModal));
            System.out.println("  Pemesanan ID: " + pemesanan.getPemesananId() + " | Total Harga: " + harga);
        }
    
        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);
    
        LaporanPendapatanResponse harian = new LaporanPendapatanResponse();
        harian.setPeriode(hariIni.toString());
        harian.setPendapatan(totalPendapatan.setScale(2, RoundingMode.HALF_UP));
        harian.setModal(totalModal.setScale(2, RoundingMode.HALF_UP));
        harian.setKeuntungan(totalKeuntungan.setScale(2, RoundingMode.HALF_UP));
    
        laporanList.add(harian);
    
        System.out.println(">> Pendapatan: " + indonesianFormat.format(totalPendapatan) +
                " | Modal: " + indonesianFormat.format(totalModal) +
                " | Keuntungan: " + indonesianFormat.format(totalKeuntungan));
    
        laporanList.sort(Comparator.comparing(LaporanPendapatanResponse::getPeriode));
    
        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanMingguan() {
        LocalDate today = LocalDate.now();

        LocalDate currentEndOfWeek = today.with(DayOfWeek.SUNDAY);
        LocalDate currentStartOfWeek = currentEndOfWeek.minusDays(6);
        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();

        DecimalFormat indonesianFormat = new DecimalFormat("#,###.00");
        indonesianFormat.setDecimalFormatSymbols(
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
        );
        
        for (int i = 0; i < 5; i++) {
            LocalDateTime startDate = currentStartOfWeek.atStartOfDay();
            LocalDateTime endDate = currentEndOfWeek.atTime(23, 59, 59);

            List<Pemesanan> pemesananMingguan = pemesananRepository.findByTanggalPembelianBetween(startDate, endDate);

            if (!pemesananMingguan.isEmpty()) {
                BigDecimal totalPendapatan = BigDecimal.ZERO;
                BigDecimal totalModal = BigDecimal.ZERO;
                BigDecimal persenModal = new BigDecimal("0.85");

                for (Pemesanan pemesanan : pemesananMingguan) {
                    BigDecimal harga = BigDecimal.valueOf(pemesanan.getTotalHarga());
                    totalPendapatan = totalPendapatan.add(harga);
                    totalModal = totalModal.add(harga.multiply(persenModal));
                }

                BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

                LaporanPendapatanResponse mingguan = new LaporanPendapatanResponse();
                mingguan.setPeriode(currentStartOfWeek + "/" + currentEndOfWeek);
                mingguan.setPendapatan(totalPendapatan.setScale(2, RoundingMode.HALF_UP));
                mingguan.setModal(totalModal.setScale(2, RoundingMode.HALF_UP));
                mingguan.setKeuntungan(totalKeuntungan.setScale(2, RoundingMode.HALF_UP));

                laporanList.add(mingguan);

                System.out.println(">> Minggu: " + mingguan.getPeriode() +
                        " | Pendapatan: " + indonesianFormat.format(totalPendapatan) +
                        " | Modal: " + indonesianFormat.format(totalModal) +
                        " | Keuntungan: " + indonesianFormat.format(totalKeuntungan));
            }
            currentEndOfWeek = currentEndOfWeek.minusWeeks(1);
            currentStartOfWeek = currentEndOfWeek.minusDays(6);
        }

        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanBulanan() {
        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        LocalDate current = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        DecimalFormat indonesianFormat = new DecimalFormat("#,###.00");
        indonesianFormat.setDecimalFormatSymbols(
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
        );
        
        for (int i = 0; i < 11; i++) {
            LocalDate bulan = current.minusMonths(i);
            LocalDate firstDay = bulan.withDayOfMonth(1);
            LocalDate lastDay = bulan.withDayOfMonth(bulan.lengthOfMonth());

            List<Pemesanan> pemesananBulanan = pemesananRepository.findByTanggalPembelianBetween(
                    firstDay.atStartOfDay(),
                    lastDay.atTime(23, 59, 59));

            if (!pemesananBulanan.isEmpty()) {
                BigDecimal totalPendapatan = pemesananBulanan.stream()
                        .map(p -> BigDecimal.valueOf(p.getTotalHarga()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal persenModal = new BigDecimal("0.85");
                BigDecimal totalModal = pemesananBulanan.stream()
                        .map(p -> BigDecimal.valueOf(p.getTotalHarga()).multiply(persenModal))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

                LaporanPendapatanResponse bulanan = new LaporanPendapatanResponse();
                bulanan.setPeriode(firstDay.format(formatter) + " - " + lastDay.format(formatter));
                bulanan.setPendapatan(totalPendapatan.setScale(2, RoundingMode.HALF_UP));
                bulanan.setModal(totalModal.setScale(2, RoundingMode.HALF_UP));
                bulanan.setKeuntungan(totalKeuntungan.setScale(2, RoundingMode.HALF_UP));

                laporanList.add(bulanan);

                System.out.println(">> Bulan: " + bulanan.getPeriode() +
                        " | Pendapatan: " + indonesianFormat.format(totalPendapatan) +
                        " | Modal: " + indonesianFormat.format(totalModal) +
                        " | Keuntungan: " + indonesianFormat.format(totalKeuntungan));
            }
        }

        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanTahunan() {
        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        DecimalFormat indonesianFormat = new DecimalFormat("#,###.00");
        indonesianFormat.setDecimalFormatSymbols(
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
        );
        
        int currentYear = LocalDate.now().getYear();

        for (int i = 0; i < 5; i++) {
            int year = currentYear - i;
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<Pemesanan> pemesananTahunan = pemesananRepository
                    .findByTanggalPembelianBetween(startOfYear.atStartOfDay(), endOfYear.atTime(23, 59, 59));

            if (!pemesananTahunan.isEmpty()) {
                BigDecimal totalPendapatan = pemesananTahunan.stream()
                        .map(p -> BigDecimal.valueOf(p.getTotalHarga()))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal persenModal = new BigDecimal("0.85");
                BigDecimal totalModal = pemesananTahunan.stream()
                        .map(p -> BigDecimal.valueOf(p.getTotalHarga()).multiply(persenModal))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

                LaporanPendapatanResponse tahunan = new LaporanPendapatanResponse();
                tahunan.setPeriode(startOfYear.format(formatter) + " - " + endOfYear.format(formatter));
                tahunan.setPendapatan(totalPendapatan.setScale(2, RoundingMode.HALF_UP));
                tahunan.setModal(totalModal.setScale(2, RoundingMode.HALF_UP));
                tahunan.setKeuntungan(totalKeuntungan.setScale(2, RoundingMode.HALF_UP));

                laporanList.add(tahunan);

                System.out.println(">> Tahun: " + year +
                        " | Pendapatan: " + indonesianFormat.format(totalPendapatan) +
                        " | Modal: " + indonesianFormat.format(totalModal) +
                        " | Keuntungan: " + indonesianFormat.format(totalKeuntungan));
            }
        }
        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> laporanPendapatan(LocalDate startDate, LocalDate endDate) {

        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalModal = BigDecimal.ZERO;

        DecimalFormat indonesianFormat = new DecimalFormat("#,###.00");
        indonesianFormat.setDecimalFormatSymbols(
            new DecimalFormatSymbols(Locale.forLanguageTag("id-ID"))
        );
        
        List<Pemesanan> pendapatan = pemesananRepository.pendapatan(
                LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), 00, 00, 00),
                LocalDateTime.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(), 23, 59, 59));

        for (Pemesanan pemesanan : pendapatan) {
            totalPendapatan = totalPendapatan.add(BigDecimal.valueOf(pemesanan.getTotalHarga()));
        }

        List<Pemesanan> modal = pemesananRepository.modal(
                LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), 00, 00, 00),
                LocalDateTime.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(), 23, 59, 59));

        for (Pemesanan pemesanan : modal) {
            BigDecimal harga = BigDecimal.valueOf(pemesanan.getTotalHarga());
            totalModal = totalModal.add(harga.multiply(new BigDecimal("0.85")));
        }

        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatanResponse response = new LaporanPendapatanResponse();
        response.setPeriode(startDate.toString() + " - " + endDate.toString());
        response.setPendapatan(totalPendapatan.setScale(2, RoundingMode.HALF_UP));
        response.setModal(totalModal.setScale(2, RoundingMode.HALF_UP));
        response.setKeuntungan(totalKeuntungan.setScale(2, RoundingMode.HALF_UP));

        System.out.println(">> Periode: " + response.getPeriode() +
                " | Pendapatan: " + indonesianFormat.format(totalPendapatan) +
                " | Modal: " + indonesianFormat.format(totalModal) +
                " | Keuntungan: " + indonesianFormat.format(totalKeuntungan));

        System.out.println("Start datetime: " + startDate.atStartOfDay());
        System.out.println("End datetime: " + endDate.atTime(23, 59, 59));
        System.out.println("Data pendapatan ditemukan: " + pendapatan.size());
        System.out.println("Data modal ditemukan: " + modal.size());

        return Collections.singletonList(response);
    }

    @Override
    public ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Laporan Pendapatan");

            DataFormat format = workbook.createDataFormat();

            IndexedColors PRIMARY_COLOR = IndexedColors.DARK_BLUE; 
            IndexedColors ACCENT_COLOR = IndexedColors.LIGHT_BLUE; 
            IndexedColors HIGHLIGHT_COLOR = IndexedColors.GOLD; 

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 20);
            titleFont.setFontName("Calibri");
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(PRIMARY_COLOR.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            titleStyle.setBorderBottom(BorderStyle.MEDIUM);

            CellStyle subtitleStyle = workbook.createCellStyle();
            Font subtitleFont = workbook.createFont();
            subtitleFont.setBold(true);
            subtitleFont.setFontHeightInPoints((short) 12);
            subtitleFont.setFontName("Calibri");
            subtitleFont.setColor(PRIMARY_COLOR.getIndex());
            subtitleStyle.setFont(subtitleFont);
            subtitleStyle.setAlignment(HorizontalAlignment.CENTER);
            subtitleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerFont.setFontName("Calibri");
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(PRIMARY_COLOR.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            CellStyle subheaderStyle = workbook.createCellStyle();
            Font subheaderFont = workbook.createFont();
            subheaderFont.setBold(true);
            subheaderFont.setFontHeightInPoints((short) 10);
            subheaderFont.setFontName("Calibri");
            subheaderFont.setColor(IndexedColors.WHITE.getIndex());
            subheaderStyle.setFont(subheaderFont);
            subheaderStyle.setFillForegroundColor(ACCENT_COLOR.getIndex());
            subheaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            subheaderStyle.setAlignment(HorizontalAlignment.CENTER);
            subheaderStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            subheaderStyle.setBorderBottom(BorderStyle.THIN);
            subheaderStyle.setBorderTop(BorderStyle.THIN);
            subheaderStyle.setBorderLeft(BorderStyle.THIN);
            subheaderStyle.setBorderRight(BorderStyle.THIN);

            CellStyle currencyStyle = workbook.createCellStyle();
            currencyStyle.setDataFormat(format.getFormat("Rp#,##0.00"));
            currencyStyle.setBorderBottom(BorderStyle.THIN);
            currencyStyle.setBorderTop(BorderStyle.THIN);
            currencyStyle.setBorderLeft(BorderStyle.THIN);
            currencyStyle.setBorderRight(BorderStyle.THIN);
            currencyStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle textStyleEven = workbook.createCellStyle();
            textStyleEven.setBorderBottom(BorderStyle.THIN);
            textStyleEven.setBorderTop(BorderStyle.THIN);
            textStyleEven.setBorderLeft(BorderStyle.THIN);
            textStyleEven.setBorderRight(BorderStyle.THIN);

            CellStyle textStyleOdd = workbook.createCellStyle();
            textStyleOdd.setBorderBottom(BorderStyle.THIN);
            textStyleOdd.setBorderTop(BorderStyle.THIN);
            textStyleOdd.setBorderLeft(BorderStyle.THIN);
            textStyleOdd.setBorderRight(BorderStyle.THIN);
            textStyleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            textStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle currencyStyleEven = workbook.createCellStyle();
            currencyStyleEven.setDataFormat(format.getFormat("Rp#,##0.00"));
            currencyStyleEven.setBorderBottom(BorderStyle.THIN);
            currencyStyleEven.setBorderTop(BorderStyle.THIN);
            currencyStyleEven.setBorderLeft(BorderStyle.THIN);
            currencyStyleEven.setBorderRight(BorderStyle.THIN);
            currencyStyleEven.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle currencyStyleOdd = workbook.createCellStyle();
            currencyStyleOdd.setDataFormat(format.getFormat("Rp#,##0.00"));
            currencyStyleOdd.setBorderBottom(BorderStyle.THIN);
            currencyStyleOdd.setBorderTop(BorderStyle.THIN);
            currencyStyleOdd.setBorderLeft(BorderStyle.THIN);
            currencyStyleOdd.setBorderRight(BorderStyle.THIN);
            currencyStyleOdd.setAlignment(HorizontalAlignment.RIGHT);
            currencyStyleOdd.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            currencyStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle totalLabelStyle = workbook.createCellStyle();
            Font totalFont = workbook.createFont();
            totalFont.setBold(true);
            totalFont.setFontHeightInPoints((short) 11);
            totalFont.setColor(IndexedColors.WHITE.getIndex());
            totalLabelStyle.setFont(totalFont);
            totalLabelStyle.setFillForegroundColor(PRIMARY_COLOR.getIndex());
            totalLabelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalLabelStyle.setBorderBottom(BorderStyle.MEDIUM);
            totalLabelStyle.setBorderTop(BorderStyle.MEDIUM);
            totalLabelStyle.setBorderLeft(BorderStyle.MEDIUM);
            totalLabelStyle.setBorderRight(BorderStyle.MEDIUM);
            totalLabelStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle totalValueStyle = workbook.createCellStyle();
            totalValueStyle.setFont(totalFont);
            totalValueStyle.setDataFormat(format.getFormat("Rp#,##0.00"));
            totalValueStyle.setFillForegroundColor(HIGHLIGHT_COLOR.getIndex());
            totalValueStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            totalValueStyle.setBorderBottom(BorderStyle.MEDIUM);
            totalValueStyle.setBorderTop(BorderStyle.MEDIUM);
            totalValueStyle.setBorderLeft(BorderStyle.MEDIUM);
            totalValueStyle.setBorderRight(BorderStyle.MEDIUM);
            totalValueStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle dateStyle = workbook.createCellStyle();
            Font dateFont = workbook.createFont();
            dateFont.setItalic(true);
            dateFont.setFontHeightInPoints((short) 10);
            dateStyle.setFont(dateFont);
            dateStyle.setAlignment(HorizontalAlignment.RIGHT);

            CellStyle companyStyle = workbook.createCellStyle();
            Font companyFont = workbook.createFont();
            companyFont.setBold(true);
            companyFont.setFontHeightInPoints((short) 12);
            companyFont.setColor(PRIMARY_COLOR.getIndex());
            companyStyle.setFont(companyFont);
            companyStyle.setAlignment(HorizontalAlignment.LEFT);

            CellStyle footerStyle = workbook.createCellStyle();
            Font footerFont = workbook.createFont();
            footerFont.setFontHeightInPoints((short) 8);
            footerFont.setItalic(true);
            footerStyle.setFont(footerFont);
            footerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row companyRow1 = sheet.createRow(0);
            companyRow1.setHeight((short) 400);
            Cell companyNameCell = companyRow1.createCell(0);
            companyNameCell.setCellValue("HAY MART");
            companyNameCell.setCellStyle(companyStyle);

            Row companyRow2 = sheet.createRow(1);
            Cell companyAddressCell = companyRow2.createCell(0);
            companyAddressCell.setCellValue("Jl. Sukaraja No. 123, Bandung");
            companyAddressCell.setCellStyle(textStyleEven);

            Row companyRow3 = sheet.createRow(2);
            Cell companyContactCell = companyRow3.createCell(0);
            companyContactCell.setCellValue("Telp: (021) 123-4567 | Email: info@haymart.com");
            companyContactCell.setCellStyle(textStyleEven);

            sheet.createRow(3).setHeight((short) 200);

            Row titleRow = sheet.createRow(4);
            titleRow.setHeight((short) 800);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LAPORAN PENDAPATAN");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(4, 4, 0, 5));

            Row subtitleRow = sheet.createRow(5);
            subtitleRow.setHeight((short) 400);
            Cell subtitleCell = subtitleRow.createCell(0);
            subtitleCell.setCellValue("HAY MART - ANALISIS KEUANGAN");
            subtitleCell.setCellStyle(subtitleStyle);
            sheet.addMergedRegion(new CellRangeAddress(5, 5, 0, 5));

            Row dateRow = sheet.createRow(6);
            Cell dateCell = dateRow.createCell(3);
            dateCell.setCellValue("Tanggal Cetak: " + new SimpleDateFormat("dd MMMM yyyy").format(new Date()));
            dateCell.setCellStyle(dateStyle);
            sheet.addMergedRegion(new CellRangeAddress(6, 6, 3, 5));

            sheet.createRow(7).setHeight((short) 300);

            Row headerRow = sheet.createRow(8);
            headerRow.setHeight((short) 450);
            String[] headers = { "No", "Periode", "Modal", "Pendapatan", "Keuntungan", "Margin (%)" };

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 9;
            double totalModal = 0, totalPendapatan = 0, totalKeuntungan = 0;
            int rowCounter = 1;

            for (LaporanPendapatanResponse laporan : data) {
                if (laporan == null || laporan.getPeriode() == null)
                    continue;

                Row row = sheet.createRow(rowNum++);

                CellStyle currentTextStyle = (rowCounter % 2 == 0) ? textStyleEven : textStyleOdd;
                CellStyle currentCurrencyStyle = (rowCounter % 2 == 0) ? currencyStyleEven : currencyStyleOdd;

                // No 
                Cell noCell = row.createCell(0);
                noCell.setCellValue(rowCounter);
                noCell.setCellStyle(currentTextStyle);

                // Periode
                Cell periodeCell = row.createCell(1);
                periodeCell.setCellValue(laporan.getPeriode());
                periodeCell.setCellStyle(currentTextStyle);

                // Modal
                Cell modalCell = row.createCell(2);
                double modal = laporan.getModal() != null ? laporan.getModal().doubleValue() : 0.0;
                modalCell.setCellValue(modal);
                modalCell.setCellStyle(currentCurrencyStyle);
                totalModal += modal;

                // Pendapatan
                Cell pendapatanCell = row.createCell(3);
                double pendapatan = laporan.getPendapatan() != null ? laporan.getPendapatan().doubleValue() : 0.0;
                pendapatanCell.setCellValue(pendapatan);
                pendapatanCell.setCellStyle(currentCurrencyStyle);
                totalPendapatan += pendapatan;

                // Keuntungan
                Cell keuntunganCell = row.createCell(4);
                double keuntungan = laporan.getKeuntungan() != null ? laporan.getKeuntungan().doubleValue() : 0.0;
                keuntunganCell.setCellValue(keuntungan);
                keuntunganCell.setCellStyle(currentCurrencyStyle);
                totalKeuntungan += keuntungan;

                // Margin (%)
                Cell marginCell = row.createCell(5);
                double margin = (modal > 0) ? (keuntungan / modal) * 100 : 0;
                marginCell.setCellValue(String.format("%.2f%%", margin));
                marginCell.setCellStyle(currentTextStyle);

                rowCounter++;
            }

            sheet.createRow(rowNum++).setHeight((short) 200);

            Row totalRow = sheet.createRow(rowNum++);
            totalRow.setHeight((short) 500);

            Cell totalLabelCell = totalRow.createCell(0);
            totalLabelCell.setCellValue("TOTAL");
            totalLabelCell.setCellStyle(totalLabelStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 1));

            Cell totalModalCell = totalRow.createCell(2);
            totalModalCell.setCellValue(totalModal);
            totalModalCell.setCellStyle(totalValueStyle);

            Cell totalPendapatanCell = totalRow.createCell(3);
            totalPendapatanCell.setCellValue(totalPendapatan);
            totalPendapatanCell.setCellStyle(totalValueStyle);

            Cell totalKeuntunganCell = totalRow.createCell(4);
            totalKeuntunganCell.setCellValue(totalKeuntungan);
            totalKeuntunganCell.setCellStyle(totalValueStyle);

            Cell totalMarginCell = totalRow.createCell(5);
            double totalMargin = (totalModal > 0) ? (totalKeuntungan / totalModal) * 100 : 0;
            totalMarginCell.setCellValue(String.format("%.2f%%", totalMargin));
            totalMarginCell.setCellStyle(totalLabelStyle);

            rowNum += 2;
            Row summaryHeaderRow = sheet.createRow(rowNum++);
            Cell summaryHeaderCell = summaryHeaderRow.createCell(0);
            summaryHeaderCell.setCellValue("RINGKASAN KINERJA KEUANGAN");
            summaryHeaderCell.setCellStyle(subheaderStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 5));

            String[] summaryLabels = {
                    "Total Modal Investasi",
                    "Total Pendapatan Usaha",
                    "Total Keuntungan Bersih",
                    "Margin Keuntungan (%)",
                    "Rasio Pendapatan terhadap Modal"
            };

            Object[] summaryValues = {
                    totalModal,
                    totalPendapatan,
                    totalKeuntungan,
                    String.format("%.2f%%", totalMargin),
                    String.format("%.2f", totalModal > 0 ? totalPendapatan / totalModal : 0)
            };

            for (int i = 0; i < summaryLabels.length; i++) {
                Row summaryRow = sheet.createRow(rowNum++);

                Cell labelCell = summaryRow.createCell(0);
                labelCell.setCellValue(summaryLabels[i]);
                labelCell.setCellStyle(textStyleEven);
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 0, 2));

                Cell valueCell = summaryRow.createCell(3);
                if (summaryValues[i] instanceof Double) {
                    valueCell.setCellValue((Double) summaryValues[i]);
                    valueCell.setCellStyle(currencyStyleEven);
                } else {
                    valueCell.setCellValue(summaryValues[i].toString());
                    valueCell.setCellStyle(textStyleEven);
                }
                sheet.addMergedRegion(new CellRangeAddress(rowNum - 1, rowNum - 1, 3, 5));
            }

            if (!data.isEmpty()) {
                Sheet chartSheet = workbook.createSheet("Grafik Pendapatan");

                int chartDataStartRow = 0;

                Row chartHeaderRow = chartSheet.createRow(chartDataStartRow++);
                String[] header = {"Periode", "Modal", "Pendapatan", "Keuntungan" };
                for (int i = 0; i < header.length; i++) {
                    chartHeaderRow.createCell(i).setCellValue(header[i]);
                }

                int sourceRowStart = 9; 
                for (int i = 0; i < data.size(); i++) {
                    Row sourceRow = sheet.getRow(sourceRowStart + i);
                    Row chartDataRow = chartSheet.createRow(chartDataStartRow++);

                    chartDataRow.createCell(0).setCellValue(sourceRow.getCell(1).getStringCellValue());
                    chartDataRow.createCell(1).setCellValue(sourceRow.getCell(2).getNumericCellValue());
                    chartDataRow.createCell(2).setCellValue(sourceRow.getCell(3).getNumericCellValue());
                    chartDataRow.createCell(3).setCellValue(sourceRow.getCell(4).getNumericCellValue());
                }

                XSSFSheet xssfChartSheet = (XSSFSheet) chartSheet;
                XSSFDrawing drawing = xssfChartSheet.createDrawingPatriarch();

                XSSFClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 10, 25);

                XSSFChart chart = drawing.createChart(anchor);

                chart.setTitleText("Grafik Pendapatan dan Keuntungan");
                chart.setTitleOverlay(false);

                chart.getOrAddLegend().setPosition(LegendPosition.BOTTOM);

                XDDFCategoryAxis bottomAxis = chart.createCategoryAxis(AxisPosition.BOTTOM);
                bottomAxis.setTitle("Periode");

                XDDFValueAxis leftAxis = chart.createValueAxis(AxisPosition.LEFT);
                leftAxis.setTitle("Nilai (Rp)");
                leftAxis.setCrosses(AxisCrosses.AUTO_ZERO);

                XDDFDataSource<String> periodeSeries = XDDFDataSourcesFactory.fromStringCellRange(
                        xssfChartSheet,
                        new CellRangeAddress(1, data.size(), 0, 0));

                XDDFNumericalDataSource<Double> modalSeries = XDDFDataSourcesFactory.fromNumericCellRange(
                        xssfChartSheet,
                        new CellRangeAddress(1, data.size(), 1, 1));

                XDDFNumericalDataSource<Double> pendapatanSeries = XDDFDataSourcesFactory.fromNumericCellRange(
                        xssfChartSheet,
                        new CellRangeAddress(1, data.size(), 2, 2));

                XDDFNumericalDataSource<Double> keuntunganSeries = XDDFDataSourcesFactory.fromNumericCellRange(
                        xssfChartSheet,
                        new CellRangeAddress(1, data.size(), 3, 3));

                XDDFChartData chartData = chart.createData(ChartTypes.BAR, bottomAxis, leftAxis);

                XDDFChartData.Series series1 = chartData.addSeries(periodeSeries, modalSeries);
                series1.setTitle("Modal", null);

                XDDFChartData.Series series2 = chartData.addSeries(periodeSeries, pendapatanSeries);
                series2.setTitle("Pendapatan", null);

                XDDFChartData.Series series3 = chartData.addSeries(periodeSeries, keuntunganSeries);
                series3.setTitle("Keuntungan", null);

                chart.plot(chartData);

                for (int i = 0; i < chartData.getSeries().size(); i++) {
                    XDDFSolidFillProperties fill = new XDDFSolidFillProperties();
                    if (i == 0) {
                        fill.setColor(XDDFColor.from(new byte[] { (byte) 0, (byte) 102, (byte) 204 })); // Modal: Biru
                    } else if (i == 1) {
                        fill.setColor(XDDFColor.from(new byte[] { (byte) 0, (byte) 204, (byte) 102 })); // Pendapatan:
                                                                                                        // Hijau
                    } else {
                        fill.setColor(XDDFColor.from(new byte[] { (byte) 204, (byte) 102, (byte) 0 })); // Keuntungan:
                                                                                                        // Oranye
                    }
                    XDDFShapeProperties shapeProperties = new XDDFShapeProperties();
                    shapeProperties.setFillProperties(fill);
                    chartData.getSeries().get(i).setShapeProperties(shapeProperties);
                }
            }

            Row footerRow = sheet.createRow(rowNum + 3);
            Cell footerCell = footerRow.createCell(0);
            footerCell.setCellValue("© HAY MART " + Calendar.getInstance().get(Calendar.YEAR)
                    + " - Laporan ini dibuat secara otomatis dan valid tanpa tanda tangan.");
            footerCell.setCellStyle(footerStyle);
            sheet.addMergedRegion(new CellRangeAddress(rowNum + 3, rowNum + 3, 0, 5));

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 1000, 255 * 256));
            }

            sheet.createFreezePane(0, 9);

            workbook.setActiveSheet(0);

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}