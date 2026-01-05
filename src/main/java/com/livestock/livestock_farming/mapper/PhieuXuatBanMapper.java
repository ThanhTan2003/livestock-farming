package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.ChiTietXuatBanDTO;
import com.livestock.livestock_farming.dto.PhieuXuatBanDTO;
import com.livestock.livestock_farming.entity.ChiTietXuatBan;
import com.livestock.livestock_farming.entity.PhieuXuatBan;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PhieuXuatBanMapper {

    public PhieuXuatBanDTO toDTO(PhieuXuatBan entity) {
        if (entity == null) return null;
        PhieuXuatBanDTO dto = new PhieuXuatBanDTO();
        BeanUtils.copyProperties(entity, dto);

        if (entity.getChiTietXuatBanList() != null) {
            List<ChiTietXuatBanDTO> listChiTiet = entity.getChiTietXuatBanList().stream()
                    .map(ct -> {
                        ChiTietXuatBanDTO ctDto = new ChiTietXuatBanDTO();
                        BeanUtils.copyProperties(ct, ctDto);
                        if(ct.getVatNuoi() != null) {
                            ctDto.setIdVatNuoi(ct.getVatNuoi().getId());
                            ctDto.setMaDinhDanhVatNuoi(ct.getVatNuoi().getMaDinhDanh());
                        }
                        return ctDto;
                    }).collect(Collectors.toList());
            dto.setDanhSachChiTiet(listChiTiet);
        }
        return dto;
    }

    // Lưu ý: Hàm này chỉ map thông tin header, chi tiết sẽ được xử lý trong Service
    public PhieuXuatBan toEntity(PhieuXuatBanDTO dto) {
        if (dto == null) return null;
        PhieuXuatBan entity = new PhieuXuatBan();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}