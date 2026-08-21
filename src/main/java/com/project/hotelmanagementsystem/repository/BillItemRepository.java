package com.project.hotelmanagementsystem.repository;

import com.project.hotelmanagementsystem.entity.BillItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 账单明细Repository接口
 */
public interface BillItemRepository extends JpaRepository<BillItem, Integer> {

    /**
     * 根据账单ID查询账单明细列表
     *
     * @param billId 账单ID
     * @return 账单明细列表
     */
    List<BillItem> findByBillId(Integer billId);

    /**
     * 根据项目类型查询账单明细列表
     *
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    List<BillItem> findByItemType(String itemType);

    /**
     * 根据账单ID和项目类型查询账单明细列表
     *
     * @param billId   账单ID
     * @param itemType 项目类型
     * @return 账单明细列表
     */
    List<BillItem> findByBillIdAndItemType(Integer billId, String itemType);

    /**
     * 根据酒店ID查询账单明细列表（通过bill → checkIn → room关联）
     *
     * @param hotelId 酒店ID
     * @return 账单明细列表
     */
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<BillItem> findAllByHotelId(@Param("hotelId") Integer hotelId);

    /**
     * 根据账单明细ID查询关联的酒店ID
     *
     * @param billItemId 账单明细ID
     * @return 酒店ID
     */
    @Query("SELECT r.hotelId FROM Room r WHERE r.id = (SELECT c.roomId FROM CheckIn c WHERE c.id = (SELECT b.checkInId FROM Bill b WHERE b.id = (SELECT bi.billId FROM BillItem bi WHERE bi.id = :billItemId)))")
    Optional<Integer> findHotelIdByBillItemId(@Param("billItemId") Integer billItemId);

    /**
     * 根据账单ID和酒店ID查询账单明细列表
     *
     * @param billId 账单ID
     * @param hotelId 酒店ID
     * @return 账单明细列表
     */
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<BillItem> findByBillIdAndHotelId(@Param("billId") Integer billId, @Param("hotelId") Integer hotelId);

    /**
     * 根据项目类型和酒店ID查询账单明细列表
     *
     * @param itemType 项目类型
     * @param hotelId 酒店ID
     * @return 账单明细列表
     */
    @Query("SELECT bi FROM BillItem bi WHERE bi.itemType = :itemType AND bi.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<BillItem> findByItemTypeAndHotelId(@Param("itemType") String itemType, @Param("hotelId") Integer hotelId);

    /**
     * 根据账单ID、项目类型和酒店ID查询账单明细列表
     *
     * @param billId   账单ID
     * @param itemType 项目类型
     * @param hotelId  酒店ID
     * @return 账单明细列表
     */
    @Query("SELECT bi FROM BillItem bi WHERE bi.billId = :billId AND bi.itemType = :itemType AND bi.billId IN (SELECT b.id FROM Bill b WHERE b.checkInId IN (SELECT c.id FROM CheckIn c WHERE c.roomId IN (SELECT r.id FROM Room r WHERE r.hotelId = :hotelId)))")
    List<BillItem> findByBillIdAndItemTypeAndHotelId(@Param("billId") Integer billId, @Param("itemType") String itemType, @Param("hotelId") Integer hotelId);

    /**
     * 统计账单明细的总金额
     */
    @Query("SELECT COALESCE(SUM(bi.amount), 0) FROM BillItem bi WHERE bi.billId = :billId")
    java.math.BigDecimal sumAmountByBillId(@Param("billId") Integer billId);
}
