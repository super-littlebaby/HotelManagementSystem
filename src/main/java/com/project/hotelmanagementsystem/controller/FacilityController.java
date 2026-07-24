package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Facility;
import com.project.hotelmanagementsystem.service.FacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 设施信息控制层
 * <p>
 * 负责酒店设施（如 WiFi、早餐、停车场等）的增删改查及按名称条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "设施管理", description = "设施信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/facilities")
public class FacilityController {

    private final FacilityService facilityService;

    /**
     * 构造函数注入设施Service
     *
     * @param facilityService 设施Service
     */
    public FacilityController(FacilityService facilityService) {
        this.facilityService = facilityService;
    }

    /**
     * 查询所有设施
     *
     * @return 设施列表
     */
    @Operation(summary = "查询所有设施", description = "返回系统中所有设施的列表")
    @GetMapping
    public ResponseResult<List<Facility>> findAll() {
        return ResponseResult.success(facilityService.findAll());
    }

    /**
     * 根据ID查询设施
     *
     * @param id 设施ID
     * @return 设施信息，不存在返回404
     */
    @Operation(summary = "根据ID查询设施", description = "根据设施ID查询单个设施详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Facility> findById(
            @Parameter(description = "设施ID", required = true) @PathVariable Integer id) {
        return facilityService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增设施
     *
     * @param facility 设施信息
     * @return 创建后的设施信息
     */
    @Operation(summary = "新增设施", description = "创建一个新的设施记录")
    @PostMapping
    public ResponseResult<Facility> create(
            @Parameter(description = "设施信息", required = true) @RequestBody Facility facility) {
        Facility saved = facilityService.save(facility);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新设施信息
     *
     * @param id       设施ID
     * @param facility 设施信息
     * @return 更新后的设施信息，不存在返回404
     */
    @Operation(summary = "更新设施信息", description = "根据设施ID更新设施信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Facility> update(
            @Parameter(description = "设施ID", required = true) @PathVariable Integer id,
            @Parameter(description = "设施信息", required = true) @RequestBody Facility facility) {
        return facilityService.findById(id)
                .map(existing -> {
                    facility.setId(id);
                    return ResponseResult.success(facilityService.save(facility));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除设施
     *
     * @param id 设施ID
     * @return 删除结果
     */
    @Operation(summary = "删除设施", description = "根据设施ID删除设施记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "设施ID", required = true) @PathVariable Integer id) {
        facilityService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据名称模糊查询设施列表
     *
     * @param name 设施名称关键字
     * @return 设施列表
     */
    @Operation(summary = "按名称模糊查询设施", description = "根据设施名称关键字模糊查询设施列表")
    @GetMapping("/search/byName")
    public ResponseResult<List<Facility>> findByNameContaining(
            @Parameter(description = "设施名称关键字", required = true) @RequestParam String name) {
        return ResponseResult.success(facilityService.findByNameContaining(name));
    }
}