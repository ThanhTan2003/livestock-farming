package com.livestock.livestock_farming.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class NhatKyChanNuoiDTO {
    private Long id;
    private Long idKhuVucChuong;
    private String tenKhuVucChuong; // Để hiển thị
    private LocalDate ngayGhiNhan;
    private Double thucAnTieuThu;
    private Integer soConOm;
    private Integer soConChet;
    private String ghiChu;
    private String nguoiGhiNhan;
}