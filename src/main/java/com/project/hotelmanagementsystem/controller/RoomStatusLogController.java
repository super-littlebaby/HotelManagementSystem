package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.dto.roomstatuslog.RoomStatusLogDTO;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.RoomStatusLog;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.RoomStatusLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 房间状态变更日志控制层
 * <p>
 * 提供房间状态变更日志的查询接口，支持按房间、操作人、酒店、状态、
 * 时间范围的多条件组合检索与分页。所有查询接口均应用数据隔离：
 * 非集团管理员只能查询所属酒店范围内的日志。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房间状态变更日志管理", description = "房间状态变更日志的条件检索、分页查询接口")
@RestController
@RequestMapping("/api/room-status-logs")
public class RoomStatusLogController {

    private final RoomStatusLogService roomStatusLogService;
    private final DataIsolationService dataIsolationService;

    public RoomStatusLogController(RoomStatusLogService roomStatusLogService,
                                    DataIsolationService dataIsolationService) {
        this.roomStatusLogService = roomStatusLogService;
        this.dataIsolationService = dataIsolationService;
    }

    /**
     * 查询所有房间状态变更日志（受数据隔离过滤）
     *
     * @param request HTTP 请求（携带员工信息）
     * @return 房间状态变更日志列表
     */
    @Operation(summary = "查询所有房间状态变更日志", description = "返回系统中所有房间状态变更日志的列表，非集团管理员仅返回所属酒店的日志")
    @GetMapping
    public ResponseResult<List<RoomStatusLogDTO>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<RoomStatusLog> logs;
        if (dataIsolationService.isGroupAdmin(employee)) {
            logs = roomStatusLogService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            logs = roomStatusLogService.findByHotelId(hotelId);
        }
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 根据ID查询房间状态变更日志
     *
     * @param id 日志ID
     * @return 房间状态变更日志信息，不存在返回404
     */
    @Operation(summary = "根据ID查询房间状态变更日志", description = "根据日志ID查询单条房间状态变更日志信息")
    @GetMapping("/{id}")
    public ResponseResult<RoomStatusLogDTO> findById(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id) {
        java.util.Optional<RoomStatusLog> optional = roomStatusLogService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        return ResponseResult.success(roomStatusLogService.convertToDTO(optional.get()));
    }

    /**
     * 新增房间状态变更日志（手动追加场景，业务流程中状态变更请走 RoomService.updateStatus 自动写入）
     *
     * @param roomStatusLog 房间状态变更日志信息
     * @return 创建后的日志信息
     */
    @Operation(summary = "新增房间状态变更日志", description = "创建一条新的房间状态变更日志记录。状态变更通常由 RoomService.updateStatus 自动写入，本接口仅用于手动补录场景")
    @PostMapping
    public ResponseResult<RoomStatusLogDTO> create(
            @Parameter(description = "房间状态变更日志信息", required = true) @RequestBody RoomStatusLog roomStatusLog) {
        RoomStatusLog saved = roomStatusLogService.save(roomStatusLog);
        return ResponseResult.success("创建成功", roomStatusLogService.convertToDTO(saved));
    }

    /**
     * 更新房间状态变更日志信息
     *
     * @param id             日志ID
     * @param roomStatusLog  房间状态变更日志信息
     * @return 更新后的日志信息，不存在返回404
     */
    @Operation(summary = "更新房间状态变更日志信息", description = "根据日志ID更新日志信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<RoomStatusLogDTO> update(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房间状态变更日志信息", required = true) @RequestBody RoomStatusLog roomStatusLog) {
        java.util.Optional<RoomStatusLog> optional = roomStatusLogService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        roomStatusLog.setId(id);
        RoomStatusLog saved = roomStatusLogService.save(roomStatusLog);
        return ResponseResult.success(roomStatusLogService.convertToDTO(saved));
    }

    /**
     * 根据ID删除房间状态变更日志
     *
     * @param id 日志ID
     * @return 删除结果
     */
    @Operation(summary = "删除房间状态变更日志", description = "根据日志ID删除房间状态变更日志记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "日志ID", required = true) @PathVariable Integer id) {
        roomStatusLogService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据房间ID查询状态变更日志列表
     *
     * @param roomId 房间ID
     * @return 状态变更日志列表
     */
    @Operation(summary = "按房间ID查询状态变更日志", description = "根据房间ID查询该房间的所有状态变更日志")
    @GetMapping("/search/byRoomId")
    public ResponseResult<List<RoomStatusLogDTO>> findByRoomId(
            @Parameter(description = "房间ID", required = true) @RequestParam Integer roomId) {
        List<RoomStatusLog> logs = roomStatusLogService.findByRoomId(roomId);
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 根据操作人ID查询状态变更日志列表
     *
     * @param changedBy 操作人ID
     * @return 状态变更日志列表
     */
    @Operation(summary = "按操作人ID查询状态变更日志", description = "根据操作人ID查询其操作的所有房间状态变更日志")
    @GetMapping("/search/byChangedBy")
    public ResponseResult<List<RoomStatusLogDTO>> findByChangedBy(
            @Parameter(description = "操作人ID", required = true) @RequestParam Integer changedBy) {
        List<RoomStatusLog> logs = roomStatusLogService.findByChangedBy(changedBy);
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 根据酒店ID查询状态变更日志列表（受数据隔离校验）
     *
     * @param hotelId 酒店ID
     * @return 状态变更日志列表
     */
    @Operation(summary = "按酒店ID查询状态变更日志", description = "根据酒店ID查询该酒店范围内所有房间状态变更日志")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<RoomStatusLogDTO>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的房间状态变更日志");
        }
        List<RoomStatusLog> logs = roomStatusLogService.findByHotelId(hotelId);
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 根据新状态查询状态变更日志列表（受数据隔离过滤）
     *
     * @param newStatus 新状态
     * @return 状态变更日志列表
     */
    @Operation(summary = "按新状态查询状态变更日志", description = "根据新状态查询房间状态变更日志，非集团管理员仅返回所属酒店的日志")
    @GetMapping("/search/byNewStatus")
    public ResponseResult<List<RoomStatusLogDTO>> findByNewStatus(
            @Parameter(description = "新状态", required = true) @RequestParam String newStatus,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<RoomStatusLog> logs = roomStatusLogService.findByNewStatus(newStatus);
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            logs = logs.stream()
                    .filter(l -> l.getRoom() != null && hotelId.equals(l.getRoom().getHotelId()))
                    .collect(Collectors.toList());
        }
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 按时间范围查询状态变更日志列表（受数据隔离过滤）
     *
     * @param startTime 开始时间（yyyy-MM-dd'T'HH:mm:ss）
     * @param endTime   结束时间（yyyy-MM-dd'T'HH:mm:ss）
     * @return 状态变更日志列表
     */
    @Operation(summary = "按时间范围查询状态变更日志", description = "根据变更时间范围查询房间状态变更日志，非集团管理员仅返回所属酒店的日志")
    @GetMapping("/search/byTimeRange")
    public ResponseResult<List<RoomStatusLogDTO>> findByTimeRange(
            @Parameter(description = "开始时间(yyyy-MM-dd'T'HH:mm:ss)", required = true)
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间(yyyy-MM-dd'T'HH:mm:ss)", required = true)
                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<RoomStatusLog> logs = roomStatusLogService.findByTimeRange(startTime, endTime);
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            logs = logs.stream()
                    .filter(l -> l.getRoom() != null && hotelId.equals(l.getRoom().getHotelId()))
                    .collect(Collectors.toList());
        }
        return ResponseResult.success(roomStatusLogService.convertToDTOList(logs));
    }

    /**
     * 多条件组合分页查询
     * <p>
     * 支持按酒店ID、房间ID、新状态、操作人ID、变更时间范围组合检索，
     * 所有条件均可选。非集团管理员将强制按其所属酒店过滤，忽略传入的 hotelId。
     * </p>
     *
     * @param hotelId    酒店ID（可空）
     * @param roomId     房间ID（可空）
     * @param newStatus  新状态（可空）
     * @param changedBy  操作人ID（可空）
     * @param startTime  变更开始时间（可空，yyyy-MM-dd'T'HH:mm:ss）
     * @param endTime    变更结束时间（可空，yyyy-MM-dd'T'HH:mm:ss）
     * @param page       页码，从0开始
     * @param size       每页大小
     * @return 分页结果
     */
    @Operation(summary = "多条件组合分页查询", description = "按酒店/房间/状态/操作人/时间范围组合检索日志，分页返回。非集团管理员强制按所属酒店过滤")
    @GetMapping("/search")
    public ResponseResult<Map<String, Object>> search(
            @Parameter(description = "酒店ID(可空)") @RequestParam(required = false) Integer hotelId,
            @Parameter(description = "房间ID(可空)") @RequestParam(required = false) Integer roomId,
            @Parameter(description = "新状态(可空)") @RequestParam(required = false) String newStatus,
            @Parameter(description = "操作人ID(可空)") @RequestParam(required = false) Integer changedBy,
            @Parameter(description = "开始时间(yyyy-MM-dd'T'HH:mm:ss,可空)")
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @Parameter(description = "结束时间(yyyy-MM-dd'T'HH:mm:ss,可空)")
                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @Parameter(description = "页码,从0开始") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Integer effectiveHotelId = hotelId;
        if (!dataIsolationService.isGroupAdmin(employee)) {
            Integer accessibleHotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (hotelId != null && !hotelId.equals(accessibleHotelId)) {
                return ResponseResult.error(403, "无权查询其他酒店的房间状态变更日志");
            }
            effectiveHotelId = accessibleHotelId;
        }
        Pageable pageable = PageRequest.of(page, size);
        Page<RoomStatusLog> logPage = roomStatusLogService.search(
                effectiveHotelId, roomId, newStatus, changedBy, startTime, endTime, pageable);
        List<RoomStatusLogDTO> items = logPage.getContent().stream()
                .map(roomStatusLogService::convertToDTO)
                .collect(Collectors.toList());
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("content", items);
        result.put("totalElements", logPage.getTotalElements());
        result.put("totalPages", logPage.getTotalPages());
        result.put("page", logPage.getNumber());
        result.put("size", logPage.getSize());
        return ResponseResult.success(result);
    }
}
