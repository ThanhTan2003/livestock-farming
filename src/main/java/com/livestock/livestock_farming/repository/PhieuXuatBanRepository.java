package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.PhieuXuatBan;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface PhieuXuatBanRepository extends JpaRepository<PhieuXuatBan, Long> {
    @Query("SELECT p FROM PhieuXuatBan p WHERE " +
            "(:tuKhoa IS NULL OR p.nameNoAccents LIKE %:tuKhoa%) AND " +
            "(:tuNgay IS NULL OR p.ngayXuat >= :tuNgay) AND " +
            "(:denNgay IS NULL OR p.ngayXuat <= :denNgay)")
    Page<PhieuXuatBan> timKiem(@Param("tuKhoa") String tuKhoa,
                               @Param("tuNgay") LocalDate tuNgay,
                               @Param("denNgay") LocalDate denNgay,
                               Pageable pageable);

    boolean existsByMaPhieu(String maPhieu);
}