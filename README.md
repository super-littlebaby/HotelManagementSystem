# HotelManagementSystem - 酒店管理系统

一个基于 Spring Boot + Vue 3 的全栈酒店管理系统，支持多门店（酒店）运营、预订管理、入住退房、账单结算、员工权限控制等完整酒店业务流程。

---

## 目录

- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [系统架构](#系统架构)
- [数据库设计](#数据库设计)
- [项目结构](#项目结构)
- [快速开始](#快速开始)
- [API 文档](#api-文档)
- [角色权限说明](#角色权限说明)
- [业务流程说明](#业务流程说明)

---

## 功能特性

### 🏨 酒店运营管理
- **多酒店支持**：支持集团下多门店独立运营，数据按酒店隔离
- **房型管理**：配置各酒店房型信息（名称、价格、床型、面积、最大入住人数等）
- **房间管理**：房间基础信息维护、楼层、房号、状态跟踪
- **设施管理**：客房设施字典及房型设施关联配置
- **房间状态日志**：记录房间状态变更历史

### 👥 客户系统（User 端）
- 用户注册 / 登录 / 个人资料维护
- 浏览酒店列表及房型详情
- 在线预订房间（选择酒店、房型、入住/退房日期）
- 我的预订查看与取消
- 历史入住记录查询

### 💼 员工系统（Staff 端）
- 员工账号管理（增删改查、角色分配）
- **预订管理**：预订列表、确认/取消预订、分配房间、搜索预订
- **入住管理**：预订客人入住、散客入住、押金收取、陪同客人登记
- **退房管理**：预结算、消费入账、退房结账
- **账单管理**：账单明细、消费项目、支付记录、退款记录
- **消耗品管理**：消耗品品类维护、客房消耗品下单
- **设施报损**：房间设施损坏上报、赔偿处理

### 🔐 权限与安全
- 基于 Token 的无状态认证（Authorization: Bearer \<token\>）
- 细粒度角色权限控制（@RequiresRoles 注解 + 前端路由守卫）
- 密码 BCrypt 加密存储
- 身份证号加密存储
- 手机号/邮箱唯一性校验
- 数据按员工所属酒店自动隔离（集团管理员除外）

---

## 技术栈

### 后端（Backend）
| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 24 | 开发语言 |
| Spring Boot | 4.1.0 | 应用框架 |
| Spring Data JPA | - | ORM 数据持久化 |
| Spring Security | - | 安全框架（仅 BCrypt 使用，认证走拦截器） |
| SQL Server | 2016+ | 关系型数据库 |
| SpringDoc OpenAPI | 3.0.3 | Swagger API 文档 |
| Hibernate | - | JPA 实现 |

### 前端（Frontend）
| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.5.x | 渐进式 JS 框架 |
| Vue Router | 4.x | 路由管理 |
| Element Plus | 2.x | UI 组件库 |
| Axios | 1.x | HTTP 客户端 |
| Vite | 6.x | 构建工具 |
| @vueuse/core | 11.x | Vue 组合式工具集 |

---

## 系统架构

```
┌─────────────────────────────────────────────────────────────────┐
│                            客户端                                │
│  ┌──────────────────────┐     ┌─────────────────────────────┐   │
│  │   User 端 (客人)      │     │     Staff 端 (员工)          │   │
│  │   Vue3 + ElementPlus │     │     Vue3 + ElementPlus       │   │
│  └──────────┬───────────┘     └──────────────┬──────────────┘   │
└─────────────┼────────────────────────────────┼──────────────────┘
              │          HTTP/JSON             │
              ▼                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Spring Boot 后端                             │
│  ┌─────────────────────────────────────────────────────────┐    │
│  │              AuthInterceptor (Token + 角色校验)          │    │
│  └─────────────────────────────┬───────────────────────────┘    │
│                                │                                │
│  ┌──────────┐  ┌──────────┐  ┌┴───────────┐  ┌────────────┐    │
│  │Controller│  │  Service  │  │ Repository │  │   Entity   │    │
│  └──────────┘  └──────────┘  └────────────┘  └────────────┘    │
│                       │                                          │
│  ┌────────────────────┴─────────────────────────────────────┐    │
│  │         统一响应 ResponseResult + 全局异常处理              │    │
│  └──────────────────────────────────────────────────────────┘    │
└───────────────────────────────┬─────────────────────────────────┘
                                │ JDBC
                                ▼
                ┌──────────────────────────────┐
                │      SQL Server 数据库        │
                └──────────────────────────────┘
```

---

## 数据库设计

项目包含以下核心数据表，完整建表脚本见根目录 [数据库.sql](./数据库.sql)：

| 表名 | 说明 | 关键字段 |
|------|------|----------|
| **hotels** | 酒店信息 | id, name, address, phone, email, description |
| **facilities** | 客房设施字典 | id, name(设施名), price(赔偿单价) |
| **room_types** | 房型 | id, hotel_id, name, base_price, max_adults, max_children, bed_type, area |
| **room_type_facilities** | 房型-设施关联 | room_type_id + facility_id(复合主键) |
| **rooms** | 房间 | id, hotel_id, room_type_id, room_number, floor, status |
| **room_status_logs** | 房间状态日志 | id, room_id, old_status, new_status, change_time, employee_id, reason |
| **employees** | 员工 | id, hotel_id(NULL=集团管理员), username, password_hash, role(admin/manager/front_desk/housekeeping/finance) |
| **guests** | 客人档案 | id, first_name, last_name, id_type, id_number(加密), phone, email, password(BCrypt) |
| **reservations** | 预订主表 | id, guest_id, hotel_id, check_in_date, check_out_date, status, total_amount, channel |
| **reservation_rooms** | 预订-房间关联 | id, reservation_id, room_type_id, assigned_room_id, quantity, unit_price |
| **check_ins** | 入住登记 | id, reservation_id, room_id, guest_id, check_in_time, expected_check_out_time, status, rate_per_night |
| **stay_guests** | 入住陪同客人 | id, check_in_id, first_name, last_name, id_type, id_number(加密) |
| **bills** | 账单 | id, check_in_id, bill_status, total_amount, paid_amount, deposit_amount |
| **bill_items** | 账单明细 | id, bill_id, item_type(room/consumable/damage/other), amount, quantity |
| **consumable_items** | 消耗品品类 | id, hotel_id, name, category, price, stock, is_active |
| **consumable_orders** | 消耗品订单 | id, check_in_id, bill_item_id, room_id, consumable_item_id, quantity, unit_price |
| **payments** | 支付记录 | id, bill_id, payment_method, payment_type(deposit/room/consumable), amount |
| **refunds** | 退款记录 | id, bill_id, refund_method, amount, reason |
| **facility_damage** | 设施报损 | id, room_id, facility_id, check_in_id, report_time, damage_level, compensation_amount, status |

---

## 项目结构

```
HotelManagementSystem/
├── pom.xml                                    # Maven 配置
├── 数据库.sql                                  # SQL Server 建库脚本
├── 数据库.txt                                  # 数据库说明文档
├── src/
│   └── main/
│       ├── java/com/project/hotelmanagementsystem/
│       │   ├── HotelManagementSystemApplication.java   # 启动类
│       │   ├── annotation/                             # 自定义注解
│       │   │   └── RequiresRoles.java                  #   角色权限注解
│       │   ├── common/                                 # 通用类
│       │   │   ├── ResponseResult.java                 #   统一响应封装
│       │   │   └── GlobalExceptionHandler.java         #   全局异常处理
│       │   ├── config/                                 # 配置类
│       │   │   ├── SecurityConfig.java                 #   Spring Security 配置（permitAll）
│       │   │   └── WebMvcConfig.java                   #   WebMVC + 拦截器注册
│       │   ├── interceptor/                            # 拦截器
│       │   │   └── AuthInterceptor.java                #   Token 认证 + 角色校验
│       │   ├── entity/                                 # JPA 实体类（19张表）
│       │   ├── dto/                                    # 数据传输对象
│       │   ├── repository/                             # Spring Data JPA 接口
│       │   ├── service/                                # 业务逻辑层（接口 + impl/）
│       │   │   ├── DataIsolationService.java           #   数据隔离服务（按酒店过滤）
│       │   │   └── impl/                               #   各 Service 实现
│       │   ├── controller/                             # REST API 控制层（18个Controller）
│       │   └── util/                                   # 工具类
│       │       ├── EncryptionUtil.java                 #   身份证号加解密
│       │       └── IdCardValidator.java                #   身份证号校验
│       └── resources/
│           └── application.yml                         # 应用配置文件
└── frontend/
    ├── user/                                    # 客人端前端
    │   ├── src/
    │   │   ├── views/                           #   页面：Home, HotelDetail, Reservation, MyReservations, HistoryStays, Profile, Login, Register
    │   │   ├── api/                             #   API 请求封装
    │   │   ├── stores/auth.js                   #   Pinia/简单状态管理
    │   │   ├── router/index.js                  #   路由配置 + 登录守卫
    │   │   ├── utils/                           #   request.js, validate.js
    │   │   └── App.vue / main.js / style.css
    │   └── package.json / vite.config.js
    └── staff/                                   # 员工端前端
        ├── src/
        │   ├── views/                           #   页面：Dashboard, Employees, Hotels, Rooms, RoomTypes, Facilities, RoomTypeFacilities, RoomStatusLogs, ConsumableItems, ConsumableOrder, Reservations, CheckIns, Bills, FacilityDamage, Profile, Login
        │   ├── api/                             #   20+ API 模块封装
        │   ├── components/HotelSelect.vue       #   通用酒店选择组件
        │   ├── stores/auth.js                   #   员工状态 + 角色权限
        │   ├── router/index.js                  #   路由 + 角色权限守卫（rolePermissions）
        │   └── utils/
        └── package.json / vite.config.js
```

---

## 快速开始

### 环境要求

- **JDK** 24+
- **Maven** 3.8+
- **Node.js** 18+
- **SQL Server** 2016+（默认实例名 SQLEXPRESS，端口 11837）

### 1. 数据库准备

1. 使用 SSMS 登录 SQL Server
2. 创建数据库 `HotelManagementSystem`
3. 执行项目根目录 [数据库.sql](./数据库.sql) 脚本完成建表（脚本包含默认管理员账号初始化）

#### 默认管理员账号（脚本自动插入，幂等不重复）

| 项目 | 值 |
|------|-----|
| 用户名 | `admin` |
| 密码 | `123456`（明文存储） |
| 角色 | `admin`（集团管理员） |
| 姓名 | 集团 管理员 |
| 所属酒店 | `NULL`（集团级别，可查看/管理全部酒店数据） |
| 状态 | 在职 (is_active = True) |

> 账号唯一性保护：脚本使用 `IF NOT EXISTS(WHERE username='admin')` 判断，重复执行 SQL 脚本不会重复插入。

### 2. 启动后端服务

```bash
# 在项目根目录执行
mvnw.cmd clean compile spring-boot:run
```

后端默认启动在 **http://localhost:8080**

默认数据源配置（`src/main/resources/application.yml`）：
```yaml
spring:
  datasource:
    url: jdbc:sqlserver://localhost:11837;instanceName=SQLEXPRESS;databaseName=HotelManagementSystem;encrypt=true;trustServerCertificate=true
    username: sa
    password: 123456
```

> **注意**：请根据实际数据库环境修改端口、用户名、密码。

### 3. 启动员工端（Staff）

```bash
cd frontend/staff
npm install
npm run dev
```

默认地址：**http://localhost:5173**

### 4. 启动客人端（User）

```bash
cd frontend/user
npm install
npm run dev
```

默认地址：**http://localhost:5174**（Vite 会自动分配下一个可用端口）

---

## API 文档

启动后端后访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

OpenAPI JSON：

```
http://localhost:8080/api-docs
```

### 统一响应格式

所有接口返回 `ResponseResult<T>`：
```json
{
  "code": 200,
  "message": "success",
  "data": { /* 业务数据 */ }
}
```

- `code = 200` 成功
- `code = 401` 未授权（Token 无效/缺失）
- `code = 403` 权限不足（角色不匹配）
- `code = 500` 其他业务错误

### 认证方式

登录后获取 Token，后续请求在 Header 中携带：
```
Authorization: Bearer <your_token_here>
```

### 主要 API 模块

| Controller | 前缀 | 说明 |
|------------|------|------|
| HotelController | `/api/hotels` | 酒店 CRUD、搜索 |
| EmployeeController | `/api/employees` | 员工 CRUD、登录、资料 |
| RoomTypeController | `/api/room-types` | 房型 CRUD |
| RoomController | `/api/rooms` | 房间 CRUD、可用房间查询 |
| FacilityController | `/api/facilities` | 设施字典 CRUD |
| RoomTypeFacilityController | `/api/room-type-facilities` | 房型设施关联 |
| RoomStatusLogController | `/api/room-status-logs` | 房间状态变更日志 |
| GuestController | `/api/guests` | 客人注册/登录/资料、搜索 |
| ReservationController | `/api/reservations` | 预订创建、确认、取消、分房、入住、退房 |
| CheckInController | `/api/check-ins` | 散客入住、退房结账、预结算 |
| BillController & BillItemController | `/api/bills` `/api/bill-items` | 账单与明细 |
| PaymentController | `/api/payments` | 支付记录（押金/房费/消费） |
| RefundController | `/api/refunds` | 退款记录 |
| ConsumableItemController | `/api/consumable-items` | 消耗品品类 |
| ConsumableOrderController | `/api/consumable-orders` | 消耗品下单（入账） |
| FacilityDamageController | `/api/facility-damage` | 设施报损上报与处理 |
| StayGuestController | `/api/stay-guests` | 入住陪同客人管理 |

---

## 角色权限说明

员工系统支持 5 种角色，通过 `@RequiresRoles` 注解 + 前端路由守卫双重控制：

| 角色 role | 名称 | 权限菜单 |
|-----------|------|----------|
| `admin` | 集团管理员 | 全部功能（可跨酒店查看数据，hotel_id = NULL） |
| `manager` | 酒店经理 | 全部功能（仅本酒店数据） |
| `front_desk` | 前台 | 消耗品管理、消耗品下单、预订管理、入住管理、账单管理 |
| `housekeeping` | 客房服务 | 房间管理、房型管理、设施管理、房型设施、设施报损 |
| `finance` | 财务 | 房型、房型设施、设施、消耗品、入住、账单 |

> **数据隔离规则**：所有酒店级员工（hotel_id != NULL）查询时自动过滤 hotel_id；集团管理员（hotel_id = NULL）可查看全部酒店数据。

---

## 业务流程说明

### 1. 预订流程

```
客人(User端)/前台(Staff端) 预订
    ↓
创建预订 (status = 'pending')
    ↓
前台确认预订 (status = 'confirmed')  [可选：分配房间]
    ↓
客人到店 → 预订入住 (status = 'checked_in')  →  生成 CheckIn + Bill
    ↓
退房 → 预订状态 = 'checked_out'
```

> 未确认的预订也可直接入住；入住时会锁定对应房间，避免重复分配。

### 2. 入住流程

**方式一：预订客人入住**
1. 在预订管理中找到已确认预订
2. 点击「入住」，填写客人信息、分配房间、收取押金
3. 系统自动生成：CheckIn（入住登记）+ Bill（账单，含押金 Payment）
4. 支持录入多位陪同客人（StayGuest）

**方式二：散客直接入住**
1. 在入住管理中点击「散客入住」
2. 选择可用房间、填写客人信息、支付押金
3. 同方式一生成 CheckIn + Bill

### 3. 在店消费

- **消耗品**：在「消耗品下单」页面选择入住记录，添加消耗品 → 自动入账到 BillItem + 生成 ConsumableOrder
- **设施损坏**：在「设施报损」页面上报 → 选择入住记录 → 赔偿金额自动入账

### 4. 退房流程

1. 在入住管理中找到入住记录
2. 点击「预结算」查看当前总费用（房费 + 消耗品 + 赔偿 - 押金）
3. 确认支付差额 / 退还押金
4. 点击「退房结账」 → CheckIn 状态 = checked_out，Bill 状态 = closed
5. 房间状态自动变更为 dirty / available

---

## 开发约束与约定

项目遵循以下工程约定（用于代码一致性，了解即可）：

- **主键生成**：所有实体使用 `GenerationType.IDENTITY`（SQL Server 自增列）
- **复合主键**：`room_type_facilities` 使用 `@IdClass(RoomTypeFacilityId.class)`
- **依赖注入**：统一构造器注入（final 字段），禁止 `@Autowired` 字段注入
- **API 返回**：Controller 方法必须返回 `ResponseResult<T>`
- **密码加密**：员工密码 `password_hash` 使用 BCrypt，API 响应绝不包含密码字段
- **身份证加密**：`id_number` 字段使用 `EncryptionUtil` 加解密后存储
- **性别枚举**：数据库存储 `male / female / other`（other 对应界面"保密"）
- **房间可用过滤**：专用服务方法 `findAvailableByRoomTypeIdAndStatus` / `findAvailableByHotelIdAndStatus`，排除已确认/已在住预订锁定的房间
- **客人唯一性**：修改个人资料时，手机号和邮箱排除自身后校验唯一性

---

## License

本项目仅供学习与开发参考使用。
