package com.livestock.livestock_farming.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VatTuKhoDTO {
    private Long id;
    private String tenVatTu;
    private Integer idLoaiVatTu;
    private String tenLoaiVatTu;
    private String donViTinh;
    private Double soLuongTon;
    private Double nguongCanhBao;
    private LocalDate ngayHetHan;
    private String ghiChu;
}