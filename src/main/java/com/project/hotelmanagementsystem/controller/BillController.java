package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Bill;
import com.project.hotelmanagementsystem.service.BillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 账单控制层
 * <p>
 * 负责客人入住期间消费账单的增删改查及按入住记录、账单状态条件检索，并支持查询最新账单，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "账单管理", description = "账单信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    private final BillService billService;

    /**
     * 构造函数注入账单Service
     *
     * @param billService 账单Service
     */
    public BillController(BillService billService) {
        this.billService = billService;
    }

    /**
     * 查询所有账单
     *
     * @return 账单列表
     */
    @Operation(summary = "查询所有账单", description = "返回系统中所有账单记录的列表")
    @GetMapping
    public ResponseResult<List<Bill>> findAll() {
        return ResponseResult.success(billService.findAll());
    }

    /**
     * 根据ID查询账单
     *
     * @param id 账单ID
     * @return 账单信息，不存在返回404
     */
    @Operation(summary = "根据ID查询账单", description = "根据账单ID查询单个账单详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Bill> findById(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id) {
        return billService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增账单
     *
     * @param bill 账单信息
     * @return 创建后的账单信息
     */
    @Operation(summary = "新增账单", description = "创建一个新的账单记录")
    @PostMapping
    public ResponseResult<Bill> create(
            @Parameter(description = "账单信息", required = true) @RequestBody Bill bill) {
        Bill saved = billService.save(bill);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新账单信息
     *
     * @param id   账单ID
     * @param bill 账单信息
     * @return 更新后的账单信息，不存在返回404
     */
    @Operation(summary = "更新账单信息", description = "根据账单ID更新账单信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Bill> update(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id,
            @Parameter(description = "账单信息", required = true) @RequestBody Bill bill) {
        return billService.findById(id)
                .map(existing -> {
                    bill.setId(id);
                    return ResponseResult.success(billService.save(bill));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除账单
     *
     * @param id 账单ID
     * @return 删除结果
     */
    @Operation(summary = "删除账单", description = "根据账单ID删除账单记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "账单ID", required = true) @PathVariable Integer id) {
        billService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据入住ID查询账单列表
     *
     * @param checkInId 入住ID
     * @return 账单列表
     */
    @Operation(summary = "按入住ID查询账单", description = "根据入住ID查询关联的所有账单")
    @GetMapping("/search/byCheckInId")
    public ResponseResult<List<Bill>> findByCheckInId(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId) {
        return ResponseResult.success(billService.findByCheckInId(checkInId));
    }

    /**
     * 根据账单状态查询账单列表
     *
     * @param billStatus 账单状态
     * @return 账单列表
     */
    @Operation(summary = "按状态查询账单", description = "根据账单状态查询账单列表")
    @GetMapping("/search/byBillStatus")
    public ResponseResult<List<Bill>> findByBillStatus(
            @Parameter(description = "账单状态", required = true) @RequestParam String billStatus) {
        return ResponseResult.success(billService.findByBillStatus(billStatus));
    }

    /**
     * 根据入住ID查询最新账单
     *
     * @param checkInId 入住ID
     * @return 最新账单信息，不存在返回404
     */
    @Operation(summary = "按入住ID查询最新账单", description = "根据入住ID查询最新一条账单记录")
    @GetMapping("/search/latestByCheckInId")
    public ResponseResult<Bill> findLatestByCheckInId(
            @Parameter(description = "入住ID", required = true) @RequestParam Integer checkInId) {
        return billService.findFirstByCheckInIdOrderByIdDesc(checkInId)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }
}