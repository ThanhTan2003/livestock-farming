package com.livestock.livestock_farming.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoaiVatTu {
    THUC_AN(1, "Thức ăn chăn nuôi"),
    THUOC(2, "Thuốc thú y"),
    THIET_BI(3, "Thiết bị & Dụng cụ");

    private final int id;
    private final String tenHienThi;
}