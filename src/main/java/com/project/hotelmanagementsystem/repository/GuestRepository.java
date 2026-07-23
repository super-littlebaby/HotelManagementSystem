package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.Guest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客人档案Repository接口
 */
@Repository
public interface GuestRepository extends JpaRepository<Guest, Integer> {

    /**
     * 根据身份证号查询客人
     *
     * @param idNumber 身份证号
     * @return 客人信息
     */
    Optional<Guest> findByIdNumber(String idNumber);

    /**
     * 根据手机号查询客人列表
     *
     * @param phone 手机号
     * @return 客人列表
     */
    List<Guest> findByPhone(String phone);

    /**
     * 根据邮箱查询客人
     *
     * @param email 邮箱
     * @return 客人信息
     */
    Optional<Guest> findByEmail(String email);
}
