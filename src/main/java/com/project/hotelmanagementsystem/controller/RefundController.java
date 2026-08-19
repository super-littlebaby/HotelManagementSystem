package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Refund;
import com.project.hotelmanagementsystem.repository.RefundRepository;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 退款记录控制层
 * <p>
 * 负责账单退款记录的增删改查及按账单、退款方式条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "退款记录管理", description = "退款记录信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/refunds")
public class RefundController {

    private final RefundService refundService;
    private final RefundRepository refundRepository;
    private final DataIsolationService dataIsolationService;

    /**
     * 构造函数注入退款记录Service
     *
     * @param refundService         退款记录Service
     * @param refundRepository      退款记录Repository
     * @param dataIsolationService  数据隔离Service
     */
    public RefundController(RefundService refundService, RefundRepository refundRepository, DataIsolationService dataIsolationService) {
        this.refundService = refundService;
        this.refundRepository = refundRepository;
        this.dataIsolationService = dataIsolationService;
    }

    /**
     * 查询所有退款记录
     *
     * @return 退款记录列表
     */
    @Operation(summary = "查询所有退款记录", description = "返回系统中所有退款记录的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<Refund>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Refund> refunds;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            refunds = refundService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            refunds = refundRepository.findAllByHotelId(hotelId);
        }
        return ResponseResult.success(refunds);
    }

    /**
     * 根据ID查询退款记录
     *
     * @param id 退款记录ID
     * @return 退款记录信息，不存在返回404
     */
    @Operation(summary = "根据ID查询退款记录", description = "根据退款记录ID查询单条退款记录详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Refund> findById(
            @Parameter(description = "退款记录ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Refund> optional = refundService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Refund refund = optional.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = refundRepository.findHotelIdByRefundId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问该退款记录");
            }
        }
        return ResponseResult.success(refund);
    }

    /**
     * 新增退款记录
     *
     * @param refund 退款记录信息
     * @return 创建后的退款记录信息
     */
    @Operation(summary = "新增退款记录", description = "创建一条新的退款记录")
    @PostMapping
    public ResponseResult<Refund> create(
            @Parameter(description = "退款记录信息", required = true) @RequestBody Refund refund,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = refundRepository.findHotelIdByRefundId(refund.getBillId());
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问其他酒店的账单");
            }
        }
        Refund saved = refundService.save(refund);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新退款记录信息
     *
     * @param id     退款记录ID
     * @param refund 退款记录信息
     * @return 更新后的退款记录信息，不存在返回404
     */
    @Operation(summary = "更新退款记录信息", description = "根据退款记录ID更新信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Refund> update(
            @Parameter(description = "退款记录ID", required = true) @PathVariable Integer id,
            @Parameter(description = "退款记录信息", required = true) @RequestBody Refund refund,
            HttpServletRequest request) {
        java.util.Optional<Refund> optional = refundService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = refundRepository.findHotelIdByRefundId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权更新其他酒店的退款记录");
            }
        }
        refund.setId(id);
        return ResponseResult.success(refundService.save(refund));
    }

    /**
     * 根据ID删除退款记录
     *
     * @param id 退款记录ID
     * @return 删除结果
     */
    @Operation(summary = "删除退款记录", description = "根据退款记录ID删除退款记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "退款记录ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Refund> optional = refundService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = refundRepository.findHotelIdByRefundId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权删除其他酒店的退款记录");
            }
        }
        refundService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据账单ID查询退款记录列表
     *
     * @param billId 账单ID
     * @return 退款记录列表
     */
    @Operation(summary = "按账单ID查询退款记录", description = "根据账单ID查询该账单下所有退款记录")
    @GetMapping("/search/byBillId")
    public ResponseResult<List<Refund>> findByBillId(
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Refund> refunds;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            refunds = refundService.findByBillId(billId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            refunds = refundRepository.findByBillIdAndHotelId(billId, hotelId);
        }
        return ResponseResult.success(refunds);
    }

    /**
     * 根据退款方式查询退款记录列表
     *
     * @param refundMethod 退款方式
     * @return 退款记录列表
     */
    @Operation(summary = "按退款方式查询退款记录", description = "根据退款方式查询退款记录列表")
    @GetMapping("/search/byRefundMethod")
    public ResponseResult<List<Refund>> findByRefundMethod(
            @Parameter(description = "退款方式", required = true) @RequestParam String refundMethod,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Refund> refunds;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            refunds = refundService.findByRefundMethod(refundMethod);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            refunds = refundRepository.findByRefundMethodAndHotelId(refundMethod, hotelId);
        }
        return ResponseResult.success(refunds);
    }
}
