package com.livestock.livestock_farming.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PageResponse<T> {
    private int page;            // Trang hiện tại
    private int size;            // Kích thước trang
    private long totalElements;  // Tổng số bản ghi
    private int totalPages;      // Tổng số trang
    private List<T> content;     // Dữ liệu
}