package com.tracker.expense.repository;

import com.tracker.expense.entity.CreditCardBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardBillRepository extends JpaRepository<CreditCardBill, Long> {
    Optional<CreditCardBill> findByCreditCardIdAndStatementMonth(Long creditCardId, String statementMonth);
}
