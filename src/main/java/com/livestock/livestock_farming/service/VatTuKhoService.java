package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.EnumDTO;
import com.livestock.livestock_farming.dto.VatTuKhoDTO;
import com.livestock.livestock_farming.entity.VatTuKho;
import com.livestock.livestock_farming.enums.LoaiVatTu;
import com.livestock.livestock_farming.mapper.VatTuKhoMapper;
import com.livestock.livestock_farming.repository.VatTuKhoRepository;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VatTuKhoService {
    private final VatTuKhoRepository vatTuKhoRepository;
    private final VatTuKhoMapper vatTuKhoMapper;

    public List<EnumDTO> layDanhSachLoaiVatTu() {
        return Arrays.stream(LoaiVatTu.values())
                .map(e -> new EnumDTO(e.getId(), e.getTenHienThi()))
                .collect(Collectors.toList());
    }

    public PageResponse<VatTuKhoDTO> timKiem(String tuKhoa, Integer idLoaiVatTu, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());
        LoaiVatTu loaiEnum = (idLoaiVatTu != null) ? LoaiVatTu.values()[idLoaiVatTu - 1] : null;
        String tuKhoaNoAccents = (tuKhoa != null && !tuKhoa.isBlank()) ? com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tuKhoa).toLowerCase() : null;
        Page<VatTuKho> pageResult = vatTuKhoRepository.timKiem(tuKhoaNoAccents, loaiEnum, pageable);
        List<VatTuKhoDTO> content = pageResult.getContent().stream()
                .map(vatTuKhoMapper::toDTO).collect(Collectors.toList());

        return PageResponse.<VatTuKhoDTO>builder()
                .page(page).size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content).build();
    }

    public VatTuKhoDTO themMoi(VatTuKhoDTO dto) {
        VatTuKho entity = vatTuKhoMapper.toEntity(dto);
        return vatTuKhoMapper.toDTO(vatTuKhoRepository.save(entity));
    }

    public VatTuKhoDTO capNhat(Long id, VatTuKhoDTO dto) {
        VatTuKho entity = vatTuKhoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Vật tư không tồn tại"));
        entity.setTenVatTu(dto.getTenVatTu());
        if(dto.getIdLoaiVatTu() != null)
            entity.setLoaiVatTu(LoaiVatTu.values()[dto.getIdLoaiVatTu() - 1]);
        entity.setDonViTinh(dto.getDonViTinh());
        entity.setSoLuongTon(dto.getSoLuongTon());
        entity.setNguongCanhBao(dto.getNguongCanhBao());
        entity.setNgayHetHan(dto.getNgayHetHan());
        entity.setGhiChu(dto.getGhiChu());
        return vatTuKhoMapper.toDTO(vatTuKhoRepository.save(entity));
    }

    public List<VatTuKhoDTO> layDanhSachCanhBao() {
        return vatTuKhoRepository.findCanhBaoTonKho().stream()
                .map(vatTuKhoMapper::toDTO).collect(Collectors.toList());
    }
}