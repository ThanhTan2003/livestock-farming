package com.livestock.livestock_farming.utils;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class AppUtils {
    // Hàm loại bỏ dấu tiếng Việt
    public static String loaiBoDau(String value) {
        if (value == null) return "";
        String temp = Normalizer.normalize(value, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ', 'd').replace('Đ', 'D');
    }
}