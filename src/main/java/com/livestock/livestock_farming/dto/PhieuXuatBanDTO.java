package com.livestock.livestock_farming.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PhieuXuatBanDTO {
    private Long id;
    private String maPhieu;
    private LocalDate ngayXuat;
    private String tenKhachHang;
    private String soDienThoai;
    private String diaChi;
    private Integer tongSoLuong;
    private Double tongKhoiLuong;
    private Double tongTien;
    private String ghiChu;

    // Danh sách chi tiết
    private List<ChiTietXuatBanDTO> danhSachChiTiet;
}