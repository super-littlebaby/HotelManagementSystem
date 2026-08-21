-- ============================================================
-- 酒店管理系统 - 完整建库脚本（可直接执行）
-- 数据库：SQL Server 2016+
-- 生成基准：全部 Entity 类字段注解 + 实际运行所需的约束/索引
-- 使用方式：
--   1. 用 SSMS 登录 SQL Server
--   2. 创建数据库后切换到该库（USE [数据库名]）
--   3. 执行本文件，会按依赖顺序建表/索引/外键
-- ============================================================
-- SET NOCOUNT ON;
-- GO

-- ============================================================
-- 1. 酒店信息表 (hotels)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'hotels' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.hotels (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        name            VARCHAR(200)    NOT NULL,                   -- 酒店名称
        address         VARCHAR(300)    NULL,                       -- 地址
        phone           VARCHAR(20)     NULL,                       -- 电话
        email           VARCHAR(100)    NULL,                       -- 邮箱
        description     VARCHAR(MAX)    NULL                        -- 简介
    );
END
GO

-- ============================================================
-- 2. 客房设施字典表 (facilities)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'facilities' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.facilities (
        id      INT IDENTITY(1,1) PRIMARY KEY,
        name    VARCHAR(100)            NOT NULL,                   -- 设施名称，如"电视机"
        price   DECIMAL(10,2)           NOT NULL DEFAULT 0          -- 损坏赔偿单价
    );
END
GO

-- ============================================================
-- 3. 客人档案表 (guests)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'guests' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.guests (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        first_name      VARCHAR(50)     NOT NULL,                   -- 名
        last_name       VARCHAR(50)     NOT NULL,                   -- 姓
        id_type         VARCHAR(20)     NULL DEFAULT 'id_card',     -- 证件类型
        id_number       VARCHAR(200)    NULL,                       -- 证件号码（加密存储）
        phone           VARCHAR(20)     NULL,                       -- 手机号
        email           VARCHAR(100)    NULL,                       -- 邮箱
        password        VARCHAR(200)    NULL,                       -- 登录密码（BCrypt 加密）
        nationality     VARCHAR(50)     NULL,                       -- 国籍
        gender          VARCHAR(10)     NULL,                       -- 性别: male/female/other
        date_of_birth   DATE            NULL,                       -- 出生日期
        notes           VARCHAR(MAX)    NULL,                       -- 备注
        created_at      DATETIME        NULL DEFAULT GETDATE(),     -- 创建时间
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_guests_gender
            CHECK (gender IN ('male','female','other')),
        CONSTRAINT CK_guests_id_type
            CHECK (id_type IN ('id_card','passport','drivers_license','other'))
    );
    -- 常用查询索引
    CREATE INDEX IX_guests_id_number   ON dbo.guests(id_number);
    CREATE INDEX IX_guests_phone       ON dbo.guests(phone);
    CREATE UNIQUE INDEX UX_guests_email ON dbo.guests(email) WHERE email IS NOT NULL;
END
GO

-- ============================================================
-- 4. 员工表 (employees)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'employees' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.employees (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        hotel_id        INT             NULL,                       -- 所属酒店 (NULL 表示集团管理员)
        username        VARCHAR(50)     NOT NULL,                   -- 登录用户名（全局唯一）
        password_hash   VARCHAR(255)    NOT NULL,                   -- 登录密码 (BCrypt)
        role            VARCHAR(20)     NULL DEFAULT 'front_desk',  -- 角色
        first_name      VARCHAR(50)     NULL,                       -- 名
        last_name       VARCHAR(50)     NULL,                       -- 姓
        phone           VARCHAR(20)     NULL,                       -- 手机号
        email           VARCHAR(100)    NULL,                       -- 邮箱
        hire_date       DATE            NULL,                       -- 入职日期
        is_active       BIT             NULL DEFAULT 1,             -- 是否在职
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_employees_role
            CHECK (role IN ('admin','manager','front_desk','housekeeping','finance')),
        -- -------- 唯一索引 --------
        CONSTRAINT UX_employees_username UNIQUE (username),
        -- -------- 外键 --------
        CONSTRAINT FK_employees_hotel
            FOREIGN KEY (hotel_id) REFERENCES dbo.hotels(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_employees_hotel_id ON dbo.employees(hotel_id);
END
GO

-- ============================================================
-- 4.1 初始化管理员账号（幂等性：不存在才插入）
--   登录用户名：admin
--   登录密码：123456  (明文存储)
--   角色：admin  (集团管理员，hotel_id = NULL，可查看全部酒店数据)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM dbo.employees WHERE username = 'admin')
BEGIN
    INSERT INTO dbo.employees (
        hotel_id,
        username,
        password_hash,
        role,
        first_name,
        last_name,
        phone,
        email,
        hire_date,
        is_active
    ) VALUES (
        NULL,                                                       -- hotel_id: NULL 表示集团管理员
        'admin',                                                    -- username: 登录用户名
        '123456',                                                   -- password_hash: 明文 123456
        'admin',                                                    -- role: 集团管理员角色
        N'管理员',                                                   -- first_name: 名
        N'集团',                                                    -- last_name: 姓
        NULL,                                                       -- phone: 手机号 (NULL)
        NULL,                                                       -- email: 邮箱 (NULL)
        NULL,                                                       -- hire_date: 入职日期 (NULL)
        1                                                           -- is_active: 在职 (True)
    );
END
GO

-- ============================================================
-- 5. 房型表 (room_types)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'room_types' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.room_types (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        hotel_id        INT             NOT NULL,                   -- 所属酒店
        name            VARCHAR(100)    NOT NULL,                   -- 房型名称，如"标准大床房"
        description     VARCHAR(MAX)    NULL,                       -- 房型描述
        max_adults      INT             NULL DEFAULT 2,             -- 可住成人
        max_children    INT             NULL DEFAULT 1,             -- 可住儿童
        base_price      DECIMAL(10,2)   NOT NULL,                   -- 基础房价(元/晚)
        area            DECIMAL(5,1)    NULL,                       -- 面积(平方米)
        bed_type        VARCHAR(50)     NULL,                       -- 床型描述
        -- -------- 外键 --------
        CONSTRAINT FK_room_types_hotel
            FOREIGN KEY (hotel_id) REFERENCES dbo.hotels(id) ON DELETE CASCADE
    );
    CREATE INDEX IX_room_types_hotel_id ON dbo.room_types(hotel_id);
END
GO

-- ============================================================
-- 6. 房型-设施关联表 (room_type_facilities) — 复合主键
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'room_type_facilities' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.room_type_facilities (
        room_type_id    INT NOT NULL,
        facility_id     INT NOT NULL,
        -- -------- 主键 --------
        CONSTRAINT PK_room_type_facilities PRIMARY KEY (room_type_id, facility_id),
        -- -------- 外键 --------
        CONSTRAINT FK_rtf_room_type
            FOREIGN KEY (room_type_id) REFERENCES dbo.room_types(id) ON DELETE CASCADE,
        CONSTRAINT FK_rtf_facility
            FOREIGN KEY (facility_id)  REFERENCES dbo.facilities(id) ON DELETE CASCADE
    );
END
GO

-- ============================================================
-- 7. 可消费项目表 (consumable_items)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'consumable_items' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.consumable_items (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        hotel_id    INT             NULL,                           -- 所属酒店（NULL 表示通用）
        name        VARCHAR(100)    NOT NULL,                       -- 品名，如"自助早餐"
        category    VARCHAR(20)     NOT NULL,                       -- 分类
        price       DECIMAL(10,2)   NOT NULL,                       -- 单价
        is_active   BIT             NULL DEFAULT 1,                 -- 是否上架
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_consumable_category
            CHECK (category IN ('food','beverage','laundry','other')),
        -- -------- 外键 --------
        CONSTRAINT FK_consumable_hotel
            FOREIGN KEY (hotel_id) REFERENCES dbo.hotels(id) ON DELETE CASCADE
    );
    CREATE INDEX IX_consumable_hotel_id ON dbo.consumable_items(hotel_id);
END
GO

-- ============================================================
-- 8. 房间表 (rooms)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'rooms' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.rooms (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        hotel_id        INT             NOT NULL,                   -- 所属酒店ID
        room_type_id    INT             NOT NULL,                   -- 房型ID
        room_number     VARCHAR(10)     NOT NULL,                   -- 房间号（全局唯一）
        floor           INT             NULL,                       -- 楼层
        status          VARCHAR(20)     NULL DEFAULT 'vacant',      -- 房态
        notes           VARCHAR(MAX)    NULL,                       -- 备注
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_rooms_status
            CHECK (status IN ('vacant','occupied','dirty','out_of_order','locked')),
        -- -------- 唯一索引 --------
        CONSTRAINT UX_rooms_room_number UNIQUE (room_number),
        -- -------- 外键 --------
        CONSTRAINT FK_rooms_hotel
            FOREIGN KEY (hotel_id)     REFERENCES dbo.hotels(id),
        CONSTRAINT FK_rooms_room_type
            FOREIGN KEY (room_type_id) REFERENCES dbo.room_types(id)
    );
    CREATE INDEX IX_rooms_hotel_id     ON dbo.rooms(hotel_id);
    CREATE INDEX IX_rooms_room_type_id ON dbo.rooms(room_type_id);
    CREATE INDEX IX_rooms_status       ON dbo.rooms(status);
END
GO

-- ============================================================
-- 9. 预订表 (reservations)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reservations' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.reservations (
        id                  INT IDENTITY(1,1) PRIMARY KEY,
        guest_id            INT             NULL,                   -- 客人账号ID（线下/电话预订为NULL）
        guest_name          VARCHAR(100)    NULL,                   -- 线下客人姓名快照
        id_type             VARCHAR(20)     NULL,                   -- 证件类型
        id_number           VARCHAR(200)    NULL,                   -- 证件号码（加密）
        phone               VARCHAR(20)     NULL,                   -- 联系电话
        booking_date        DATETIME        NULL DEFAULT GETDATE(), -- 预订时间
        check_in_date       DATE            NOT NULL,               -- 入住日期
        check_out_date      DATE            NOT NULL,               -- 退房日期
        status              VARCHAR(20)     NULL DEFAULT 'pending', -- 预订状态
        total_amount        DECIMAL(10,2)   NULL,                   -- 估算总金额
        special_requests    VARCHAR(MAX)    NULL,                   -- 特殊要求
        employee_id         INT             NULL,                   -- 操作员工
        channel             VARCHAR(20)     NULL DEFAULT 'online',  -- 预订渠道
        hotel_id            INT             NULL,                   -- 所属酒店ID（数据隔离用）
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_reservations_id_type
            CHECK (id_type IN ('id_card','passport','drivers_license','other')),
        CONSTRAINT CK_reservations_status
            CHECK (status IN ('pending','confirmed','checked_in','cancelled','no_show')),
        CONSTRAINT CK_reservations_channel
            CHECK (channel IN ('online','phone','walk_in','ota')),
        -- -------- 外键 --------
        CONSTRAINT FK_reservations_guest
            FOREIGN KEY (guest_id)    REFERENCES dbo.guests(id) ON DELETE SET NULL,
        CONSTRAINT FK_reservations_hotel
            FOREIGN KEY (hotel_id)    REFERENCES dbo.hotels(id) ON DELETE SET NULL,
        CONSTRAINT FK_reservations_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.employees(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_reservations_guest_id      ON dbo.reservations(guest_id);
    CREATE INDEX IX_reservations_hotel_id      ON dbo.reservations(hotel_id);
    CREATE INDEX IX_reservations_status        ON dbo.reservations(status);
    CREATE INDEX IX_reservations_checkin_date  ON dbo.reservations(check_in_date);
END
GO

-- ============================================================
-- 10. 预订房间明细表 (reservation_rooms)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'reservation_rooms' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.reservation_rooms (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        reservation_id  INT             NOT NULL,                   -- 所属预订ID
        room_type_id    INT             NOT NULL,                   -- 预订的房型
        room_id         INT             NULL,                       -- 实际分配的房间（未分配时 NULL）
        adults          INT             NULL DEFAULT 1,             -- 成人数
        children        INT             NULL DEFAULT 0,             -- 儿童数
        rate_per_night  DECIMAL(10,2)   NOT NULL,                   -- 锁定时房价
        -- -------- 外键 --------
        CONSTRAINT FK_resrooms_reservation
            FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(id) ON DELETE CASCADE,
        CONSTRAINT FK_resrooms_room_type
            FOREIGN KEY (room_type_id)   REFERENCES dbo.room_types(id),
        CONSTRAINT FK_resrooms_room
            FOREIGN KEY (room_id)        REFERENCES dbo.rooms(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_resrooms_reservation_id ON dbo.reservation_rooms(reservation_id);
    CREATE INDEX IX_resrooms_room_type_id   ON dbo.reservation_rooms(room_type_id);
    CREATE INDEX IX_resrooms_room_id        ON dbo.reservation_rooms(room_id);
END
GO

-- ============================================================
-- 11. 入住登记表 (check_ins)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'check_ins' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.check_ins (
        id                      INT IDENTITY(1,1) PRIMARY KEY,
        reservation_id          INT             NULL,               -- 关联预订（walk-in 为空）
        hotel_id                INT             NULL,               -- 所属酒店ID
        guest_id                INT             NULL,               -- 主登记客人（线下客户可为空）
        guest_name              VARCHAR(100)    NULL,               -- 线下客人姓名快照
        id_type                 VARCHAR(20)     NULL,               -- 证件类型
        id_number               VARCHAR(200)    NULL,               -- 证件号码（加密存储）
        phone                   VARCHAR(20)     NULL,               -- 联系电话
        room_id                 INT             NOT NULL,            -- 入住房间
        adults                  INT             NULL DEFAULT 1,     -- 成人数
        children                INT             NULL DEFAULT 0,     -- 儿童数
        check_in_time           DATETIME        NOT NULL,           -- 实际入住时间
        expected_check_out_time DATETIME        NULL,               -- 预计退房时间
        actual_check_out_time   DATETIME        NULL,               -- 实际退房时间
        status                  VARCHAR(20)     NULL DEFAULT 'in_house', -- 入住状态
        rate_per_night          DECIMAL(10,2)   NULL,               -- 实际执行房价
        total_charge            DECIMAL(10,2)   NULL,               -- 累计总费用（房费+消费+损坏）
        notes                   VARCHAR(MAX)    NULL,               -- 备注
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_checkins_id_type
            CHECK (id_type IN ('id_card','passport','drivers_license','other')),
        CONSTRAINT CK_checkins_status
            CHECK (status IN ('in_house','checked_out','early_check_out')),
        -- -------- 外键 --------
        CONSTRAINT FK_checkins_reservation
            FOREIGN KEY (reservation_id) REFERENCES dbo.reservations(id) ON DELETE SET NULL,
        CONSTRAINT FK_checkins_hotel
            FOREIGN KEY (hotel_id)       REFERENCES dbo.hotels(id) ON DELETE SET NULL,
        CONSTRAINT FK_checkins_guest
            FOREIGN KEY (guest_id)       REFERENCES dbo.guests(id) ON DELETE SET NULL,
        CONSTRAINT FK_checkins_room
            FOREIGN KEY (room_id)        REFERENCES dbo.rooms(id)
    );
    CREATE INDEX IX_checkins_reservation_id ON dbo.check_ins(reservation_id);
    CREATE INDEX IX_checkins_room_id        ON dbo.check_ins(room_id);
    CREATE INDEX IX_checkins_status         ON dbo.check_ins(status);
    CREATE INDEX IX_checkins_hotel_id       ON dbo.check_ins(hotel_id);
END
GO

-- ============================================================
-- 12. 同住客人表 (stay_guests)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'stay_guests' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.stay_guests (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        check_in_id INT             NOT NULL,                       -- 所属入住登记
        guest_id    INT             NULL,                           -- 关联客人账号（线下客户为空）
        name        VARCHAR(100)    NULL,                           -- 线下客人姓名
        id_type     VARCHAR(20)     NULL,                           -- 证件类型
        id_number   VARCHAR(200)    NULL,                           -- 证件号码（加密存储）
        is_primary  BIT             NULL DEFAULT 0,                 -- 是否主登记人
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_stayguests_id_type
            CHECK (id_type IN ('id_card','passport','drivers_license','other')),
        -- -------- 外键 --------
        CONSTRAINT FK_stayguests_checkin
            FOREIGN KEY (check_in_id) REFERENCES dbo.check_ins(id) ON DELETE CASCADE,
        CONSTRAINT FK_stayguests_guest
            FOREIGN KEY (guest_id)    REFERENCES dbo.guests(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_stayguests_check_in_id ON dbo.stay_guests(check_in_id);
    CREATE INDEX IX_stayguests_guest_id    ON dbo.stay_guests(guest_id);
END
GO

-- ============================================================
-- 13. 房间状态变更日志 (room_status_logs)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'room_status_logs' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.room_status_logs (
        id          INT IDENTITY(1,1) PRIMARY KEY,
        room_id     INT             NOT NULL,                       -- 房间ID
        old_status  VARCHAR(20)     NULL,                           -- 变更前房态
        new_status  VARCHAR(20)     NOT NULL,                       -- 变更后房态
        changed_by  INT             NULL,                           -- 操作员工ID
        changed_at  DATETIME        NULL DEFAULT GETDATE(),         -- 变更时间
        notes       VARCHAR(MAX)    NULL,                           -- 备注
        -- -------- 外键 --------
        CONSTRAINT FK_statuslog_room
            FOREIGN KEY (room_id)    REFERENCES dbo.rooms(id) ON DELETE CASCADE,
        CONSTRAINT FK_statuslog_employee
            FOREIGN KEY (changed_by) REFERENCES dbo.employees(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_statuslog_room_id    ON dbo.room_status_logs(room_id);
    CREATE INDEX IX_statuslog_changed_at ON dbo.room_status_logs(changed_at);
END
GO

-- ============================================================
-- 14. 账单表 (bills)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bills' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.bills (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        check_in_id     INT             NOT NULL,                   -- 关联入住登记
        bill_status     VARCHAR(20)     NULL DEFAULT 'open',        -- 账单状态
        total_amount    DECIMAL(10,2)   NULL DEFAULT 0.00,          -- 账单总金额
        paid_amount     DECIMAL(10,2)   NULL DEFAULT 0.00,          -- 已付款金额（含押金）
        deposit_amount  DECIMAL(10,2)   NULL DEFAULT 0.00,          -- 收取押金金额
        created_at      DATETIME        NULL DEFAULT GETDATE(),     -- 开单时间
        closed_at       DATETIME        NULL,                       -- 关单时间
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_bills_status
            CHECK (bill_status IN ('open','closed','void')),
        -- -------- 外键 --------
        CONSTRAINT FK_bills_checkin
            FOREIGN KEY (check_in_id) REFERENCES dbo.check_ins(id)
    );
    CREATE UNIQUE INDEX UX_bills_check_in_id ON dbo.bills(check_in_id) WHERE check_in_id IS NOT NULL;
END
GO

-- ============================================================
-- 15. 账单明细表 (bill_items)
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'bill_items' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.bill_items (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        bill_id         INT             NOT NULL,                   -- 所属账单
        item_type       VARCHAR(20)     NOT NULL,                   -- 项目类型
        description     VARCHAR(255)    NULL,                       -- 项目描述
        facility_id     INT             NULL,                       -- 若为"损坏赔偿"，关联设施
        consumable_id   INT             NULL,                       -- 若为"消费项目"，关联消费品
        quantity        DECIMAL(10,2)   NULL DEFAULT 1,             -- 数量
        unit_price      DECIMAL(10,2)   NOT NULL,                   -- 单价
        amount          DECIMAL(10,2)   NOT NULL,                   -- 小计
        charge_date     DATE            NULL,                       -- 消费日期
        employee_id     INT             NULL,                       -- 操作员工
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_billitems_item_type
            CHECK (item_type IN ('room_charge','food','beverage','laundry','damage','other')),
        -- -------- 外键 --------
        CONSTRAINT FK_billitems_bill
            FOREIGN KEY (bill_id)       REFERENCES dbo.bills(id) ON DELETE CASCADE,
        CONSTRAINT FK_billitems_facility
            FOREIGN KEY (facility_id)   REFERENCES dbo.facilities(id) ON DELETE SET NULL,
        CONSTRAINT FK_billitems_consumable
            FOREIGN KEY (consumable_id) REFERENCES dbo.consumable_items(id) ON DELETE SET NULL,
        CONSTRAINT FK_billitems_employee
            FOREIGN KEY (employee_id)   REFERENCES dbo.employees(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_billitems_bill_id ON dbo.bill_items(bill_id);
END
GO

-- ============================================================
-- 16. 收款记录表 (payments)
--   - 包含押金收取(deposit) + 消费付款(charge)
--   - payment_method: cash / credit_card / debit_card / wechat / alipay / bank_transfer
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'payments' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.payments (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        bill_id         INT             NOT NULL,                   -- 所属账单
        amount          DECIMAL(10,2)   NOT NULL,                   -- 金额
        payment_method  VARCHAR(20)     NOT NULL,                   -- 支付方式
        payment_type    VARCHAR(20)     NULL DEFAULT 'charge',      -- deposit / charge
        payment_date    DATETIME        NULL DEFAULT GETDATE(),     -- 支付时间
        transaction_ref VARCHAR(100)    NULL,                       -- 三方交易号
        employee_id     INT             NULL,                       -- 操作员工
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_payments_method
            CHECK (payment_method IN ('cash','credit_card','debit_card','wechat','alipay','bank_transfer')),
        CONSTRAINT CK_payments_type
            CHECK (payment_type IN ('deposit','charge')),
        -- -------- 外键 --------
        CONSTRAINT FK_payments_bill
            FOREIGN KEY (bill_id)     REFERENCES dbo.bills(id),
        CONSTRAINT FK_payments_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.employees(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_payments_bill_id ON dbo.payments(bill_id);
END
GO

-- ============================================================
-- 17. 退款记录表 (refunds)
--   - 主要用于退房退押金；refund_method 同支付方式
-- ============================================================
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'refunds' AND schema_id = SCHEMA_ID('dbo'))
BEGIN
    CREATE TABLE dbo.refunds (
        id              INT IDENTITY(1,1) PRIMARY KEY,
        bill_id         INT             NOT NULL,                   -- 所属账单
        amount          DECIMAL(10,2)   NOT NULL,                   -- 退款金额
        refund_method   VARCHAR(20)     NOT NULL,                   -- 退款方式
        refund_date     DATETIME        NULL DEFAULT GETDATE(),     -- 退款时间
        transaction_ref VARCHAR(100)    NULL,                       -- 三方交易号
        employee_id     INT             NULL,                       -- 操作员工
        notes           VARCHAR(255)    NULL,                       -- 备注
        -- -------- CHECK 约束 --------
        CONSTRAINT CK_refunds_method
            CHECK (refund_method IN ('cash','credit_card','debit_card','wechat','alipay','bank_transfer')),
        -- -------- 外键 --------
        CONSTRAINT FK_refunds_bill
            FOREIGN KEY (bill_id)     REFERENCES dbo.bills(id),
        CONSTRAINT FK_refunds_employee
            FOREIGN KEY (employee_id) REFERENCES dbo.employees(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_refunds_bill_id ON dbo.refunds(bill_id);
END
GO

-- ============================================================
-- 全部 17 张表创建完成
--   hotels (1) - room_types (5) → rooms (8) → reservations (9)
--        |          |             |             |
--        v          v             v             v
--   employees (4)  facilities (2) room_status_logs (13)
--   consumable_items (7)          check_ins (11) → stay_guests (12)
--   room_type_facilities (6)                                  |
--                                                            v
--                                                     bills (14) → bill_items (15)
//                                                        |
//                                                   payments (16), refunds (17)
-- ============================================================
PRINT '============================================================';
PRINT '酒店管理系统建表完成，共创建 17 张业务表。';
PRINT '请使用 application.yml 中配置的数据库连接验证数据是否能正常访问。';
PRINT '============================================================';
GO
