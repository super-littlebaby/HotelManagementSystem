package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import com.project.hotelmanagementsystem.service.FacilityDamageService;
import com.project.hotelmanagementsystem.service.RoomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.Comparator;

@Service
@Transactional
public class FacilityDamageServiceImpl implements FacilityDamageService {

    private final RoomRepository roomRepository;
    private final CheckInRepository checkInRepository;
    private final GuestRepository guestRepository;
    private final RoomTypeFacilityRepository roomTypeFacilityRepository;
    private final FacilityRepository facilityRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final RoomService roomService;

    public FacilityDamageServiceImpl(RoomRepository roomRepository,
                                      CheckInRepository checkInRepository,
                                      GuestRepository guestRepository,
                                      RoomTypeFacilityRepository roomTypeFacilityRepository,
                                      FacilityRepository facilityRepository,
                                      BillRepository billRepository,
                                      BillItemRepository billItemRepository,
                                      RoomService roomService) {
        this.roomRepository = roomRepository;
        this.checkInRepository = checkInRepository;
        this.guestRepository = guestRepository;
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
        this.facilityRepository = facilityRepository;
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.roomService = roomService;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRoomDamageInfo(String roomNumber, Integer employeeId) {
        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            throw new RuntimeException("房间不存在");
        }
        Room room = roomOpt.get();

        Map<String, Object> result = new HashMap<>();
        result.put("roomId", room.getId());
        result.put("roomNumber", room.getRoomNumber());
        result.put("hotelId", room.getHotelId());
        result.put("roomTypeId", room.getRoomTypeId());
        result.put("roomTypeName", room.getRoomType() != null ? room.getRoomType().getName() : "");
        result.put("hotelName", room.getHotel() != null ? room.getHotel().getName() : "");
        result.put("status", room.getStatus());

        List<Integer> facilityIds = roomTypeFacilityRepository.findFacilityIdsByRoomTypeId(room.getRoomTypeId());
        List<Map<String, Object>> facilities = new ArrayList<>();
        for (Integer fid : facilityIds) {
            Optional<Facility> facOpt = facilityRepository.findById(fid);
            facOpt.ifPresent(f -> {
                Map<String, Object> fm = new HashMap<>();
                fm.put("id", f.getId());
                fm.put("name", f.getName());
                fm.put("price", f.getPrice());
                facilities.add(fm);
            });
        }
        result.put("facilities", facilities);

        List<CheckIn> checkIns = checkInRepository.findByRoomId(room.getId());
        CheckIn currentOrRecent = null;
        if (!checkIns.isEmpty()) {
            currentOrRecent = checkIns.stream()
                    .filter(c -> "in_house".equals(c.getStatus()) || "checked_in".equals(c.getStatus()))
                    .findFirst()
                    .orElse(checkIns.stream()
                            .max(Comparator.comparing(CheckIn::getCheckInTime))
                            .orElse(checkIns.get(0)));
        }

        Map<String, Object> guestInfo = new HashMap<>();
        if (currentOrRecent != null) {
            guestInfo.put("checkInId", currentOrRecent.getId());
            guestInfo.put("checkInStatus", currentOrRecent.getStatus());
            guestInfo.put("checkInTime", currentOrRecent.getCheckInTime());
            guestInfo.put("guestId", currentOrRecent.getGuestId());

            if (currentOrRecent.getGuestId() != null) {
                Optional<Guest> guestOpt = guestRepository.findById(currentOrRecent.getGuestId());
                if (guestOpt.isPresent()) {
                    Guest g = guestOpt.get();
                    String fullName = (g.getFirstName() != null ? g.getFirstName() : "") +
                            (g.getLastName() != null ? g.getLastName() : "");
                    guestInfo.put("guestName", fullName.isEmpty() ? currentOrRecent.getGuestName() : fullName);
                    guestInfo.put("guestPhone", g.getPhone());
                    guestInfo.put("guestEmail", g.getEmail());
                }
            }
            if (!guestInfo.containsKey("guestName")) {
                guestInfo.put("guestName", currentOrRecent.getGuestName());
                guestInfo.put("guestPhone", currentOrRecent.getPhone());
            }
        }
        result.put("guest", guestInfo);

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> reportDamage(Map<String, Object> requestBody, Integer employeeId) {
        String roomNumber = (String) requestBody.get("roomNumber");
        String notes = (String) requestBody.getOrDefault("notes", "");
        Boolean guestCausedDamage = (Boolean) requestBody.get("guestCausedDamage");
        List<Map<String, Object>> damagedFacilities = (List<Map<String, Object>>) requestBody.get("damagedFacilities");

        if (roomNumber == null || roomNumber.trim().isEmpty()) {
            throw new RuntimeException("房间号不能为空");
        }
        if (damagedFacilities == null || damagedFacilities.isEmpty()) {
            throw new RuntimeException("至少需要选择一个损坏的设施");
        }

        Optional<Room> roomOpt = roomRepository.findByRoomNumber(roomNumber);
        if (roomOpt.isEmpty()) {
            throw new RuntimeException("房间不存在");
        }
        Room room = roomOpt.get();

        boolean wasOccupied = "occupied".equals(room.getStatus()) || "in_house".equals(room.getStatus());
        if (wasOccupied) {
            roomService.updateStatus(room.getId(), room.getStatus(), employeeId,
                    "设施损坏：" + notes + "（入住中，待退房后转为维修中）");
        } else {
            roomService.updateStatus(room.getId(), "out_of_order", employeeId,
                    "设施损坏：" + notes);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("roomNumber", roomNumber);
        result.put("roomStatus", wasOccupied ? room.getStatus() : "out_of_order");

        if (Boolean.TRUE.equals(guestCausedDamage)) {
            CheckIn activeCheckIn = findActiveCheckIn(room.getId());
            if (activeCheckIn == null) {
                result.put("billCreated", false);
                result.put("message", wasOccupied ?
                        "损坏信息已记录（入住中），但未找到入住记录，无法生成账单" :
                        "房间已设置为维修中，但未找到任何入住记录，无法生成账单");
                return result;
            }

            Bill bill = findOrCreateBill(activeCheckIn.getId());

            BigDecimal totalDamageAmount = BigDecimal.ZERO;
            List<Map<String, Object>> billItemDetails = new ArrayList<>();

            for (Map<String, Object> df : damagedFacilities) {
                Integer facilityId = (Integer) df.get("facilityId");
                BigDecimal compensationPercent = new BigDecimal(df.get("compensationPercent").toString());

                Optional<Facility> facOpt = facilityRepository.findById(facilityId);
                if (facOpt.isEmpty()) continue;

                Facility facility = facOpt.get();
                BigDecimal facilityPrice = facility.getPrice();
                BigDecimal damageAmount = facilityPrice.multiply(compensationPercent)
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

                BillItem billItem = new BillItem();
                billItem.setBillId(bill.getId());
                billItem.setItemType("damage");
                billItem.setDescription("设施损坏赔偿：" + facility.getName() +
                        "（原价" + facilityPrice + "元，赔偿" + compensationPercent + "%）" +
                        (notes != null ? "，备注：" + notes : ""));
                billItem.setFacilityId(facilityId);
                billItem.setQuantity(BigDecimal.ONE);
                billItem.setUnitPrice(damageAmount);
                billItem.setAmount(damageAmount);
                billItem.setChargeDate(LocalDate.now());
                billItem.setEmployeeId(employeeId);
                billItemRepository.save(billItem);

                totalDamageAmount = totalDamageAmount.add(damageAmount);

                Map<String, Object> detail = new HashMap<>();
                detail.put("facilityName", facility.getName());
                detail.put("facilityPrice", facilityPrice);
                detail.put("compensationPercent", compensationPercent);
                detail.put("damageAmount", damageAmount);
                billItemDetails.add(detail);
            }

            bill.setTotalAmount(bill.getTotalAmount() != null ?
                    bill.getTotalAmount().add(totalDamageAmount) : totalDamageAmount);
            billRepository.save(bill);

            result.put("billId", bill.getId());
            result.put("billStatus", bill.getBillStatus());
            result.put("totalDamageAmount", totalDamageAmount);
            result.put("billItems", billItemDetails);
            result.put("billCreated", true);
            result.put("message", "损坏赔偿已记入账单，共" + billItemDetails.size() + "项设施，合计" +
                    totalDamageAmount + "元");
        } else {
            result.put("billCreated", false);
            result.put("message", wasOccupied ?
                    "损坏信息已记录（入住中），待退房后转为维修中，损坏非客人原因，未生成账单" :
                    "房间已设置为维修中，损坏非客人原因，未生成账单");
        }

        return result;
    }

    private CheckIn findActiveCheckIn(Integer roomId) {
        List<CheckIn> checkIns = checkInRepository.findByRoomId(roomId);
        CheckIn active = checkIns.stream()
                .filter(c -> "in_house".equals(c.getStatus()))
                .findFirst()
                .orElse(null);
        if (active != null) {
            return active;
        }
        return checkIns.stream()
                .max(Comparator.comparing(CheckIn::getCheckInTime))
                .orElse(null);
    }

    private Bill findOrCreateBill(Integer checkInId) {
        List<Bill> bills = billRepository.findByCheckInId(checkInId);
        Bill openBill = bills.stream()
                .filter(b -> "open".equals(b.getBillStatus()))
                .findFirst()
                .orElse(null);

        if (openBill != null) {
            return openBill;
        }

        Bill newBill = new Bill();
        newBill.setCheckInId(checkInId);
        newBill.setBillStatus("open");
        newBill.setTotalAmount(BigDecimal.ZERO);
        newBill.setPaidAmount(BigDecimal.ZERO);
        newBill.setDepositAmount(BigDecimal.ZERO);
        newBill.setCreatedAt(java.time.LocalDateTime.now());
        return billRepository.save(newBill);
    }
}
