package com.example.hay_mart.services.laporan;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import com.example.hay_mart.dto.laporan.LaporanPendapatanResponse;
import com.example.hay_mart.models.Pemesanan;
import com.example.hay_mart.models.User;
import com.example.hay_mart.repositorys.PemesananRepository;
import com.example.hay_mart.repositorys.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@Component
@RequiredArgsConstructor
public class LaporanPendapatanServiceImpl implements LaporanPendapatanService {
    @Autowired
    private PemesananRepository pemesananRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<LaporanPendapatanResponse> generateLaporanHarian() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUsersByEmail(auth.getName());

        List<Pemesanan> semuaPemesanan = pemesananRepository.findByUserKasir(user);

        Map<LocalDate, List<Pemesanan>> pemesananPerTanggal = semuaPemesanan.stream()
                .collect(Collectors.groupingBy(p -> p.getTanggalPembelian().toLocalDate()));

        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        BigDecimal persenModal = new BigDecimal("0.85");

        for (Map.Entry<LocalDate, List<Pemesanan>> entry : pemesananPerTanggal.entrySet()) {
            LocalDate tanggal = entry.getKey();
            List<Pemesanan> pesananPerTanggal = entry.getValue();

            BigDecimal totalPendapatan = BigDecimal.ZERO;
            BigDecimal totalModal = BigDecimal.ZERO;

            for (Pemesanan pemesanan : pesananPerTanggal) {
                BigDecimal harga = BigDecimal.valueOf(pemesanan.getTotalHarga());
                totalPendapatan = totalPendapatan.add(harga);
                totalModal = totalModal.add(harga.multiply(persenModal));
            }

            BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

            LaporanPendapatanResponse harian = new LaporanPendapatanResponse();
            harian.setPeriode(tanggal.toString());
            harian.setPendapatan(totalPendapatan);
            harian.setModal(totalModal);
            harian.setKeuntungan(totalKeuntungan);

            laporanList.add(harian);
        }

        laporanList.sort(Comparator.comparing(LaporanPendapatanResponse::getPeriode));
        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanMingguan() {
        LocalDate today = LocalDate.now();

        LocalDate currentEndOfWeek = today.with(DayOfWeek.SUNDAY);
        LocalDate currentStartOfWeek = currentEndOfWeek.minusDays(6);
        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();

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
                mingguan.setPeriode(currentStartOfWeek + " - " + currentEndOfWeek);
                mingguan.setPendapatan(totalPendapatan);
                mingguan.setModal(totalModal);
                mingguan.setKeuntungan(totalKeuntungan);

                laporanList.add(mingguan);
            }
            currentEndOfWeek = currentEndOfWeek.minusWeeks(1);
            currentStartOfWeek = currentEndOfWeek.minusDays(6);
        }

        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanBulanan() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUsersByEmail(auth.getName());

        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        LocalDate current = LocalDate.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        for (int i = 0; i < 11; i++) {
            LocalDate bulan = current.minusMonths(i);
            LocalDate firstDay = bulan.withDayOfMonth(1);
            LocalDate lastDay = bulan.withDayOfMonth(bulan.lengthOfMonth());

            List<Pemesanan> pemesananBulanan = pemesananRepository.bulanan(
                    user,
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
                bulanan.setPendapatan(totalPendapatan);
                bulanan.setModal(totalModal);
                bulanan.setKeuntungan(totalKeuntungan);

                laporanList.add(bulanan);
            }
        }

        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> generateLaporanTahunan() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUsersByEmail(auth.getName());

        List<LaporanPendapatanResponse> laporanList = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy");

        int currentYear = LocalDate.now().getYear();

        for (int i = 0; i < 5; i++) {
            int year = currentYear - i;
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<Pemesanan> pemesananTahunan = pemesananRepository.findByUserAndTanggalPembelianBetween(
                    user,
                    startOfYear.atStartOfDay(),
                    endOfYear.atTime(23, 59, 59));

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
                tahunan.setPendapatan(totalPendapatan);
                tahunan.setModal(totalModal);
                tahunan.setKeuntungan(totalKeuntungan);

                laporanList.add(tahunan);
            }
        }
        return laporanList;
    }

    @Override
    public List<LaporanPendapatanResponse> laporanPendapatan(LocalDate startDate, LocalDate endDate) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findUsersByEmail(auth.getName());

        BigDecimal totalPendapatan = BigDecimal.ZERO;
        BigDecimal totalModal = BigDecimal.ZERO;

        List<Pemesanan> pendapatan = pemesananRepository.pendapatan(user,
                LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), 00, 00, 00),
                LocalDateTime.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(), 23, 59, 59));

        for (Pemesanan pemesanan : pendapatan) {
            totalPendapatan = totalPendapatan.add(BigDecimal.valueOf(pemesanan.getTotalHarga()));
        }

        List<Pemesanan> modal = pemesananRepository.modal(user,
                LocalDateTime.of(startDate.getYear(), startDate.getMonth(), startDate.getDayOfMonth(), 00, 00, 00),
                LocalDateTime.of(endDate.getYear(), endDate.getMonth(), endDate.getDayOfMonth(), 23, 59, 59));

        for (Pemesanan pemesanan : modal) {
            BigDecimal harga = BigDecimal.valueOf(pemesanan.getTotalHarga());
            totalModal = totalModal.add(harga.multiply(new BigDecimal("0.85")));
        }

        BigDecimal totalKeuntungan = totalPendapatan.subtract(totalModal);

        LaporanPendapatanResponse response = new LaporanPendapatanResponse();
        response.setPeriode(startDate.toString() + " - " + endDate.toString()); 
        response.setPendapatan(totalPendapatan);
        response.setModal(totalModal);
        response.setKeuntungan(totalKeuntungan);

        return Collections.singletonList(response); 
    }

    @Override
    public ByteArrayInputStream generateExcel(List<LaporanPendapatanResponse> data) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Laporan Pendapatan");

            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 16);
            titleFont.setColor(IndexedColors.WHITE.getIndex());
            titleStyle.setFont(titleFont);
            titleStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            titleStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);
            titleStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("LAPORAN PENDAPATAN HAY_MART");
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
            titleCell.setCellStyle(titleStyle);

            Row footerRow = sheet.createRow(1);
            Cell footerCell = footerRow.createCell(2);
            String tanggalLaporan = "Laporan dicetak: " + new SimpleDateFormat("dd-MM-yyyy").format(new Date());
            footerCell.setCellValue(tanggalLaporan);

            CellStyle footerStyle = workbook.createCellStyle();
            footerStyle.setAlignment(HorizontalAlignment.RIGHT);
            footerCell.setCellStyle(footerStyle);

            Row headerRow = sheet.createRow(3);
            String[] headers = { "Tanggal", "Modal", "Pendapatan", "Keuntungan" };
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 4;
            for (LaporanPendapatanResponse laporan : data) {
                if (laporan == null || laporan.getPeriode() == null)
                    continue;

                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(laporan.getPeriode());
                row.createCell(1).setCellValue(laporan.getModal() != null ? laporan.getModal().toString() : "0");
                row.createCell(2)
                        .setCellValue(laporan.getPendapatan() != null ? laporan.getPendapatan().toString() : "0");
                row.createCell(3)
                        .setCellValue(laporan.getKeuntungan() != null ? laporan.getKeuntungan().toString() : "0");

                for (int i = 0; i < 4; i++) {
                    Cell cell = row.getCell(i);
                    if (cell == null) {
                        cell = row.createCell(i);
                    }
                    CellStyle dataStyle = workbook.createCellStyle();
                    dataStyle.setBorderBottom(BorderStyle.THIN);
                    dataStyle.setBorderTop(BorderStyle.THIN);
                    dataStyle.setBorderLeft(BorderStyle.THIN);
                    dataStyle.setBorderRight(BorderStyle.THIN);
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < 4; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}