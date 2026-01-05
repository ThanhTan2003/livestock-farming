package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.VatTuKho;
import com.livestock.livestock_farming.enums.LoaiVatTu;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VatTuKhoRepository extends JpaRepository<VatTuKho, Long> {
    @Query("SELECT v FROM VatTuKho v WHERE " +
            "(:tuKhoa IS NULL OR v.nameNoAccents LIKE %:tuKhoa%) AND " +
            "(:loaiVatTu IS NULL OR v.loaiVatTu = :loaiVatTu)")
    Page<VatTuKho> timKiem(@Param("tuKhoa") String tuKhoa,
                           @Param("loaiVatTu") LoaiVatTu loaiVatTu,
                           Pageable pageable);

    // Tìm vật tư sắp hết (Tồn <= Cảnh báo)
    @Query("SELECT v FROM VatTuKho v WHERE v.soLuongTon <= v.nguongCanhBao")
    List<VatTuKho> findCanhBaoTonKho();
}