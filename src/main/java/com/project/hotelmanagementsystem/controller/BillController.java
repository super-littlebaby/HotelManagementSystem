package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.dto.BillDTO;
import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import com.project.hotelmanagementsystem.service.BillService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账单控制层
 * <p>
 * 负责客人入住期间消费账单的增删改查及按入住记录、账单状态条件检索，并支持查询最新账单，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "账单管理", description = "账单信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;
    private final BillRepository billRepository;
    private final DataIsolationService dataIsolationService;
    private final CheckInRepository checkInRepository;
    private final RoomRepository roomRepository;
    private final GuestRepository guestRepository;
    private final BillItemRepository billItemRepository;
    private final RefundRepository refundRepository;
    private final PaymentRepository paymentRepository;

    public BillController(BillService billService, BillRepository billRepository, DataIsolationService dataIsolationService,
                          CheckInRepository checkInRepository, RoomRepository roomRepository,
                          GuestRepository guestRepository, BillItemRepository billItemRepository,
                          RefundRepository refundRepository, PaymentRepository paymentRepository) {
        this.billService = billService;
        this.billRepository = billRepository;
        this.dataIsolationService = dataIsolationService;
        this.checkInRepository = checkInRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.billItemRepository = billItemRepository;
        this.refundRepository = refundRepository;
        this.paymentRepository = paymentRepository;
    }

    private BillDTO convertToDTO(Bill bill) {
        BillDTO dto = new BillDTO();
        dto.setId(bill.getId());
        dto.setCheckInId(bill.getCheckInId());
        dto.setBillStatus(bill.getBillStatus());
        dto.setTotalAmount(bill.getTotalAmount());
        dto.setPaidAmount(bill.getPaidAmount());
        dto.setDepositAmount(bill.getDepositAmount());
        dto.setCreatedAt(bill.getCreatedAt());
        dto.setClosedAt(bill.getClosedAt());

        CheckIn checkIn = checkInRepository.findById(bill.getCheckInId()).orElse(null);
        if (checkIn != null) {
            String guestName = null;
            if (checkIn.getGuestId() != null) {
                Guest guest = guestRepository.findById(checkIn.getGuestId()).orElse(null);
                if (guest != null) {
                    guestName = (guest.getFirstName() != null ? guest.getFirstName() : "") + " " +
                            (guest.getLastName() != null ? guest.getLastName() : "");
                    guestName = guestName.trim();
                }
            }
            if (guestName == null || guestName.isEmpty()) {
                guestName = checkIn.getGuestName();
            }
            dto.setGuestName(guestName);

            if (checkIn.getRoomId() != null) {
                Room room = roomRepository.findById(checkIn.getRoomId()).orElse(null);
                if (room != null) {
                    dto.setRoomNumber(room.getRoomNumber());
                }
            }

            BigDecimal roomCharge = BigDecimal.ZERO;
            if (checkIn.getRatePerNight() != null && checkIn.getCheckInTime() != null) {
                java.time.LocalDateTime checkInTime = checkIn.getCheckInTime();
                java.time.LocalDateTime checkOutTime = bill.getClosedAt() != null ? bill.getClosedAt() : java.time.LocalDateTime.now();
                long days = java.time.temporal.ChronoUnit.DAYS.between(checkInTime.toLocalDate(), checkOutTime.toLocalDate());
                days = Math.max(days, 1);
                roomCharge = checkIn.getRatePerNight().multiply(BigDecimal.valueOf(days));
            }
            dto.setRoomCharge(roomCharge);

            BigDecimal additionalCharges = billItemRepository.sumAmountByBillId(bill.getId());
            if (additionalCharges == null) {
                additionalCharges = BigDecimal.ZERO;
            }
            dto.setAdditionalCharges(additionalCharges);
        }

        BigDecimal refundAmount = BigDecimal.ZERO;
        BigDecimal additionalPaymentAmount = BigDecimal.ZERO;

        List<Refund> refunds = refundRepository.findByBillId(bill.getId());
        if (refunds != null && !refunds.isEmpty()) {
            for (Refund r : refunds) {
                refundAmount = refundAmount.add(r.getAmount());
            }
        }

        List<Payment> payments = paymentRepository.findByBillId(bill.getId());
        if (payments != null && !payments.isEmpty()) {
            for (Payment p : payments) {
                if ("charge".equals(p.getPaymentType())) {
                    additionalPaymentAmount = additionalPaymentAmount.add(p.getAmount());
                }
            }
        }

        dto.setRefundAmount(refundAmount);
        dto.setAdditionalPaymentAmount(additionalPaymentAmount);

        return dto;
    }

    /**
     * 查询所有账单
     *
     * @return 账单列表
     */
    @Operation(summary = "查询所有账单", description = "返回系统中所有账单记录的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<BillDTO>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Bill> bills;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            bills = billService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            bills = billRepository.findAllByHotelId(hotelId);
        }
        List<BillDTO> dtos = bills.stream().map(this::convertToDTO).collect(Collectors.toList());
        return ResponseResult.success(dtos);
    }

    /**
     * 根据ID查询账单
     *
     * @param id 账单ID
     * @return 账单信息，不存在返回404
     */
    @Operation(summary = "根据ID查询账单", description = "根据账单ID查询单个账单详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Bill> findById(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Bill> optional = billService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Bill bill = optional.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问该账单");
            }
        }
        return ResponseResult.success(bill);
    }

    /**
     * 新增账单
     *
     * @param bill 账单信息
     * @return 创建后的账单信息
     */
    @Operation(summary = "新增账单", description = "创建一个新的账单记录")
    @PostMapping
    public ResponseResult<Bill> create(
            @Parameter(description = "账单信息", required = true) @RequestBody Bill bill,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(bill.getCheckInId());
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问其他酒店的入住记录");
            }
        }
        Bill saved = billService.save(bill);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新账单信息
     *
     * @param id   账单ID
     * @param bill 账单信息
     * @return 更新后的账单信息，不存在返回404
     */
    @Operation(summary = "更新账单信息", description = "根据账单ID更新账单信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Bill> update(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            @Parameter(description = "账单信息", required = true) @RequestBody Bill bill,
            HttpServletRequest request) {
        java.util.Optional<Bill> optional = billService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权更新其他酒店的账单");
            }
        }
        bill.setId(id);
        return ResponseResult.success(billService.save(bill));
    }

    /**
     * 根据ID删除账单
     *
     * @param id 账单ID
     * @return 删除结果
     */
    @Operation(summary = "删除账单", description = "根据账单ID删除账单记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Bill> optional = billService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权删除其他酒店的账单");
            }
        }
        billService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据入住ID查询账单列表
     *
     * @param checkInId 入住ID
     * @return 账单列表
     */
    @Operation(summary = "按入住ID查询账单", description = "根据入住ID查询关联的所有账单")
    @GetMapping("/search/byCheckInId")
    public ResponseResult<List<Bill>> findByCheckInId(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Bill> bills;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            bills = billService.findByCheckInId(checkInId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            bills = billRepository.findByCheckInIdAndHotelId(checkInId, hotelId);
        }
        return ResponseResult.success(bills);
    }

    /**
     * 根据账单状态查询账单列表
     *
     * @param billStatus 账单状态
     * @return 账单列表
     */
    @Operation(summary = "按状态查询账单", description = "根据账单状态查询账单列表")
    @GetMapping("/search/byBillStatus")
    public ResponseResult<List<Bill>> findByBillStatus(
            @Parameter(description = "账单状态", required = true) @RequestParam String billStatus,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Bill> bills;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            bills = billService.findByBillStatus(billStatus);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            bills = billRepository.findByBillStatusAndHotelId(billStatus, hotelId);
        }
        return ResponseResult.success(bills);
    }

    /**
     * 根据入住ID查询最新账单
     *
     * @param checkInId 入住ID
     * @return 最新账单信息，不存在返回404
     */
    @Operation(summary = "按入住ID查询最新账单", description = "根据入住ID查询最新一条账单记录")
    @GetMapping("/search/latestByCheckInId")
    public ResponseResult<Bill> findLatestByCheckInId(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        java.util.Optional<Bill> optional;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            optional = billService.findFirstByCheckInIdOrderByIdDesc(checkInId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            List<Bill> bills = billRepository.findByCheckInIdAndHotelIdOrderByIdDesc(checkInId, hotelId);
            optional = bills.isEmpty() ? java.util.Optional.empty() : java.util.Optional.of(bills.get(0));
        }
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        return ResponseResult.success(optional.get());
    }
}
