package com.project.hotelmanagementsystem.service.impl;

import com.project.hotelmanagementsystem.entity.Bill;
import com.project.hotelmanagementsystem.repository.BillRepository;
import com.project.hotelmanagementsystem.service.BillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 账单Service实现类
 */
@Service
@Transactional
public class BillServiceImpl implements BillService {

    private final BillRepository billRepository;

    @Autowired
    public BillServiceImpl(BillRepository billRepository) {
        this.billRepository = billRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> findById(Integer id) {
        return billRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findAll() {
        return billRepository.findAll();
    }

    @Override
    public Bill save(Bill bill) {
        return billRepository.save(bill);
    }

    @Override
    public void deleteById(Integer id) {
        billRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findByCheckInId(Integer checkInId) {
        return billRepository.findByCheckInId(checkInId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bill> findByBillStatus(String billStatus) {
        return billRepository.findByBillStatus(billStatus);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Bill> findFirstByCheckInIdOrderByIdDesc(Integer checkInId) {
        return billRepository.findFirstByCheckInIdOrderByIdDesc(checkInId);
    }
}