package com.project.hotelmanagementsystem.controller;

import com.project.hotelmanagementsystem.common.ResponseResult;
import com.project.hotelmanagementsystem.entity.Guest;
import com.project.hotelmanagementsystem.service.AuthService;
import com.project.hotelmanagementsystem.service.GuestService;
import com.project.hotelmanagementsystem.util.EncryptionUtil;
import com.project.hotelmanagementsystem.util.IdCardValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Tag(name = "客人档案管理", description = "客人档案信息的增删改查及条件检索接口")
@RestController
@RequestMapping("/api/guests")
public class GuestController {

    private final GuestService guestService;
    private final AuthService authService;

    public GuestController(GuestService guestService, AuthService authService) {
        this.guestService = guestService;
        this.authService = authService;
    }

    @Operation(summary = "查询所有客人", description = "返回系统中所有客人档案的列表")
    @GetMapping
    public ResponseResult<List<Map<String, Object>>> findAll() {
        List<Map<String, Object>> guests = guestService.findAll().stream()
                .map(this::maskGuestSensitiveInfo)
                .collect(Collectors.toList());
        return ResponseResult.success(guests);
    }

    @Operation(summary = "根据ID查询客人", description = "根据客人ID查询单个客人档案详细信息")
    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> findById(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id) {
        return guestService.findById(id)
                .map(this::maskGuestSensitiveInfo)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    @Operation(summary = "新增客人档案", description = "创建一个新的客人档案记录")
    @PostMapping
    public ResponseResult<Map<String, Object>> create(
            @Parameter(description = "客人信息", required = true) @RequestBody Guest guest) {
        String validationError = validateGuestInfo(guest);
        if (validationError != null) {
            return ResponseResult.error(400, validationError);
        }
        
        if (guest.getIdNumber() != null && !guest.getIdNumber().isEmpty()) {
            guest.setIdNumber(EncryptionUtil.encrypt(guest.getIdNumber()));
        }
        
        Guest saved = guestService.save(guest);
        return ResponseResult.success("创建成功", maskGuestSensitiveInfo(saved));
    }

    @Operation(summary = "更新客人档案信息", description = "根据客人ID更新客人档案信息，不存在则返回404")
    @PutMapping("/{id}")
    public ResponseResult<Map<String, Object>> update(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id,
            @Parameter(description = "客人信息", required = true) @RequestBody Guest guest) {
        Optional<Guest> existingOpt = guestService.findById(id);
        if (!existingOpt.isPresent()) {
            return ResponseResult.error(404, "资源不存在");
        }
        
        Guest existing = existingOpt.get();
        
        if (guest.getIdNumber() != null && !guest.getIdNumber().isEmpty()) {
            String validationError = IdCardValidator.validate(guest.getIdNumber());
            if (validationError != null) {
                return ResponseResult.error(400, validationError);
            }
            guest.setIdNumber(EncryptionUtil.encrypt(guest.getIdNumber()));
        }
        
        if (guest.getPhone() != null && !guest.getPhone().isEmpty()) {
            String phoneError = validatePhone(guest.getPhone());
            if (phoneError != null) {
                return ResponseResult.error(400, phoneError);
            }
        }
        
        guest.setId(id);
        Guest saved = guestService.save(guest);
        return ResponseResult.success(maskGuestSensitiveInfo(saved));
    }

    @Operation(summary = "删除客人档案", description = "根据客人ID删除客人档案记录")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteById(
            @Parameter(description = "客人ID", required = true) @PathVariable Integer id) {
        guestService.deleteById(id);
        return ResponseResult.success("删除成功", null);
    }

    @Operation(summary = "按证件号查询客人", description = "根据证件号查询单个客人档案信息")
    @GetMapping("/search/byIdNumber")
    public ResponseResult<Map<String, Object>> findByIdNumber(
            @Parameter(description = "证件号", required = true) @RequestParam String idNumber) {
        String encryptedIdNumber = EncryptionUtil.encrypt(idNumber);
        return guestService.findByIdNumber(encryptedIdNumber)
                .map(this::maskGuestSensitiveInfo)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    @Operation(summary = "按手机号查询客人", description = "根据手机号查询客人档案列表")
    @GetMapping("/search/byPhone")
    public ResponseResult<List<Map<String, Object>>> findByPhone(
            @Parameter(description = "手机号", required = true) @RequestParam String phone) {
        String phoneError = validatePhone(phone);
        if (phoneError != null) {
            return ResponseResult.error(400, phoneError);
        }
        
        List<Map<String, Object>> guests = guestService.findByPhone(phone).stream()
                .map(this::maskGuestSensitiveInfo)
                .collect(Collectors.toList());
        return ResponseResult.success(guests);
    }

    @Operation(summary = "按邮箱查询客人", description = "根据邮箱查询单个客人档案信息")
    @GetMapping("/search/byEmail")
    public ResponseResult<Map<String, Object>> findByEmail(
            @Parameter(description = "邮箱", required = true) @RequestParam String email) {
        String emailError = validateEmail(email);
        if (emailError != null) {
            return ResponseResult.error(400, emailError);
        }
        
        return guestService.findByEmail(email)
                .map(this::maskGuestSensitiveInfo)
                .map(ResponseResult::success)
                .orElse(ResponseResult.error(404, "资源不存在"));
    }

    @Operation(summary = "客人登录", description = "客人通过手机号或邮箱和密码登录")
    @PostMapping("/login")
    public ResponseResult<Map<String, Object>> login(
            @RequestBody Map<String, String> loginData) {
        String account = loginData.get("account");
        String password = loginData.get("password");
        
        if (account == null || password == null) {
            return ResponseResult.error(400, "账号和密码不能为空");
        }
        
        Optional<Guest> guestOpt = Optional.empty();
        
        // 判断是手机号还是邮箱
        if (account.contains("@")) {
            // 邮箱登录
            String emailError = validateEmail(account);
            if (emailError != null) {
                return ResponseResult.error(400, emailError);
            }
            guestOpt = guestService.findByEmail(account);
        } else {
            // 手机号登录
            String phoneError = validatePhone(account);
            if (phoneError != null) {
                return ResponseResult.error(400, phoneError);
            }
            List<Guest> guests = guestService.findByPhone(account);
            if (!guests.isEmpty()) {
                guestOpt = Optional.of(guests.get(0));
            }
        }
        
        if (guestOpt.isPresent()) {
            Guest guest = guestOpt.get();
            if (new BCryptPasswordEncoder().matches(password, guest.getPassword())) {
                String token = UUID.randomUUID().toString();
                authService.saveGuestToken(token, guest);
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("guest", maskGuestSensitiveInfo(guest));
                return ResponseResult.success("登录成功", result);
            }
        }
        return ResponseResult.error(401, "账号或密码错误");
    }

    @Operation(summary = "客人注册", description = "创建新的客人账号")
    @PostMapping("/register")
    public ResponseResult<Map<String, Object>> register(@RequestBody Guest guest) {
        if (guest.getPhone() == null || guest.getPassword() == null || 
            guest.getFirstName() == null || guest.getLastName() == null) {
            return ResponseResult.error(400, "必填信息不能为空");
        }
        
        // 手机号校验（必填）
        String phoneError = validatePhone(guest.getPhone());
        if (phoneError != null) {
            return ResponseResult.error(400, phoneError);
        }
        
        // 检查手机号是否已被注册
        List<Guest> guestsByPhone = guestService.findByPhone(guest.getPhone());
        if (!guestsByPhone.isEmpty()) {
            return ResponseResult.error(400, "该手机号已被注册");
        }
        
        // 邮箱校验（可选）
        if (guest.getEmail() != null && !guest.getEmail().isEmpty()) {
            String emailError = validateEmail(guest.getEmail());
            if (emailError != null) {
                return ResponseResult.error(400, emailError);
            }
            
            if (guestService.findByEmail(guest.getEmail()).isPresent()) {
                return ResponseResult.error(400, "该邮箱已被注册");
            }
        }
        
        if (guest.getIdNumber() != null && !guest.getIdNumber().isEmpty()) {
            String idType = guest.getIdType() != null ? guest.getIdType() : "id_card";
            String idCardError = validateIdNumber(idType, guest.getIdNumber());
            if (idCardError != null) {
                return ResponseResult.error(400, idCardError);
            }
            
            String rawIdNumber = guest.getIdNumber();
            
            if ("id_card".equals(idType)) {
                String genderFromId = IdCardValidator.extractGender(rawIdNumber);
                if (genderFromId != null && guest.getGender() == null) {
                    guest.setGender(genderFromId);
                }
                
                String birthDateStr = IdCardValidator.extractBirthDate(rawIdNumber);
                if (birthDateStr != null && guest.getDateOfBirth() == null) {
                    try {
                        guest.setDateOfBirth(LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyyMMdd")));
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
            
            guest.setIdNumber(EncryptionUtil.encrypt(rawIdNumber));
        }
        
        if (guest.getGender() != null && "secret".equals(guest.getGender())) {
            guest.setGender("other");
        }
        
        guest.setPassword(new BCryptPasswordEncoder().encode(guest.getPassword()));
        guest.setCreatedAt(LocalDateTime.now());
        if (guest.getIdType() == null) {
            guest.setIdType("id_card");
        }
        
        Guest saved = guestService.save(guest);
        return ResponseResult.success("注册成功", maskGuestSensitiveInfo(saved));
    }

    @Operation(summary = "获取当前登录客人信息", description = "获取当前登录客人的详细信息")
    @GetMapping("/info")
    public ResponseResult<Map<String, Object>> getGuestInfo(HttpServletRequest request) {
        Guest guest = (Guest) request.getAttribute("guest");
        if (guest == null) {
            return ResponseResult.error(401, "未登录");
        }
        return ResponseResult.success("获取成功", maskGuestSensitiveInfo(guest));
    }

    @Operation(summary = "更新客人信息", description = "更新当前登录客人的个人信息")
    @PutMapping("/update")
    public ResponseResult<Map<String, Object>> updateGuest(@RequestBody Map<String, Object> updateData) {
        if (!updateData.containsKey("id")) {
            return ResponseResult.error(400, "客人ID不能为空");
        }
        
        Integer id = null;
        try {
            id = Integer.parseInt(updateData.get("id").toString());
        } catch (Exception e) {
            return ResponseResult.error(400, "无效的客人ID");
        }
        
        Optional<Guest> existingOpt = guestService.findById(id);
        if (!existingOpt.isPresent()) {
            return ResponseResult.error(404, "客人不存在");
        }
        
        Guest existing = existingOpt.get();
        
        if (updateData.containsKey("firstName")) {
            String firstName = updateData.get("firstName").toString();
            if (!firstName.isEmpty()) {
                existing.setFirstName(firstName);
            }
        }
        
        if (updateData.containsKey("lastName")) {
            String lastName = updateData.get("lastName").toString();
            if (!lastName.isEmpty()) {
                existing.setLastName(lastName);
            }
        }
        
        if (updateData.containsKey("phone")) {
            String phone = updateData.get("phone").toString();
            if (!phone.isEmpty()) {
                String phoneError = validatePhone(phone);
                if (phoneError != null) {
                    return ResponseResult.error(400, phoneError);
                }
                existing.setPhone(phone);
            } else {
                existing.setPhone(null);
            }
        }
        
        if (updateData.containsKey("idType")) {
            String idType = updateData.get("idType").toString();
            existing.setIdType(idType);
        }
        
        if (updateData.containsKey("idNumber")) {
            String idNumber = updateData.get("idNumber").toString();
            if (!idNumber.isEmpty()) {
                String idType = existing.getIdType() != null ? existing.getIdType() : "id_card";
                if (updateData.containsKey("idType")) {
                    idType = updateData.get("idType").toString();
                }
                
                String idCardError = validateIdNumber(idType, idNumber);
                if (idCardError != null) {
                    return ResponseResult.error(400, idCardError);
                }
                
                String rawIdNumber = idNumber;
                
                if ("id_card".equals(idType)) {
                    String genderFromId = IdCardValidator.extractGender(rawIdNumber);
                    if (genderFromId != null && !updateData.containsKey("gender")) {
                        existing.setGender(genderFromId);
                    }
                    
                    String birthDateStr = IdCardValidator.extractBirthDate(rawIdNumber);
                    if (birthDateStr != null && !updateData.containsKey("dateOfBirth")) {
                        try {
                            existing.setDateOfBirth(LocalDate.parse(birthDateStr, DateTimeFormatter.ofPattern("yyyyMMdd")));
                        } catch (Exception e) {
                            // ignore
                        }
                    }
                }
                
                existing.setIdNumber(EncryptionUtil.encrypt(rawIdNumber));
            }
        }
        
        if (updateData.containsKey("email")) {
            String email = updateData.get("email").toString();
            if (!email.isEmpty()) {
                String emailError = validateEmail(email);
                if (emailError != null) {
                    return ResponseResult.error(400, emailError);
                }
                existing.setEmail(email);
            }
        }
        
        if (updateData.containsKey("gender")) {
            String gender = updateData.get("gender").toString();
            if ("secret".equals(gender)) {
                gender = "other";
            }
            existing.setGender(gender);
        }
        
        if (updateData.containsKey("nationality")) {
            String nationality = updateData.get("nationality").toString();
            existing.setNationality(nationality.isEmpty() ? null : nationality);
        }
        
        if (updateData.containsKey("dateOfBirth")) {
            String dateOfBirth = updateData.get("dateOfBirth").toString();
            if (!dateOfBirth.isEmpty()) {
                try {
                    existing.setDateOfBirth(LocalDate.parse(dateOfBirth));
                } catch (Exception e) {
                    return ResponseResult.error(400, "出生日期格式不正确");
                }
            } else {
                existing.setDateOfBirth(null);
            }
        }
        
        if (updateData.containsKey("notes")) {
            String notes = updateData.get("notes").toString();
            existing.setNotes(notes.isEmpty() ? null : notes);
        }
        
        Guest saved = guestService.save(existing);
        return ResponseResult.success("更新成功", maskGuestSensitiveInfo(saved));
    }

    private String validateGuestInfo(Guest guest) {
        if (guest.getFirstName() == null || guest.getFirstName().isEmpty()) {
            return "姓不能为空";
        }
        
        if (guest.getLastName() == null || guest.getLastName().isEmpty()) {
            return "名不能为空";
        }
        
        if (guest.getEmail() != null && !guest.getEmail().isEmpty()) {
            String emailError = validateEmail(guest.getEmail());
            if (emailError != null) {
                return emailError;
            }
        }
        
        if (guest.getPhone() != null && !guest.getPhone().isEmpty()) {
            String phoneError = validatePhone(guest.getPhone());
            if (phoneError != null) {
                return phoneError;
            }
        }
        
        if (guest.getIdNumber() != null && !guest.getIdNumber().isEmpty()) {
            String idCardError = IdCardValidator.validate(guest.getIdNumber());
            if (idCardError != null) {
                return idCardError;
            }
        }
        
        return null;
    }

    private String validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            return "邮箱不能为空";
        }
        
        if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return "邮箱格式不正确";
        }
        
        return null;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "手机号不能为空";
        }
        
        String cleanedPhone = phone.replaceAll("[\\s\\-()]", "");
        
        if (cleanedPhone.startsWith("86") || cleanedPhone.startsWith("+86")) {
            String chinaPhone = cleanedPhone.replaceAll("^\\+?86", "");
            if (!chinaPhone.matches("^1[3-9]\\d{9}$")) {
                return "中国手机号格式不正确，必须为11位";
            }
        } else if (!cleanedPhone.matches("^[+]?[1-9]\\d{1,14}$")) {
            return "手机号格式不正确";
        }
        
        return null;
    }
    
    private String validateIdNumber(String idType, String idNumber) {
        if (idNumber == null || idNumber.isEmpty()) {
            return null;
        }
        
        switch (idType) {
            case "id_card":
                return IdCardValidator.validate(idNumber);
            case "passport":
                if (!idNumber.matches("^[A-Za-z0-9]{6,20}$")) {
                    return "护照号码格式不正确";
                }
                break;
            case "drivers_license":
                if (!idNumber.matches("^[A-Za-z0-9]{8,20}$")) {
                    return "驾驶证号码格式不正确";
                }
                break;
            case "other":
                if (idNumber.length() < 4 || idNumber.length() > 50) {
                    return "证件号码长度必须在4-50位之间";
                }
                break;
            default:
                return "无效的证件类型";
        }
        
        return null;
    }

    private Map<String, Object> maskGuestSensitiveInfo(Guest guest) {
        Map<String, Object> result = new HashMap<>();
        result.put("id", guest.getId());
        result.put("firstName", guest.getFirstName());
        result.put("lastName", guest.getLastName());
        result.put("idType", guest.getIdType());
        
        if (guest.getIdNumber() != null && !guest.getIdNumber().isEmpty()) {
            try {
                String decrypted = EncryptionUtil.decrypt(guest.getIdNumber());
                result.put("idNumber", EncryptionUtil.maskIdNumber(decrypted));
            } catch (Exception e) {
                result.put("idNumber", "****");
            }
        } else {
            result.put("idNumber", null);
        }
        
        result.put("phone", maskPhone(guest.getPhone()));
        result.put("email", guest.getEmail());
        result.put("nationality", guest.getNationality());
        String gender = guest.getGender();
        if ("other".equals(gender)) {
            gender = "secret";
        }
        result.put("gender", gender);
        result.put("dateOfBirth", guest.getDateOfBirth());
        result.put("notes", guest.getNotes());
        result.put("createdAt", guest.getCreatedAt());
        
        return result;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
