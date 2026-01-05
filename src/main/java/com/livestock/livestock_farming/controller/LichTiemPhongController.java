package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.LichTiemPhongDTO;
import com.livestock.livestock_farming.service.LichTiemPhongService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/lich-tiem")
@RequiredArgsConstructor
public class LichTiemPhongController {
    private final LichTiemPhongService lichTiemPhongService;

    // 1. Lấy tất cả (Không phân trang - để export hoặc dropdown)
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<LichTiemPhongDTO>> layTatCa() {
        return ResponseEntity.ok(lichTiemPhongService.layTatCa());
    }

    // 2. Lấy lịch sắp tới (Phân trang)
    @GetMapping("/sap-toi")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<LichTiemPhongDTO>> layLichSapToi(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(lichTiemPhongService.layLichSapToi(page, size));
    }

    // 3. Lấy lịch trễ hạn (Phân trang)
    @GetMapping("/tre-han")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<LichTiemPhongDTO>> layLichTreHan(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(lichTiemPhongService.layLichTreHan(page, size));
    }

    // 4. Lấy lịch sử tiêm của vật nuôi (Phân trang)
    @GetMapping("/lich-su/{idVatNuoi}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<LichTiemPhongDTO>> layLichSuTiemCuaVatNuoi(
            @PathVariable Long idVatNuoi,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(lichTiemPhongService.layLichSuTiemCuaVatNuoi(idVatNuoi, page, size));
    }

    // 5. CRUD
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<LichTiemPhongDTO> taoLichTiem(@RequestBody LichTiemPhongDTO dto) {
        return ResponseEntity.ok(lichTiemPhongService.taoLichTiem(dto));
    }

    @PutMapping("/{id}/hoan-thanh")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xacNhanHoanThanh(
            @PathVariable Long id,
            @RequestParam String nguoiThucHien,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayThucTe) {
        lichTiemPhongService.capNhatTrangThai(id, true, ngayThucTe, nguoiThucHien);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xoa(@PathVariable Long id) {
        lichTiemPhongService.xoa(id);
        return ResponseEntity.noContent().build();
    }

    // 6. Tìm kiếm nâng cao (Phân trang)
    @GetMapping("/tim-kiem")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<LichTiemPhongDTO>> timKiem(
            @RequestParam(required = false, defaultValue = "") String tuKhoa,
            @RequestParam(required = false) Long idVatNuoi,
            @RequestParam(required = false) Integer idLoaiVatNuoi,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(required = false) Boolean daHoanThanh,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(lichTiemPhongService.timKiem(
                tuKhoa, idVatNuoi, idLoaiVatNuoi, tuNgay, denNgay, daHoanThanh, page, size));
    }
}