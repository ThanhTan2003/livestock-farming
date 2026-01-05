package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.ChiTietXuatBan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ChiTietXuatBanRepository extends JpaRepository<ChiTietXuatBan, Long> {
    List<ChiTietXuatBan> findByPhieuXuatBan_Id(Long idPhieu);
}