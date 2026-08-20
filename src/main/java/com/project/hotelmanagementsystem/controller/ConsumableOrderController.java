package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.entity.*;
import com.project.hotelmanagementsystem.repository.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 消费下单控制器
 * 前台为在住客人添加消费项目到账单
 */
@RestController
@RequestMapping("/api/consumable-orders")
@CrossOrigin(origins = "*")
public class ConsumableOrderController {

    private final CheckInRepository checkInRepository;
    private final BillRepository billRepository;
    private final BillItemRepository billItemRepository;
    private final RoomRepository roomRepository;
    private final ConsumableItemRepository consumableItemRepository;
    private final EmployeeRepository employeeRepository;

    public ConsumableOrderController(CheckInRepository checkInRepository,
                                      BillRepository billRepository,
                                      BillItemRepository billItemRepository,
                                      RoomRepository roomRepository,
                                      ConsumableItemRepository consumableItemRepository,
                                      EmployeeRepository employeeRepository) {
        this.checkInRepository = checkInRepository;
        this.billRepository = billRepository;
        this.billItemRepository = billItemRepository;
        this.roomRepository = roomRepository;
        this.consumableItemRepository = consumableItemRepository;
        this.employeeRepository = employeeRepository;
    }

    /**
     * 根据房间号查询在住信息
     */
    @GetMapping("/check-in-info")
    public ResponseEntity<?> getCheckInInfo(@RequestParam String roomNumber,
                                             HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取当前员工
            Employee employee = (Employee) request.getAttribute("employee");
            if (employee == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return ResponseEntity.ok(result);
            }

            // 根据房间号查找房间
            Room room = roomRepository.findByRoomNumber(roomNumber).orElse(null);
            if (room == null) {
                result.put("code", 404);
                result.put("message", "房间不存在");
                return ResponseEntity.ok(result);
            }

            // 数据隔离检查
            if (employee.getHotelId() != null && !employee.getHotelId().equals(room.getHotelId())) {
                result.put("code", 403);
                result.put("message", "无权操作该酒店的房间");
                return ResponseEntity.ok(result);
            }

            // 查找在住记录（状态为 in_house）
            CheckIn checkIn = checkInRepository.findByRoomIdAndStatus(room.getId(), "in_house")
                    .stream().findFirst().orElse(null);

            if (checkIn == null) {
                result.put("code", 404);
                result.put("message", "该房间当前没有在住客人");
                return ResponseEntity.ok(result);
            }

            // 查找账单
            Bill bill = billRepository.findByCheckInIdAndBillStatus(checkIn.getId(), "open")
                    .stream().findFirst().orElse(null);

            if (bill == null) {
                // 如果没有账单，创建一个
                bill = new Bill();
                bill.setCheckInId(checkIn.getId());
                bill.setBillStatus("open");
                bill.setTotalAmount(BigDecimal.ZERO);
                bill.setPaidAmount(BigDecimal.ZERO);
                bill.setDepositAmount(BigDecimal.ZERO);
                bill.setCreatedAt(java.time.LocalDateTime.now());
                bill = billRepository.save(bill);
            }

            // 返回信息
            Map<String, Object> data = new HashMap<>();
            data.put("checkInId", checkIn.getId());
            data.put("guestName", checkIn.getGuestName());
            data.put("roomNumber", room.getRoomNumber());
            data.put("billId", bill.getId());
            data.put("totalAmount", bill.getTotalAmount());
            data.put("depositAmount", bill.getDepositAmount());

            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 添加消费项目到账单
     */
    @PostMapping("/add")
    public ResponseEntity<?> addConsumableToBill(@RequestBody Map<String, Object> params,
                                                   HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            // 获取当前员工
            Employee employee = (Employee) request.getAttribute("employee");
            if (employee == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return ResponseEntity.ok(result);
            }

            Integer checkInId = (Integer) params.get("checkInId");
            Integer consumableId = (Integer) params.get("consumableId");
            BigDecimal quantity = new BigDecimal(params.get("quantity").toString());
            String description = (String) params.get("description");

            // 验证参数
            if (checkInId == null || consumableId == null || quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
                result.put("code", 400);
                result.put("message", "参数不完整");
                return ResponseEntity.ok(result);
            }

            // 查找入住记录
            CheckIn checkIn = checkInRepository.findById(checkInId).orElse(null);
            if (checkIn == null) {
                result.put("code", 404);
                result.put("message", "入住记录不存在");
                return ResponseEntity.ok(result);
            }

            // 数据隔离检查
            if (employee.getHotelId() != null && !employee.getHotelId().equals(checkIn.getHotelId())) {
                result.put("code", 403);
                result.put("message", "无权操作该酒店的账单");
                return ResponseEntity.ok(result);
            }

            // 查找消费项目
            ConsumableItem consumable = consumableItemRepository.findById(consumableId).orElse(null);
            if (consumable == null) {
                result.put("code", 404);
                result.put("message", "消费项目不存在");
                return ResponseEntity.ok(result);
            }

            if (!consumable.getIsActive()) {
                result.put("code", 400);
                result.put("message", "该消费项目已停用");
                return ResponseEntity.ok(result);
            }

            // 查找或创建账单
            Bill bill = billRepository.findByCheckInIdAndBillStatus(checkInId, "open")
                    .stream().findFirst().orElse(null);

            if (bill == null) {
                bill = new Bill();
                bill.setCheckInId(checkInId);
                bill.setBillStatus("open");
                bill.setTotalAmount(BigDecimal.ZERO);
                bill.setPaidAmount(BigDecimal.ZERO);
                bill.setDepositAmount(BigDecimal.ZERO);
                bill.setCreatedAt(java.time.LocalDateTime.now());
                bill = billRepository.save(bill);
            }

            // 创建账单明细
            BillItem billItem = new BillItem();
            billItem.setBillId(bill.getId());
            billItem.setItemType(consumable.getCategory());
            billItem.setConsumableId(consumableId);
            billItem.setQuantity(quantity);
            billItem.setUnitPrice(consumable.getPrice());
            billItem.setAmount(consumable.getPrice().multiply(quantity));
            billItem.setChargeDate(LocalDate.now());
            billItem.setEmployeeId(employee.getId());
            billItem.setDescription(description != null ? description : consumable.getName());

            BillItem savedItem = billItemRepository.save(billItem);

            // 更新账单总金额
            BigDecimal newTotal = bill.getTotalAmount().add(savedItem.getAmount());
            bill.setTotalAmount(newTotal);
            billRepository.save(bill);

            result.put("code", 200);
            result.put("message", "添加成功");

            Map<String, Object> data = new HashMap<>();
            data.put("billItemId", savedItem.getId());
            data.put("billId", bill.getId());
            data.put("totalAmount", newTotal);
            data.put("itemName", consumable.getName());
            data.put("itemPrice", consumable.getPrice());
            data.put("quantity", quantity);
            data.put("amount", savedItem.getAmount());
            result.put("data", data);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "添加失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 查询消费项目列表（按酒店筛选）
     */
    @GetMapping("/consumable-items")
    public ResponseEntity<?> getConsumableItems(HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        try {
            Employee employee = (Employee) request.getAttribute("employee");
            if (employee == null) {
                result.put("code", 401);
                result.put("message", "未登录");
                return ResponseEntity.ok(result);
            }

            java.util.List<ConsumableItem> items;
            if (employee.getHotelId() != null) {
                items = consumableItemRepository.findByHotelIdAndIsActive(employee.getHotelId(), true);
            } else {
                items = consumableItemRepository.findByIsActive(true);
            }

            result.put("code", 200);
            result.put("message", "查询成功");
            result.put("data", items);

        } catch (Exception e) {
            result.put("code", 500);
            result.put("message", "查询失败：" + e.getMessage());
        }

        return ResponseEntity.ok(result);
    }
}
