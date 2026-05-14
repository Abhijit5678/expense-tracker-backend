package com.tracker.expense.service;

import com.tracker.expense.dto.AnalyticsSummary;
import com.tracker.expense.dto.CategoryExpense;
import com.tracker.expense.dto.MonthlyTrend;
import com.tracker.expense.entity.Transaction;
import com.tracker.expense.entity.TransactionType;
import com.tracker.expense.entity.User;
import com.tracker.expense.exception.ResourceNotFoundException;
import com.tracker.expense.repository.TransactionRepository;
import com.tracker.expense.repository.UserRepository;
import com.tracker.expense.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public AnalyticsSummary getSummary() {
        User currentUser = getCurrentUser();
        List<Transaction> transactions = transactionRepository.findByUserOrderByTransactionDateDesc(currentUser);
        
        BigDecimal totalIncome = transactions.stream()
                .filter(t -> t.getType() == TransactionType.INCOME)
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalExpense = transactions.stream()
                .filter(t -> t.getType() == TransactionType.EXPENSE && !t.isReimbursed())
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return AnalyticsSummary.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .build();
    }

    public List<CategoryExpense> getCategoryWiseExpense() {
        User currentUser = getCurrentUser();
        List<Transaction> expenses = transactionRepository.findByUserAndType(currentUser, TransactionType.EXPENSE).stream()
                .filter(t -> !t.isReimbursed())
                .collect(Collectors.toList());
        
        Map<String, BigDecimal> categoryMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return categoryMap.entrySet().stream()
                .map(entry -> new CategoryExpense(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public List<MonthlyTrend> getMonthlyTrend() {
        User currentUser = getCurrentUser();
        List<Transaction> expenses = transactionRepository.findByUserAndType(currentUser, TransactionType.EXPENSE).stream()
                .filter(t -> !t.isReimbursed())
                .collect(Collectors.toList());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        Map<String, BigDecimal> monthlyMap = expenses.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getTransactionDate().format(formatter),
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return monthlyMap.entrySet().stream()
                .map(entry -> new MonthlyTrend(entry.getKey(), entry.getValue()))
                .sorted((a, b) -> a.getMonth().compareTo(b.getMonth()))
                .collect(Collectors.toList());
    }
}
