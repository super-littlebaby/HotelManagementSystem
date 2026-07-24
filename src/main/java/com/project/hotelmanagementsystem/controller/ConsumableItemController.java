package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.ConsumableItem;
import com.project.hotelmanagementsystem.service.ConsumableItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 可消费项目控制层
 * <p>
 * 负责客房可消费物品（如迷你吧商品等）的增删改查及按酒店、分类、激活状态条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "可消费项目管理", description = "可消费项目信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/consumable-items")
public class ConsumableItemController {

    private final ConsumableItemService consumableItemService;

    /**
     * 构造函数注入可消费项目Service
     *
     * @param consumableItemService 可消费项目Service
     */
    public ConsumableItemController(ConsumableItemService consumableItemService) {
        this.consumableItemService = consumableItemService;
    }

    /**
     * 查询所有可消费项目
     *
     * @return 可消费项目列表
     */
    @Operation(summary = "查询所有可消费项目", description = "返回系统中所有可消费项目的列表")
    @GetMapping
    public ResponseResult<List<ConsumableItem>> findAll() {
        return ResponseResult.success(consumableItemService.findAll());
    }

    /**
     * 根据ID查询可消费项目
     *
     * @param id 可消费项目ID
     * @return 可消费项目信息，不存在返回404
     */
    @Operation(summary = "根据ID查询可消费项目", description = "根据可消费项目ID查询单个可消费项目详细信息")
    @GetMapping("/{id}")
    public ResponseResult<ConsumableItem> findById(
            @Parameter(description = "可消费项目ID", required = true) @PathVariable Integer id) {
        return consumableItemService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增可消费项目
     *
     * @param consumableItem 可消费项目信息
     * @return 创建后的可消费项目信息
     */
    @Operation(summary = "新增可消费项目", description = "创建一个新的可消费项目记录")
    @PostMapping
    public ResponseResult<ConsumableItem> create(
            @Parameter(description = "可消费项目信息", required = true) @RequestBody ConsumableItem consumableItem) {
        ConsumableItem saved = consumableItemService.save(consumableItem);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新可消费项目信息
     *
     * @param id             可消费项目ID
     * @param consumableItem 可消费项目信息
     * @return 更新后的可消费项目信息，不存在返回404
     */
    @Operation(summary = "更新可消费项目信息", description = "根据可消费项目ID更新信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<ConsumableItem> update(
            @Parameter(description = "可消费项目ID", required = true) @PathVariable Integer id,
            @Parameter(description = "可消费项目信息", required = true) @RequestBody ConsumableItem consumableItem) {
        return consumableItemService.findById(id)
                .map(existing -> {
                    consumableItem.setId(id);
                    return ResponseResult.success(consumableItemService.save(consumableItem));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除可消费项目
     *
     * @param id 可消费项目ID
     * @return 删除结果
     */
    @Operation(summary = "删除可消费项目", description = "根据可消费项目ID删除可消费项目记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "可消费项目ID", required = true) @PathVariable Integer id) {
        consumableItemService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据酒店ID查询可消费项目列表
     *
     * @param hotelId 酒店ID
     * @return 可消费项目列表
     */
    @Operation(summary = "按酒店ID查询可消费项目", description = "根据酒店ID查询该酒店下所有可消费项目")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<ConsumableItem>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId) {
        return ResponseResult.success(consumableItemService.findByHotelId(hotelId));
    }

    /**
     * 根据分类查询可消费项目列表
     *
     * @param category 分类
     * @return 可消费项目列表
     */
    @Operation(summary = "按分类查询可消费项目", description = "根据分类查询可消费项目列表")
    @GetMapping("/search/byCategory")
    public ResponseResult<List<ConsumableItem>> findByCategory(
            @Parameter(description = "分类", required = true) @RequestParam String category) {
        return ResponseResult.success(consumableItemService.findByCategory(category));
    }

    /**
     * 根据激活状态查询可消费项目列表
     *
     * @param isActive 激活状态
     * @return 可消费项目列表
     */
    @Operation(summary = "按激活状态查询可消费项目", description = "根据激活状态查询可消费项目列表")
    @GetMapping("/search/byIsActive")
    public ResponseResult<List<ConsumableItem>> findByIsActive(
            @Parameter(description = "激活状态", required = true) @RequestParam Boolean isActive) {
        return ResponseResult.success(consumableItemService.findByIsActive(isActive));
    }

    /**
     * 根据酒店ID和分类查询可消费项目列表
     *
     * @param hotelId  酒店ID
     * @param category 分类
     * @return 可消费项目列表
     */
    @Operation(summary = "按酒店ID和分类查询可消费项目", description = "根据酒店ID和分类联合查询可消费项目列表")
    @GetMapping("/search/byHotelIdAndCategory")
    public ResponseResult<List<ConsumableItem>> findByHotelIdAndCategory(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "分类", required = true) @RequestParam String category) {
        return ResponseResult.success(consumableItemService.findByHotelIdAndCategory(hotelId, category));
    }

    /**
     * 根据酒店ID和激活状态查询可消费项目列表
     *
     * @param hotelId  酒店ID
     * @param isActive 激活状态
     * @return 可消费项目列表
     */
    @Operation(summary = "按酒店ID和激活状态查询可消费项目", description = "根据酒店ID和激活状态联合查询可消费项目列表")
    @GetMapping("/search/byHotelIdAndIsActive")
    public ResponseResult<List<ConsumableItem>> findByHotelIdAndIsActive(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "激活状态", required = true) @RequestParam Boolean isActive) {
        return ResponseResult.success(consumableItemService.findByHotelIdAndIsActive(hotelId, isActive));
    }
}