package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.KhuVucChuongDTO;
import com.livestock.livestock_farming.service.KhuVucChuongService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/khu-vuc-chuong")
@RequiredArgsConstructor
public class KhuVucChuongController {
    private final KhuVucChuongService service;

    @GetMapping
    public ResponseEntity<List<KhuVucChuongDTO>> layTatCa() {
        return ResponseEntity.ok(service.layTatCa());
    }

    @GetMapping("/tim-kiem")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<KhuVucChuongDTO>> timKiem(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(service.timKiem(tuKhoa, page, size));
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<KhuVucChuongDTO> themMoi(@RequestBody KhuVucChuongDTO dto) {
        return ResponseEntity.ok(service.themMoi(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<KhuVucChuongDTO> capNhat(@PathVariable Long id, @RequestBody KhuVucChuongDTO dto) {
        return ResponseEntity.ok(service.capNhat(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xoa(@PathVariable Long id) {
        service.xoa(id);
        return ResponseEntity.noContent().build();
    }
}