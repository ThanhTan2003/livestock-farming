package com.livestock.livestock_farming.entity;

import com.livestock.livestock_farming.enums.LoaiVatNuoi;
import com.livestock.livestock_farming.enums.TrangThaiVatNuoi;
import com.livestock.livestock_farming.utils.AppUtils;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "vat_nuoi")
@Data
public class VatNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ma_dinh_danh", unique = true, nullable = false)
    private String maDinhDanh;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_vat_nuoi")
    private LoaiVatNuoi loaiVatNuoi;

    @ManyToOne
    @JoinColumn(name = "khu_vuc_chuong_id")
    private KhuVucChuong khuVucChuong;

    @Column(name = "giong_loai")
    private String giongLoai;

    @Column(name = "nguon_goc")
    private String nguonGoc;

    @Column(name = "ngay_nhap")
    private LocalDate ngayNhap;

    @Column(name = "khoi_luong_nhap")
    private Double khoiLuongNhap;

    @Column(name = "khoi_luong_hien_tai")
    private Double khoiLuongHienTai;

    @Enumerated(EnumType.STRING)
    @Column(name = "trang_thai")
    private TrangThaiVatNuoi trangThai;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    @PrePersist
    @PreUpdate
    public void taoNameNoAccents() {
        String raw = (this.maDinhDanh + " " + this.giongLoai + " " + this.nguonGoc);
        this.nameNoAccents = AppUtils.loaiBoDau(raw).toLowerCase();
    }
}