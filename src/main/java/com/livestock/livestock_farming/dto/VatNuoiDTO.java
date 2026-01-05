package com.livestock.livestock_farming.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class VatNuoiDTO {
    private Long id;
    private String maDinhDanh;
    private Integer idLoaiVatNuoi;
    private String tenLoaiVatNuoi;
    private Long idKhuVucChuong;
    private String tenKhuVucChuong;
    private String giongLoai;
    private String nguonGoc;
    private LocalDate ngayNhap;
    private Double khoiLuongNhap;
    private Double khoiLuongHienTai;
    private Integer idTrangThai;
    private String tenTrangThai;
}