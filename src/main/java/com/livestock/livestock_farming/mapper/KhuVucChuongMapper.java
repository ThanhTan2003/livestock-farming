package com.livestock.livestock_farming.mapper;

import com.livestock.livestock_farming.dto.KhuVucChuongDTO;
import com.livestock.livestock_farming.entity.KhuVucChuong;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class KhuVucChuongMapper {
    public KhuVucChuongDTO toDTO(KhuVucChuong entity) {
        if (entity == null) return null;
        KhuVucChuongDTO dto = new KhuVucChuongDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }

    public KhuVucChuong toEntity(KhuVucChuongDTO dto) {
        if (dto == null) return null;
        KhuVucChuong entity = new KhuVucChuong();
        BeanUtils.copyProperties(dto, entity);
        return entity;
    }
}