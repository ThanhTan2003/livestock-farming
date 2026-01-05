package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.NhatKyChanNuoiDTO;
import com.livestock.livestock_farming.entity.KhuVucChuong;
import com.livestock.livestock_farming.entity.NhatKyChanNuoi;
import com.livestock.livestock_farming.mapper.NhatKyChanNuoiMapper;
import com.livestock.livestock_farming.repository.KhuVucChuongRepository;
import com.livestock.livestock_farming.repository.NhatKyChanNuoiRepository;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NhatKyChanNuoiService {
    private final NhatKyChanNuoiRepository nhatKyChanNuoiRepository;
    private final KhuVucChuongRepository khuVucChuongRepository;
    private final NhatKyChanNuoiMapper nhatKyChanNuoiMapper;

    // 1. Tìm kiếm phân trang
    public PageResponse<NhatKyChanNuoiDTO> timKiem(Long idKhuVuc, LocalDate tuNgay, LocalDate denNgay, String tuKhoa, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayGhiNhan").descending());
        String tuKhoaNoAccents = (tuKhoa != null && !tuKhoa.isBlank()) ? com.livestock.livestock_farming.utils.AppUtils.loaiBoDau(tuKhoa).toLowerCase() : null;
        Page<NhatKyChanNuoi> pageResult = nhatKyChanNuoiRepository.timKiem(idKhuVuc, tuNgay, denNgay, tuKhoaNoAccents, pageable);

        List<NhatKyChanNuoiDTO> content = pageResult.getContent().stream()
                .map(nhatKyChanNuoiMapper::toDTO).collect(Collectors.toList());

        return PageResponse.<NhatKyChanNuoiDTO>builder()
                .page(page).size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content).build();
    }

    // 2. Lấy chi tiết
    public NhatKyChanNuoiDTO layChiTiet(Long id) {
        NhatKyChanNuoi entity = nhatKyChanNuoiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhật ký với ID: " + id));
        return nhatKyChanNuoiMapper.toDTO(entity);
    }

    // 3. Thêm mới
    @Transactional
    public NhatKyChanNuoiDTO themMoi(NhatKyChanNuoiDTO dto) {
        NhatKyChanNuoi entity = nhatKyChanNuoiMapper.toEntity(dto);
        if(dto.getIdKhuVucChuong() != null) {
            KhuVucChuong khuVuc = khuVucChuongRepository.findById(dto.getIdKhuVucChuong())
                    .orElseThrow(() -> new IllegalArgumentException("Khu vực chuồng không tồn tại"));
            entity.setKhuVucChuong(khuVuc);
        }
        return nhatKyChanNuoiMapper.toDTO(nhatKyChanNuoiRepository.save(entity));
    }

    // 4. Cập nhật
    @Transactional
    public NhatKyChanNuoiDTO capNhat(Long id, NhatKyChanNuoiDTO dto) {
        NhatKyChanNuoi entity = nhatKyChanNuoiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhật ký để cập nhật"));

        // Cập nhật các trường thông tin
        entity.setNgayGhiNhan(dto.getNgayGhiNhan());
        entity.setThucAnTieuThu(dto.getThucAnTieuThu());
        entity.setSoConOm(dto.getSoConOm());
        entity.setSoConChet(dto.getSoConChet());
        entity.setGhiChu(dto.getGhiChu());
        entity.setNguoiGhiNhan(dto.getNguoiGhiNhan());

        // Kiểm tra nếu có thay đổi khu vực chuồng
        if (dto.getIdKhuVucChuong() != null) {
            // Nếu entity chưa có chuồng hoặc ID chuồng mới khác ID chuồng cũ
            if (entity.getKhuVucChuong() == null || !entity.getKhuVucChuong().getId().equals(dto.getIdKhuVucChuong())) {
                KhuVucChuong khuVucMoi = khuVucChuongRepository.findById(dto.getIdKhuVucChuong())
                        .orElseThrow(() -> new IllegalArgumentException("Khu vực chuồng mới không tồn tại"));
                entity.setKhuVucChuong(khuVucMoi);
            }
        } else {
            // Nếu dto truyền lên null thì set null (hoặc giữ nguyên tùy nghiệp vụ, ở đây tôi set null)
            entity.setKhuVucChuong(null);
        }

        return nhatKyChanNuoiMapper.toDTO(nhatKyChanNuoiRepository.save(entity));
    }

    // 5. Xóa
    @Transactional
    public void xoa(Long id) {
        if (!nhatKyChanNuoiRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy nhật ký để xóa");
        }
        nhatKyChanNuoiRepository.deleteById(id);
    }
}