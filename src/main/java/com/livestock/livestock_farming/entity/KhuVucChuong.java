package com.livestock.livestock_farming.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "khu_vuc_chuong")
@Data
public class KhuVucChuong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ten_khu_vuc", nullable = false)
    private String tenKhuVuc;

    @Column(name = "suc_chua")
    private Integer sucChua; // Số lượng tối đa vật nuôi

    @Column(name = "so_luong_hien_tai")
    private Integer soLuongHienTai = 0;

    @Column(name = "vi_tri")
    private String viTri;

    @Column(name = "mo_ta", columnDefinition = "TEXT")
    private String moTa;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    @PrePersist
    @PreUpdate
    private void updateNameNoAccents() {
        if (tenKhuVuc != null) {
            this.nameNoAccents = com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tenKhuVuc).toLowerCase();
        } else {
            this.nameNoAccents = null;
        }
    }
}