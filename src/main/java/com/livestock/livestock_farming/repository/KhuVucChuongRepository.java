package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.KhuVucChuong;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KhuVucChuongRepository extends JpaRepository<KhuVucChuong, Long> {
    @Query("SELECT k FROM KhuVucChuong k WHERE (:tuKhoa IS NULL OR k.nameNoAccents LIKE %:tuKhoa%)")
    Page<KhuVucChuong> timKiem(@Param("tuKhoa") String tuKhoa, Pageable pageable);
}