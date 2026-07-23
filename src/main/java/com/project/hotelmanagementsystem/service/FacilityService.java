package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Facility;

import java.util.List;
import java.util.Optional;

/**
 * 设施Service接口
 */
public interface FacilityService {

    /**
     * 根据ID查询设施
     *
     * @param id 设施ID
     * @return 设施信息
     */
    Optional<Facility> findById(Integer id);

    /**
     * 查询所有设施
     *
     * @return 设施列表
     */
    List<Facility> findAll();

    /**
     * 保存/更新设施
     *
     * @param facility 设施信息
     * @return 保存后的设施信息
     */
    Facility save(Facility facility);

    /**
     * 根据ID删除设施
     *
     * @param id 设施ID
     */
    void deleteById(Integer id);

    /**
     * 根据名称查询设施列表
     *
     * @param name 设施名称
     * @return 设施列表
     */
    List<Facility> findByNameContaining(String name);
}