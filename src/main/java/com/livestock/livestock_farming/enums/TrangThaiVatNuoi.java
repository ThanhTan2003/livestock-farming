package com.livestock.livestock_farming.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TrangThaiVatNuoi {
    BINH_THUONG(1, "Bình thường"),
    OM(2, "Đang ốm"),
    DA_CHET(3, "Đã chết"),
    DA_XUAT_BAN(4, "Đã xuất bán");

    private final int id;
    private final String tenHienThi;
}