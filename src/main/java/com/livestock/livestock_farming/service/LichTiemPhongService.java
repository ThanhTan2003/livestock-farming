package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.dto.LichTiemPhongDTO;
import com.livestock.livestock_farming.entity.LichTiemPhong;
import com.livestock.livestock_farming.entity.VatNuoi;
import com.livestock.livestock_farming.enums.LoaiVatNuoi;
import com.livestock.livestock_farming.mapper.LichTiemPhongMapper;
import com.livestock.livestock_farming.repository.LichTiemPhongRepository;
import com.livestock.livestock_farming.repository.VatNuoiRepository;
import com.livestock.livestock_farming.utils.AppUtils;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LichTiemPhongService {
    private final LichTiemPhongRepository lichTiemPhongRepository;
    private final VatNuoiRepository vatNuoiRepository;
    private final LichTiemPhongMapper lichTiemPhongMapper;

    // --- LIST ALL (Không phân trang) ---
    public List<LichTiemPhongDTO> layTatCa() {
        return lichTiemPhongRepository.findAll(Sort.by(Sort.Direction.DESC, "ngayTiemDuKien")).stream()
                .map(lichTiemPhongMapper::toDTO)
                .collect(Collectors.toList());
    }

    // --- CHỨC NĂNG NGHIỆP VỤ (Có phân trang) ---

    // 1. Sắp tới (Ngày tiêm tăng dần)
    public PageResponse<LichTiemPhongDTO> layLichSapToi(int page, int size) {
        LocalDate homNay = LocalDate.now();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTiemDuKien").ascending());

        Page<LichTiemPhong> pageResult = lichTiemPhongRepository.findByNgayTiemDuKienGreaterThanEqual(homNay, pageable);
        return mapToPageResponse(pageResult, page, size);
    }

    // 2. Trễ hạn (Ngày tiêm giảm dần - trễ nhất lên trước hoặc tùy nhu cầu)
    public PageResponse<LichTiemPhongDTO> layLichTreHan(int page, int size) {
        LocalDate homNay = LocalDate.now();
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTiemDuKien").descending());

        Page<LichTiemPhong> pageResult = lichTiemPhongRepository.findByDaHoanThanhFalseAndNgayTiemDuKienLessThan(homNay, pageable);
        return mapToPageResponse(pageResult, page, size);
    }

    // 3. Lịch sử của vật nuôi
    public PageResponse<LichTiemPhongDTO> layLichSuTiemCuaVatNuoi(Long idVatNuoi, int page, int size) {
        if(!vatNuoiRepository.existsById(idVatNuoi)) {
            throw new IllegalArgumentException("Vật nuôi không tồn tại");
        }
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTiemDuKien").descending());
        Page<LichTiemPhong> pageResult = lichTiemPhongRepository.findByVatNuoi_Id(idVatNuoi, pageable);
        return mapToPageResponse(pageResult, page, size);
    }

    // 4. Tìm kiếm nâng cao
    public PageResponse<LichTiemPhongDTO> timKiem(String tuKhoa, Long idVatNuoi, Integer idLoaiVatNuoi,
                                                  LocalDate tuNgay, LocalDate denNgay, Boolean daHoanThanh,
                                                  int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("ngayTiemDuKien").ascending());
        String tuKhoaChuanHoa = (tuKhoa != null && !tuKhoa.isEmpty()) ? AppUtils.loaiBoDau(tuKhoa).toLowerCase() : null;
        LoaiVatNuoi loaiEnum = (idLoaiVatNuoi != null) ? LoaiVatNuoi.values()[idLoaiVatNuoi - 1] : null;

        Page<LichTiemPhong> pageResult = lichTiemPhongRepository.timKiemLichTiem(
                idVatNuoi, tuKhoaChuanHoa, loaiEnum, tuNgay, denNgay, daHoanThanh, pageable);
        return mapToPageResponse(pageResult, page, size);
    }

    // --- Helper Map PageResponse ---
    private PageResponse<LichTiemPhongDTO> mapToPageResponse(Page<LichTiemPhong> pageResult, int page, int size) {
        List<LichTiemPhongDTO> content = pageResult.getContent().stream()
                .map(lichTiemPhongMapper::toDTO)
                .collect(Collectors.toList());

        return PageResponse.<LichTiemPhongDTO>builder()
                .page(page)
                .size(size)
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .content(content)
                .build();
    }

    // --- CRUD ---
    public LichTiemPhongDTO taoLichTiem(LichTiemPhongDTO dto) {
        VatNuoi vatNuoi = vatNuoiRepository.findById(dto.getIdVatNuoi())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vật nuôi"));
        LichTiemPhong entity = new LichTiemPhong();
        entity.setVatNuoi(vatNuoi);
        entity.setNgayTiemDuKien(dto.getNgayTiemDuKien());
        entity.setTenVacXin(dto.getTenVacXin());
        entity.setDaHoanThanh(false);
        return lichTiemPhongMapper.toDTO(lichTiemPhongRepository.save(entity));
    }

    public void capNhatTrangThai(Long id, Boolean daHoanThanh, LocalDate ngayThucTe, String nguoiThucHien) {
        LichTiemPhong entity = lichTiemPhongRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lịch tiêm không tồn tại"));
        entity.setDaHoanThanh(daHoanThanh);
        if (daHoanThanh) {
            entity.setNgayTiemThucTe(ngayThucTe != null ? ngayThucTe : LocalDate.now());
            entity.setNguoiThucHien(nguoiThucHien);
        }
        lichTiemPhongRepository.save(entity);
    }

    public void xoa(Long id) {
        if (!lichTiemPhongRepository.existsById(id)) {
            throw new IllegalArgumentException("Lịch tiêm không tồn tại để xóa");
        }
        lichTiemPhongRepository.deleteById(id);
    }
}