package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 员工信息控制层
 * <p>
 * 负责员工账号信息的增删改查及按用户名、酒店、角色、激活状态条件检索，对外提供 RESTful 接口。
 * </p>
 *
 * @author HotelManagementSystem
 */
@Tag(name = "员工管理", description = "员工信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 构造函数注入员工Service
     *
     * @param employeeService 员工Service
     */
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    @Operation(summary = "查询所有员工", description = "返回系统中所有员工的列表")
    @GetMapping
    public ResponseResult<List<Employee>> findAll() {
        return ResponseResult.success(employeeService.findAll());
    }

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 员工信息，不存在返回404
     */
    @Operation(summary = "根据ID查询员工", description = "根据员工ID查询单个员工详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Employee> findById(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id) {
        return employeeService.findById(id)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 新增员工
     *
     * @param employee 员工信息
     * @return 创建后的员工信息
     */
    @Operation(summary = "新增员工", description = "创建一个新的员工记录")
    @PostMapping
    public ResponseResult<Employee> create(
            @Parameter(description = "员工信息", required = true) @RequestBody Employee employee) {
        Employee saved = employeeService.save(employee);
        return ResponseResult.success("创建成功", saved);
    }

    /**
     * 更新员工信息
     *
     * @param id       员工ID
     * @param employee 员工信息
     * @return 更新后的员工信息，不存在返回404
     */
    @Operation(summary = "更新员工信息", description = "根据员工ID更新员工信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Employee> update(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id,
            @Parameter(description = "员工信息", required = true) @RequestBody Employee employee) {
        return employeeService.findById(id)
                .map(existing -> {
                    employee.setId(id);
                    return ResponseResult.success(employeeService.save(employee));
                })
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据ID删除员工
     *
     * @param id 员工ID
     * @return 删除结果
     */
    @Operation(summary = "删除员工", description = "根据员工ID删除员工记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id) {
        employeeService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    /**
     * 根据用户名查询员工
     *
     * @param username 用户名
     * @return 员工信息，不存在返回404
     */
    @Operation(summary = "按用户名查询员工", description = "根据用户名查询单个员工信息")
    @GetMapping("/search/byUsername")
    public ResponseResult<Employee> findByUsername(
            @Parameter(description = "用户名", required = true) @RequestParam String username) {
        return employeeService.findByUsername(username)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    /**
     * 根据酒店ID查询员工列表
     *
     * @param hotelId 酒店ID
     * @return 员工列表
     */
    @Operation(summary = "按酒店ID查询员工", description = "根据酒店ID查询该酒店下所有员工")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<Employee>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId) {
        return ResponseResult.success(employeeService.findByHotelId(hotelId));
    }

    /**
     * 根据角色查询员工列表
     *
     * @param role 角色
     * @return 员工列表
     */
    @Operation(summary = "按角色查询员工", description = "根据角色查询员工列表")
    @GetMapping("/search/byRole")
    public ResponseResult<List<Employee>> findByRole(
            @Parameter(description = "角色", required = true) @RequestParam String role) {
        return ResponseResult.success(employeeService.findByRole(role));
    }

    /**
     * 根据酒店ID和角色查询员工列表
     *
     * @param hotelId 酒店ID
     * @param role    角色
     * @return 员工列表
     */
    @Operation(summary = "按酒店ID和角色查询员工", description = "根据酒店ID和角色联合查询员工列表")
    @GetMapping("/search/byHotelIdAndRole")
    public ResponseResult<List<Employee>> findByHotelIdAndRole(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "角色", required = true) @RequestParam String role) {
        return ResponseResult.success(employeeService.findByHotelIdAndRole(hotelId, role));
    }

    /**
     * 根据酒店ID和激活状态查询员工列表
     *
     * @param hotelId  酒店ID
     * @param isActive 激活状态
     * @return 员工列表
     */
    @Operation(summary = "按酒店ID和激活状态查询员工", description = "根据酒店ID和激活状态联合查询员工列表")
    @GetMapping("/search/byHotelIdAndIsActive")
    public ResponseResult<List<Employee>> findByHotelIdAndIsActive(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "激活状态", required = true) @RequestParam Boolean isActive) {
        return ResponseResult.success(employeeService.findByHotelIdAndIsActive(hotelId, isActive));
    }
}