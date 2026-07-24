package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import com.project.hotelmanagementsystem.service.RoomTypeFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房型-设施关联控制层
 * <p>
 * 负责房型与设施多对多关联关系的维护，使用复合主键（roomTypeId + facilityId）进行定位，
 * 对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房型-设施关联管理", description = "房型与设施关联关系的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/room-type-facilities")
public class RoomTypeFacilityController {

    private final RoomTypeFacilityService roomTypeFacilityService;

    /**
     * 构造函数注入房型-设施关联Service
     *
     * @param roomTypeFacilityService 房型-设施关联Service
     */
    public RoomTypeFacilityController(RoomTypeFacilityService roomTypeFacilityService) {
        this.roomTypeFacilityService = roomTypeFacilityService;
    }

    /**
     * 查询所有房型-设施关联
     *
     * @return 房型-设施关联列表
     */
    @Operation(summary = "查询所有房型-设施关联", description = "返回系统中所有房型与设施的关联关系列表")
    @GetMapping
    public ResponseResult<List<RoomTypeFacility>> findAll() {
        return ResponseResult.success(roomTypeFacilityService.findAll());
    }

    /**
     * 根据复合主键查询房型-设施关联
     *
     * @param roomTypeId 房型ID
     * @param facilityId 设施ID
     * @return 房型-设施关联信息，不存在返回404
     */
    @Operation(summary = "根据复合主键查询房型-设施关联", description = "根据房型ID与设施ID组合主键查询单条关联记录")
    @GetMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<RoomTypeFacility> findById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId) {
        return roomTypeFacilityService.findById(new RoomTypeFacilityId(roomTypeId, facilityId))
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增房型-设施关联
     *
     * @param roomTypeFacility 房型-设施关联信息
     * @return 创建后的关联信息
     */
    @Operation(summary = "新增房型-设施关联", description = "创建一条房型与设施的关联关系记录")
    @PostMapping
    public ResponseResult<RoomTypeFacility> create(
            @Parameter(description = "房型-设施关联信息", required = true) @RequestBody RoomTypeFacility roomTypeFacility) {
        RoomTypeFacility saved = roomTypeFacilityService.save(roomTypeFacility);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新房型-设施关联
     *
     * @param roomTypeId        房型ID
     * @param facilityId        设施ID
     * @param roomTypeFacility  房型-设施关联信息
     * @return 更新后的关联信息，不存在返回404
     */
    @Operation(summary = "更新房型-设施关联", description = "根据复合主键更新关联信息，不存在则返回404")
    @PutMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<RoomTypeFacility> update(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId,
            @Parameter(description = "房型-设施关联信息", required = true) @RequestBody RoomTypeFacility roomTypeFacility) {
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
     * @return 删除结果
     */
    @Operation(summary = "删除房型-设施关联", description = "根据复合主键删除房型与设施的关联关系")
    @DeleteMapping("/{roomTypeId}/{facilityId}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer roomTypeId,
            @Parameter(description = "设施ID", required = true) @PathVariable Integer facilityId) {
        roomTypeFacilityService.deleteById(new RoomTypeFacilityId(roomTypeId, facilityId));
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据房型ID查询关联的设施列表
     *
     * @param roomTypeId 房型ID
     * @return 房型-设施关联列表
     */
    @Operation(summary = "按房型ID查询关联设施", description = "根据房型ID查询该房型关联的所有设施关系")
    @GetMapping("/search/byRoomTypeId")
    public ResponseResult<List<RoomTypeFacility>> findByRoomTypeId(
            @Parameter(description = "房型ID", required = true) @RequestParam Integer roomTypeId) {
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
}