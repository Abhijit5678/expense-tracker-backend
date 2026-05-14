package com.tracker.expense.repository;

import com.tracker.expense.entity.Budget;
import com.tracker.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByMonthYear(String monthYear);
    
    // User-filtered query
    Optional<Budget> findByUserAndMonthYear(User user, String monthYear);
}
