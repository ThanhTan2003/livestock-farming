package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.VatNuoiDTO;
import com.livestock.livestock_farming.entity.VatNuoi;
import org.springframework.stereotype.Component;

@Component
public class VatNuoiMapper {
    public VatNuoiDTO toDTO(VatNuoi entity) {
        if (entity == null) return null;
        VatNuoiDTO dto = new VatNuoiDTO();
        dto.setId(entity.getId());
        dto.setMaDinhDanh(entity.getMaDinhDanh());
        if (entity.getLoaiVatNuoi() != null) {
            dto.setIdLoaiVatNuoi(entity.getLoaiVatNuoi().getId());
            dto.setTenLoaiVatNuoi(entity.getLoaiVatNuoi().getTenHienThi());
        }

        if (entity.getKhuVucChuong() != null) {
            dto.setIdKhuVucChuong(entity.getKhuVucChuong().getId());
            dto.setTenKhuVucChuong(entity.getKhuVucChuong().getTenKhuVuc());
        }

        dto.setGiongLoai(entity.getGiongLoai());
        dto.setNguonGoc(entity.getNguonGoc());
        dto.setNgayNhap(entity.getNgayNhap());
        dto.setKhoiLuongNhap(entity.getKhoiLuongNhap());
        dto.setKhoiLuongHienTai(entity.getKhoiLuongHienTai());
        if (entity.getTrangThai() != null) {
            dto.setIdTrangThai(entity.getTrangThai().getId());
            dto.setTenTrangThai(entity.getTrangThai().getTenHienThi());
        }
        return dto;
    }

    public VatNuoi toEntity(VatNuoiDTO dto) {
        if (dto == null) return null;
        VatNuoi entity = new VatNuoi();
        entity.setId(dto.getId());
        entity.setMaDinhDanh(dto.getMaDinhDanh());
        entity.setGiongLoai(dto.getGiongLoai());
        entity.setNguonGoc(dto.getNguonGoc());
        entity.setNgayNhap(dto.getNgayNhap());
        entity.setKhoiLuongNhap(dto.getKhoiLuongNhap());
        entity.setKhoiLuongHienTai(dto.getKhoiLuongHienTai());
        // KhuVucChuong sẽ được set trong Service
        return entity;
    }
}