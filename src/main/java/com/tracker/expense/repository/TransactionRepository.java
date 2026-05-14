package com.tracker.expense.repository;

import com.tracker.expense.entity.Transaction;
import com.tracker.expense.entity.TransactionType;
import com.tracker.expense.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByType(TransactionType type);
    List<Transaction> findByCategory(String category);
    List<Transaction> findByTransactionDateBetween(LocalDate startDate, LocalDate endDate);
    
    // User-filtered queries
    List<Transaction> findByUserOrderByTransactionDateDesc(User user);
    List<Transaction> findByUserAndType(User user, TransactionType type);
    List<Transaction> findByUserAndCategory(User user, String category);
    List<Transaction> findByUserAndTransactionDateBetween(User user, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "t.user = :user AND " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "ORDER BY t.transactionDate DESC")
    List<Transaction> filterTransactionsByUser(User user, TransactionType type, String category, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT t FROM Transaction t WHERE " +
           "(:type IS NULL OR t.type = :type) AND " +
           "(:category IS NULL OR t.category = :category) AND " +
           "(:startDate IS NULL OR t.transactionDate >= :startDate) AND " +
           "(:endDate IS NULL OR t.transactionDate <= :endDate)")
    List<Transaction> filterTransactions(TransactionType type, String category, LocalDate startDate, LocalDate endDate);
}
