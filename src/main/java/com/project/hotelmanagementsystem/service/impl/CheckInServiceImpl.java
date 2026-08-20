package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Bill;
import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.entity.Payment;
import com.project.hotelmanagementsystem.entity.Refund;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.StayGuest;
import com.project.hotelmanagementsystem.repository.BillItemRepository;
import com.project.hotelmanagementsystem.repository.BillRepository;
import com.project.hotelmanagementsystem.repository.CheckInRepository;
import com.project.hotelmanagementsystem.repository.GuestRepository;
import com.project.hotelmanagementsystem.repository.PaymentRepository;
import com.project.hotelmanagementsystem.repository.RefundRepository;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.repository.StayGuestRepository;
import com.project.hotelmanagementsystem.service.CheckInService;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 入住登记Service实现类
 */
@Service
@Transactional
public class CheckInServiceImpl implements CheckInService {

    private final CheckInRepository checkInRepository;
    private final RoomRepository roomRepository;
    private final StayGuestRepository stayGuestRepository;
    private final GuestRepository guestRepository;
    private final RoomStatusLogService roomStatusLogService;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;

    @Autowired
    public CheckInServiceImpl(CheckInRepository checkInRepository, RoomRepository roomRepository,
                              StayGuestRepository stayGuestRepository, GuestRepository guestRepository,
                              RoomStatusLogService roomStatusLogService,
                              BillRepository billRepository, BillItemRepository billItemRepository,
                              PaymentRepository paymentRepository, RefundRepository refundRepository) {
        this.checkInRepository = checkInRepository;
        this.roomRepository = roomRepository;
        this.stayGuestRepository = stayGuestRepository;
        this.guestRepository = guestRepository;
        this.roomStatusLogService = roomStatusLogService;
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.paymentRepository = paymentRepository;
        this.refundRepository = refundRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CheckIn> findById(Integer id) {
        return checkInRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findAll() {
        return checkInRepository.findAll();
    }

    @Override
    public CheckIn save(CheckIn checkIn) {
        return checkInRepository.save(checkIn);
    }

    @Override
    public void deleteById(Integer id) {
        checkInRepository.deleteById(id);
    }

    @Override
    public CheckIn saveWithStayGuests(CheckIn checkIn, List<StayGuest> stayGuests) {
        if (checkIn.getGuestId() == null && checkIn.getIdNumber() != null && !checkIn.getIdNumber().isEmpty()) {
            Optional<Guest> existingGuest = guestRepository.findByIdNumber(checkIn.getIdNumber());
            if (existingGuest.isPresent()) {
                checkIn.setGuestId(existingGuest.get().getId());
                if (checkIn.getGuestName() == null || checkIn.getGuestName().isEmpty()) {
                    checkIn.setGuestName(existingGuest.get().getFirstName() + " " + existingGuest.get().getLastName());
                }
                if (checkIn.getPhone() == null || checkIn.getPhone().isEmpty()) {
                    checkIn.setPhone(existingGuest.get().getPhone());
                }
            }
        }

        if (checkIn.getExpectedCheckOutTime() != null && checkIn.getCheckInTime() != null) {
            // 只比较日期部分，要求退房日期必须晚于或等于入住日期
            if (checkIn.getExpectedCheckOutTime().toLocalDate().isBefore(checkIn.getCheckInTime().toLocalDate())) {
                throw new IllegalArgumentException("预计退房日期必须晚于或等于入住日期");
            }
        }

        CheckIn saved = checkInRepository.save(checkIn);

        // 构建同住客人列表，包含主登记人
        List<StayGuest> allStayGuests = new java.util.ArrayList<>();

        // 添加主登记人到同住客人表
        StayGuest primaryGuest = new StayGuest();
        primaryGuest.setCheckInId(saved.getId());
        primaryGuest.setGuestId(checkIn.getGuestId());
        primaryGuest.setName(checkIn.getGuestName());
        primaryGuest.setIdType(checkIn.getIdType());
        primaryGuest.setIdNumber(checkIn.getIdNumber());
        primaryGuest.setIsPrimary(true);
        allStayGuests.add(primaryGuest);

        // 添加其他同住人员
        if (stayGuests != null && !stayGuests.isEmpty()) {
            for (StayGuest sg : stayGuests) {
                sg.setCheckInId(saved.getId());
                sg.setIsPrimary(false);
                if (sg.getGuestId() == null && sg.getIdNumber() != null && !sg.getIdNumber().isEmpty()) {
                    Optional<Guest> existingGuest = guestRepository.findByIdNumber(sg.getIdNumber());
                    existingGuest.ifPresent(guest -> sg.setGuestId(guest.getId()));
                }
                allStayGuests.add(sg);
            }
        }

        stayGuestRepository.saveAll(allStayGuests);

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByGuestId(Integer guestId) {
        return checkInRepository.findByGuestId(guestId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByRoomId(Integer roomId) {
        return checkInRepository.findByRoomId(roomId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByStatus(String status) {
        return checkInRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByReservationId(Integer reservationId) {
        return checkInRepository.findByReservationId(reservationId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckIn> findByRoomIdAndStatus(Integer roomId, String status) {
        return checkInRepository.findByRoomIdAndStatus(roomId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> preCheckOut(Integer id) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("入住记录不存在: " + id));

        if (!"in_house".equals(checkIn.getStatus())) {
            throw new IllegalStateException("只有在住状态的入住记录才能办理退房");
        }

        LocalDateTime now = LocalDateTime.now();

        // 计算实际入住天数（当天入住当天退房按一天计算）
        long actualDays = 0;
        if (checkIn.getCheckInTime() != null) {
            actualDays = ChronoUnit.DAYS.between(
                    checkIn.getCheckInTime().toLocalDate(),
                    now.toLocalDate()
            );
            actualDays = Math.max(actualDays, 1);
        }

        // 计算房费 = 实际入住天数 * 单价
        BigDecimal roomCharge = BigDecimal.ZERO;
        if (checkIn.getRatePerNight() != null && actualDays > 0) {
            roomCharge = checkIn.getRatePerNight().multiply(BigDecimal.valueOf(actualDays));
        }

        // 查找账单，计算额外消费总和
        Bill bill = billRepository.findByCheckInIdAndBillStatus(id, "open")
                .stream().findFirst().orElse(null);

        BigDecimal additionalCharges = BigDecimal.ZERO;
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (bill != null) {
            additionalCharges = billItemRepository.sumAmountByBillId(bill.getId());
            if (additionalCharges == null) {
                additionalCharges = BigDecimal.ZERO;
            }
            depositAmount = bill.getDepositAmount() != null ? bill.getDepositAmount() : BigDecimal.ZERO;
        }

        // 总费用 = 房费 + 额外消费
        BigDecimal totalCharge = roomCharge.add(additionalCharges);

        // 差额 = 押金 - 总费用（正值表示需退款，负值表示需补价）
        BigDecimal diff = depositAmount.subtract(totalCharge);

        Map<String, Object> result = new HashMap<>();
        result.put("checkInId", id);
        result.put("actualDays", actualDays);
        result.put("roomCharge", roomCharge);
        result.put("additionalCharges", additionalCharges);
        result.put("depositAmount", depositAmount);
        result.put("totalCharge", totalCharge);
        result.put("diff", diff);
        result.put("needRefund", diff.compareTo(BigDecimal.ZERO) > 0);
        result.put("needPay", diff.compareTo(BigDecimal.ZERO) < 0);
        result.put("refundAmount", diff.compareTo(BigDecimal.ZERO) > 0 ? diff : BigDecimal.ZERO);
        result.put("payAmount", diff.compareTo(BigDecimal.ZERO) < 0 ? diff.abs() : BigDecimal.ZERO);

        return result;
    }

    @Override
    public CheckIn checkOut(Integer id) {
        return checkOut(id, null, null, null);
    }

    @Override
    public CheckIn checkOut(Integer id, Integer changedBy) {
        return checkOut(id, changedBy, null, null);
    }

    @Override
    public CheckIn checkOut(Integer id, Integer changedBy, String paymentMethod, String refundMethod) {
        CheckIn checkIn = checkInRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("入住记录不存在: " + id));

        if (!"in_house".equals(checkIn.getStatus())) {
            throw new IllegalStateException("只有在住状态的入住记录才能办理退房");
        }

        LocalDateTime checkOutTime = LocalDateTime.now();
        checkIn.setStatus("checked_out");
        checkIn.setActualCheckOutTime(checkOutTime);

        // 计算实际入住天数（当天入住当天退房按一天计算）
        long actualDays = 0;
        if (checkIn.getCheckInTime() != null) {
            actualDays = ChronoUnit.DAYS.between(
                    checkIn.getCheckInTime().toLocalDate(),
                    checkOutTime.toLocalDate()
            );
            actualDays = Math.max(actualDays, 1);
        }

        // 计算房费 = 实际入住天数 * 单价
        BigDecimal roomCharge = BigDecimal.ZERO;
        if (checkIn.getRatePerNight() != null && actualDays > 0) {
            roomCharge = checkIn.getRatePerNight().multiply(BigDecimal.valueOf(actualDays));
        }

        // 查找账单，计算额外消费总和
        Bill bill = billRepository.findByCheckInIdAndBillStatus(id, "open")
                .stream().findFirst().orElse(null);

        BigDecimal additionalCharges = BigDecimal.ZERO;
        BigDecimal depositAmount = BigDecimal.ZERO;
        if (bill != null) {
            additionalCharges = billItemRepository.sumAmountByBillId(bill.getId());
            if (additionalCharges == null) {
                additionalCharges = BigDecimal.ZERO;
            }
            depositAmount = bill.getDepositAmount() != null ? bill.getDepositAmount() : BigDecimal.ZERO;
        }

        // 总费用 = 房费 + 额外消费
        BigDecimal totalCharge = roomCharge.add(additionalCharges);
        checkIn.setTotalCharge(totalCharge);

        // 更新账单：设置总金额、关闭账单
        if (bill == null) {
            bill = new Bill();
            bill.setCheckInId(id);
            bill.setBillStatus("closed");
            bill.setTotalAmount(totalCharge);
            bill.setPaidAmount(depositAmount);
            bill.setDepositAmount(depositAmount);
            bill.setCreatedAt(checkOutTime);
            bill.setClosedAt(checkOutTime);
        } else {
            bill.setTotalAmount(totalCharge);
            bill.setBillStatus("closed");
            bill.setClosedAt(checkOutTime);
        }

        // 处理押金与总费用的差额
        BigDecimal diff = depositAmount.subtract(totalCharge);
        if (bill.getId() != null) {
            if (diff.compareTo(BigDecimal.ZERO) < 0) {
                // 押金不足，需要补差价
                BigDecimal needToPay = diff.abs();
                String payMethod = paymentMethod != null ? paymentMethod : "cash";
                Payment payment = new Payment();
                payment.setBillId(bill.getId());
                payment.setAmount(needToPay);
                payment.setPaymentMethod(payMethod);
                payment.setPaymentType("charge");
                payment.setPaymentDate(checkOutTime);
                payment.setEmployeeId(changedBy);
                paymentRepository.save(payment);

                // 更新已付金额
                bill.setPaidAmount(depositAmount.add(needToPay));
            } else if (diff.compareTo(BigDecimal.ZERO) > 0) {
                // 押金多余，需要退款
                BigDecimal needToRefund = diff;
                String refundM = refundMethod != null ? refundMethod : "cash";
                Refund refund = new Refund();
                refund.setBillId(bill.getId());
                refund.setAmount(needToRefund);
                refund.setRefundMethod(refundM);
                refund.setRefundDate(checkOutTime);
                refund.setEmployeeId(changedBy);
                refund.setNotes("押金退款");
                refundRepository.save(refund);

                // 更新已付金额为实际消费金额
                bill.setPaidAmount(totalCharge);
            }
        }

        billRepository.save(bill);

        // 更新房间状态为待打扫，并写入状态变更日志
        Room room = roomRepository.findById(checkIn.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("房间不存在: " + checkIn.getRoomId()));
        String oldStatus = room.getStatus();
        room.setStatus("dirty");
        roomRepository.save(room);

        roomStatusLogService.logStatusChange(
                room.getId(), oldStatus, "dirty", changedBy,
                "退房：入住记录 #" + checkIn.getId()
                        + (checkIn.getGuestName() != null ? "，客人：" + checkIn.getGuestName() : ""));

        return checkInRepository.save(checkIn);
    }
}