package com.livestock.livestock_farming.controller;

import com.livestock.livestock_farming.dto.PhieuXuatBanDTO;
import com.livestock.livestock_farming.entity.PhieuXuatBan;
import com.livestock.livestock_farming.repository.PhieuXuatBanRepository;
import com.livestock.livestock_farming.service.PhieuXuatBanService;
import com.livestock.livestock_farming.utils.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/phieu-xuat")
@RequiredArgsConstructor
public class PhieuXuatBanController {
    private final PhieuXuatBanService phieuXuatBanService;
    private final PhieuXuatBanRepository phieuXuatBanRepository;

    // 1. Tìm kiếm và Lọc
    @GetMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PageResponse<PhieuXuatBanDTO>> timKiem(
            @RequestParam(defaultValue = "") String tuKhoa,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tuNgay,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate denNgay,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(phieuXuatBanService.timKiem(tuKhoa, tuNgay, denNgay, page, size));
    }

    // 2. Xem chi tiết
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PhieuXuatBanDTO> layChiTiet(@PathVariable Long id) {
        return ResponseEntity.ok(phieuXuatBanService.layChiTiet(id));
    }

    // 3. Tạo phiếu xuất (Bán hàng)
    @PostMapping
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<PhieuXuatBanDTO> taoPhieuXuat(@RequestBody PhieuXuatBanDTO dto) {
        return ResponseEntity.ok(phieuXuatBanService.taoPhieuXuat(dto));
    }

    // 4. Xóa phiếu (Hoàn tác)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Void> xoaPhieu(@PathVariable Long id) {
        phieuXuatBanService.xoaPhieu(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/export-pdf")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<byte[]> exportPhieuXuatPdf(@PathVariable Long id) {
        try {
            // 1. Lấy dữ liệu phiếu từ Database
            PhieuXuatBan phieu = phieuXuatBanRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy phiếu xuất id: " + id));

            // 2. Gọi Service để xử lý Excel và convert sang PDF
            byte[] fileContent = phieuXuatBanService.exportPhieuXuatPdf(phieu);

            // 3. Tạo tên file động: Phieu_Xuat_Vat_Nuoi_PX001.pdf
            String maPhieu = (phieu.getMaPhieu() != null) ? phieu.getMaPhieu() : "Unknown";
            String fileName = "Phieu_Xuat_Vat_Nuoi_" + maPhieu + ".pdf";

            // 4. Trả về file cho trình duyệt tải xuống
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(fileContent);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}