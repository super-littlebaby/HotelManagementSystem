package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Hotel;
import com.project.hotelmanagementsystem.entity.RoomType;
import com.project.hotelmanagementsystem.repository.RoomTypeRepository;
import com.project.hotelmanagementsystem.service.DataIsolationService;
import com.project.hotelmanagementsystem.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "酒店管理", description = "酒店基础信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/hotels")
public class HotelController {

    private final HotelService hotelService;
    private final DataIsolationService dataIsolationService;
    private final RoomTypeRepository roomTypeRepository;

    public HotelController(HotelService hotelService, DataIsolationService dataIsolationService, RoomTypeRepository roomTypeRepository) {
        this.hotelService = hotelService;
        this.dataIsolationService = dataIsolationService;
        this.roomTypeRepository = roomTypeRepository;
    }

    private void fillMinPrice(List<Hotel> hotels) {
        if (hotels.isEmpty()) return;
        for (Hotel hotel : hotels) {
            List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotel.getId());
            hotel.setMinPrice(roomTypes.stream()
                    .map(RoomType::getBasePrice)
                    .reduce(BigDecimal::min).orElse(null));
        }
    }

    private void fillMinPrice(Hotel hotel) {
        if (hotel == null) return;
        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotel.getId());
        hotel.setMinPrice(roomTypes.stream()
                .map(RoomType::getBasePrice)
                .reduce(BigDecimal::min).orElse(null));
    }

    @Operation(summary = "查询所有酒店", description = "返回系统中所有酒店的列表，根据员工权限过滤")
    @GetMapping
    public ResponseResult<List<Hotel>> findAll(HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Hotel> hotels;
        if (employee == null) {
            hotels = hotelService.findAll();
        } else if (dataIsolationService.isGroupAdmin(employee)) {
            hotels = hotelService.findAll();
        } else {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (hotelId != null) {
                hotels = hotelService.findById(hotelId)
                        .map(List::of)
                        .orElse(List.of());
            } else {
                hotels = hotelService.findAll();
            }
        }
        fillMinPrice(hotels);
        return ResponseResult.success(hotels);
    }

    @Operation(summary = "根据ID查询酒店", description = "根据酒店ID查询单个酒店详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Hotel> findById(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Hotel> hotelOpt = hotelService.findById(id);
        if (hotelOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Hotel hotel = hotelOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.canAccessHotel(employee, hotel.getId())) {
            return ResponseResult.error(403, "无权访问该酒店信息");
        }
        fillMinPrice(hotel);
        return ResponseResult.success(hotel);
    }

    @Operation(summary = "新增酒店", description = "创建一个新的酒店记录")
    @PostMapping
    public ResponseResult<Hotel> create(
            @Parameter(description = "酒店信息", required = true) @RequestBody Hotel hotel,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            return ResponseResult.error(403, "无权创建酒店");
        }
        Hotel saved = hotelService.save(hotel);
        return ResponseResult.success("创建成功", saved);
    }

    @Operation(summary = "更新酒店信息", description = "根据酒店ID更新酒店信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Hotel> update(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id,
            @Parameter(description = "酒店信息", required = true) @RequestBody Hotel hotel,
            HttpServletRequest request) {
        java.util.Optional<Hotel> existingOpt = hotelService.findById(id);
        if (existingOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Hotel existing = existingOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            if (employee == null || !"manager".equals(employee.getRole())) {
                return ResponseResult.error(403, "只有管理员和经理可以修改酒店信息");
            }
            if (!dataIsolationService.canAccessHotel(employee, existing.getId())) {
                return ResponseResult.error(403, "无权更新其他酒店的信息");
            }
        }
        hotel.setId(id);
        return ResponseResult.success(hotelService.save(hotel));
    }

    @Operation(summary = "删除酒店", description = "根据酒店ID删除酒店记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "酒店ID", required = true) @PathVariable Integer id,
            HttpServletRequest request) {
        java.util.Optional<Hotel> hotelOpt = hotelService.findById(id);
        if (hotelOpt.isEmpty()) {
            return ResponseResult.error(404, "资源不存在");
        }
        Hotel hotel = hotelOpt.get();
        Employee employee = (Employee) request.getAttribute("employee");
        if (!dataIsolationService.isGroupAdmin(employee)) {
            return ResponseResult.error(403, "无权删除酒店");
        }
        hotelService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按名称模糊查询酒店", description = "根据酒店名称关键字模糊查询酒店列表")
    @GetMapping("/search/byName")
    public ResponseResult<List<Hotel>> findByNameContaining(
            @Parameter(description = "酒店名称关键字", required = true) @RequestParam String name,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Hotel> hotels = hotelService.findByNameContaining(name);
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (hotelId != null) {
                hotels = hotels.stream().filter(h -> h.getId().equals(hotelId)).toList();
            }
        }
        fillMinPrice(hotels);
        return ResponseResult.success(hotels);
    }

    @Operation(summary = "按地址模糊查询酒店", description = "根据酒店地址关键字模糊查询酒店列表")
    @GetMapping("/search/byAddress")
    public ResponseResult<List<Hotel>> findByAddressContaining(
            @Parameter(description = "酒店地址关键字", required = true) @RequestParam String address,
            HttpServletRequest request) {
        Employee employee = (Employee) request.getAttribute("employee");
        List<Hotel> hotels = hotelService.findByAddressContaining(address);
        if (employee != null && !dataIsolationService.isGroupAdmin(employee)) {
            Integer hotelId = dataIsolationService.getAccessibleHotelId(employee);
            if (hotelId != null) {
                hotels = hotels.stream().filter(h -> h.getId().equals(hotelId)).toList();
            }
        }
        fillMinPrice(hotels);
        return ResponseResult.success(hotels);
    }
}