package com.project.hotelmanagementsystem.service;

import com.project.hotelmanagementsystem.entity.Hotel;

import java.util.List;
import java.util.Optional;

/**
 * 酒店Service接口
 */
public interface HotelService {

    /**
     * 根据ID查询酒店
     *
     * @param id 酒店ID
     * @return 酒店信息
     */
    Optional<Hotel> findById(Integer id);

    /**
     * 查询所有酒店
     *
     * @return 酒店列表
     */
    List<Hotel> findAll();

    /**
     * 保存/更新酒店
     *
     * @param hotel 酒店信息
     * @return 保存后的酒店信息
     */
    Hotel save(Hotel hotel);

    /**
     * 根据ID删除酒店
     *
     * @param id 酒店ID
     */
    void deleteById(Integer id);

    /**
     * 根据名称查询酒店列表
     *
     * @param name 酒店名称
     * @return 酒店列表
     */
    List<Hotel> findByNameContaining(String name);

    /**
     * 根据地址查询酒店列表
     *
     * @param address 酒店地址
     * @return 酒店列表
     */
    List<Hotel> findByAddressContaining(String address);
}