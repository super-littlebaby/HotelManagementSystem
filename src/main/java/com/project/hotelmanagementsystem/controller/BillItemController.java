package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.BillItem;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.repository.BillItemRepository;
import com.project.hotelmanagementsystem.service.BillItemService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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
    private final BillItemRepository billItemRepository;
    private final DataIsolationService dataIsolationService;

    /**
     * 构造函数注入账单明细Service
     *
     * @param billItemService       账单明细Service
     * @param billItemRepository    账单明细Repository
     * @param dataIsolationService  数据隔离Service
     */
    public BillItemController(BillItemService billItemService, BillItemRepository billItemRepository, DataIsolationService dataIsolationService) {
        this.billItemService = billItemService;
        this.billItemRepository = billItemRepository;
        this.dataIsolationService = dataIsolationService;
    }

    /**
     * 查询所有账单明细
     *
     * @return 账单明细列表
     */
    @Operation(summary = "查询所有账单明细", description = "返回系统中所有账单明细记录的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<BillItem>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<BillItem> billItems;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            billItems = billItemService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            billItems = billItemRepository.findAllByHotelId(hotelId);
        }
        return ResponseResult.success(billItems);
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
            @Parameter(description = "账单明细ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<BillItem> optional = billItemService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        BillItem billItem = optional.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billItemRepository.findHotelIdByBillItemId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问该账单明细");
            }
        }
        return ResponseResult.success(billItem);
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
            @Parameter(description = "账单明细信息", required = true) @RequestBody BillItem billItem,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billItemRepository.findHotelIdByBillItemId(billItem.getBillId());
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权访问其他酒店的账单");
            }
        }
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
            @Parameter(description = "账单明细信息", required = true) @RequestBody BillItem billItem,
            HttpServletRequest request) {
        java.util.Optional<BillItem> optional = billItemService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billItemRepository.findHotelIdByBillItemId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权更新其他酒店的账单明细");
            }
        }
        billItem.setId(id);
        return ResponseResult.success(billItemService.save(billItem));
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
            @Parameter(description = "账单明细ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<BillItem> optional = billItemService.findById(id);
        if (optional.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = (Employee) request.getAttribute("employee");
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            java.util.Optional<Integer> hotelIdOpt = billItemRepository.findHotelIdByBillItemId(id);
            if (hotelIdOpt.isPresent() && !dataIsolationService.canAccessHotel(employee, hotelIdOpt.get())) {
                return ResponseResult.error(403, "无权删除其他酒店的账单明细");
            }
        }
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
            @Parameter(description = "账单ID", required = true) @RequestParam Integer billId,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<BillItem> billItems;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            billItems = billItemService.findByBillId(billId);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            billItems = billItemRepository.findByBillIdAndHotelId(billId, hotelId);
        }
        return ResponseResult.success(billItems);
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
            @Parameter(description = "项目类型", required = true) @RequestParam String itemType,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<BillItem> billItems;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            billItems = billItemService.findByItemType(itemType);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            billItems = billItemRepository.findByItemTypeAndHotelId(itemType, hotelId);
        }
        return ResponseResult.success(billItems);
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
            @Parameter(description = "项目类型", required = true) @RequestParam String itemType,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<BillItem> billItems;
        if (employee == null || dataIsolationService.isGroupAdmin(employee)) {
            billItems = billItemService.findByBillIdAndItemType(billId, itemType);
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            billItems = billItemRepository.findByBillIdAndItemTypeAndHotelId(billId, itemType, hotelId);
        }
        return ResponseResult.success(billItems);
    }
}
