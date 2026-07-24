package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.service.GuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 客人档案控制层
 * <p>
 * 负责客人档案信息的增删改查及按证件号、手机号、邮箱条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "客人档案管理", description = "客人档案信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;

    /**
     * 构造函数注入客人档案Service
     *
     * @param guestService 客人档案Service
     */
    public GuestController(GuestService guestService) {
        this.guestService = guestService;
    }

    /**
     * 查询所有客人
     *
     * @return 客人列表
     */
    @Operation(summary = "查询所有客人", description = "返回系统中所有客人档案的列表")
    @GetMapping
    public ResponseResult<List<Guest>> findAll() {
        return ResponseResult.success(guestService.findAll());
    }

    /**
     * 根据ID查询客人
     *
     * @param id 客人ID
     * @return 客人信息，不存在返回404
     */
    @Operation(summary = "根据ID查询客人", description = "根据客人ID查询单个客人档案详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Guest> findById(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id) {
        return guestService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增客人档案
     *
     * @param guest 客人信息
     * @return 创建后的客人信息
     */
    @Operation(summary = "新增客人档案", description = "创建一个新的客人档案记录")
    @PostMapping
    public ResponseResult<Guest> create(
            @Parameter(description = "客人信息", required = true) @RequestBody Guest guest) {
        Guest saved = guestService.save(guest);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新客人档案信息
     *
     * @param id    客人ID
     * @param guest 客人信息
     * @return 更新后的客人信息，不存在返回404
     */
    @Operation(summary = "更新客人档案信息", description = "根据客人ID更新客人档案信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Guest> update(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id,
            @Parameter(description = "客人信息", required = true) @RequestBody Guest guest) {
        return guestService.findById(id)
                .map(existing -> {
                    guest.setId(id);
                    return ResponseResult.success(guestService.save(guest));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除客人档案
     *
     * @param id 客人ID
     * @return 删除结果
     */
    @Operation(summary = "删除客人档案", description = "根据客人ID删除客人档案记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id) {
        guestService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据证件号查询客人
     *
     * @param idNumber 证件号
     * @return 客人信息，不存在返回404
     */
    @Operation(summary = "按证件号查询客人", description = "根据证件号查询单个客人档案信息")
    @GetMapping("/search/byIdNumber")
    public ResponseResult<Guest> findByIdNumber(
            @Parameter(description = "证件号", required = true) @RequestParam String idNumber) {
        return guestService.findByIdNumber(idNumber)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据手机号查询客人列表
     *
     * @param phone 手机号
     * @return 客人列表
     */
    @Operation(summary = "按手机号查询客人", description = "根据手机号查询客人档案列表")
    @GetMapping("/search/byPhone")
    public ResponseResult<List<Guest>> findByPhone(
            @Parameter(description = "手机号", required = true) @RequestParam String phone) {
        return ResponseResult.success(guestService.findByPhone(phone));
    }

    /**
     * 根据邮箱查询客人
     *
     * @param email 邮箱
     * @return 客人信息，不存在返回404
     */
    @Operation(summary = "按邮箱查询客人", description = "根据邮箱查询单个客人档案信息")
    @GetMapping("/search/byEmail")
    public ResponseResult<Guest> findByEmail(
            @Parameter(description = "邮箱", required = true) @RequestParam String email) {
        return guestService.findByEmail(email)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }
}