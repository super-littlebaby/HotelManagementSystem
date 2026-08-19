package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.RoomTypeFacility;
import com.project.hotelmanagementsystem.entity.RoomTypeFacilityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    /**
     * 根据房型ID删除所有关联
     *
     * @param roomTypeId 房型ID
     */
    @Modifying
    @Query("DELETE FROM RoomTypeFacility rtf WHERE rtf.roomTypeId = :roomTypeId")
    void deleteByRoomTypeId(@Param("roomTypeId") Integer roomTypeId);

    /**
     * 根据房型ID和设施ID列表批量删除
     *
     * @param roomTypeId  房型ID
     * @param facilityIds 设施ID列表
     */
    @Modifying
    @Query("DELETE FROM RoomTypeFacility rtf WHERE rtf.roomTypeId = :roomTypeId AND rtf.facilityId IN :facilityIds")
    void deleteByRoomTypeIdAndFacilityIdIn(@Param("roomTypeId") Integer roomTypeId, @Param("facilityIds") List<Integer> facilityIds);

    /**
     * 根据房型ID查询已关联的设施ID列表
     *
     * @param roomTypeId 房型ID
     * @return 设施ID列表
     */
    @Query("SELECT rtf.facilityId FROM RoomTypeFacility rtf WHERE rtf.roomTypeId = :roomTypeId")
    List<Integer> findFacilityIdsByRoomTypeId(@Param("roomTypeId") Integer roomTypeId);
}
