package com.livestock.livestock_farming.dto;

import lombok.Data;

@Data
public class KhuVucChuongDTO {
    private Long id;
    private String tenKhuVuc;
    private Integer sucChua;
    private Integer soLuongHienTai;
    private String viTri;
    private String moTa;
}