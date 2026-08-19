package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Facility;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.RoomTypeFacilityService;
import com.project.hotelmanagementsystem.service.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * 房型-设施关联控制层
 * <p>
 * 负责房型与设施多对多关联关系的维护，使用复合主键（roomTypeId + facilityId）进行定位，
 * 对外提供 RESTful 接口。所有接口按员工 hotel_id 进行数据隔离。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房型-设施关联管理", description = "房型与设施关联关系的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/room-type-facilities")
public class RoomTypeFacilityController {

    private final RoomTypeFacilityService roomTypeFacilityService;
    private final RoomTypeService roomTypeService;
    private final DataIsolationService dataIsolationService;

    /**
     * 构造函数注入
     *
     * @param roomTypeFacilityService 房型-设施关联Service
     * @param roomTypeService         房型Service
     * @param dataIsolationService    数据隔离Service
     */
    public RoomTypeFacilityController(RoomTypeFacilityService roomTypeFacilityService,
                                      RoomTypeService roomTypeService,
                                      DataIsolationService dataIsolationService) {
        this.roomTypeFacilityService = roomTypeFacilityService;
        this.roomTypeService = roomTypeService;
        this.dataIsolationService = dataIsolationService;
    }

    /**
     * 查询所有房型-设施关联
     *
     * @param request HTTP请求
     * @return 房型-设施关联列表
     */
    @Operation(summary = "查询所有房型-设施关联", description = "返回系统中所有房型与设施的关联关系列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<RoomTypeFacility>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<RoomTypeFacility> allRelations = roomTypeFacilityService.findAll();
        if (dataIsolationService.isGroupAdmin(employee)) {
            return ResponseResult.success(allRelations);
        }
        Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
        List<RoomTypeFacility> filtered = allRelations.stream()
                .filter(rtf -> {
                    Optional<RoomType> roomTypeOpt = roomTypeService.findById(rtf.getRoomTypeId());
                    return roomTypeOpt.isPresent() && hotelId.equals(roomTypeOpt.get().getHotelId());
                })
                .toList();
        return ResponseResult.success(filtered);
    }

    /**
     * 根据复合主键查询房型-设施关联
     *
     * @param roomTypeId 房型ID
     * @param facilityId 设施ID
     * @param request    HTTP请求
     * @return 房型-设施关联信息，不存在返回404
     */
    @Operation(summary = "根据复合主键查询房型-设施关联", description = "根据房型ID与设施ID组合主键查询单条关联记录")
    @GetMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<RoomTypeFacility> findById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权访问该房型的设施关联");
        }
        return roomTypeFacilityService.findById(new RoomTypeFacilityId(roomTypeId, facilityId))
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增房型-设施关联
     *
     * @param roomTypeFacility 房型-设施关联信息
     * @param request          HTTP请求
     * @return 创建后的关联信息
     */
    @Operation(summary = "新增房型-设施关联", description = "创建一条房型与设施的关联关系记录")
    @PostMapping
    public ResponseResult<RoomTypeFacility> create(
            @Parameter(description = "房型-设施关联信息", required = true) @RequestBody RoomTypeFacility roomTypeFacility,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeFacility.getRoomTypeId());
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        RoomTypeFacility saved = roomTypeFacilityService.save(roomTypeFacility);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新房型-设施关联
     *
     * @param roomTypeId       房型ID
     * @param facilityId       设施ID
     * @param roomTypeFacility 房型-设施关联信息
     * @param request          HTTP请求
     * @return 更新后的关联信息，不存在返回404
     */
    @Operation(summary = "更新房型-设施关联", description = "根据复合主键更新关联信息，不存在则返回404")
    @PutMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<RoomTypeFacility> update(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId,
            @Parameter(description = "房型-设施关联信息", required = true) @RequestBody RoomTypeFacility roomTypeFacility,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        RoomTypeFacilityId id = new RoomTypeFacilityId(roomTypeId, facilityId);
        return roomTypeFacilityService.findById(id)
                .map(existing -> {
                    roomTypeFacility.setRoomTypeId(roomTypeId);
                    roomTypeFacility.setFacilityId(facilityId);
                    return ResponseResult.success(roomTypeFacilityService.save(roomTypeFacility));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据复合主键删除房型-设施关联
     *
     * @param roomTypeId 房型ID
     * @param facilityId 设施ID
     * @param request    HTTP请求
     * @return 删除结果
     */
    @Operation(summary = "删除房型-设施关联", description = "根据复合主键删除房型与设施的关联关系")
    @DeleteMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        roomTypeFacilityService.deleteById(new RoomTypeFacilityId(roomTypeId, facilityId));
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据房型ID查询关联的设施列表
     *
     * @param roomTypeId 房型ID
     * @param request    HTTP请求
     * @return 房型-设施关联列表
     */
    @Operation(summary = "按房型ID查询关联设施", description = "根据房型ID查询该房型关联的所有设施关系")
    @GetMapping("/search/byRoomTypeId")
    public ResponseResult<List<RoomTypeFacility>> findByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权访问该房型的设施关联");
        }
        return ResponseResult.success(roomTypeFacilityService.findByRoomTypeId(roomTypeId));
    }

    /**
     * 根据设施ID查询关联的房型列表
     *
     * @param facilityId 设施ID
     * @return 房型-设施关联列表
     */
    @Operation(summary = "按设施ID查询关联房型", description = "根据设施ID查询关联该设施的所有房型关系")
    @GetMapping("/search/byFacilityId")
    public ResponseResult<List<RoomTypeFacility>> findByFacilityId(
            @Parameter(description = "设施ID", required = true) @RequestParam Integer facilityId) {
        return ResponseResult.success(roomTypeFacilityService.findByFacilityId(facilityId));
    }

    // ==================== 批量操作接口 ====================

    /**
     * 查询房型关联的设施详情列表
     *
     * @param roomTypeId 房型ID
     * @param request    HTTP请求
     * @return 设施详情列表
     */
    @Operation(summary = "查询房型关联的设施详情", description = "根据房型ID查询该房型关联的所有设施详情列表")
    @GetMapping("/{roomTypeId}/facilities")
    public ResponseResult<List<Facility>> findFacilitiesByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权访问该房型的设施关联");
        }
        return ResponseResult.success(roomTypeFacilityService.findFacilitiesByRoomTypeId(roomTypeId));
    }

    /**
     * 批量添加设施到房型
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 设施ID列表
     * @param request     HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "批量添加设施到房型", description = "将多个设施批量关联到指定房型")
    @PostMapping("/{roomTypeId}/facilities")
    public ResponseResult<Void> addFacilitiesToRoomType(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID数组", required = true) @RequestBody List<Integer> facilityIds,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        roomTypeFacilityService.addFacilitiesToRoomType(roomTypeId, facilityIds);
        return ResponseResult.success("添加成功", null);
    }

    /**
     * 批量移除房型的设施
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 设施ID列表
     * @param request     HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "批量移除房型的设施", description = "批量移除房型关联的指定设施")
    @DeleteMapping("/{roomTypeId}/facilities")
    public ResponseResult<Void> removeFacilitiesFromRoomType(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID数组", required = true) @RequestBody List<Integer> facilityIds,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        roomTypeFacilityService.removeFacilitiesFromRoomType(roomTypeId, facilityIds);
        return ResponseResult.success("移除成功", null);
    }

    /**
     * 替换房型的所有设施
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 新的设施ID列表
     * @param request     HTTP请求
     * @return 操作结果
     */
    @Operation(summary = "替换房型的所有设施", description = "先清空房型的所有设施关联，再批量添加新的设施关联")
    @PutMapping("/{roomTypeId}/facilities")
    public ResponseResult<Void> replaceFacilitiesForRoomType(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID数组", required = true) @RequestBody List<Integer> facilityIds,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(roomTypeId);
        if (roomTypeOpt.isEmpty()) {
            return ResponseResult.error(404, "房型不存在");
        }
        if (!dataIsolationService.canAccessHotel(employee, roomTypeOpt.get().getHotelId())) {
            return ResponseResult.error(403, "无权操作该房型的设施关联");
        }
        roomTypeFacilityService.replaceFacilitiesForRoomType(roomTypeId, facilityIds);
        return ResponseResult.success("替换成功", null);
    }
}
