package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Hotel;
import com.project.hotelmanagementsystem.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 酒店信息控制层
 * <p>
 * 负责酒店基础信息的增删改查及按名称、地址条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "酒店管理", description = "酒店基础信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;

    /**
     * 构造函数注入酒店Service
     *
     * @param hotelService 酒店Service
     */
    public HotelController(HotelService hotelService) {
        this.hotelService = hotelService;
    }

    /**
     * 查询所有酒店
     *
     * @return 酒店列表
     */
    @Operation(summary = "查询所有酒店", description = "返回系统中所有酒店的列表")
    @GetMapping
    public ResponseResult<List<Hotel>> findAll() {
        return ResponseResult.success(hotelService.findAll());
    }

    /**
     * 根据ID查询酒店
     *
     * @param id 酒店ID
     * @return 酒店信息，不存在返回404
     */
    @Operation(summary = "根据ID查询酒店", description = "根据酒店ID查询单个酒店详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Hotel> findById(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id) {
        return hotelService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增酒店
     *
     * @param hotel 酒店信息
     * @return 创建后的酒店信息
     */
    @Operation(summary = "新增酒店", description = "创建一个新的酒店记录")
    @PostMapping
    public ResponseResult<Hotel> create(
            @Parameter(description = "酒店信息", required = true) @RequestBody Hotel hotel) {
        Hotel saved = hotelService.save(hotel);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新酒店信息
     *
     * @param id    酒店ID
     * @param hotel 酒店信息
     * @return 更新后的酒店信息，不存在返回404
     */
    @Operation(summary = "更新酒店信息", description = "根据酒店ID更新酒店信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Hotel> update(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id,
            @Parameter(description = "酒店信息", required = true) @RequestBody Hotel hotel) {
        return hotelService.findById(id)
                .map(existing -> {
                    hotel.setId(id);
                    return ResponseResult.success(hotelService.save(hotel));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除酒店
     *
     * @param id 酒店ID
     * @return 删除结果
     */
    @Operation(summary = "删除酒店", description = "根据酒店ID删除酒店记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id) {
        hotelService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据名称模糊查询酒店列表
     *
     * @param name 酒店名称关键字
     * @return 酒店列表
     */
    @Operation(summary = "按名称模糊查询酒店", description = "根据酒店名称关键字模糊查询酒店列表")
    @GetMapping("/search/byName")
    public ResponseResult<List<Hotel>> findByNameContaining(
            @Parameter(description = "酒店名称关键字", required = true) @RequestParam String name) {
        return ResponseResult.success(hotelService.findByNameContaining(name));
    }

    /**
     * 根据地址模糊查询酒店列表
     *
     * @param address 酒店地址关键字
     * @return 酒店列表
     */
    @Operation(summary = "按地址模糊查询酒店", description = "根据酒店地址关键字模糊查询酒店列表")
    @GetMapping("/search/byAddress")
    public ResponseResult<List<Hotel>> findByAddressContaining(
            @Parameter(description = "酒店地址关键字", required = true) @RequestParam String address) {
        return ResponseResult.success(hotelService.findByAddressContaining(address));
    }
}
