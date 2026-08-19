package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.dto.reservation.CheckInReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.CreateReservationRequest;
import com.project.hotelmanagementsystem.dto.reservation.ReservationResponse;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.entity.Reservation;
import com.project.hotelmanagementsystem.service.GuestService;
import com.project.hotelmanagementsystem.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 预订信息控制层
 * <p>
 * 负责客人预订记录的创建、查询、确认、取消、入住等业务操作，
 * 以及按客人、酒店、状态等条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "预订管理", description = "预订信息的创建、查询、确认、取消、入住等接口")
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final GuestService guestService;

    /**
     * 构造函数注入
     *
     * @param reservationService 预订Service
     * @param guestService       客人Service
     */
    public ReservationController(ReservationService reservationService, GuestService guestService) {
        this.reservationService = reservationService;
        this.guestService = guestService;
    }

    // ==================== 客人端接口 ====================

    /**
     * 创建预订
     *
     * @param request 创建预订请求
     * @return 预订详情
     */
    @Operation(summary = "创建预订", description = "客人创建新的预订订单，支持多房间预订")
    @PostMapping("/create")
    public ResponseResult<ReservationResponse> createReservation(
            @Parameter(description = "创建预订请求", required = true)
            @Valid @RequestBody CreateReservationRequest request) {

        // 校验客人ID
        if (request.getGuestId() == null) {
            return ResponseResult.error(400, "客人ID不能为空");
        }

        Guest guest = guestService.findById(request.getGuestId())
                .orElse(null);
        if (guest == null) {
            return ResponseResult.error(404, "客人不存在");
        }

        try {
            ReservationResponse response = reservationService.createReservation(request, guest);
            return ResponseResult.success("预订成功", response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 查询我的预订
     *
     * @param guestId 客人ID
     * @return 预订列表
     */
    @Operation(summary = "查询我的预订", description = "根据客人ID查询该客人的所有预订记录")
    @GetMapping("/my/{guestId}")
    public ResponseResult<List<ReservationResponse>> getMyReservations(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer guestId) {
        List<ReservationResponse> reservations = reservationService.findDetailByGuestId(guestId);
        return ResponseResult.success(reservations);
    }

    /**
     * 通过手机号查询预订
     *
     * @param phone 客人手机号
     * @return 预订列表
     */
    @Operation(summary = "通过手机号查询预订", description = "根据客人手机号查询相关预订记录")
    @GetMapping("/search/byGuestPhone")
    public ResponseResult<List<ReservationResponse>> searchByGuestPhone(
            @Parameter(description = "客人手机号", required = true) @RequestParam String phone) {
        List<ReservationResponse> reservations = reservationService.findByGuestPhone(phone);
        return ResponseResult.success(reservations);
    }

    /**
     * 通过邮箱查询预订
     *
     * @param email 客人邮箱
     * @return 预订列表
     */
    @Operation(summary = "通过邮箱查询预订", description = "根据客人邮箱查询相关预订记录")
    @GetMapping("/search/byGuestEmail")
    public ResponseResult<List<ReservationResponse>> searchByGuestEmail(
            @Parameter(description = "客人邮箱", required = true) @RequestParam String email) {
        List<ReservationResponse> reservations = reservationService.findByGuestEmail(email);
        return ResponseResult.success(reservations);
    }

    /**
     * 通过姓名查询预订
     *
     * @param name 客人姓名
     * @return 预订列表
     */
    @Operation(summary = "通过姓名查询预订", description = "根据客人姓名模糊查询相关预订记录")
    @GetMapping("/search/byGuestName")
    public ResponseResult<List<ReservationResponse>> searchByGuestName(
            @Parameter(description = "客人姓名", required = true) @RequestParam String name) {
        List<ReservationResponse> reservations = reservationService.findByGuestName(name);
        return ResponseResult.success(reservations);
    }

    /**
     * 取消预订（客人端）
     *
     * @param id 预订ID
     * @return 预订详情
     */
    @Operation(summary = "取消预订", description = "客人取消待确认或已确认的预订")
    @PutMapping("/{id}/cancel")
    public ResponseResult<ReservationResponse> cancelReservation(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id) {
        try {
            ReservationResponse response = reservationService.cancelReservation(id);
            return ResponseResult.success("取消成功", response);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 查询预订详情
     *
     * @param id 预订ID
     * @return 预订详情
     */
    @Operation(summary = "查询预订详情", description = "根据预订ID查询预订详细信息，包含房间明细")
    @GetMapping("/{id}")
    public ResponseResult<ReservationResponse> getDetailById(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id) {
        java.util.Optional<ReservationResponse> optional = reservationService.findDetailById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "预订不存在");
        }
        return ResponseResult.success(optional.get());
    }

    // ==================== 员工端接口 ====================

    /**
     * 确认预订（员工端）
     *
     * @param id      预订ID
     * @param body    请求体，包含房间ID
     * @param request HTTP请求
     * @return 预订详情
     */
    @Operation(summary = "确认预订", description = "员工确认预订并分配房间")
    @PutMapping("/{id}/confirm")
    public ResponseResult<ReservationResponse> confirmReservation(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Integer> body,
            HttpServletRequest request) {

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未授权");
        }

        Integer roomId = (body != null && body.containsKey("roomId")) ? body.get("roomId") : null;

        try {
            ReservationResponse response = reservationService.confirmReservation(id, employee.getId(), roomId);
            return ResponseResult.success("确认成功", response);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 分配房间（员工端）
     *
     * @param id      预订ID
     * @param body    请求体，包含房间ID
     * @param request HTTP请求
     * @return 预订详情
     */
    @Operation(summary = "分配房间", description = "员工为已确认的预订分配房间")
    @PutMapping("/{id}/assign-room")
    public ResponseResult<ReservationResponse> assignRoom(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id,
            @RequestBody(required = false) Map<String, Integer> body,
            HttpServletRequest request) {

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未授权");
        }

        Integer roomId = (body != null && body.containsKey("roomId")) ? body.get("roomId") : null;

        try {
            ReservationResponse response = reservationService.assignRoom(id, roomId);
            return ResponseResult.success("分配房间成功", response);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 办理入住（员工端，支持同住客人信息录入）
     *
     * @param id           预订ID
     * @param requestBody  请求体（含同住客人信息）
     * @param request      HTTP请求
     * @return 预订详情
     */
    @Operation(summary = "办理入住", description = "员工为已确认的预订办理入住手续，支持录入同住客人信息")
    @PutMapping("/{id}/check-in")
    public ResponseResult<ReservationResponse> checkInReservation(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id,
            @RequestBody(required = false) CheckInReservationRequest requestBody,
            HttpServletRequest request) {

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未授权");
        }

        java.util.List<com.project.hotelmanagementsystem.dto.checkin.CreateCheckInRequest.StayGuestRequest> stayGuests =
                (requestBody != null && requestBody.getStayGuests() != null) ? requestBody.getStayGuests() : null;

        try {
            ReservationResponse response = reservationService.checkInReservation(id, stayGuests);
            return ResponseResult.success("入住成功", response);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 办理退房（员工端）
     *
     * @param id      预订ID
     * @param request HTTP请求
     * @return 预订详情
     */
    @Operation(summary = "办理退房", description = "员工为已入住的预订办理退房手续")
    @PutMapping("/{id}/check-out")
    public ResponseResult<ReservationResponse> checkOutReservation(
            @Parameter(description = "预订ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未授权");
        }

        try {
            ReservationResponse response = reservationService.checkOutReservation(id);
            return ResponseResult.success("退房成功", response);
        } catch (IllegalArgumentException e) {
            return ResponseResult.error(404, e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseResult.error(400, e.getMessage());
        }
    }

    /**
     * 按酒店过滤查询所有预订（员工端）
     *
     * @param hotelId 酒店ID（可选）
     * @param request HTTP请求
     * @return 预订列表
     */
    @Operation(summary = "查询所有预订（按酒店过滤）", description = "员工查询预订列表，按酒店进行数据隔离")
    @GetMapping
    public ResponseResult<List<ReservationResponse>> findAll(
            @Parameter(description = "酒店ID") @RequestParam(required = false) Integer hotelId,
            HttpServletRequest request) {

        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null) {
            return ResponseResult.error(401, "未授权");
        }

        // 数据隔离：非管理员只能查看自己酒店的预订
        Integer filterHotelId = hotelId;
        Integer employeeHotelId = employee.getHotelId();
        if (employeeHotelId != null) {
            // 有酒店归属的员工，强制过滤到自己的酒店
            filterHotelId = employeeHotelId;
        }

        List<ReservationResponse> reservations = reservationService.findAllWithHotelFilter(filterHotelId);
        return ResponseResult.success(reservations);
    }

    // ==================== 兼容旧接口 ====================

    /**
     * 根据客人ID查询预订列表（旧接口）
     *
     * @param guestId 客人ID
     * @return 预订列表
     */
    @Operation(summary = "按客人ID查询预订", description = "根据客人ID查询该客人的所有预订记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<Reservation>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId) {
        return ResponseResult.success(reservationService.findByGuestId(guestId));
    }

    /**
     * 根据状态查询预订列表（旧接口）
     *
     * @param status 预订状态
     * @return 预订列表
     */
    @Operation(summary = "按状态查询预订", description = "根据预订状态查询预订列表")
    @GetMapping("/search/byStatus")
    public ResponseResult<List<Reservation>> findByStatus(
            @Parameter(description = "预订状态", required = true) @RequestParam String status) {
        return ResponseResult.success(reservationService.findByStatus(status));
    }

    /**
     * 根据员工ID查询预订列表（旧接口）
     *
     * @param employeeId 员工ID
     * @return 预订列表
     */
    @Operation(summary = "按员工ID查询预订", description = "根据操作员工ID查询其处理的预订记录")
    @GetMapping("/search/byEmployeeId")
    public ResponseResult<List<Reservation>> findByEmployeeId(
            @Parameter(description = "员工ID", required = true) @RequestParam Integer employeeId) {
        return ResponseResult.success(reservationService.findByEmployeeId(employeeId));
    }

    /**
     * 根据客人ID和状态查询预订列表（旧接口）
     *
     * @param guestId 客人ID
     * @param status  预订状态
     * @return 预订列表
     */
    @Operation(summary = "按客人ID和状态查询预订", description = "根据客人ID和预订状态联合查询预订列表")
    @GetMapping("/search/byGuestIdAndStatus")
    public ResponseResult<List<Reservation>> findByGuestIdAndStatus(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId,
            @Parameter(description = "预订状态", required = true) @RequestParam String status) {
        return ResponseResult.success(reservationService.findByGuestIdAndStatus(guestId, status));
    }

    /**
     * 根据入住日期范围查询预订列表（旧接口）
     *
     * @param checkInDate  入住开始日期
     * @param checkOutDate 入住结束日期
     * @return 预订列表
     */
    @Operation(summary = "按入住日期范围查询预订", description = "根据入住日期范围查询落在区间内的预订列表")
    @GetMapping("/search/byCheckInDateBetween")
    public ResponseResult<List<Reservation>> findByCheckInDateBetween(
            @Parameter(description = "入住开始日期(yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @Parameter(description = "入住结束日期(yyyy-MM-dd)", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate) {
        return ResponseResult.success(reservationService.findByCheckInDateBetween(checkInDate, checkOutDate));
    }
}
