package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.dto.bill.BillDTO;
import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import com.project.hotelmanagementsystem.service.BillService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    private static final Logger logger = LoggerFactory.getLogger(BillController.class);

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
            // 优先使用入住登记时记录的客人姓名（check_in.guest_name）
            if (checkIn.getGuestName() != null && !checkIn.getGuestName().isEmpty()) {
                guestName = checkIn.getGuestName().trim();
            }
            // 回退：通过 guest_id 查询客人档案并拼接姓名（中间不加空格）
            if ((guestName == null || guestName.isEmpty()) && checkIn.getGuestId() != null) {
                Guest guest = guestRepository.findById(checkIn.getGuestId()).orElse(null);
                if (guest != null) {
                    String first = guest.getFirstName() != null ? guest.getFirstName().trim() : "";
                    String last = guest.getLastName() != null ? guest.getLastName().trim() : "";
                    guestName = (first + last).trim();
                }
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

            // 检查是否仅包含损坏赔偿明细（且至少有一条）
            List<BillItem> allItems = billItemRepository.findByBillId(bill.getId());
            boolean damageOnly = !allItems.isEmpty() && allItems.stream().allMatch(item -> "damage".equals(item.getItemType()));
            dto.setHasDamageItem(damageOnly);
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

    /**
     * 结算账单（仅限包含损坏赔偿明细的账单）
     *
     * @param id 账单ID
     * @param paymentMethod 支付方式：cash/credit_card/debit_card/wechat/alipay/bank_transfer
     * @return 结算后的账单信息
     */
    @Operation(summary = "结算账单", description = "结算包含损坏赔偿明细的账单，选择支付方式并写入收款记录")
    @PutMapping("/{id}/settle")
    @Transactional
    public ResponseResult<Bill> settleBill(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            @Parameter(description = "支付方式：cash现金/credit_card信用卡/debit_card借记卡/wechat微信/alipay支付宝/bank_transfer银行转账", required = true)
                @RequestParam String paymentMethod,
            HttpServletRequest request) {
        java.util.Optional<Bill> optional = billService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "账单不存在");
        }
        Bill bill = optional.get();

        // 权限校验
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权操作其他酒店的账单");
            }
        }

        // 支付方式校验
        java.util.Set<String> validMethods = java.util.Set.of(
                "cash", "credit_card", "debit_card", "wechat", "alipay", "bank_transfer");
        if (paymentMethod == null || !validMethods.contains(paymentMethod)) {
            return ResponseResult.error(400, "无效的支付方式，可选值：cash/credit_card/debit_card/wechat/alipay/bank_transfer");
        }

        // 状态校验：只有open状态可以结算
        if (!"open".equals(bill.getBillStatus())) {
            return ResponseResult.error(400, "只有未结算的账单才能结算");
        }

        // 业务校验：只有仅包含损坏赔偿明细的账单才能结算
        List<BillItem> allItems = billItemRepository.findByBillId(id);
        if (allItems.isEmpty() || !allItems.stream().allMatch(item -> "damage".equals(item.getItemType()))) {
            return ResponseResult.error(400, "只有设施损坏追责产生的账单才能结算");
        }

        // 计算赔偿总金额
        BigDecimal damageTotal = allItems.stream()
                .map(BillItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 更新账单状态和金额
        bill.setBillStatus("closed");
        bill.setClosedAt(java.time.LocalDateTime.now());
        bill.setTotalAmount(damageTotal);
        bill.setPaidAmount(damageTotal);
        Bill saved = billRepository.saveAndFlush(bill);

        // 创建收款记录
        try {
            Payment payment = new Payment();
            payment.setBillId(id);
            payment.setAmount(damageTotal);
            payment.setPaymentMethod(paymentMethod);
            payment.setPaymentType("charge");
            payment.setTransactionRef(null);
            payment.setPaymentDate(java.time.LocalDateTime.now());
            if (employee != null) {
                payment.setEmployeeId(employee.getId());
            }
            paymentRepository.saveAndFlush(payment);
        } catch (Exception e) {
            logger.error("Failed to create payment record for bill {}", id, e);
            return ResponseResult.error(500, "结算成功但创建收款记录失败: " + e.getMessage());
        }

        return ResponseResult.success("结算成功", saved);
    }

    /**
     * 作废账单（仅限已结算的账单）
     *
     * @param id 账单ID
     * @return 作废后的账单信息
     */
    @Operation(summary = "作废账单", description = "作废已结算的账单，将状态从closed改为void")
    @PutMapping("/{id}/void")
    public ResponseResult<Bill> voidBill(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Bill> optional = billService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "账单不存在");
        }
        Bill bill = optional.get();

        // 权限校验
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billRepository.findHotelIdByBillId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权操作其他酒店的账单");
            }
        }

        // 状态校验：只有closed状态可以作废
        if (!"closed".equals(bill.getBillStatus())) {
            return ResponseResult.error(400, "只有已结算的账单才能作废");
        }

        bill.setBillStatus("void");
        Bill saved = billService.save(bill);
        return ResponseResult.success("作废成功", saved);
    }

    /**
     * 首页累计收入：当前月份已结算（closed）账单的 total_amount 汇总
     * 按当前登录员工权限自动做酒店数据隔离；null hotelId = 集团管理员统计全部酒店
     *
     * @return 当月累计收入金额
     */
    @Operation(summary = "当月累计收入统计", description = "返回系统当月已结算（closed）账单的收入总额，按员工所属酒店做数据隔离")
    @GetMapping("/stats/monthly-revenue")
    public ResponseResult<java.math.BigDecimal> getMonthlyClosedRevenue(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime startOfMonth = today.withDayOfMonth(1).atStartOfDay();
        java.time.LocalDateTime startOfNextMonth = today.plusMonths(1).withDayOfMonth(1).atStartOfDay();

        java.math.BigDecimal revenue;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            revenue = billRepository.sumMonthlyClosedRevenue(startOfMonth, startOfNextMonth);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            revenue = billRepository.sumMonthlyClosedRevenueByHotelId(hotelId, startOfMonth, startOfNextMonth);
        }
        if (revenue == null) revenue = java.math.BigDecimal.ZERO;
        return ResponseResult.success(revenue);
    }
}
