package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.NhatKyChanNuoiDTO;
import com.livestock.livestock_farming.entity.NhatKyChanNuoi;
import org.springframework.stereotype.Component;

@Component
public class NhatKyChanNuoiMapper {
    public NhatKyChanNuoiDTO toDTO(NhatKyChanNuoi entity) {
        if (entity == null) return null;
        NhatKyChanNuoiDTO dto = new NhatKyChanNuoiDTO();
        dto.setId(entity.getId());
        if (entity.getKhuVucChuong() != null) {
            dto.setIdKhuVucChuong(entity.getKhuVucChuong().getId());
            dto.setTenKhuVucChuong(entity.getKhuVucChuong().getTenKhuVuc());
        }
        dto.setNgayGhiNhan(entity.getNgayGhiNhan());
        dto.setThucAnTieuThu(entity.getThucAnTieuThu());
        dto.setSoConOm(entity.getSoConOm());
        dto.setSoConChet(entity.getSoConChet());
        dto.setGhiChu(entity.getGhiChu());
        dto.setNguoiGhiNhan(entity.getNguoiGhiNhan());
        return dto;
    }

    public NhatKyChanNuoi toEntity(NhatKyChanNuoiDTO dto) {
        if (dto == null) return null;
        NhatKyChanNuoi entity = new NhatKyChanNuoi();
        entity.setId(dto.getId());
        entity.setNgayGhiNhan(dto.getNgayGhiNhan());
        entity.setThucAnTieuThu(dto.getThucAnTieuThu());
        entity.setSoConOm(dto.getSoConOm());
        entity.setSoConChet(dto.getSoConChet());
        entity.setGhiChu(dto.getGhiChu());
        entity.setNguoiGhiNhan(dto.getNguoiGhiNhan());
        // KhuVucChuong set ở Service để an toàn
        return entity;
    }
}