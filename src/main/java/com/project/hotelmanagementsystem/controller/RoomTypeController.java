package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.service.RoomTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 房型信息控制层
 * <p>
 * 负责房型（如大床房、双床房等）的增删改查及按酒店、床型条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "房型管理", description = "房型信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    /**
     * 构造函数注入房型Service
     *
     * @param roomTypeService 房型Service
     */
    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    /**
     * 查询所有房型
     *
     * @return 房型列表
     */
    @Operation(summary = "查询所有房型", description = "返回系统中所有房型的列表")
    @GetMapping
    public ResponseResult<List<RoomType>> findAll() {
        return ResponseResult.success(roomTypeService.findAll());
    }

    /**
     * 根据ID查询房型
     *
     * @param id 房型ID
     * @return 房型信息，不存在返回404
     */
    @Operation(summary = "根据ID查询房型", description = "根据房型ID查询单个房型详细信息")
    @GetMapping("/{id}")
    public ResponseResult<RoomType> findById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id) {
        return roomTypeService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增房型
     *
     * @param roomType 房型信息
     * @return 创建后的房型信息
     */
    @Operation(summary = "新增房型", description = "创建一个新的房型记录")
    @PostMapping
    public ResponseResult<RoomType> create(
            @Parameter(description = "房型信息", required = true) @RequestBody RoomType roomType) {
        RoomType saved = roomTypeService.save(roomType);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新房型信息
     *
     * @param id       房型ID
     * @param roomType 房型信息
     * @return 更新后的房型信息，不存在返回404
     */
    @Operation(summary = "更新房型信息", description = "根据房型ID更新房型信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<RoomType> update(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id,
            @Parameter(description = "房型信息", required = true) @RequestBody RoomType roomType) {
        return roomTypeService.findById(id)
                .map(existing -> {
                    roomType.setId(id);
                    return ResponseResult.success(roomTypeService.save(roomType));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除房型
     *
     * @param id 房型ID
     * @return 删除结果
     */
    @Operation(summary = "删除房型", description = "根据房型ID删除房型记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "房型ID", required = true) @PathVariable Integer id) {
        roomTypeService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据酒店ID查询房型列表
     *
     * @param hotelId 酒店ID
     * @return 房型列表
     */
    @Operation(summary = "按酒店ID查询房型", description = "根据酒店ID查询该酒店下所有房型")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<RoomType>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId) {
        return ResponseResult.success(roomTypeService.findByHotelId(hotelId));
    }

    /**
     * 根据酒店ID和床型查询房型列表
     *
     * @param hotelId 酒店ID
     * @param bedType 床型
     * @return 房型列表
     */
    @Operation(summary = "按酒店ID和床型查询房型", description = "根据酒店ID和床型联合查询房型列表")
    @GetMapping("/search/byHotelIdAndBedType")
    public ResponseResult<List<RoomType>> findByHotelIdAndBedType(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "床型", required = true) @RequestParam String bedType) {
        return ResponseResult.success(roomTypeService.findByHotelIdAndBedType(hotelId, bedType));
    }
}