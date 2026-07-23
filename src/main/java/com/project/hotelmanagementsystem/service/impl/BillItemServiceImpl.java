package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.BillItem;
import com.project.hotelmanagementsystem.repository.BillItemRepository;
import com.project.hotelmanagementsystem.service.BillItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 账单明细Service实现类
 */
@Service
@Transactional
public class BillItemServiceImpl implements BillItemService {

    private final BillItemRepository billItemRepository;

    @Autowired
    public BillItemServiceImpl(BillItemRepository billItemRepository) {
        this.billItemRepository = billItemRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BillItem> findById(Integer id) {
        return billItemRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillItem> findAll() {
        return billItemRepository.findAll();
    }

    @Override
    public BillItem save(BillItem billItem) {
        return billItemRepository.save(billItem);
    }

    @Override
    public void deleteById(Integer id) {
        billItemRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillItem> findByBillId(Integer billId) {
        return billItemRepository.findByBillId(billId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillItem> findByItemType(String itemType) {
        return billItemRepository.findByItemType(itemType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BillItem> findByBillIdAndItemType(Integer billId, String itemType) {
        return billItemRepository.findByBillIdAndItemType(billId, itemType);
    }
}