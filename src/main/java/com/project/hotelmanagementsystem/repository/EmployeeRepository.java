package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 员工Repository接口
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Integer> {

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
