package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.EnumDTO;
import com.livestock.livestock_farming.dto.VatNuoiDTO;
import com.livestock.livestock_farming.entity.KhuVucChuong;
import com.livestock.livestock_farming.entity.VatNuoi;
import com.livestock.livestock_farming.enums.LoaiVatNuoi;
import com.livestock.livestock_farming.enums.TrangThaiVatNuoi;
import com.livestock.livestock_farming.mapper.VatNuoiMapper;
import com.livestock.livestock_farming.repository.KhuVucChuongRepository;
import com.livestock.livestock_farming.repository.VatNuoiRepository;
import com.livestock.livestock_farming.utils.AppUtils;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VatNuoiService {
    private final VatNuoiRepository vatNuoiRepository;
    private final KhuVucChuongRepository khuVucChuongRepository;
    private final VatNuoiMapper vatNuoiMapper;

    public List<EnumDTO> layDanhSachLoaiVatNuoi() {
        return Arrays.stream(LoaiVatNuoi.values())
                .map(e -> new EnumDTO(e.getId(), e.getTenHienThi()))
                .collect(Collectors.toList());
    }

    public List<EnumDTO> layDanhSachTrangThaiVatNuoi() {
        return Arrays.stream(TrangThaiVatNuoi.values())
                .map(e -> new EnumDTO(e.getId(), e.getTenHienThi()))
                .collect(Collectors.toList());
    }

    public List<VatNuoiDTO> layTatCa() {
        return vatNuoiRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream()
                .map(vatNuoiMapper::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách vật nuôi khỏe mạnh (BÌNH THƯỜNG) để chuẩn bị xuất bán
     */
    public List<VatNuoiDTO> getVatNuoiSanSangXuat() {
        return vatNuoiRepository.findAllByTrangThai(TrangThaiVatNuoi.BINH_THUONG).stream()
                .map(vatNuoiMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public VatNuoiDTO themMoi(VatNuoiDTO dto) {
        if (vatNuoiRepository.existsByMaDinhDanh(dto.getMaDinhDanh())) {
            throw new IllegalArgumentException("Mã định danh vật nuôi đã tồn tại: " + dto.getMaDinhDanh());
        }
        VatNuoi entity = vatNuoiMapper.toEntity(dto);
        if (dto.getIdLoaiVatNuoi() != null) {
            entity.setLoaiVatNuoi(LoaiVatNuoi.values()[dto.getIdLoaiVatNuoi() - 1]);
        }

        // Xử lý chuồng trại và cập nhật số lượng
        if(dto.getIdKhuVucChuong() != null) {
            KhuVucChuong chuong = khuVucChuongRepository.findById(dto.getIdKhuVucChuong())
                    .orElseThrow(() -> new IllegalArgumentException("Khu vực chuồng không tồn tại"));

            // Check sức chứa
            if(chuong.getSucChua() != null && chuong.getSoLuongHienTai() >= chuong.getSucChua()){
                throw new IllegalArgumentException("Chuồng đã đầy");
            }
            chuong.setSoLuongHienTai((chuong.getSoLuongHienTai() == null ? 0 : chuong.getSoLuongHienTai()) + 1);
            khuVucChuongRepository.save(chuong);
            entity.setKhuVucChuong(chuong);
        }

        entity.setTrangThai(TrangThaiVatNuoi.BINH_THUONG);
        return vatNuoiMapper.toDTO(vatNuoiRepository.save(entity));
    }

    @Transactional
    public VatNuoiDTO capNhat(Long id, VatNuoiDTO dto) {
        VatNuoi entity = vatNuoiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật nuôi với ID: " + id));

        // Logic chuyển chuồng
        if (dto.getIdKhuVucChuong() != null) {
            // Nếu có sự thay đổi chuồng (Chưa có chuồng hoặc chuồng mới khác chuồng cũ)
            if (entity.getKhuVucChuong() == null || !entity.getKhuVucChuong().getId().equals(dto.getIdKhuVucChuong())) {
                // 1. Giảm số lượng ở chuồng cũ (nếu có)
                if (entity.getKhuVucChuong() != null) {
                    KhuVucChuong chuongCu = entity.getKhuVucChuong();
                    chuongCu.setSoLuongHienTai(Math.max(0, chuongCu.getSoLuongHienTai() - 1));
                    khuVucChuongRepository.save(chuongCu);
                }
                // 2. Tăng số lượng ở chuồng mới
                KhuVucChuong chuongMoi = khuVucChuongRepository.findById(dto.getIdKhuVucChuong())
                        .orElseThrow(() -> new IllegalArgumentException("Chuồng mới không tồn tại"));
                chuongMoi.setSoLuongHienTai((chuongMoi.getSoLuongHienTai() == null ? 0 : chuongMoi.getSoLuongHienTai()) + 1);
                khuVucChuongRepository.save(chuongMoi);

                entity.setKhuVucChuong(chuongMoi);
            }
        }

        entity.setGiongLoai(dto.getGiongLoai());
        entity.setNguonGoc(dto.getNguonGoc());
        entity.setKhoiLuongHienTai(dto.getKhoiLuongHienTai());

        if (dto.getIdTrangThai() != null) {
            entity.setTrangThai(TrangThaiVatNuoi.values()[dto.getIdTrangThai() - 1]);
        }
        return vatNuoiMapper.toDTO(vatNuoiRepository.save(entity));
    }

    public void xoa(Long id) {
        if (!vatNuoiRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy vật nuôi để xóa");
        }
        // Nên thêm logic giảm số lượng trong chuồng khi xóa
        VatNuoi entity = vatNuoiRepository.findById(id).get();
        if(entity.getKhuVucChuong() != null){
            KhuVucChuong chuong = entity.getKhuVucChuong();
            chuong.setSoLuongHienTai(Math.max(0, chuong.getSoLuongHienTai() - 1));
            khuVucChuongRepository.save(chuong);
        }
        vatNuoiRepository.deleteById(id);
    }

    public VatNuoiDTO layChiTiet(Long id) {
        return vatNuoiRepository.findById(id)
                .map(vatNuoiMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật nuôi"));
    }

    public PageResponse<VatNuoiDTO> timKiem(String tuKhoa, Integer idLoaiVatNuoi, Integer idTrangThai, Long idKhuVucChuong, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        String tuKhoaChuanHoa = (tuKhoa != null && !tuKhoa.isEmpty()) ? AppUtils.loaiBoDau(tuKhoa).toLowerCase() : null;
        LoaiVatNuoi loaiEnum = (idLoaiVatNuoi != null) ? LoaiVatNuoi.values()[idLoaiVatNuoi - 1] : null;
        TrangThaiVatNuoi trangThaiEnum = (idTrangThai != null) ? TrangThaiVatNuoi.values()[idTrangThai - 1] : null;

        Page<VatNuoi> pageResult = vatNuoiRepository.timKiemVatNuoi(tuKhoaChuanHoa, loaiEnum, trangThaiEnum, idKhuVucChuong, pageable);

        List<VatNuoiDTO> content = pageResult.getContent().stream()
                .map(vatNuoiMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<VatNuoiDTO>builder()
                .page(page).size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content).build();
    }
}