package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.VatTuKhoDTO;
import com.livestock.livestock_farming.entity.VatTuKho;
import com.livestock.livestock_farming.enums.LoaiVatTu;
import org.springframework.stereotype.Component;

@Component
public class VatTuKhoMapper {
    public VatTuKhoDTO toDTO(VatTuKho entity) {
        if (entity == null) return null;
        VatTuKhoDTO dto = new VatTuKhoDTO();
        dto.setId(entity.getId());
        dto.setTenVatTu(entity.getTenVatTu());
        if (entity.getLoaiVatTu() != null) {
            dto.setIdLoaiVatTu(entity.getLoaiVatTu().getId());
            dto.setTenLoaiVatTu(entity.getLoaiVatTu().getTenHienThi());
        }
        dto.setDonViTinh(entity.getDonViTinh());
        dto.setSoLuongTon(entity.getSoLuongTon());
        dto.setNguongCanhBao(entity.getNguongCanhBao());
        dto.setNgayHetHan(entity.getNgayHetHan());
        dto.setGhiChu(entity.getGhiChu());
        return dto;
    }

    public VatTuKho toEntity(VatTuKhoDTO dto) {
        if (dto == null) return null;
        VatTuKho entity = new VatTuKho();
        entity.setId(dto.getId());
        entity.setTenVatTu(dto.getTenVatTu());
        if (dto.getIdLoaiVatTu() != null) {
            entity.setLoaiVatTu(LoaiVatTu.values()[dto.getIdLoaiVatTu() - 1]);
        }
        entity.setDonViTinh(dto.getDonViTinh());
        entity.setSoLuongTon(dto.getSoLuongTon());
        entity.setNguongCanhBao(dto.getNguongCanhBao());
        entity.setNgayHetHan(dto.getNgayHetHan());
        entity.setGhiChu(dto.getGhiChu());
        return entity;
    }
}