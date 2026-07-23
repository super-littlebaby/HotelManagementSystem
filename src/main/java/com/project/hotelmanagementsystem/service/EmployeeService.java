package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Employee;

import java.util.List;
import java.util.Optional;

/**
 * 员工Service接口
 */
public interface EmployeeService {

    /**
     * 根据ID查询员工
     *
     * @param id 员工ID
     * @return 员工信息
     */
    Optional<Employee> findById(Integer id);

    /**
     * 查询所有员工
     *
     * @return 员工列表
     */
    List<Employee> findAll();

    /**
     * 保存/更新员工
     *
     * @param employee 员工信息
     * @return 保存后的员工信息
     */
    Employee save(Employee employee);

    /**
     * 根据ID删除员工
     *
     * @param id 员工ID
     */
    void deleteById(Integer id);

    /**
     * 根据用户名查询员工
     *
     * @param username 用户名
     * @return 员工信息
     */
    Optional<Employee> findByUsername(String username);

    /**
     * 根据酒店ID查询员工列表
     *
     * @param hotelId 酒店ID
     * @return 员工列表
     */
    List<Employee> findByHotelId(Integer hotelId);

    /**
     * 根据角色查询员工列表
     *
     * @param role 角色
     * @return 员工列表
     */
    List<Employee> findByRole(String role);

    /**
     * 根据酒店ID和角色查询员工列表
     *
     * @param hotelId 酒店ID
     * @param role    角色
     * @return 员工列表
     */
    List<Employee> findByHotelIdAndRole(Integer hotelId, String role);

    /**
     * 根据酒店ID和激活状态查询员工列表
     *
     * @param hotelId  酒店ID
     * @param isActive 激活状态
     * @return 员工列表
     */
    List<Employee> findByHotelIdAndIsActive(Integer hotelId, Boolean isActive);
}