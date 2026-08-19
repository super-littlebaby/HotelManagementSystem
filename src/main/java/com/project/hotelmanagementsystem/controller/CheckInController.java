package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.CheckIn;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Room;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.CheckInRepository;
import com.project.hotelmanagementsystem.repository.RoomRepository;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.CheckInService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    private final DataIsolationService dataIsolationService;

    /**
     * 构造函数注入入住登记Service
     *
     * @param checkInService        入住登记Service
     * @param checkInRepository     入住登记Repository
     * @param roomRepository        房间Repository
     * @param roomTypeRepository    房型Repository
     * @param dataIsolationService  数据隔离Service
     */
    public CheckInController(CheckInService checkInService, CheckInRepository checkInRepository, RoomRepository roomRepository, RoomTypeRepository roomTypeRepository, DataIsolationService dataIsolationService) {
        this.checkInService = checkInService;
        this.checkInRepository = checkInRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.dataIsolationService = dataIsolationService;
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
        return ResponseResult.success(checkIns);
    }

    /**
     * 办理退房
     *
     * @param id      入住记录ID
     * @param request HTTP请求
     * @return 更新后的入住记录信息
     */
    @Operation(summary = "办理退房", description = "将入住记录状态更新为已退房，并将房间状态设为待打扫")
    @PutMapping("/{id}/check-out")
    public ResponseResult<CheckIn> checkOut(
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
            CheckIn checkIn = checkInService.checkOut(id);
            return ResponseResult.success("退房成功", checkIn);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }
}
