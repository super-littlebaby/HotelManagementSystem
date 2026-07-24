package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.BillItem;
import com.project.hotelmanagementsystem.service.BillItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账单明细控制层
 * <p>
 * 负责账单下消费明细项的增删改查及按账单、项目类型条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "账单明细管理", description = "账单明细信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/bill-items")
public class BillItemController {

    private final BillItemService billItemService;

    /**
     * 构造函数注入账单明细Service
     *
     * @param billItemService 账单明细Service
     */
    public BillItemController(BillItemService billItemService) {
        this.billItemService = billItemService;
    }

    /**
     * 查询所有账单明细
     *
     * @return 账单明细列表
     */
    @Operation(summary = "查询所有账单明细", description = "返回系统中所有账单明细记录的列表")
    @GetMapping
    public ResponseResult<List<BillItem>> findAll() {
        return ResponseResult.success(billItemService.findAll());
    }

    /**
     * 根据ID查询账单明细
     *
     * @param id 账单明细ID
     * @return 账单明细信息，不存在返回404
     */
    @Operation(summary = "根据ID查询账单明细", description = "根据账单明细ID查询单条账单明细信息")
    @GetMapping("/{id}")
    public ResponseResult<BillItem> findById(
            @Parameter(description = "账单明细ID", required = true) @PathVariable Integer id) {
        return billItemService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增账单明细
     *
     * @param billItem 账单明细信息
     * @return 创建后的账单明细信息
     */
    @Operation(summary = "新增账单明细", description = "创建一条新的账单明细记录")
    @PostMapping
    public ResponseResult<BillItem> create(
            @Parameter(description = "账单明细信息", required = true) @RequestBody BillItem billItem) {
        BillItem saved = billItemService.save(billItem);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新账单明细信息
     *
     * @param id       账单明细ID
     * @param billItem 账单明细信息
     * @return 更新后的账单明细信息，不存在返回404
     */
    @Operation(summary = "更新账单明细信息", description = "根据账单明细ID更新信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<BillItem> update(
            @Parameter(description = "账单明细ID", required = true) @PathVariable Integer id,
            @Parameter(description = "账单明细信息", required = true) @RequestBody BillItem billItem) {
        return billItemService.findById(id)
                .map(existing -> {
                    billItem.setId(id);
                    return ResponseResult.success(billItemService.save(billItem));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除账单明细
     *
     * @param id 账单明细ID
     * @return 删除结果
     */
    @Operation(summary = "删除账单明细", description = "根据账单明细ID删除账单明细记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "账单明细ID", required = true) @PathVariable Integer id) {
        billItemService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据账单ID查询账单明细列表
     *
     * @param billId 账单ID
     * @return 账单明细列表
     */
    @Operation(summary = "按账单ID查询账单明细", description = "根据账单ID查询该账单下所有明细")
    @GetMapping("/search/byBillId")
    public ResponseResult<List<BillItem>> findByBillId(
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId) {
        return ResponseResult.success(billItemService.findByBillId(billId));
    }

    /**
     * 根据项目类型查询账单明细列表
     *
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    @Operation(summary = "按项目类型查询账单明细", description = "根据项目类型查询账单明细列表")
    @GetMapping("/search/byItemType")
    public ResponseResult<List<BillItem>> findByItemType(
            @Parameter(description = "项目类型", required = true) @RequestParam String itemType) {
        return ResponseResult.success(billItemService.findByItemType(itemType));
    }

    /**
     * 根据账单ID和项目类型查询账单明细列表
     *
     * @param billId   账单ID
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    @Operation(summary = "按账单ID和项目类型查询账单明细", description = "根据账单ID和项目类型联合查询账单明细列表")
    @GetMapping("/search/byBillIdAndItemType")
    public ResponseResult<List<BillItem>> findByBillIdAndItemType(
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId,
            @Parameter(description = "项目类型", required = true) @RequestParam String itemType) {
        return ResponseResult.success(billItemService.findByBillIdAndItemType(billId, itemType));
    }
}