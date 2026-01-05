package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.LichTiemPhongDTO;
import com.livestock.livestock_farming.entity.LichTiemPhong;
import org.springframework.stereotype.Component;

@Component
public class LichTiemPhongMapper {
    public LichTiemPhongDTO toDTO(LichTiemPhong entity) {
        if (entity == null) return null;
        LichTiemPhongDTO dto = new LichTiemPhongDTO();
        dto.setId(entity.getId());
        if (entity.getVatNuoi() != null) {
            dto.setIdVatNuoi(entity.getVatNuoi().getId());
            dto.setMaDinhDanhVatNuoi(entity.getVatNuoi().getMaDinhDanh());
            if (entity.getVatNuoi().getLoaiVatNuoi() != null) {
                dto.setTenLoaiVatNuoi(entity.getVatNuoi().getLoaiVatNuoi().getTenHienThi());
            }
        }
        dto.setNgayTiemDuKien(entity.getNgayTiemDuKien());
        dto.setNgayTiemThucTe(entity.getNgayTiemThucTe());
        dto.setTenVacXin(entity.getTenVacXin());
        dto.setNguoiThucHien(entity.getNguoiThucHien());
        dto.setDaHoanThanh(entity.getDaHoanThanh());
        return dto;
    }
}