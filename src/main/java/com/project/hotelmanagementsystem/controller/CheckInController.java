package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Bill;
import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Hotel;
import com.project.hotelmanagementsystem.entity.Payment;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.BillRepository;
import com.project.hotelmanagementsystem.repository.CheckInRepository;
import com.project.hotelmanagementsystem.repository.HotelRepository;
import com.project.hotelmanagementsystem.repository.PaymentRepository;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.CheckInService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 入住登记控制层
 * <p>
 * 负责客人入住登记记录的增删改查及按客人、房间、预订、状态条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "入住登记管理", description = "入住登记记录的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;
    private final CheckInRepository checkInRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final DataIsolationService dataIsolationService;
    private final RoomStatusLogService roomStatusLogService;
    private final BillRepository billRepository;
    private final PaymentRepository paymentRepository;

    /**
     * 构造函数注入入住登记Service
     *
     * @param checkInService        入住登记Service
     * @param checkInRepository     入住登记Repository
     * @param roomRepository        房间Repository
     * @param roomTypeRepository    房型Repository
     * @param dataIsolationService  数据隔离Service
     * @param roomStatusLogService  房间状态变更日志Service
     * @param billRepository        账单Repository
     * @param paymentRepository     收款记录Repository
     */
    public CheckInController(CheckInService checkInService, CheckInRepository checkInRepository,
                             RoomRepository roomRepository, RoomTypeRepository roomTypeRepository,
                             HotelRepository hotelRepository,
                             DataIsolationService dataIsolationService,
                             RoomStatusLogService roomStatusLogService,
                             BillRepository billRepository,
                             PaymentRepository paymentRepository) {
        this.checkInService = checkInService;
        this.checkInRepository = checkInRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.dataIsolationService = dataIsolationService;
        this.roomStatusLogService = roomStatusLogService;
        this.billRepository = billRepository;
        this.paymentRepository = paymentRepository;
    }

    /**
     * 查询所有入住记录
     *
     * @return 入住记录列表
     */
    @Operation(summary = "查询所有入住记录", description = "返回系统中所有入住登记记录的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<CheckIn>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findAllByHotelId(hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 根据ID查询入住记录
     *
     * @param id 入住记录ID
     * @return 入住记录信息，不存在返回404
     */
    @Operation(summary = "根据ID查询入住记录", description = "根据入住记录ID查询单条入住登记详细信息")
    @GetMapping("/{id}")
    public ResponseResult<CheckIn> findById(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<CheckIn> optional = checkInService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        CheckIn checkIn = optional.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = checkInRepository.findHotelIdByCheckInId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问该入住记录");
            }
        }
        decorateCheckIn(checkIn);
        return ResponseResult.success(checkIn);
    }

    /**
     * 新增入住登记（支持同住人员）
     *
     * @param requestBody 入住记录信息（含同住人员）
     * @param request     HTTP请求
     * @return 创建后的入住记录信息
     */
    @Operation(summary = "新增入住登记", description = "创建一条新的入住登记记录，支持同时添加同住人员")
    @PostMapping
    public ResponseResult<CheckIn> create(
            @Parameter(description = "入住记录信息（含同住人员）", required = true) @RequestBody com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest requestBody,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");

        CheckIn checkIn = requestBody.toCheckIn();

        // 从房间获取 hotelId 和 roomTypeId
        java.util.Optional<Room> roomOpt = roomRepository.findById(requestBody.getRoomId());
        if (roomOpt.isEmpty()) {
            return ResponseResult.error(404, "房间不存在");
        }
        Room room = roomOpt.get();
        Integer roomHotelId = room.getHotelId();
        checkIn.setHotelId(roomHotelId);

        if (employee != null && !dataIsolationService.canAccessHotel(employee, roomHotelId)) {
            return ResponseResult.error(403, "无权访问其他酒店的房间");
        }

        int adults = requestBody.getAdults() != null ? requestBody.getAdults() : 0;
        int children = requestBody.getChildren() != null ? requestBody.getChildren() : 0;
        int totalDeclared = adults + children;

        java.util.List<com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest.StayGuestRequest> guestReqs = requestBody.getStayGuests();
        long validStayGuestsCount = guestReqs == null ? 0 : guestReqs.stream()
                .filter(g -> g.getIdNumber() != null && !g.getIdNumber().isEmpty()
                          && g.getName() != null && !g.getName().isEmpty())
                .count();
        int totalRegistered = 1 + (int) validStayGuestsCount; // 1为主登记人

        // 校验人数一致性：登记人数必须等于 成人+儿童 总数
        if (totalRegistered > totalDeclared) {
            return ResponseResult.error(400, "登记人数(" + totalRegistered + ")超过了填写的人数(" + totalDeclared + ")，请减少同住人员或增加成人/儿童数");
        }
        if (totalRegistered < totalDeclared) {
            return ResponseResult.error(400, "登记人数(" + totalRegistered + ")少于填写的人数(" + totalDeclared + ")，请补充同住人员信息");
        }

        // 验证房型人数限制
        if (room.getRoomTypeId() != null) {
            java.util.Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(room.getRoomTypeId());
            if (roomTypeOpt.isPresent()) {
                RoomType roomType = roomTypeOpt.get();

                // 检查成人数量
                if (roomType.getMaxAdults() != null && adults > roomType.getMaxAdults()) {
                    return ResponseResult.error(400, "成人数量(" + adults + ")超过房型上限(" + roomType.getMaxAdults() + ")");
                }

                // 检查儿童数量
                if (roomType.getMaxChildren() != null && children > roomType.getMaxChildren()) {
                    return ResponseResult.error(400, "儿童数量(" + children + ")超过房型上限(" + roomType.getMaxChildren() + ")");
                }
            }
        }

        java.util.List<com.project.hotelmanagementsystem.entity.StayGuest> stayGuests = requestBody.toStayGuests(null);

        CheckIn saved = checkInService.saveWithStayGuests(checkIn, stayGuests);

        // 计算押金并创建账单
        if (room.getRoomTypeId() != null && saved.getCheckInTime() != null && saved.getExpectedCheckOutTime() != null) {
            java.util.Optional<RoomType> roomTypeOpt = roomTypeRepository.findById(room.getRoomTypeId());
            if (roomTypeOpt.isPresent() && roomTypeOpt.get().getBasePrice() != null) {
                RoomType rt = roomTypeOpt.get();
                BigDecimal basePrice = rt.getBasePrice();

                // 计算入住天数（按自然日计算，至少1天）
                long days = ChronoUnit.DAYS.between(
                        saved.getCheckInTime().toLocalDate(),
                        saved.getExpectedCheckOutTime().toLocalDate()
                );
                days = Math.max(days, 1);

                // 押金 = 房间单价 * 入住天数
                BigDecimal depositAmount = basePrice.multiply(BigDecimal.valueOf(days));

                // 设置入住记录的实际执行房价
                saved.setRatePerNight(basePrice);
                checkInRepository.save(saved);

                // 创建账单记录
                Bill bill = new Bill();
                bill.setCheckInId(saved.getId());
                bill.setBillStatus("open");
                bill.setTotalAmount(BigDecimal.ZERO);
                bill.setPaidAmount(depositAmount);
                bill.setDepositAmount(depositAmount);
                bill.setCreatedAt(LocalDateTime.now());
                Bill savedBill = billRepository.save(bill);

                // 创建押金支付记录
                Payment payment = new Payment();
                payment.setBillId(savedBill.getId());
                payment.setAmount(depositAmount);
                payment.setPaymentMethod("cash");
                payment.setPaymentType("deposit");
                payment.setPaymentDate(LocalDateTime.now());
                payment.setEmployeeId(employee != null ? employee.getId() : null);
                paymentRepository.save(payment);
            }
        }

        // 入住时将房间状态置为 occupied，并通过统一日志入口记录状态变更
        String oldRoomStatus = room.getStatus();
        if (!"occupied".equals(oldRoomStatus)) {
            room.setStatus("occupied");
            roomRepository.save(room);
            Integer changedBy = employee != null ? employee.getId() : null;
            String guestDesc = saved.getGuestName() != null ? "，客人：" + saved.getGuestName() : "";
            roomStatusLogService.logStatusChange(
                    room.getId(), oldRoomStatus, "occupied", changedBy,
                    "入住：入住记录 #" + saved.getId() + guestDesc);
        }

        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新入住登记信息
     *
     * @param id      入住记录ID
     * @param checkIn 入住记录信息
     * @return 更新后的入住记录信息，不存在返回404
     */
    @Operation(summary = "更新入住登记信息", description = "根据入住记录ID更新入住登记信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<CheckIn> update(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            @Parameter(description = "入住记录信息", required = true) @RequestBody CheckIn checkIn,
            HttpServletRequest request) {
        java.util.Optional<CheckIn> optional = checkInService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = checkInRepository.findHotelIdByCheckInId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权更新其他酒店的入住记录");
            }
        }
        checkIn.setId(id);
        return ResponseResult.success(checkInService.save(checkIn));
    }

    /**
     * 根据ID删除入住记录
     *
     * @param id 入住记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除入住记录", description = "根据入住记录ID删除入住登记记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<CheckIn> optional = checkInService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = checkInRepository.findHotelIdByCheckInId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权删除其他酒店的入住记录");
            }
        }
        checkInService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据客人ID查询入住记录列表
     *
     * @param guestId 客人ID
     * @return 入住记录列表
     */
    @Operation(summary = "按客人ID查询入住记录", description = "根据客人ID查询该客人的所有入住记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<CheckIn>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findByGuestId(guestId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findByGuestIdAndHotelId(guestId, hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 根据房间ID查询入住记录列表
     *
     * @param roomId 房间ID
     * @return 入住记录列表
     */
    @Operation(summary = "按房间ID查询入住记录", description = "根据房间ID查询关联该房间的入住记录列表")
    @GetMapping("/search/byRoomId")
    public ResponseResult<List<CheckIn>> findByRoomId(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findByRoomId(roomId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findByRoomIdAndHotelId(roomId, hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 根据状态查询入住记录列表
     *
     * @param status 入住状态
     * @return 入住记录列表
     */
    @Operation(summary = "按状态查询入住记录", description = "根据入住状态查询入住记录列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<CheckIn>> findByStatus(
            @Parameter(description = "入住状态", required = true) @RequestParam String status,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findByStatus(status);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findByStatusAndHotelId(status, hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 根据预订ID查询入住记录列表
     *
     * @param reservationId 预订ID
     * @return 入住记录列表
     */
    @Operation(summary = "按预订ID查询入住记录", description = "根据预订ID查询关联该预订的入住记录列表")
    @GetMapping("/search/byReservationId")
    public ResponseResult<List<CheckIn>> findByReservationId(
            @Parameter(description = "预订ID", required = true) @RequestParam Integer reservationId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findByReservationId(reservationId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findByReservationIdAndHotelId(reservationId, hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 根据房间ID和状态查询入住记录列表
     *
     * @param roomId 房间ID
     * @param status 入住状态
     * @return 入住记录列表
     */
    @Operation(summary = "按房间ID和状态查询入住记录", description = "根据房间ID和入住状态联合查询入住记录列表")
    @GetMapping("/search/byRoomIdAndStatus")
    public ResponseResult<List<CheckIn>> findByRoomIdAndStatus(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId,
            @Parameter(description = "入住状态", required = true) @RequestParam String status,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<CheckIn> checkIns;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            checkIns = checkInService.findByRoomIdAndStatus(roomId, status);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            checkIns = checkInRepository.findByRoomIdAndStatusAndHotelId(roomId, status, hotelId);
        }
        decorateCheckIns(checkIns);
        return ResponseResult.success(checkIns);
    }

    /**
     * 退房前预计算
     *
     * @param id      入住记录ID
     * @param request HTTP请求
     * @return 预计算结果
     */
    @Operation(summary = "退房前预计算", description = "在办理退房前计算总费用、押金和差额")
    @GetMapping("/{id}/pre-check-out")
    public ResponseResult<java.util.Map<String, Object>> preCheckOut(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<CheckIn> optional = checkInService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = checkInRepository.findHotelIdByCheckInId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权操作其他酒店的入住记录");
            }
        }

        try {
            java.util.Map<String, Object> result = checkInService.preCheckOut(id);
            return ResponseResult.success(result);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 办理退房
     *
     * @param id      入住记录ID
     * @param requestBody 请求体，包含paymentMethod和refundMethod
     * @param request HTTP请求
     * @return 更新后的入住记录信息
     */
    @Operation(summary = "办理退房", description = "将入住记录状态更新为已退房，并将房间状态设为待打扫。押金不足时需补差价，押金多余时需退款")
    @PutMapping("/{id}/check-out")
    public ResponseResult<CheckIn> checkOut(
            @Parameter(description = "入住记录ID", required = true) @PathVariable Integer id,
            @RequestBody(required = false) java.util.Map<String, String> requestBody,
            HttpServletRequest request) {
        java.util.Optional<CheckIn> optional = checkInService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = checkInRepository.findHotelIdByCheckInId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权操作其他酒店的入住记录");
            }
        }

        try {
            Integer changedBy = employee != null ? employee.getId() : null;
            String paymentMethod = null;
            String refundMethod = null;
            if (requestBody != null) {
                paymentMethod = requestBody.get("paymentMethod");
                refundMethod = requestBody.get("refundMethod");
            }
            CheckIn checkIn = checkInService.checkOut(id, changedBy, paymentMethod, refundMethod);
            decorateCheckIn(checkIn);
            return ResponseResult.success("退房成功", checkIn);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 为单条入住记录填充展示字段（房型名称、酒店名称、房间号）
     */
    private void decorateCheckIn(CheckIn checkIn) {
        if (checkIn == null) {
            return;
        }
        decorateCheckIns(Collections.singletonList(checkIn));
    }

    /**
     * 批量为入住记录填充展示字段（房型名称、酒店名称、房间号）
     * <p>
     * 采用批量查询避免 N+1 问题。
     * 优先使用 CheckIn 已加载的 Room 嵌套对象获取 roomTypeId/hotelId/roomNumber，
     * 缺失时按 checkIn.roomId 再次从 RoomRepository 拉取。
     * </p>
     */
    private void decorateCheckIns(List<CheckIn> checkIns) {
        if (checkIns == null || checkIns.isEmpty()) {
            return;
        }

        // 第一阶段：从 CheckIn.room（EAGER 加载）或 roomId 补查，收集 roomTypeId/hotelId/roomNumber
        Map<Integer, Integer> checkInRoomTypeIdMap = new HashMap<>();
        Map<Integer, Integer> checkInHotelIdMap = new HashMap<>();
        Map<Integer, String> checkInRoomNumberMap = new HashMap<>();
        Set<Integer> missingRoomIds = new HashSet<>();

        for (CheckIn c : checkIns) {
            if (c.getId() == null) {
                continue;
            }
            Room r = c.getRoom();
            if (r == null && c.getRoomId() != null) {
                missingRoomIds.add(c.getRoomId());
            }
            if (r != null) {
                if (r.getRoomTypeId() != null) {
                    checkInRoomTypeIdMap.put(c.getId(), r.getRoomTypeId());
                }
                if (r.getHotelId() != null) {
                    checkInHotelIdMap.put(c.getId(), r.getHotelId());
                } else if (c.getHotelId() != null) {
                    checkInHotelIdMap.put(c.getId(), c.getHotelId());
                }
                if (r.getRoomNumber() != null) {
                    checkInRoomNumberMap.put(c.getId(), r.getRoomNumber());
                }
            } else {
                if (c.getHotelId() != null) {
                    checkInHotelIdMap.put(c.getId(), c.getHotelId());
                }
            }
        }

        // 补查缺失的 Room 基础属性
        if (!missingRoomIds.isEmpty()) {
            List<Room> missingRooms = roomRepository.findAllById(missingRoomIds);
            Map<Integer, Room> missingRoomMap = missingRooms.stream()
                    .filter(r -> r.getId() != null)
                    .collect(Collectors.toMap(Room::getId, r -> r, (a, b) -> a));
            for (CheckIn c : checkIns) {
                if (c.getId() == null || c.getRoomId() == null) {
                    continue;
                }
                if (!missingRoomMap.containsKey(c.getRoomId())) {
                    continue;
                }
                Room r = missingRoomMap.get(c.getRoomId());
                if (r.getRoomTypeId() != null) {
                    checkInRoomTypeIdMap.putIfAbsent(c.getId(), r.getRoomTypeId());
                }
                if (r.getHotelId() != null) {
                    checkInHotelIdMap.putIfAbsent(c.getId(), r.getHotelId());
                }
                if (r.getRoomNumber() != null) {
                    checkInRoomNumberMap.putIfAbsent(c.getId(), r.getRoomNumber());
                }
            }
        }

        // 第二阶段：批量查询 RoomType 名称
        Map<Integer, String> roomTypeNameMap = new HashMap<>();
        Set<Integer> roomTypeIds = checkInRoomTypeIdMap.values().stream()
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (!roomTypeIds.isEmpty()) {
            List<RoomType> roomTypes = roomTypeRepository.findAllById(roomTypeIds);
            for (RoomType rt : roomTypes) {
                if (rt.getId() != null && rt.getName() != null) {
                    roomTypeNameMap.put(rt.getId(), rt.getName());
                }
            }
        }

        // 第三阶段：批量查询 Hotel 名称
        Map<Integer, String> hotelNameMap = new HashMap<>();
        Set<Integer> hotelIds = checkInHotelIdMap.values().stream()
                .filter(Objects::nonNull).collect(Collectors.toSet());
        if (!hotelIds.isEmpty()) {
            List<Hotel> hotels = hotelRepository.findAllById(hotelIds);
            for (Hotel h : hotels) {
                if (h.getId() != null && h.getName() != null) {
                    hotelNameMap.put(h.getId(), h.getName());
                }
            }
        }

        // 第四阶段：回填到 CheckIn 的 @Transient 字段
        for (CheckIn c : checkIns) {
            if (c.getId() == null) {
                continue;
            }
            if (c.getRoomNumber() == null) {
                c.setRoomNumber(checkInRoomNumberMap.get(c.getId()));
            }
            if (c.getRoomTypeName() == null) {
                Integer rtId = checkInRoomTypeIdMap.get(c.getId());
                c.setRoomTypeName(rtId == null ? null : roomTypeNameMap.get(rtId));
            }
            if (c.getHotelName() == null) {
                Integer hId = checkInHotelIdMap.get(c.getId());
                c.setHotelName(hId == null ? null : hotelNameMap.get(hId));
            }
        }
    }
}
