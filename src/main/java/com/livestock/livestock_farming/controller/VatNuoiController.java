package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.EnumDTO;
import com.livestock.livestock_farming.dto.VatNuoiDTO;
import com.livestock.livestock_farming.service.VatNuoiService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vat-nuoi")
@RequiredArgsConstructor
public class VatNuoiController {
    private final VatNuoiService vatNuoiService;

    @GetMapping
    public ResponseEntity<List<VatNuoiDTO>> layTatCa() {
        return ResponseEntity.ok(vatNuoiService.layTatCa());
    }

    @GetMapping("/danh-sach-loai")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<EnumDTO>> layDanhSachLoai() {
        return ResponseEntity.ok(vatNuoiService.layDanhSachLoaiVatNuoi());
    }

    @GetMapping("/danh-sach-trang-thai")
    public ResponseEntity<List<EnumDTO>> layDanhSachTrangThai() {
        return ResponseEntity.ok(vatNuoiService.layDanhSachTrangThaiVatNuoi());
    }

    /**
     * API lấy danh sách vật nuôi có trạng thái BÌNH THƯỜNG
     * URL: GET /api/vat-nuoi/san-sang-xuat
     */
    @GetMapping("/san-sang-xuat")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<List<VatNuoiDTO>> getVatNuoiSanSangXuat() {
        List<VatNuoiDTO> list = vatNuoiService.getVatNuoiSanSangXuat();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<VatNuoiDTO> themMoi(@RequestBody VatNuoiDTO dto) {
        return ResponseEntity.ok(vatNuoiService.themMoi(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<VatNuoiDTO> capNhat(@PathVariable Long id, @RequestBody VatNuoiDTO dto) {
        return ResponseEntity.ok(vatNuoiService.capNhat(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xoa(@PathVariable Long id) {
        vatNuoiService.xoa(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<VatNuoiDTO> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(vatNuoiService.layChiTiet(id));
    }

    @GetMapping("/tim-kiem")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<VatNuoiDTO>> timKiem(
            @RequestParam(required = false, defaultValue = "") String tuKhoa,
            @RequestParam(required = false) Integer idLoaiVatNuoi,
            @RequestParam(required = false) Integer idTrangThai,
            @RequestParam(required = false) Long idKhuVucChuong,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(vatNuoiService.timKiem(tuKhoa, idLoaiVatNuoi, idTrangThai, idKhuVucChuong, page, size));
    }
}