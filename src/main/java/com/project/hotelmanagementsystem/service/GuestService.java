package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Guest;

import java.util.List;
import java.util.Optional;

/**
 * 客人档案Service接口
 */
public interface GuestService {

    /**
     * 根据ID查询客人
     *
     * @param id 客人ID
     * @return 客人信息
     */
    Optional<Guest> findById(Integer id);

    /**
     * 查询所有客人
     *
     * @return 客人列表
     */
    List<Guest> findAll();

    /**
     * 保存/更新客人
     *
     * @param guest 客人信息
     * @return 保存后的客人信息
     */
    Guest save(Guest guest);

    /**
     * 根据ID删除客人
     *
     * @param id 客人ID
     */
    void deleteById(Integer id);

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