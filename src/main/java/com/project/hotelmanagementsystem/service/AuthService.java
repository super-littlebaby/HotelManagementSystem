package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Employee;
import com.project.hotelmanagementsystem.entity.Guest;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 存储 token 和员工信息
     *
     * @param token    令牌
     * @param employee 员工信息
     */
    void storeToken(String token, Employee employee);

    /**
     * 根据 token 获取员工信息
     *
     * @param token 令牌
     * @return 员工信息，未找到返回 null
     */
    Employee getEmployeeByToken(String token);

    /**
     * 移除员工 token
     *
     * @param token 令牌
     */
    void removeToken(String token);

    /**
     * 存储客人 token 和客人信息
     *
     * @param token 令牌
     * @param guest 客人信息
     */
    void saveGuestToken(String token, Guest guest);

    /**
     * 根据 token 获取客人信息
     *
     * @param token 令牌
     * @return 客人信息，未找到返回 null
     */
    Guest getGuestByToken(String token);

    /**
     * 移除客人 token
     *
     * @param token 令牌
     */
    void removeGuestToken(String token);
}