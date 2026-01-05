package com.livestock.livestock_farming.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum LoaiVatNuoi {
    BO(1, "Bò"),
    HEO(2, "Heo"),
    GA(3, "Gà");

    private final int id;
    private final String tenHienThi;
}