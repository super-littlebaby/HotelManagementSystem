package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.ConsumableItem;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.ConsumableItemService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 可消费项目控制层
 */
@Tag(name = "可消费项目管理", description = "可消费项目信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/consumable-items")
public class ConsumableItemController {

    private final ConsumableItemService consumableItemService;
    private final DataIsolationService dataIsolationService;

    public ConsumableItemController(ConsumableItemService consumableItemService,
                                    DataIsolationService dataIsolationService) {
        this.consumableItemService = consumableItemService;
        this.dataIsolationService = dataIsolationService;
    }

    private Employee getCurrentEmployee(HttpServletRequest request) {
        return (Employee) request.getAttribute("employee");
    }

    @Operation(summary = "查询所有可消费项目")
    @GetMapping
    public ResponseResult<List<ConsumableItem>> findAll(HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (dataIsolationService.isGroupAdmin(emp)) {
            return ResponseResult.success(consumableItemService.findAll());
        }
        Integer hotelId = dataIsolationService.getAccessibleHotelId(emp);
        return ResponseResult.success(consumableItemService.findByHotelId(hotelId));
    }

    @Operation(summary = "根据ID查询可消费项目")
    @GetMapping("/{id}")
    public ResponseResult<ConsumableItem> findById(
            @Parameter(description = "可消费项目ID") @PathVariable Integer id,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        return consumableItemService.findById(id)
                .map(item -> {
                    if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, item.getHotelId())) {
                        return ResponseResult.<ConsumableItem>error(403, "无权访问");
                    }
                    return ResponseResult.success(item);
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    @Operation(summary = "新增可消费项目")
    @PostMapping
    public ResponseResult<ConsumableItem> create(
            @RequestBody ConsumableItem consumableItem,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp)) {
            consumableItem.setHotelId(dataIsolationService.getAccessibleHotelId(emp));
        }
        ConsumableItem saved = consumableItemService.save(consumableItem);
        return ResponseResult.success("创建成功", saved);
    }

    @Operation(summary = "更新可消费项目信息")
    @PutMapping("/{id}")
    public ResponseResult<ConsumableItem> update(
            @PathVariable Integer id,
            @RequestBody ConsumableItem consumableItem,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        return consumableItemService.findById(id)
                .map(existing -> {
                    if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, existing.getHotelId())) {
                        return ResponseResult.<ConsumableItem>error(403, "无权修改");
                    }
                    consumableItem.setId(id);
                    return ResponseResult.success(consumableItemService.save(consumableItem));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    @Operation(summary = "根据ID删除可消费项目")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(@PathVariable Integer id, HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        consumableItemService.findById(id).ifPresent(item -> {
            if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, item.getHotelId())) {
                throw new RuntimeException("无权删除");
            }
        });
        consumableItemService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按酒店ID查询可消费项目")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<ConsumableItem>> findByHotelId(
            @RequestParam Integer hotelId,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, hotelId)) {
            return ResponseResult.error(403, "无权访问");
        }
        return ResponseResult.success(consumableItemService.findByHotelId(hotelId));
    }

    @Operation(summary = "按分类查询可消费项目")
    @GetMapping("/search/byCategory")
    public ResponseResult<List<ConsumableItem>> findByCategory(@RequestParam String category,
                                                               HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(emp);
            return ResponseResult.success(consumableItemService.findByHotelIdAndCategory(hotelId, category));
        }
        return ResponseResult.success(consumableItemService.findByCategory(category));
    }

    @Operation(summary = "按激活状态查询可消费项目")
    @GetMapping("/search/byIsActive")
    public ResponseResult<List<ConsumableItem>> findByIsActive(@RequestParam Boolean isActive,
                                                               HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(emp);
            return ResponseResult.success(consumableItemService.findByHotelIdAndIsActive(hotelId, isActive));
        }
        return ResponseResult.success(consumableItemService.findByIsActive(isActive));
    }

    @Operation(summary = "按酒店ID和分类查询")
    @GetMapping("/search/byHotelIdAndCategory")
    public ResponseResult<List<ConsumableItem>> findByHotelIdAndCategory(
            @RequestParam Integer hotelId, @RequestParam String category,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, hotelId)) {
            return ResponseResult.error(403, "无权访问");
        }
        return ResponseResult.success(consumableItemService.findByHotelIdAndCategory(hotelId, category));
    }

    @Operation(summary = "按酒店ID和激活状态查询")
    @GetMapping("/search/byHotelIdAndIsActive")
    public ResponseResult<List<ConsumableItem>> findByHotelIdAndIsActive(
            @RequestParam Integer hotelId, @RequestParam Boolean isActive,
            HttpServletRequest request) {
        Employee emp = getCurrentEmployee(request);
        if (emp == null) return ResponseResult.error(401, "未登录");
        if (!dataIsolationService.isGroupAdmin(emp) && !dataIsolationService.canAccessHotel(emp, hotelId)) {
            return ResponseResult.error(403, "无权访问");
        }
        return ResponseResult.success(consumableItemService.findByHotelIdAndIsActive(hotelId, isActive));
    }
}