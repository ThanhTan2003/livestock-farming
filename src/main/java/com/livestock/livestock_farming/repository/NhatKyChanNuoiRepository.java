package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.NhatKyChanNuoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface NhatKyChanNuoiRepository extends JpaRepository<NhatKyChanNuoi, Long> {
    @Query("SELECT n FROM NhatKyChanNuoi n WHERE " +
            "(:idKhuVuc IS NULL OR n.khuVucChuong.id = :idKhuVuc) AND " +
            "(:tuNgay IS NULL OR n.ngayGhiNhan >= :tuNgay) AND " +
            "(:denNgay IS NULL OR n.ngayGhiNhan <= :denNgay) AND " +
            "(:tuKhoa IS NULL OR n.nameNoAccents LIKE %:tuKhoa%)")
    Page<NhatKyChanNuoi> timKiem(@Param("idKhuVuc") Long idKhuVuc,
                                 @Param("tuNgay") LocalDate tuNgay,
                                 @Param("denNgay") LocalDate denNgay,
                                 @Param("tuKhoa") String tuKhoa,
                                 Pageable pageable);
}