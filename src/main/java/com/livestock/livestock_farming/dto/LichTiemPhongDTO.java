package com.livestock.livestock_farming.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class LichTiemPhongDTO {
    private Long id;
    private Long idVatNuoi;
    private String maDinhDanhVatNuoi; // Hiển thị mã con vật
    private String tenLoaiVatNuoi;    // Hiển thị loại (Bò/Heo)
    private LocalDate ngayTiemDuKien;
    private LocalDate ngayTiemThucTe;
    private String tenVacXin;
    private String nguoiThucHien;
    private Boolean daHoanThanh;
}