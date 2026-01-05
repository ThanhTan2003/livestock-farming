package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.NhatKyChanNuoiDTO;
import com.livestock.livestock_farming.service.NhatKyChanNuoiService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/nhat-ky")
@RequiredArgsConstructor
public class NhatKyChanNuoiController {
    private final NhatKyChanNuoiService service;

    // 1. Tìm kiếm (đã có)
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<NhatKyChanNuoiDTO>> timKiem(
            @RequestParam(required = false, defaultValue = "") String tuKhoa,
            @RequestParam(required = false) Long idKhuVuc,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.timKiem(idKhuVuc, tuNgay, denNgay, tuKhoa, page, size));
    }

    // 2. Lấy chi tiết (Mới)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<NhatKyChanNuoiDTO> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(service.layChiTiet(id));
    }

    // 3. Thêm mới (đã có)
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<NhatKyChanNuoiDTO> themMoi(@RequestBody NhatKyChanNuoiDTO dto) {
        return ResponseEntity.ok(service.themMoi(dto));
    }

    // 4. Cập nhật (Mới)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<NhatKyChanNuoiDTO> capNhat(@PathVariable Long id, @RequestBody NhatKyChanNuoiDTO dto) {
        return ResponseEntity.ok(service.capNhat(id, dto));
    }

    // 5. Xóa (Mới)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.noContent().build();
    }
}