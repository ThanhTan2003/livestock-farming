package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.ChiTietXuatBanDTO;
import com.livestock.livestock_farming.dto.PhieuXuatBanDTO;
import com.livestock.livestock_farming.entity.ChiTietXuatBan;
import com.livestock.livestock_farming.entity.KhuVucChuong;
import com.livestock.livestock_farming.entity.PhieuXuatBan;
import com.livestock.livestock_farming.entity.VatNuoi;
import com.livestock.livestock_farming.enums.TrangThaiVatNuoi;
import com.livestock.livestock_farming.mapper.PhieuXuatBanMapper;
import com.livestock.livestock_farming.repository.ChiTietXuatBanRepository;
import com.livestock.livestock_farming.repository.KhuVucChuongRepository;
import com.livestock.livestock_farming.repository.PhieuXuatBanRepository;
import com.livestock.livestock_farming.repository.VatNuoiRepository;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.aspose.cells.PdfSaveOptions;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.apache.poi.ss.usermodel.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PhieuXuatBanService {
    private final PhieuXuatBanRepository phieuXuatBanRepository;
    private final ChiTietXuatBanRepository chiTietXuatBanRepository;
    private final VatNuoiRepository vatNuoiRepository;
    private final KhuVucChuongRepository khuVucChuongRepository;
    private final PhieuXuatBanMapper phieuXuatBanMapper;

    // 1. Tìm kiếm
    public PageResponse<PhieuXuatBanDTO> timKiem(String tuKhoa, LocalDate tuNgay, LocalDate denNgay, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayXuat").descending());
        String tuKhoaNoAccents = (tuKhoa != null && !tuKhoa.isBlank()) ? com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tuKhoa).toLowerCase() : null;
        Page<PhieuXuatBan> pageResult = phieuXuatBanRepository.timKiem(tuKhoaNoAccents, tuNgay, denNgay, pageable);
        List<PhieuXuatBanDTO> content = pageResult.getContent().stream()
                .map(phieuXuatBanMapper::toDTO).collect(Collectors.toList());

        return PageResponse.<PhieuXuatBanDTO>builder()
                .page(page).size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content).build();
    }

    // 2. Lấy chi tiết
    public PhieuXuatBanDTO layChiTiet(Long id) {
        PhieuXuatBan entity = phieuXuatBanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu xuất không tồn tại"));
        return phieuXuatBanMapper.toDTO(entity);
    }

    // 3. Tạo phiếu xuất (Logic phức tạp)
    @Transactional
    public PhieuXuatBanDTO taoPhieuXuat(PhieuXuatBanDTO dto) {
        PhieuXuatBan phieu = new PhieuXuatBan();

        // Tạo mã phiếu tự động nếu không có
        if (dto.getMaPhieu() == null || dto.getMaPhieu().isEmpty()) {
            phieu.setMaPhieu("PX-" + System.currentTimeMillis());
        } else {
            if (phieuXuatBanRepository.existsByMaPhieu(dto.getMaPhieu())) {
                throw new IllegalArgumentException("Mã phiếu đã tồn tại");
            }
            phieu.setMaPhieu(dto.getMaPhieu());
        }

        phieu.setNgayXuat(dto.getNgayXuat() != null ? dto.getNgayXuat() : LocalDate.now());
        phieu.setTenKhachHang(dto.getTenKhachHang());
        phieu.setSoDienThoai(dto.getSoDienThoai());
        phieu.setDiaChi(dto.getDiaChi());
        phieu.setGhiChu(dto.getGhiChu());

        List<ChiTietXuatBan> chiTietList = new ArrayList<>();
        double tongTien = 0;
        double tongKhoiLuong = 0;
        int tongSoLuong = 0;

        if (dto.getDanhSachChiTiet() != null) {
            for (ChiTietXuatBanDTO ctDto : dto.getDanhSachChiTiet()) {
                VatNuoi vatNuoi = vatNuoiRepository.findById(ctDto.getIdVatNuoi())
                        .orElseThrow(() -> new IllegalArgumentException("Vật nuôi ID " + ctDto.getIdVatNuoi() + " không tồn tại"));

                if (!TrangThaiVatNuoi.BINH_THUONG.equals(vatNuoi.getTrangThai())) {
                    throw new IllegalArgumentException("Vật nuôi " + vatNuoi.getMaDinhDanh() + " không ở trạng thái bình thường để bán");
                }

                // Cập nhật trạng thái vật nuôi -> ĐÃ XUẤT BÁN
                vatNuoi.setTrangThai(TrangThaiVatNuoi.DA_XUAT_BAN);
                vatNuoi.setKhoiLuongHienTai(ctDto.getKhoiLuongXuat()); // Cập nhật khối lượng lúc bán

                // Giảm số lượng trong chuồng
                if (vatNuoi.getKhuVucChuong() != null) {
                    KhuVucChuong chuong = vatNuoi.getKhuVucChuong();
                    chuong.setSoLuongHienTai(Math.max(0, chuong.getSoLuongHienTai() - 1));
                    khuVucChuongRepository.save(chuong);
                }
                vatNuoiRepository.save(vatNuoi);

                // Tạo chi tiết
                ChiTietXuatBan ct = new ChiTietXuatBan();
                ct.setPhieuXuatBan(phieu);
                ct.setVatNuoi(vatNuoi);
                ct.setKhoiLuongXuat(ctDto.getKhoiLuongXuat());
                ct.setDonGia(ctDto.getDonGia());

                // Tính thành tiền (Khối lượng * Đơn giá hoặc theo con tùy logic, ở đây giả sử theo khối lượng)
                double thanhTien = ctDto.getKhoiLuongXuat() * ctDto.getDonGia();
                ct.setThanhTien(thanhTien);

                chiTietList.add(ct);

                // Cộng dồn
                tongTien += thanhTien;
                tongKhoiLuong += ctDto.getKhoiLuongXuat();
                tongSoLuong++;
            }
        }

        phieu.setChiTietXuatBanList(chiTietList);
        phieu.setTongTien(tongTien);
        phieu.setTongKhoiLuong(tongKhoiLuong);
        phieu.setTongSoLuong(tongSoLuong);

        return phieuXuatBanMapper.toDTO(phieuXuatBanRepository.save(phieu));
    }

    // 4. Xóa phiếu (Phải hoàn tác trạng thái vật nuôi)
    @Transactional
    public void xoaPhieu(Long id) {
        PhieuXuatBan phieu = phieuXuatBanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Phiếu không tồn tại"));

        // Hoàn tác trạng thái vật nuôi
        for (ChiTietXuatBan ct : phieu.getChiTietXuatBanList()) {
            VatNuoi vn = ct.getVatNuoi();
            if (vn != null) {
                vn.setTrangThai(TrangThaiVatNuoi.BINH_THUONG);
                // Cộng lại số lượng vào chuồng
                if (vn.getKhuVucChuong() != null) {
                    KhuVucChuong chuong = vn.getKhuVucChuong();
                    chuong.setSoLuongHienTai(chuong.getSoLuongHienTai() + 1);
                    khuVucChuongRepository.save(chuong);
                }
                vatNuoiRepository.save(vn);
            }
        }
        phieuXuatBanRepository.delete(phieu);
    }

    public byte[] exportPhieuXuatPdf(PhieuXuatBan phieu) throws IOException {
        // Đường dẫn file template trong folder resources
        String templatePath = "/templates/phieuxuat_templates.xlsx";

        try (InputStream is = getClass().getResourceAsStream(templatePath);
             XSSFWorkbook workbook = (is != null) ? new XSSFWorkbook(is) : new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            if (is == null) {
                throw new IOException("Không tìm thấy file template tại: " + templatePath);
            }

            XSSFSheet sheet = workbook.getSheetAt(0);

            // ================= 1. KHỞI TẠO STYLE =================
            Font fontCommon = workbook.createFont();
            fontCommon.setFontName("Times New Roman");
            fontCommon.setFontHeightInPoints((short) 13);

            // Style cho bảng (Có viền)
            CellStyle styleTable = workbook.createCellStyle();
            styleTable.setFont(fontCommon);
            styleTable.setBorderTop(BorderStyle.THIN);
            styleTable.setBorderBottom(BorderStyle.THIN);
            styleTable.setBorderLeft(BorderStyle.THIN);
            styleTable.setBorderRight(BorderStyle.THIN);
            styleTable.setWrapText(true);
            styleTable.setVerticalAlignment(VerticalAlignment.CENTER);

            // Style căn giữa
            CellStyle styleCenter = workbook.createCellStyle();
            styleCenter.cloneStyleFrom(styleTable);
            styleCenter.setAlignment(HorizontalAlignment.CENTER);

            // Style tiền tệ (Căn phải, format số)
            DataFormat format = workbook.createDataFormat();
            CellStyle styleCurrency = workbook.createCellStyle();
            styleCurrency.cloneStyleFrom(styleTable);
            styleCurrency.setAlignment(HorizontalAlignment.RIGHT);
            styleCurrency.setDataFormat(format.getFormat("#,##0"));

            // Style cho Header (Không viền)
            CellStyle styleInfo = workbook.createCellStyle();
            styleInfo.setFont(fontCommon);
            styleInfo.setWrapText(true);

            // Style đậm cho Tổng cộng
            CellStyle styleBold = workbook.createCellStyle();
            styleBold.cloneStyleFrom(styleTable);
            Font fontBold = workbook.createFont();
            fontBold.setFontName("Times New Roman");
            fontBold.setFontHeightInPoints((short) 13);
            fontBold.setBold(true);
            styleBold.setFont(fontBold);

            CellStyle styleCurrencyBold = workbook.createCellStyle();
            styleCurrencyBold.cloneStyleFrom(styleCurrency);
            styleCurrencyBold.setFont(fontBold);

            // ================= 2. ĐIỀN THÔNG TIN HEADER =================
            // C12: Mã phiếu
            setCellValue(sheet, 11, 2, phieu.getMaPhieu(), styleInfo);

            // E12: Ngày lập
            String ngayLap = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            setCellValue(sheet, 11, 4, ngayLap, styleInfo);

            // C13: Khách hàng
            setCellValue(sheet, 12, 2, phieu.getTenKhachHang(), styleInfo);

            // E13: Số điện thoại
            setCellValue(sheet, 12, 4, phieu.getSoDienThoai(), styleInfo);

            // C14: Địa chỉ
            setCellValue(sheet, 13, 2, phieu.getDiaChi(), styleInfo);

            // ================= 3. ĐIỀN DỮ LIỆU BẢNG =================
            List<ChiTietXuatBan> listChiTiet = phieu.getChiTietXuatBanList();
            int startRow = 16; // Dòng 17 Excel

            // Shift rows nếu dữ liệu > 1 dòng
            if (listChiTiet != null && listChiTiet.size() > 1) {
                sheet.shiftRows(startRow + 1, sheet.getLastRowNum(), listChiTiet.size() - 1);
            }

            BigDecimal totalAmount = BigDecimal.ZERO;
            int currentRow = startRow;

            if (listChiTiet != null) {
                for (int i = 0; i < listChiTiet.size(); i++) {
                    ChiTietXuatBan ct = listChiTiet.get(i);
                    Row row = getOrCreateRow(sheet, currentRow);
                    row.setHeightInPoints(25);

                    // A: STT
                    createCell(row, 0, (i + 1), styleCenter);

                    // B: Mã vật nuôi
                    String maVatNuoi = (ct.getVatNuoi() != null) ? ct.getVatNuoi().getMaDinhDanh() : "";
                    createCell(row, 1, maVatNuoi, styleTable);

                    // C: Tên vật nuôi
                    String tenVatNuoi = "";
                    if (ct.getVatNuoi() != null && ct.getVatNuoi().getLoaiVatNuoi() != null) {
                        tenVatNuoi = ct.getVatNuoi().getLoaiVatNuoi().getTenHienThi();
                        if (ct.getVatNuoi().getGiongLoai() != null) {
                            tenVatNuoi += " - " + ct.getVatNuoi().getGiongLoai();
                        }
                    }
                    createCell(row, 2, tenVatNuoi, styleTable);

                    // D: Khối lượng xuất
                    createCell(row, 3, ct.getKhoiLuongXuat(), styleCenter);

                    // E: Thành tiền (FIXED HERE)
                    // Lấy giá trị Double từ entity
                    Double valThanhTien = ct.getThanhTien();

                    // Convert Double -> BigDecimal để tính toán
                    BigDecimal thanhTien = (valThanhTien != null)
                            ? BigDecimal.valueOf(valThanhTien)
                            : BigDecimal.ZERO;

                    createCell(row, 4, thanhTien, styleCurrency);

                    totalAmount = totalAmount.add(thanhTien);
                    currentRow++;
                }
            }

            // ================= 4. DÒNG TỔNG CỘNG =================
            Row totalRow = getOrCreateRow(sheet, currentRow);
            totalRow.setHeightInPoints(30);

            // Merge A->D
            CellRangeAddress mergedRegion = new CellRangeAddress(currentRow, currentRow, 0, 3);
            sheet.addMergedRegion(mergedRegion);

            // Label Tổng cộng
            Cell cellTotalText = totalRow.createCell(0);
            cellTotalText.setCellValue("Tổng cộng");
            cellTotalText.setCellStyle(styleBold);

            // Kẻ viền cho vùng merge
            for (int i = 1; i <= 3; i++) {
                totalRow.createCell(i).setCellStyle(styleBold);
            }

            // Giá trị Tổng tiền
            createCell(totalRow, 4, totalAmount, styleCurrencyBold);

            // ================= 5. XUẤT RA PDF =================
            workbook.write(out);
            byte[] excelBytes = out.toByteArray();

            return convertExcelToPdf(excelBytes);
        }
    }

    // --- Các hàm tiện ích (Helper Methods) ---

    private void setCellValue(XSSFSheet sheet, int r, int c, Object value, CellStyle style) {
        Row row = getOrCreateRow(sheet, r);
        Cell cell = row.getCell(c);
        if (cell == null) cell = row.createCell(c);

        if (style != null) cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (style != null) cell.setCellStyle(style);

        if (value == null) {
            cell.setCellValue("");
        } else if (value instanceof Integer) {
            cell.setCellValue((Integer) value);
        } else if (value instanceof Double) {
            cell.setCellValue((Double) value);
        } else if (value instanceof BigDecimal) {
            cell.setCellValue(((BigDecimal) value).doubleValue());
        } else {
            cell.setCellValue(value.toString());
        }
    }

    private Row getOrCreateRow(XSSFSheet sheet, int rowIndex) {
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            row = sheet.createRow(rowIndex);
        }
        return row;
    }

    // Hàm chuyển đổi Excel -> PDF dùng Aspose.Cells
    private byte[] convertExcelToPdf(byte[] excelBytes) throws IOException {
        try (ByteArrayInputStream excelInput = new ByteArrayInputStream(excelBytes);
             ByteArrayOutputStream pdfOutput = new ByteArrayOutputStream()) {

            // Load Excel workbook bằng Aspose
            com.aspose.cells.Workbook workbook = new com.aspose.cells.Workbook(excelInput);

            // Cấu hình lưu PDF
            PdfSaveOptions options = new PdfSaveOptions();
            options.setOnePagePerSheet(true); // Cố gắng scale để vừa 1 trang giấy

            // Save ra stream
            workbook.save(pdfOutput, options);
            return pdfOutput.toByteArray();
        } catch (Exception e) {

            throw new IOException("Lỗi Aspose convert: " + e.getMessage(), e);
        }
        }
}