package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.VatNuoi;
import com.livestock.livestock_farming.enums.LoaiVatNuoi;
import com.livestock.livestock_farming.enums.TrangThaiVatNuoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VatNuoiRepository extends JpaRepository<VatNuoi, Long> {

    boolean existsByMaDinhDanh(String maDinhDanh);

    List<VatNuoi> findAllByTrangThai(TrangThaiVatNuoi trangThai);

    @Query("SELECT v FROM VatNuoi v WHERE " +
            "(:tuKhoa IS NULL OR v.nameNoAccents LIKE %:tuKhoa%) AND " +
            "(:loaiVatNuoi IS NULL OR v.loaiVatNuoi = :loaiVatNuoi) AND " +
            "(:trangThai IS NULL OR v.trangThai = :trangThai) AND " +
            "(:idKhuVucChuong IS NULL OR v.khuVucChuong.id = :idKhuVucChuong)")
    Page<VatNuoi> timKiemVatNuoi(@Param("tuKhoa") String tuKhoa,
                                 @Param("loaiVatNuoi") LoaiVatNuoi loaiVatNuoi,
                                 @Param("trangThai") TrangThaiVatNuoi trangThai,
                                 @Param("idKhuVucChuong") Long idKhuVucChuong,
                                 Pageable pageable);
}