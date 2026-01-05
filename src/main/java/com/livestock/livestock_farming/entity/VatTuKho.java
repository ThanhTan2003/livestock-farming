package com.livestock.livestock_farming.entity;

import com.livestock.livestock_farming.enums.LoaiVatTu;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "vat_tu_kho")
@Data
public class VatTuKho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_vat_tu", nullable = false)
    private String tenVatTu;

    @Enumerated(EnumType.STRING)
    @Column(name = "loai_vat_tu")
    private LoaiVatTu loaiVatTu;

    @Column(name = "don_vi_tinh")
    private String donViTinh; // kg, bao, chai, cai

    @Column(name = "so_luong_ton")
    private Double soLuongTon;

    @Column(name = "nguong_canh_bao")
    private Double nguongCanhBao; // Tồn kho thấp hơn mức này sẽ báo

    @Column(name = "ngay_het_han")
    private LocalDate ngayHetHan;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    @PrePersist
    @PreUpdate
    private void updateNameNoAccents() {
        if (tenVatTu != null) {
            this.nameNoAccents = com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tenVatTu).toLowerCase();
        } else {
            this.nameNoAccents = null;
        }
    }
}