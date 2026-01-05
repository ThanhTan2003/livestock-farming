package com.livestock.livestock_farming.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "phieu_xuat_ban")
@Data
public class PhieuXuatBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_phieu", unique = true, nullable = false)
    private String maPhieu; // Ví dụ: PX-20231027-001

    @Column(name = "ngay_xuat")
    private LocalDate ngayXuat;

    @Column(name = "ten_khach_hang")
    private String tenKhachHang;

    @Column(name = "so_dien_thoai")
    private String soDienThoai;

    @Column(name = "dia_chi")
    private String diaChi;

    @Column(name = "tong_so_luong")
    private Integer tongSoLuong;

    @Column(name = "tong_khoi_luong")
    private Double tongKhoiLuong;

    @Column(name = "tong_tien")
    private Double tongTien;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    // Cascade.ALL để khi lưu PhieuXuatBan thì lưu luôn List ChiTiet
    @OneToMany(mappedBy = "phieuXuatBan", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChiTietXuatBan> chiTietXuatBanList;

    @PrePersist
    @PreUpdate
    private void updateNameNoAccents() {
        if (tenKhachHang != null) {
            this.nameNoAccents = com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tenKhachHang).toLowerCase();
        } else {
            this.nameNoAccents = null;
        }
    }
}