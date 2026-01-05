package com.livestock.livestock_farming.dto;

import lombok.Data;

@Data
public class ChiTietXuatBanDTO {
    private Long id;
    private Long idVatNuoi;
    private String maDinhDanhVatNuoi; // Để hiển thị
    private Double khoiLuongXuat;
    private Double donGia;
    private Double thanhTien;
}