package com.livestock.livestock_farming.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Table(name = "lich_tiem_phong")
@Data
public class LichTiemPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "vat_nuoi_id")
    private VatNuoi vatNuoi;

    @Column(name = "ngay_tiem_du_kien")
    private LocalDate ngayTiemDuKien;

    @Column(name = "ngay_tiem_thuc_te")
    private LocalDate ngayTiemThucTe;

    @Column(name = "ten_vac_xin")
    private String tenVacXin;

    @Column(name = "nguoi_thuc_hien")
    private String nguoiThucHien;

    @Column(name = "da_hoan_thanh")
    private Boolean daHoanThanh;

    @Column(name = "name_no_accents")
    private String nameNoAccents;

    @PrePersist
    @PreUpdate
    private void updateNameNoAccents() {
        if (tenVacXin != null) {
            this.nameNoAccents = com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tenVacXin).toLowerCase();
        } else {
            this.nameNoAccents = null;
        }
    }
}