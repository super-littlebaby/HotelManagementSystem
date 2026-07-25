package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Employee;

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
     * 移除 token
     *
     * @param token 令牌
     */
    void removeToken(String token);
}