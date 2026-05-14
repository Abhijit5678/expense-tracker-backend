package com.tracker.expense.repository;

import com.tracker.expense.entity.CreditCard;
import com.tracker.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    // User-filtered query
    List<CreditCard> findByUser(User user);
}
