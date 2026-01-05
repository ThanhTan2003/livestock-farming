package com.livestock.livestock_farming.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "nhat_ky_chan_nuoi")
@Data
public class NhatKyChanNuoi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "khu_vuc_chuong_id")
    private KhuVucChuong khuVucChuong;

    @Column(name = "ngay_ghi_nhan")
    private LocalDate ngayGhiNhan;

    @Column(name = "thuc_an_tieu_thu")
    private Double thucAnTieuThu; // Tổng kg thức ăn trong ngày

    @Column(name = "so_con_om")
    private Integer soConOm;

    @Column(name = "so_con_chet")
    private Integer soConChet;

    @Column(name = "ghi_chu", columnDefinition = "TEXT")
    private String ghiChu;

    @Column(name = "nguoi_ghi_nhan")
    private String nguoiGhiNhan;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    @PrePersist
    @PreUpdate
    private void updateNameNoAccents() {
        if (nguoiGhiNhan != null) {
            this.nameNoAccents = com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(nguoiGhiNhan).toLowerCase();
        } else {
            this.nameNoAccents = null;
        }
    }
}