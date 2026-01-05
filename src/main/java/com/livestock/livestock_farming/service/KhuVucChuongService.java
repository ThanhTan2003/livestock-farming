package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.KhuVucChuongDTO;
import com.livestock.livestock_farming.entity.KhuVucChuong;
import com.livestock.livestock_farming.mapper.KhuVucChuongMapper;
import com.livestock.livestock_farming.repository.KhuVucChuongRepository;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class KhuVucChuongService {
    private final KhuVucChuongRepository khuVucChuongRepository;
    private final KhuVucChuongMapper khuVucChuongMapper;

    public List<KhuVucChuongDTO> layTatCa() {
        return khuVucChuongRepository.findAll().stream()
                .map(khuVucChuongMapper::toDTO)
                .collect(Collectors.toList());
    }

    public PageResponse<KhuVucChuongDTO> timKiem(String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<KhuVucChuong> pageResult = khuVucChuongRepository.timKiem(tuKhoa, pageable);

        List<KhuVucChuongDTO> content = pageResult.getContent().stream()
                .map(khuVucChuongMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<KhuVucChuongDTO>builder()
                .page(page).size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content).build();
    }

    public KhuVucChuongDTO themMoi(KhuVucChuongDTO dto) {
        KhuVucChuong entity = khuVucChuongMapper.toEntity(dto);
        entity.setSoLuongHienTai(0); // Khởi tạo 0
        return khuVucChuongMapper.toDTO(khuVucChuongRepository.save(entity));
    }

    public KhuVucChuongDTO capNhat(Long id, KhuVucChuongDTO dto) {
        KhuVucChuong entity = khuVucChuongRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khu vực không tồn tại"));
        entity.setTenKhuVuc(dto.getTenKhuVuc());
        entity.setSucChua(dto.getSucChua());
        entity.setViTri(dto.getViTri());
        entity.setMoTa(dto.getMoTa());
        return khuVucChuongMapper.toDTO(khuVucChuongRepository.save(entity));
    }

    public void xoa(Long id) {
        if(!khuVucChuongRepository.existsById(id)) {
            throw new IllegalArgumentException("Khu vực không tồn tại");
        }
        // Có thể thêm check xem còn vật nuôi trong chuồng không trước khi xóa
        khuVucChuongRepository.deleteById(id);
    }
}