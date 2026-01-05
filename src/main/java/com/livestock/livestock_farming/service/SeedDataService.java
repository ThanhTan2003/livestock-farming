package com.livestock.livestock_farming.service;

import com.livestock.livestock_farming.entity.*;
import com.livestock.livestock_farming.enums.*;
import com.livestock.livestock_farming.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeedDataService {

    private final KhuVucChuongRepository khuVucChuongRepository;
    private final VatTuKhoRepository vatTuKhoRepository;
    private final VatNuoiRepository vatNuoiRepository;
    private final LichTiemPhongRepository lichTiemPhongRepository;
    private final NhatKyChanNuoiRepository nhatKyChanNuoiRepository;
    private final PhieuXuatBanRepository phieuXuatBanRepository;
    private final ChiTietXuatBanRepository chiTietXuatBanRepository;

    // --- 1. DỮ LIỆU CỐ ĐỊNH (CONSTANTS) ---
    private final String[] NHAN_VIEN = {
            "Nguyễn Văn Hùng", "Trần Thị Mai", "Lê Minh Tuấn", "Phạm Quốc Bảo",
            "Hoàng Thị Lan", "Vũ Đức Đam", "Đặng Ngọc Ánh", "Bùi Văn Long"
    };

    private final String[] KHACH_HANG = {
            "Công ty CP Chăn nuôi C.P. Việt Nam", "Hệ thống Siêu thị WinMart",
            "Thương lái Nguyễn Văn Nam", "Lò mổ gia súc Thịnh Liệt",
            "Nhà máy chế biến thực phẩm Đức Việt"
    };

    // BỆNH LÝ THEO LOÀI
    private final Map<LoaiVatNuoi, List<String>> MAP_BENH = Map.of(
            LoaiVatNuoi.BO, List.of("Lở mồm long móng", "Viêm da nổi cục", "Tụ huyết trùng", "Chướng hơi dạ cỏ"),
            LoaiVatNuoi.HEO, List.of("Tai xanh (PRRS)", "Dịch tả lợn", "Tiêu chảy cấp (PED)", "Viêm phổi"),
            LoaiVatNuoi.GA, List.of("Newcastle (Gà rù)", "Gumboro", "Hen khẹc (CRD)", "Cầu trùng")
    );

    // VẮC XIN THEO LOÀI
    private final Map<LoaiVatNuoi, List<String>> MAP_VACXIN = Map.of(
            LoaiVatNuoi.BO, List.of("Vắc-xin Lở mồm long móng", "Vắc-xin Tụ huyết trùng"),
            LoaiVatNuoi.HEO, List.of("Vắc-xin Tai xanh", "Vắc-xin Dịch tả lợn", "Vắc-xin E.coli"),
            LoaiVatNuoi.GA, List.of("Vắc-xin Newcastle", "Vắc-xin Gumboro", "Vắc-xin Cúm H5N1")
    );

    // THỨC ĂN THEO LOÀI
    private final Map<LoaiVatNuoi, List<String>> MAP_THUC_AN = Map.of(
            LoaiVatNuoi.BO, List.of("Cỏ voi ủ chua", "Cám bò thịt vỗ béo", "Rơm khô"),
            LoaiVatNuoi.HEO, List.of("Cám heo cai sữa", "Cám heo thịt", "Cám đậm đặc"),
            LoaiVatNuoi.GA, List.of("Cám gà con", "Cám gà đẻ trứng", "Ngô mảnh")
    );

    // THUỐC DÙNG CHUNG
    private final String[] THUOC_THU_Y = {
            "Kháng sinh Amoxicillin", "Vitamin B-Complex", "Thuốc sát trùng Iodine",
            "Gluco-KC Thảo dược", "Điện giải Oresol"
    };

    // VẮC XIN BẮT BUỘC (Dùng để sinh lịch tiêm)
    private final Map<LoaiVatNuoi, List<String>> REQUIRED_VACCINES = Map.of(
            LoaiVatNuoi.BO, List.of("Vắc-xin Lở mồm long móng", "Vắc-xin Tụ huyết trùng"),
            LoaiVatNuoi.HEO, List.of("Vắc-xin Tai xanh", "Vắc-xin Dịch tả lợn"),
            LoaiVatNuoi.GA, List.of("Vắc-xin Newcastle", "Vắc-xin Gumboro")
    );

    // --- 2. HÀM CHÍNH (INIT DATA) ---
    @Transactional
    public String initData() {
        log.info("Bắt đầu đồng bộ dữ liệu mẫu (Check & Insert)...");
        StringBuilder resultMsg = new StringBuilder();

        // B1. Master Data (Khu vực, Vật tư, Vật nuôi)
        List<KhuVucChuong> allChuongs = syncKhuVucChuong(resultMsg);
        syncVatTuKho(resultMsg);
        List<VatNuoi> allVatNuois = syncVatNuoi(allChuongs, resultMsg);

        // B2. Transaction Data (Lịch tiêm, Nhật ký, Xuất bán)
        syncLichTiemPhong(allVatNuois, resultMsg);
        syncNhatKyChanNuoi(allChuongs, allVatNuois, resultMsg);
        syncPhieuXuatBan(allVatNuois, resultMsg);

        return resultMsg.toString().isEmpty() ? "Dữ liệu đã đầy đủ, không có gì thay đổi." : resultMsg.toString();
    }

    // --- 3. CÁC HÀM LOGIC CHI TIẾT ---

    // === SYNC KHU VỰC CHUỒNG ===
    private List<KhuVucChuong> syncKhuVucChuong(StringBuilder msg) {
        List<KhuVucChuong> result = new ArrayList<>();
        int addedCount = 0;

        // Kế hoạch chuồng trại
        List<String[]> plan = new ArrayList<>();
        // Format: {Tên, Vị trí, Sức chứa}
        for (int i = 1; i <= 5; i++) plan.add(new String[]{"Chuồng Bò số " + i, "Khu A (Cao nguyên)", "20"});
        for (int i = 1; i <= 8; i++) plan.add(new String[]{"Chuồng Heo số " + i, "Khu B (Chuồng kín)", "50"});
        for (int i = 1; i <= 5; i++) plan.add(new String[]{"Chuồng Gà số " + i, "Khu C (Vườn đồi)", "500"});

        List<KhuVucChuong> existing = khuVucChuongRepository.findAll();

        for (String[] p : plan) {
            String ten = p[0];
            Optional<KhuVucChuong> opt = existing.stream().filter(k -> k.getTenKhuVuc().equals(ten)).findFirst();
            if (opt.isPresent()) {
                result.add(opt.get());
            } else {
                KhuVucChuong k = new KhuVucChuong();
                k.setTenKhuVuc(ten);
                k.setViTri(p[1]);
                k.setSucChua(Integer.parseInt(p[2]));
                k.setSoLuongHienTai(0); // Tính sau
                k.setMoTa("Chuồng trại tiêu chuẩn VietGAP.");
                result.add(khuVucChuongRepository.save(k));
                addedCount++;
            }
        }
        if (addedCount > 0) msg.append("Đã thêm ").append(addedCount).append(" khu vực chuồng. ");
        return result;
    }

    // === SYNC VẬT TƯ KHO ===
    private void syncVatTuKho(StringBuilder msg) {
        int addedCount = 0;
        List<String> itemsToCheck = new ArrayList<>();
        MAP_THUC_AN.values().forEach(itemsToCheck::addAll);
        MAP_VACXIN.values().forEach(itemsToCheck::addAll);
        Collections.addAll(itemsToCheck, THUOC_THU_Y);

        List<VatTuKho> existing = vatTuKhoRepository.findAll();

        for (String ten : itemsToCheck) {
            boolean exists = existing.stream().anyMatch(v -> v.getTenVatTu().equalsIgnoreCase(ten));
            if (!exists) {
                VatTuKho v = new VatTuKho();
                v.setTenVatTu(ten);
                if (ten.contains("Cám") || ten.contains("Cỏ") || ten.contains("Ngô") || ten.contains("Rơm")) {
                    v.setLoaiVatTu(LoaiVatTu.THUC_AN);
                    v.setDonViTinh(ten.contains("Cỏ") || ten.contains("Ngô") ? "Kg" : "Bao 25kg");
                } else if (ten.contains("vệ sinh")) {
                    v.setLoaiVatTu(LoaiVatTu.THIET_BI);
                    v.setDonViTinh("Cái");
                } else {
                    v.setLoaiVatTu(LoaiVatTu.THUOC);
                    v.setDonViTinh(ten.contains("Vắc-xin") ? "Liều" : "Chai/Lọ");
                }
                v.setSoLuongTon((double) randomInt(50, 500));
                v.setNguongCanhBao(20.0);
                v.setNgayHetHan(LocalDate.now().plusMonths(randomInt(3, 12)));
                vatTuKhoRepository.save(v);
                addedCount++;
            }
        }
        if (addedCount > 0) msg.append("Đã thêm ").append(addedCount).append(" loại vật tư. ");
    }

    // === SYNC VẬT NUÔI ===
    private List<VatNuoi> syncVatNuoi(List<KhuVucChuong> chuongs, StringBuilder msg) {
        int addedCount = 0;

        for (KhuVucChuong chuong : chuongs) {
            LoaiVatNuoi loai = chuong.getTenKhuVuc().contains("Bò") ? LoaiVatNuoi.BO :
                    (chuong.getTenKhuVuc().contains("Heo") ? LoaiVatNuoi.HEO : LoaiVatNuoi.GA);

            // Lấy số chuồng từ tên (vd: "Chuồng Bò số 1" -> "1")
            String[] parts = chuong.getTenKhuVuc().split(" ");
            String chuongIndex = parts[parts.length - 1];

            // Định mức số con để tạo
            int target = (loai == LoaiVatNuoi.BO) ? 3 : (loai == LoaiVatNuoi.HEO ? 8 : 20);

            for (int k = 1; k <= target; k++) {
                String maDinhDanh = String.format("VN-%s-%s-%03d",
                        (loai == LoaiVatNuoi.BO ? "BO" : (loai == LoaiVatNuoi.HEO ? "HEO" : "GA")),
                        chuongIndex, k);

                if (!vatNuoiRepository.existsByMaDinhDanh(maDinhDanh)) {
                    VatNuoi v = new VatNuoi();
                    v.setMaDinhDanh(maDinhDanh);
                    v.setLoaiVatNuoi(loai);
                    v.setKhuVucChuong(chuong);
                    v.setGiongLoai(getRandomBreed(loai));
                    v.setNguonGoc("Trại giống TW");

                    int daysAgo = randomInt(30, 180);
                    v.setNgayNhap(LocalDate.now().minusDays(daysAgo));

                    double weight = calculateWeight(loai, daysAgo);
                    v.setKhoiLuongNhap(weight * 0.4);
                    v.setKhoiLuongHienTai(weight);

                    // Random trạng thái: 80% Bình thường, 5% Ốm, 15% Đã bán
                    int rand = randomInt(1, 100);
                    if (rand <= 80) v.setTrangThai(TrangThaiVatNuoi.BINH_THUONG);
                    else if (rand <= 85) v.setTrangThai(TrangThaiVatNuoi.OM);
                    else v.setTrangThai(TrangThaiVatNuoi.DA_XUAT_BAN);

                    vatNuoiRepository.save(v);
                    addedCount++;
                }
            }

            // Cập nhật lại số lượng thực tế
            long currentStock = vatNuoiRepository.findAll().stream()
                    .filter(v -> v.getKhuVucChuong() != null
                            && v.getKhuVucChuong().getId().equals(chuong.getId())
                            && v.getTrangThai() != TrangThaiVatNuoi.DA_XUAT_BAN
                            && v.getTrangThai() != TrangThaiVatNuoi.DA_CHET)
                    .count();
            chuong.setSoLuongHienTai((int) currentStock);
            khuVucChuongRepository.save(chuong);
        }
        if (addedCount > 0) msg.append("Đã thêm ").append(addedCount).append(" vật nuôi. ");
        return vatNuoiRepository.findAll();
    }

    // === SYNC LỊCH TIÊM PHÒNG (Upsert theo vắc xin bắt buộc) ===
    private void syncLichTiemPhong(List<VatNuoi> vatNuois, StringBuilder msg) {
        int count = 0;
        List<LichTiemPhong> existingLich = lichTiemPhongRepository.findAll();

        for (VatNuoi vn : vatNuois) {
            if (vn.getTrangThai() == TrangThaiVatNuoi.DA_XUAT_BAN || vn.getTrangThai() == TrangThaiVatNuoi.DA_CHET)
                continue;

            List<String> required = REQUIRED_VACCINES.get(vn.getLoaiVatNuoi());
            for (String vacxin : required) {
                // Check xem con này đã có lịch cho vắc xin này chưa
                boolean has = existingLich.stream().anyMatch(l ->
                        l.getVatNuoi().getId().equals(vn.getId()) && l.getTenVacXin().equals(vacxin));

                if (!has) {
                    LichTiemPhong l = new LichTiemPhong();
                    l.setVatNuoi(vn);
                    l.setTenVacXin(vacxin);

                    int rand = randomInt(1, 100);
                    if (rand <= 60) { // 60% Đã tiêm
                        l.setDaHoanThanh(true);
                        l.setNgayTiemDuKien(LocalDate.now().minusDays(randomInt(5, 60)));
                        l.setNgayTiemThucTe(l.getNgayTiemDuKien());
                        l.setNguoiThucHien(NHAN_VIEN[randomInt(0, NHAN_VIEN.length - 1)]);
                    } else if (rand <= 90) { // 30% Sắp tiêm
                        l.setDaHoanThanh(false);
                        l.setNgayTiemDuKien(LocalDate.now().plusDays(randomInt(1, 30)));
                    } else { // 10% Trễ hạn (Cảnh báo)
                        l.setDaHoanThanh(false);
                        l.setNgayTiemDuKien(LocalDate.now().minusDays(randomInt(1, 5)));
                    }
                    lichTiemPhongRepository.save(l);
                    count++;
                }
            }
        }
        if (count > 0) msg.append("Đã cập nhật ").append(count).append(" lịch tiêm. ");
    }

    // === SYNC NHẬT KÝ (Upsert 10 ngày gần nhất) ===
    private void syncNhatKyChanNuoi(List<KhuVucChuong> chuongs, List<VatNuoi> vatNuois, StringBuilder msg) {
        int count = 0;
        List<NhatKyChanNuoi> existingLogs = nhatKyChanNuoiRepository.findAll();
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 10; i++) {
            LocalDate date = today.minusDays(i);

            for (KhuVucChuong chuong : chuongs) {
                if (chuong.getSoLuongHienTai() == 0) continue;

                boolean hasLog = existingLogs.stream().anyMatch(nk ->
                        nk.getKhuVucChuong().getId().equals(chuong.getId()) && nk.getNgayGhiNhan().isEqual(date));

                if (!hasLog) {
                    NhatKyChanNuoi nk = new NhatKyChanNuoi();
                    nk.setKhuVucChuong(chuong);
                    nk.setNgayGhiNhan(date);
                    nk.setNguoiGhiNhan(NHAN_VIEN[randomInt(0, NHAN_VIEN.length - 1)]);

                    double dinhMuc = chuong.getTenKhuVuc().contains("Bò") ? 15.0 :
                            (chuong.getTenKhuVuc().contains("Heo") ? 2.5 : 0.15);
                    nk.setThucAnTieuThu(dinhMuc * chuong.getSoLuongHienTai());

                    // Đếm số con ốm trong chuồng (Giả định trạng thái ốm kéo dài)
                    long soConOm = vatNuois.stream().filter(v ->
                            v.getKhuVucChuong().getId().equals(chuong.getId()) && v.getTrangThai() == TrangThaiVatNuoi.OM
                    ).count();

                    nk.setSoConOm((int) soConOm);
                    nk.setSoConChet(0);

                    if (soConOm > 0) {
                        LoaiVatNuoi loai = chuong.getTenKhuVuc().contains("Bò") ? LoaiVatNuoi.BO :
                                (chuong.getTenKhuVuc().contains("Heo") ? LoaiVatNuoi.HEO : LoaiVatNuoi.GA);
                        String benh = MAP_BENH.get(loai).get(randomInt(0, MAP_BENH.get(loai).size() - 1));
                        nk.setGhiChu("Phát hiện " + soConOm + " con nghi nhiễm " + benh + ".");
                    } else {
                        nk.setGhiChu("Bình thường.");
                    }
                    nhatKyChanNuoiRepository.save(nk);
                    count++;
                }
            }
        }
        if (count > 0) msg.append("Đã cập nhật ").append(count).append(" nhật ký chăn nuôi. ");
    }

    // === SYNC PHIẾU XUẤT BÁN (Gom con chưa có hóa đơn) ===
    private void syncPhieuXuatBan(List<VatNuoi> vatNuois, StringBuilder msg) {
        int count = 0;

        // 1. Lấy tất cả con đã bán
        List<VatNuoi> soldAnimals = vatNuois.stream()
                .filter(v -> v.getTrangThai() == TrangThaiVatNuoi.DA_XUAT_BAN)
                .collect(Collectors.toList());

        // 2. Lấy danh sách ID đã có trong chi tiết hóa đơn
        List<Long> soldIdsInDb = chiTietXuatBanRepository.findAll().stream()
                .map(ct -> ct.getVatNuoi().getId())
                .collect(Collectors.toList());

        // 3. Lọc ra những con chưa có hóa đơn
        List<VatNuoi> animalsNeedingBill = soldAnimals.stream()
                .filter(v -> !soldIdsInDb.contains(v.getId()))
                .collect(Collectors.toList());

        if (animalsNeedingBill.isEmpty()) return;

        // 4. Tạo phiếu (Batch 5 con)
        int batchSize = 5;
        for (int i = 0; i < animalsNeedingBill.size(); i += batchSize) {
            int end = Math.min(i + batchSize, animalsNeedingBill.size());
            List<VatNuoi> batch = animalsNeedingBill.subList(i, end);

            PhieuXuatBan p = new PhieuXuatBan();
            p.setMaPhieu("PX-" + System.currentTimeMillis() + "-" + (i / batchSize));
            p.setNgayXuat(LocalDate.now());
            p.setTenKhachHang(KHACH_HANG[randomInt(0, KHACH_HANG.length - 1)]);
            p.setSoDienThoai("09" + randomInt(10000000, 99999999));
            p.setDiaChi("Hà Nội");
            p.setGhiChu("Xuất bán bổ sung (Seed Data)");
            p = phieuXuatBanRepository.save(p);

            List<ChiTietXuatBan> chiTiets = new ArrayList<>();
            double tongTien = 0;
            double tongKL = 0;

            for (VatNuoi vn : batch) {
                ChiTietXuatBan ct = new ChiTietXuatBan();
                ct.setPhieuXuatBan(p);
                ct.setVatNuoi(vn);
                ct.setKhoiLuongXuat(vn.getKhoiLuongHienTai());
                double donGia = (vn.getLoaiVatNuoi() == LoaiVatNuoi.BO) ? 95000 :
                        (vn.getLoaiVatNuoi() == LoaiVatNuoi.HEO ? 65000 : 120000);
                ct.setDonGia(donGia);
                ct.setThanhTien(donGia * vn.getKhoiLuongHienTai());
                tongTien += ct.getThanhTien();
                tongKL += ct.getKhoiLuongXuat();
                chiTiets.add(ct);
            }
            chiTietXuatBanRepository.saveAll(chiTiets);
            p.setTongSoLuong(batch.size());
            p.setTongKhoiLuong(tongKL);
            p.setTongTien(tongTien);
            phieuXuatBanRepository.save(p);
            count++;
        }
        if (count > 0) msg.append("Đã tạo thêm ").append(count).append(" phiếu xuất bán.");
    }

    // --- UTILS ---
    private String getRandomBreed(LoaiVatNuoi loai) {
        if (loai == LoaiVatNuoi.BO)
            return List.of("Bò 3B (Bỉ)", "Bò Brahman", "Bò Angus").get(randomInt(0, 2));
        if (loai == LoaiVatNuoi.HEO)
            return List.of("Heo Landrace", "Heo Yorkshire", "Heo Duroc").get(randomInt(0, 2));
        return List.of("Gà Ri lai", "Gà Đông Tảo", "Gà Mía").get(randomInt(0, 2));
    }

    private double calculateWeight(LoaiVatNuoi loai, int days) {
        if (loai == LoaiVatNuoi.BO) return 150 + days * 0.8;
        if (loai == LoaiVatNuoi.HEO) return 10 + days * 0.6;
        return 0.2 + days * 0.02;
    }

    private int randomInt(int min, int max) {
        if (min >= max) return min;
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}