package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.EnumDTO;
import com.livestock.livestock_farming.dto.VatTuKhoDTO;
import com.livestock.livestock_farming.service.VatTuKhoService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vat-tu-kho")
@RequiredArgsConstructor
public class VatTuKhoController {
    private final VatTuKhoService service;

    @GetMapping("/tim-kiem")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<VatTuKhoDTO>> timKiem(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(required = false) Integer idLoaiVatTu,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.timKiem(tuKhoa, idLoaiVatTu, page, size));
    }

    @GetMapping("/danh-sach-loai")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<EnumDTO>> layDanhSachLoai() {
        return ResponseEntity.ok(service.layDanhSachLoaiVatTu());
    }

    @GetMapping("/canh-bao")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<VatTuKhoDTO>> layCanhBao() {
        return ResponseEntity.ok(service.layDanhSachCanhBao());
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<VatTuKhoDTO> themMoi(@RequestBody VatTuKhoDTO dto) {
        return ResponseEntity.ok(service.themMoi(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<VatTuKhoDTO> capNhat(@PathVariable Long id, @RequestBody VatTuKhoDTO dto) {
        return ResponseEntity.ok(service.capNhat(id, dto));
    }
}