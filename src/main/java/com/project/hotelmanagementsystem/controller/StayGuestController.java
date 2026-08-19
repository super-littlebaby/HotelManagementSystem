package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.StayGuest;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.StayGuestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 同住客人控制层
 * <p>
 * 负责同住客人记录的增删改查及条件检索，对外提供 RESTful 接口。
 * </p>
 */
@Tag(name = "同住客人管理", description = "同住客人记录的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/stay-guests")
public class StayGuestController {

    private final StayGuestService stayGuestService;
    private final DataIsolationService dataIsolationService;

    public StayGuestController(StayGuestService stayGuestService, DataIsolationService dataIsolationService) {
        this.stayGuestService = stayGuestService;
        this.dataIsolationService = dataIsolationService;
    }

    /**
     * 查询所有同住客人记录
     *
     * @return 同住客人记录列表
     */
    @Operation(summary = "查询所有同住客人记录", description = "返回系统中所有同住客人记录的列表")
    @GetMapping
    public ResponseResult<List<StayGuest>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            return ResponseResult.success(stayGuestService.findAll());
        }
        return ResponseResult.success(stayGuestService.findAll());
    }

    /**
     * 根据ID查询同住客人记录
     *
     * @param id 同住客人记录ID
     * @return 同住客人记录信息，不存在返回404
     */
    @Operation(summary = "根据ID查询同住客人记录", description = "根据同住客人记录ID查询单条详细信息")
    @GetMapping("/{id}")
    public ResponseResult<StayGuest> findById(
            @Parameter(description = "同住客人记录ID", required = true) @PathVariable Integer id) {
        java.util.Optional<StayGuest> optional = stayGuestService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        return ResponseResult.success(optional.get());
    }

    /**
     * 根据入住记录ID查询同住客人列表
     *
     * @param checkInId 入住记录ID
     * @return 同住客人列表
     */
    @Operation(summary = "按入住记录ID查询同住客人", description = "根据入住记录ID查询关联的所有同住客人")
    @GetMapping("/search/byCheckInId")
    public ResponseResult<List<StayGuest>> findByCheckInId(
            @Parameter(description = "入住记录ID", required = true) @RequestParam Integer checkInId) {
        return ResponseResult.success(stayGuestService.findByCheckInId(checkInId));
    }

    /**
     * 根据客人ID查询同住记录列表
     *
     * @param guestId 客人ID
     * @return 同住记录列表
     */
    @Operation(summary = "按客人ID查询同住记录", description = "根据客人ID查询该客人的所有同住记录")
    @GetMapping("/search/byGuestId")
    public ResponseResult<List<StayGuest>> findByGuestId(
            @Parameter(description = "客人ID", required = true) @RequestParam Integer guestId) {
        return ResponseResult.success(stayGuestService.findByGuestId(guestId));
    }

    /**
     * 新增同住客人记录
     *
     * @param stayGuest 同住客人记录信息
     * @return 创建后的同住客人记录信息
     */
    @Operation(summary = "新增同住客人记录", description = "创建一条新的同住客人记录")
    @PostMapping
    public ResponseResult<StayGuest> create(
            @Parameter(description = "同住客人记录信息", required = true) @RequestBody StayGuest stayGuest) {
        StayGuest saved = stayGuestService.save(stayGuest);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 批量新增同住客人记录
     *
     * @param stayGuests 同住客人记录列表
     * @return 创建后的同住客人记录列表
     */
    @Operation(summary = "批量新增同住客人记录", description = "批量创建多条同住客人记录")
    @PostMapping("/batch")
    public ResponseResult<List<StayGuest>> createBatch(
            @Parameter(description = "同住客人记录列表", required = true) @RequestBody List<StayGuest> stayGuests) {
        List<StayGuest> saved = stayGuestService.saveAll(stayGuests);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新同住客人记录信息
     *
     * @param id         同住客人记录ID
     * @param stayGuest  同住客人记录信息
     * @return 更新后的同住客人记录信息，不存在返回404
     */
    @Operation(summary = "更新同住客人记录信息", description = "根据同住客人记录ID更新信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<StayGuest> update(
            @Parameter(description = "同住客人记录ID", required = true) @PathVariable Integer id,
            @Parameter(description = "同住客人记录信息", required = true) @RequestBody StayGuest stayGuest) {
        java.util.Optional<StayGuest> optional = stayGuestService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        stayGuest.setId(id);
        return ResponseResult.success(stayGuestService.save(stayGuest));
    }

    /**
     * 根据ID删除同住客人记录
     *
     * @param id 同住客人记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除同住客人记录", description = "根据同住客人记录ID删除记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "同住客人记录ID", required = true) @PathVariable Integer id) {
        java.util.Optional<StayGuest> optional = stayGuestService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        stayGuestService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }
}