package com.livestock.livestock_farming.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "chi_tiet_xuat_ban")
@Data
public class ChiTietXuatBan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "phieu_xuat_ban_id")
    private PhieuXuatBan phieuXuatBan;

    @ManyToOne
    @JoinColumn(name = "vat_nuoi_id")
    private VatNuoi vatNuoi;

    @Column(name = "khoi_luong_xuat")
    private Double khoiLuongXuat;

    @Column(name = "don_gia")
    private Double donGia;

    @Column(name = "thanh_tien")
    private Double thanhTien;
}