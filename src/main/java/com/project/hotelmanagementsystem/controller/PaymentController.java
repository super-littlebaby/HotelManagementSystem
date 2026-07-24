package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Payment;
import com.project.hotelmanagementsystem.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收款记录控制层
 * <p>
 * 负责账单收款记录的增删改查及按账单、支付方式、支付类型条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "收款记录管理", description = "收款记录信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * 构造函数注入收款记录Service
     *
     * @param paymentService 收款记录Service
     */
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * 查询所有收款记录
     *
     * @return 收款记录列表
     */
    @Operation(summary = "查询所有收款记录", description = "返回系统中所有收款记录的列表")
    @GetMapping
    public ResponseResult<List<Payment>> findAll() {
        return ResponseResult.success(paymentService.findAll());
    }

    /**
     * 根据ID查询收款记录
     *
     * @param id 收款记录ID
     * @return 收款记录信息，不存在返回404
     */
    @Operation(summary = "根据ID查询收款记录", description = "根据收款记录ID查询单条收款记录详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Payment> findById(
            @Parameter(description = "收款记录ID", required = true) @PathVariable Integer id) {
        return paymentService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增收款记录
     *
     * @param payment 收款记录信息
     * @return 创建后的收款记录信息
     */
    @Operation(summary = "新增收款记录", description = "创建一条新的收款记录")
    @PostMapping
    public ResponseResult<Payment> create(
            @Parameter(description = "收款记录信息", required = true) @RequestBody Payment payment) {
        Payment saved = paymentService.save(payment);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新收款记录信息
     *
     * @param id      收款记录ID
     * @param payment 收款记录信息
     * @return 更新后的收款记录信息，不存在返回404
     */
    @Operation(summary = "更新收款记录信息", description = "根据收款记录ID更新信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Payment> update(
            @Parameter(description = "收款记录ID", required = true) @PathVariable Integer id,
            @Parameter(description = "收款记录信息", required = true) @RequestBody Payment payment) {
        return paymentService.findById(id)
                .map(existing -> {
                    payment.setId(id);
                    return ResponseResult.success(paymentService.save(payment));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除收款记录
     *
     * @param id 收款记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除收款记录", description = "根据收款记录ID删除收款记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "收款记录ID", required = true) @PathVariable Integer id) {
        paymentService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据账单ID查询收款记录列表
     *
     * @param billId 账单ID
     * @return 收款记录列表
     */
    @Operation(summary = "按账单ID查询收款记录", description = "根据账单ID查询该账单下所有收款记录")
    @GetMapping("/search/byBillId")
    public ResponseResult<List<Payment>> findByBillId(
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId) {
        return ResponseResult.success(paymentService.findByBillId(billId));
    }

    /**
     * 根据支付方式查询收款记录列表
     *
     * @param paymentMethod 支付方式
     * @return 收款记录列表
     */
    @Operation(summary = "按支付方式查询收款记录", description = "根据支付方式查询收款记录列表")
    @GetMapping("/search/byPaymentMethod")
    public ResponseResult<List<Payment>> findByPaymentMethod(
            @Parameter(description = "支付方式", required = true) @RequestParam String paymentMethod) {
        return ResponseResult.success(paymentService.findByPaymentMethod(paymentMethod));
    }

    /**
     * 根据支付类型查询收款记录列表
     *
     * @param paymentType 支付类型
     * @return 收款记录列表
     */
    @Operation(summary = "按支付类型查询收款记录", description = "根据支付类型查询收款记录列表")
    @GetMapping("/search/byPaymentType")
    public ResponseResult<List<Payment>> findByPaymentType(
            @Parameter(description = "支付类型", required = true) @RequestParam String paymentType) {
        return ResponseResult.success(paymentService.findByPaymentType(paymentType));
    }

    /**
     * 根据账单ID和支付类型查询收款记录列表
     *
     * @param billId      账单ID
     * @param paymentType 支付类型
     * @return 收款记录列表
     */
    @Operation(summary = "按账单ID和支付类型查询收款记录", description = "根据账单ID和支付类型联合查询收款记录列表")
    @GetMapping("/search/byBillIdAndPaymentType")
    public ResponseResult<List<Payment>> findByBillIdAndPaymentType(
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId,
            @Parameter(description = "支付类型", required = true) @RequestParam String paymentType) {
        return ResponseResult.success(paymentService.findByBillIdAndPaymentType(billId, paymentType));
    }
}