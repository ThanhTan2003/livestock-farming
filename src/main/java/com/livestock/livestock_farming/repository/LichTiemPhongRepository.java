package com.livestock.livestock_farming.repository;

import com.livestock.livestock_farming.entity.LichTiemPhong;
import com.livestock.livestock_farming.enums.LoaiVatNuoi;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;

@Repository
public interface LichTiemPhongRepository extends JpaRepository<LichTiemPhong, Long> {

    // 1. Tìm kiếm tổng hợp (đã có từ trước)
    @Query("SELECT l FROM LichTiemPhong l WHERE " +
            "(:idVatNuoi IS NULL OR l.vatNuoi.id = :idVatNuoi) AND " +
            "(:tuKhoa IS NULL OR l.vatNuoi.nameNoAccents LIKE %:tuKhoa% OR l.nameNoAccents LIKE %:tuKhoa%) AND " +
            "(:loaiVatNuoi IS NULL OR l.vatNuoi.loaiVatNuoi = :loaiVatNuoi) AND " +
            "(:tuNgay IS NULL OR l.ngayTiemDuKien >= :tuNgay) AND " +
            "(:denNgay IS NULL OR l.ngayTiemDuKien <= :denNgay) AND " +
            "(:daHoanThanh IS NULL OR l.daHoanThanh = :daHoanThanh)")
    Page<LichTiemPhong> timKiemLichTiem(@Param("idVatNuoi") Long idVatNuoi,
                                        @Param("tuKhoa") String tuKhoa,
                                        @Param("loaiVatNuoi") LoaiVatNuoi loaiVatNuoi,
                                        @Param("tuNgay") LocalDate tuNgay,
                                        @Param("denNgay") LocalDate denNgay,
                                        @Param("daHoanThanh") Boolean daHoanThanh,
                                        Pageable pageable);

    // 2. Tìm lịch sắp tới (Ngày dự kiến >= hiện tại) - Trả về Page
    Page<LichTiemPhong> findByNgayTiemDuKienGreaterThanEqual(LocalDate ngayHienTai, Pageable pageable);

    // 3. Tìm lịch trễ hạn (Chưa xong AND Ngày dự kiến < hiện tại) - Trả về Page
    Page<LichTiemPhong> findByDaHoanThanhFalseAndNgayTiemDuKienLessThan(LocalDate ngayHienTai, Pageable pageable);

    // 4. Tìm lịch sử theo ID vật nuôi - Trả về Page
    Page<LichTiemPhong> findByVatNuoi_Id(Long idVatNuoi, Pageable pageable);
}