package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.service.AuthService;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "员工管理", description = "员工信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DataIsolationService dataIsolationService;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    public EmployeeController(EmployeeService employeeService, DataIsolationService dataIsolationService, 
                              AuthService authService, PasswordEncoder passwordEncoder) {
        this.employeeService = employeeService;
        this.dataIsolationService = dataIsolationService;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "查询所有员工", description = "返回系统中所有员工的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<Map<String, Object>>> findAll(HttpServletRequest request) {
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        List<Employee> employees;
        if (dataIsolationService.isGroupAdmin(currentEmployee)) {
            employees = employeeService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(currentEmployee);
            employees = employeeService.findByHotelId(hotelId);
        }
        return ResponseResult.success(employees.stream()
                .map(this::maskEmployeeInfo)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "根据ID查询员工", description = "根据员工ID查询单个员工详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> findById(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Employee> employeeOpt = employeeService.findById(id);
        if (employeeOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = employeeOpt.get();
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee) && 
            !dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
            return ResponseResult.error(403, "无权访问该员工信息");
        }
        return ResponseResult.success(maskEmployeeInfo(employee));
    }

    @Operation(summary = "新增员工", description = "创建一个新的员工记录")
    @PostMapping
    public ResponseResult<Map<String, Object>> create(
            @Parameter(description = "员工信息", required = true) @Valid @RequestBody Employee employee,
            HttpServletRequest request) {
        if (employee.getPasswordHash() == null || employee.getPasswordHash().isBlank()) {
            return ResponseResult.error(400, "密码不能为空");
        }
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee)) {
            if (!dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
                return ResponseResult.error(403, "无权创建该酒店的员工");
            }
            if ("admin".equals(employee.getRole())) {
                return ResponseResult.error(403, "无权创建管理员角色的员工");
            }
        }
        // 入职时间默认取创建当天（服务端时间）
        if (employee.getHireDate() == null) {
            employee.setHireDate(java.time.LocalDate.now());
        }
        Employee saved = employeeService.save(employee);
        return ResponseResult.success("创建成功", maskEmployeeInfo(saved));
    }

    @Operation(summary = "更新员工信息", description = "根据员工ID更新员工信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> update(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id,
            @Parameter(description = "员工信息", required = true) @Valid @RequestBody Employee employee,
            HttpServletRequest request) {
        java.util.Optional<Employee> existingOpt = employeeService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee existing = existingOpt.get();
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee)) {
            if (!dataIsolationService.canAccessHotel(currentEmployee, existing.getHotelId())) {
                return ResponseResult.error(403, "无权更新该员工信息");
            }
            if (!dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
                return ResponseResult.error(403, "无权将员工分配到该酒店");
            }
            if ("admin".equals(employee.getRole()) && !"admin".equals(existing.getRole())) {
                return ResponseResult.error(403, "无权将员工升级为管理员");
            }
            if ("admin".equals(existing.getRole())) {
                return ResponseResult.error(403, "无权修改管理员信息");
            }
            String currentRole = currentEmployee.getRole();
            String targetRole = existing.getRole();
            if (!"manager".equals(currentRole) || !isLowerRole(targetRole, "manager")) {
                if (!"manager".equals(currentRole) || "manager".equals(targetRole)) {
                    return ResponseResult.error(403, "无权修改同级或更高权限的员工");
                }
            }
        }
        if ("admin".equals(existing.getRole()) && employee.getIsActive() != null && !employee.getIsActive()) {
            return ResponseResult.error(403, "管理员账号状态不可修改为离职");
        }
        if (employee.getPasswordHash() == null || employee.getPasswordHash().isEmpty()) {
            employee.setPasswordHash(existing.getPasswordHash());
        }
        if (employee.getIsActive() == null) {
            employee.setIsActive(existing.getIsActive());
        }
        if (employee.getHireDate() == null) {
            employee.setHireDate(existing.getHireDate());
        }
        employee.setId(id);
        return ResponseResult.success(maskEmployeeInfo(employeeService.save(employee)));
    }

    @Operation(summary = "修改员工状态", description = "切换员工在职/离职状态，管理员账号状态不可修改为离职")
    @PatchMapping("/{id}/status")
    public ResponseResult<Map<String, Object>> toggleStatus(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id,
            @Parameter(description = "目标状态：true=在职，false=离职", required = true) @RequestParam Boolean isActive,
            HttpServletRequest request) {
        java.util.Optional<Employee> employeeOpt = employeeService.findById(id);
        if (employeeOpt.isEmpty()) {
            return ResponseResult.error(404, "员工不存在");
        }
        Employee employee = employeeOpt.get();
        Employee currentEmployee = (Employee) request.getAttribute("employee");

        if (!dataIsolationService.isGroupAdmin(currentEmployee)) {
            if (!dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
                return ResponseResult.error(403, "无权修改该员工状态");
            }
            if ("admin".equals(employee.getRole())) {
                return ResponseResult.error(403, "无权修改管理员状态");
            }
            String currentRole = currentEmployee.getRole();
            String targetRole = employee.getRole();
            if (!"manager".equals(currentRole) || !isLowerRole(targetRole, "manager")) {
                if (!"manager".equals(currentRole) || "manager".equals(targetRole)) {
                    return ResponseResult.error(403, "无权修改同级或更高权限员工的状态");
                }
            }
        }

        if ("admin".equals(employee.getRole()) && !isActive) {
            return ResponseResult.error(403, "管理员账号状态不可修改为离职");
        }

        employee.setIsActive(isActive);
        Employee saved = employeeService.save(employee);
        return ResponseResult.success(isActive ? "已设置为在职" : "已设置为离职", maskEmployeeInfo(saved));
    }

    @Operation(summary = "删除员工", description = "根据员工ID删除员工记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "员工ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Employee> employeeOpt = employeeService.findById(id);
        if (employeeOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = employeeOpt.get();
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee)) {
            if (!dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
                return ResponseResult.error(403, "无权删除该员工");
            }
            if ("admin".equals(employee.getRole())) {
                return ResponseResult.error(403, "无权删除管理员");
            }
            String currentRole = currentEmployee.getRole();
            String targetRole = employee.getRole();
            if (!"manager".equals(currentRole) || !isLowerRole(targetRole, "manager")) {
                if (!"manager".equals(currentRole) || "manager".equals(targetRole)) {
                    return ResponseResult.error(403, "无权删除同级或更高权限的员工");
                }
            }
        }
        employeeService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按用户名查询员工", description = "根据用户名查询单个员工信息")
    @GetMapping("/search/byUsername")
    public ResponseResult<Map<String, Object>> findByUsername(
            @Parameter(description = "用户名", required = true) @RequestParam String username,
            HttpServletRequest request) {
        java.util.Optional<Employee> employeeOpt = employeeService.findByUsername(username);
        if (employeeOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Employee employee = employeeOpt.get();
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee) && 
            !dataIsolationService.canAccessHotel(currentEmployee, employee.getHotelId())) {
            return ResponseResult.error(403, "无权访问该员工信息");
        }
        return ResponseResult.success(maskEmployeeInfo(employee));
    }

    @Operation(summary = "按酒店ID查询员工", description = "根据酒店ID查询该酒店下所有员工")
    @GetMapping("/search/byHotelId")
    public ResponseResult<List<Map<String, Object>>> findByHotelId(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            HttpServletRequest request) {
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(currentEmployee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的员工信息");
        }
        return ResponseResult.success(employeeService.findByHotelId(hotelId).stream()
                .map(this::maskEmployeeInfo)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "按角色查询员工", description = "根据角色查询员工列表")
    @GetMapping("/search/byRole")
    public ResponseResult<List<Map<String, Object>>> findByRole(
            @Parameter(description = "角色", required = true) @RequestParam String role,
            HttpServletRequest request) {
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(currentEmployee) && "admin".equals(role)) {
            return ResponseResult.error(403, "无权查询管理员角色的员工");
        }
        List<Employee> employees = employeeService.findByRole(role);
        if (!dataIsolationService.isGroupAdmin(currentEmployee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(currentEmployee);
            employees = employees.stream().filter(e -> e.getHotelId() != null && e.getHotelId().equals(hotelId)).collect(Collectors.toList());
        }
        return ResponseResult.success(employees.stream()
                .map(this::maskEmployeeInfo)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "按酒店ID和角色查询员工", description = "根据酒店ID和角色联合查询员工列表")
    @GetMapping("/search/byHotelIdAndRole")
    public ResponseResult<List<Map<String, Object>>> findByHotelIdAndRole(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "角色", required = true) @RequestParam String role,
            HttpServletRequest request) {
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(currentEmployee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的员工信息");
        }
        return ResponseResult.success(employeeService.findByHotelIdAndRole(hotelId, role).stream()
                .map(this::maskEmployeeInfo)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "按酒店ID和激活状态查询员工", description = "根据酒店ID和激活状态联合查询员工列表")
    @GetMapping("/search/byHotelIdAndIsActive")
    public ResponseResult<List<Map<String, Object>>> findByHotelIdAndIsActive(
            @Parameter(description = "酒店ID", required = true) @RequestParam Integer hotelId,
            @Parameter(description = "激活状态", required = true) @RequestParam Boolean isActive,
            HttpServletRequest request) {
        Employee currentEmployee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(currentEmployee, hotelId)) {
            return ResponseResult.error(403, "无权访问该酒店的员工信息");
        }
        return ResponseResult.success(employeeService.findByHotelIdAndIsActive(hotelId, isActive).stream()
                .map(this::maskEmployeeInfo)
                .collect(Collectors.toList()));
    }

    @Operation(summary = "更新个人资料", description = "员工自助更新个人资料（仅允许修改姓名、电话、邮箱）")
    @PutMapping("/profile")
    public ResponseResult<Map<String, Object>> updateProfile(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        Employee currentEmployee = (Employee) httpRequest.getAttribute("employee");
        if (currentEmployee == null) {
            return ResponseResult.error(401, "未登录");
        }

        String firstName = request.get("firstName");
        String lastName = request.get("lastName");
        String phone = request.get("phone");
        String email = request.get("email");

        if (firstName != null) currentEmployee.setFirstName(firstName);
        if (lastName != null) currentEmployee.setLastName(lastName);
        if (phone != null) currentEmployee.setPhone(phone);
        if (email != null) currentEmployee.setEmail(email);

        Employee saved = employeeService.save(currentEmployee);
        authService.storeToken((String) httpRequest.getAttribute("token"), saved);
        return ResponseResult.success("更新成功", maskEmployeeInfo(saved));
    }

    @Operation(summary = "修改密码", description = "员工修改个人登录密码")
    @PutMapping("/password")
    public ResponseResult<Map<String, Object>> changePassword(
            @RequestBody Map<String, String> request,
            HttpServletRequest httpRequest) {
        Employee currentEmployee = (Employee) httpRequest.getAttribute("employee");
        if (currentEmployee == null) {
            return ResponseResult.error(401, "未登录");
        }

        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || newPassword == null) {
            return ResponseResult.error(400, "密码不能为空");
        }

        java.util.Optional<Employee> employeeOpt = employeeService.findById(currentEmployee.getId());
        if (employeeOpt.isEmpty()) {
            return ResponseResult.error(404, "员工不存在");
        }

        Employee employee = employeeOpt.get();
        String storedPassword = employee.getPasswordHash();
        boolean isEncrypted = storedPassword != null &&
            (storedPassword.startsWith("$2a$") ||
             storedPassword.startsWith("$2b$") ||
             storedPassword.startsWith("$2y$"));

        boolean passwordMatch;
        if (isEncrypted) {
            passwordMatch = passwordEncoder.matches(currentPassword, storedPassword);
        } else {
            passwordMatch = currentPassword.equals(storedPassword);
        }

        if (!passwordMatch) {
            return ResponseResult.error(400, "当前密码不正确");
        }

        employee.setPasswordHash(newPassword);
        Employee saved = employeeService.save(employee);
        authService.storeToken((String) httpRequest.getAttribute("token"), saved);
        return ResponseResult.success("密码修改成功", maskEmployeeInfo(saved));
    }

    @Operation(summary = "员工登录", description = "员工通过用户名和密码登录系统")
    @PostMapping("/login")
    public ResponseResult<Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        java.util.Optional<Employee> existingOpt = employeeService.findByUsername(username);
        if (existingOpt.isEmpty()) {
            return ResponseResult.error(401, "用户名或密码错误");
        }
        Employee existing = existingOpt.get();
        if (existing.getIsActive() == null || !existing.getIsActive()) {
            return ResponseResult.error(403, "该账号已离职，禁止登录系统");
        }

        java.util.Optional<Employee> employeeOpt = employeeService.login(username, password);
        if (employeeOpt.isPresent()) {
            Employee employee = employeeOpt.get();
            String token = UUID.randomUUID().toString();
            authService.storeToken(token, employee);
            Map<String, Object> result = new HashMap<>();
            result.put("token", token);
            Map<String, Object> employeeInfo = new HashMap<>();
            employeeInfo.put("id", employee.getId());
            employeeInfo.put("username", employee.getUsername());
            employeeInfo.put("firstName", employee.getFirstName());
            employeeInfo.put("lastName", employee.getLastName());
            employeeInfo.put("email", employee.getEmail());
            employeeInfo.put("phone", employee.getPhone());
            employeeInfo.put("role", employee.getRole());
            employeeInfo.put("hotelId", employee.getHotelId());
            employeeInfo.put("isActive", employee.getIsActive());
            employeeInfo.put("hireDate", employee.getHireDate());
            result.put("employee", employeeInfo);
            return ResponseResult.success("登录成功", result);
        } else {
            return ResponseResult.error(401, "用户名或密码错误");
        }
    }

    @Operation(summary = "获取员工信息", description = "获取当前登录员工的详细信息")
    @GetMapping("/profile")
    public ResponseResult<Map<String, Object>> getProfile(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        return ResponseResult.success("success", maskEmployeeInfo(employee));
    }
    
    private boolean isLowerRole(String targetRole, String currentRole) {
        int targetLevel = getRoleLevel(targetRole);
        int currentLevel = getRoleLevel(currentRole);
        return targetLevel > currentLevel;
    }

    private int getRoleLevel(String role) {
        return switch (role) {
            case "admin" -> 0;
            case "manager" -> 1;
            case "front_desk" -> 2;
            case "housekeeping" -> 3;
            case "finance" -> 3;
            default -> 4;
        };
    }

    private Map<String, Object> maskEmployeeInfo(Employee employee) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", employee.getId());
        result.put("username", employee.getUsername());
        result.put("firstName", employee.getFirstName());
        result.put("lastName", employee.getLastName());
        result.put("email", employee.getEmail());
        result.put("phone", employee.getPhone());
        result.put("role", employee.getRole());
        result.put("hotelId", employee.getHotelId());
        result.put("isActive", employee.getIsActive());
        result.put("hireDate", employee.getHireDate());
        return result;
    }
}