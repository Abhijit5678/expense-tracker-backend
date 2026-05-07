package com.tracker.expense.service;

import com.tracker.expense.dto.BudgetRequest;
import com.tracker.expense.dto.BudgetResponse;
import com.tracker.expense.entity.Budget;
import com.tracker.expense.exception.ResourceNotFoundException;
import com.tracker.expense.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;

    @Transactional
    public BudgetResponse setBudget(BudgetRequest request) {
        Budget budget = budgetRepository.findByMonthYear(request.getMonthYear())
                .orElse(new Budget());
        
        budget.setMonthYear(request.getMonthYear());
        budget.setAmount(request.getAmount());
        if (budget.getCurrentSpent() == null) {
            budget.setCurrentSpent(BigDecimal.ZERO);
        }

        Budget saved = budgetRepository.save(budget);
        return mapToResponse(saved);
    }

    public BudgetResponse getBudget(String monthYear) {
        Budget budget = budgetRepository.findByMonthYear(monthYear)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not set for: " + monthYear));
        return mapToResponse(budget);
    }

    public BudgetResponse checkBudgetStatus(String monthYear) {
        return getBudget(monthYear);
    }

    @Transactional
    public void updateBudgetSpent(LocalDate date, BigDecimal amount) {
        String monthYear = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        budgetRepository.findByMonthYear(monthYear).ifPresent(budget -> {
            budget.setCurrentSpent(budget.getCurrentSpent().add(amount));
            budgetRepository.save(budget);
        });
    }

    @Transactional
    public void revertBudgetSpent(LocalDate date, BigDecimal amount) {
        String monthYear = date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
        budgetRepository.findByMonthYear(monthYear).ifPresent(budget -> {
            budget.setCurrentSpent(budget.getCurrentSpent().subtract(amount));
            budgetRepository.save(budget);
        });
    }

    private BudgetResponse mapToResponse(Budget budget) {
        boolean isExceeded = budget.getCurrentSpent().compareTo(budget.getAmount()) > 0;
        return BudgetResponse.builder()
                .id(budget.getId())
                .monthYear(budget.getMonthYear())
                .amount(budget.getAmount())
                .currentSpent(budget.getCurrentSpent())
                .isExceeded(isExceeded)
                .build();
    }
}
