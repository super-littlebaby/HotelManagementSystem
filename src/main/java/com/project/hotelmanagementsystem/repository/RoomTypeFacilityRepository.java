package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 房型-设施关联Repository接口
 */
@Repository
public interface RoomTypeFacilityRepository extends JpaRepository<RoomTypeFacility, RoomTypeFacilityId> {

    /**
     * 根据房型ID查询关联的设施
     *
     * @param roomTypeId 房型ID
     * @return 房型-设施关联列表
     */
    List<RoomTypeFacility> findByRoomTypeId(Integer roomTypeId);

    /**
     * 根据设施ID查询关联的房型
     *
     * @param facilityId 设施ID
     * @return 房型-设施关联列表
     */
    List<RoomTypeFacility> findByFacilityId(Integer facilityId);
}
