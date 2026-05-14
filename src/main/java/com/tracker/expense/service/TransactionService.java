package com.tracker.expense.service;

import com.tracker.expense.dto.TransactionRequest;
import com.tracker.expense.dto.TransactionResponse;
import com.tracker.expense.entity.Transaction;
import com.tracker.expense.entity.TransactionType;
import com.tracker.expense.entity.User;
import com.tracker.expense.exception.ResourceNotFoundException;
import com.tracker.expense.repository.TransactionRepository;
import com.tracker.expense.repository.UserRepository;
import com.tracker.expense.repository.CreditCardRepository;
import com.tracker.expense.repository.CreditCardBillRepository;
import com.tracker.expense.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;
    private final com.tracker.expense.repository.CreditCardRepository creditCardRepository;
    private final com.tracker.expense.repository.CreditCardBillRepository creditCardBillRepository;

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        User currentUser = getCurrentUser();
        
        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .type(request.getType())
                .category(request.getCategory())
                .transactionDate(request.getTransactionDate())
                .description(request.getDescription())
                .paymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : com.tracker.expense.entity.PaymentMethod.CASH)
                .isReimbursed(request.isReimbursed())
                .user(currentUser)
                .build();

        if (request.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && request.getCreditCardId() != null) {
            com.tracker.expense.entity.CreditCard card = creditCardRepository.findById(request.getCreditCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Credit Card not found"));
            
            // Verify that the card belongs to the current user
            if (!card.getUser().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Credit Card not found");
            }
            transaction.setCreditCard(card);
        }

        Transaction saved = transactionRepository.save(transaction);
        
        if (saved.getType() == TransactionType.EXPENSE && !saved.isReimbursed()) {
            budgetService.updateBudgetSpent(saved.getTransactionDate(), saved.getAmount());
        }

        if (saved.getType() == TransactionType.EXPENSE && saved.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && saved.getCreditCard() != null) {
            updateCreditCardBill(saved.getCreditCard(), saved.getTransactionDate(), saved.getAmount());
        }
        
        return mapToResponse(saved);
    }

    public List<TransactionResponse> getAllTransactions() {
        User currentUser = getCurrentUser();
        return transactionRepository.findByUserOrderByTransactionDateDesc(currentUser).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TransactionResponse> filterTransactions(TransactionType type, String category, LocalDate startDate, LocalDate endDate) {
        User currentUser = getCurrentUser();
        return transactionRepository.filterTransactionsByUser(currentUser, type, category, startDate, endDate).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse updateTransaction(Long id, TransactionRequest request) {
        User currentUser = getCurrentUser();
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Verify that the transaction belongs to the current user
        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }

        // Revert old budget and old bill if expense
        if (transaction.getType() == TransactionType.EXPENSE) {
            if (!transaction.isReimbursed()) {
                budgetService.revertBudgetSpent(transaction.getTransactionDate(), transaction.getAmount());
            }
            if (transaction.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && transaction.getCreditCard() != null) {
                revertCreditCardBill(transaction.getCreditCard(), transaction.getTransactionDate(), transaction.getAmount());
            }
        }

        transaction.setAmount(request.getAmount());
        transaction.setType(request.getType());
        transaction.setCategory(request.getCategory());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setDescription(request.getDescription());
        transaction.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : com.tracker.expense.entity.PaymentMethod.CASH);
        transaction.setReimbursed(request.isReimbursed());

        if (request.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && request.getCreditCardId() != null) {
            com.tracker.expense.entity.CreditCard card = creditCardRepository.findById(request.getCreditCardId())
                    .orElseThrow(() -> new ResourceNotFoundException("Credit Card not found"));
            
            // Verify that the card belongs to the current user
            if (!card.getUser().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Credit Card not found");
            }
            transaction.setCreditCard(card);
        } else {
            transaction.setCreditCard(null);
        }

        Transaction saved = transactionRepository.save(transaction);
        
        // Apply new budget and bill if expense
        if (saved.getType() == TransactionType.EXPENSE) {
            if (!saved.isReimbursed()) {
                budgetService.updateBudgetSpent(saved.getTransactionDate(), saved.getAmount());
            }
            if (saved.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && saved.getCreditCard() != null) {
                updateCreditCardBill(saved.getCreditCard(), saved.getTransactionDate(), saved.getAmount());
            }
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTransaction(Long id) {
        User currentUser = getCurrentUser();
        
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        // Verify that the transaction belongs to the current user
        if (!transaction.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Transaction not found with id: " + id);
        }

        if (transaction.getType() == TransactionType.EXPENSE) {
            if (!transaction.isReimbursed()) {
                budgetService.revertBudgetSpent(transaction.getTransactionDate(), transaction.getAmount());
            }
            if (transaction.getPaymentMethod() == com.tracker.expense.entity.PaymentMethod.CREDIT_CARD && transaction.getCreditCard() != null) {
                revertCreditCardBill(transaction.getCreditCard(), transaction.getTransactionDate(), transaction.getAmount());
            }
        }

        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .transactionDate(transaction.getTransactionDate())
                .description(transaction.getDescription())
                .paymentMethod(transaction.getPaymentMethod())
                .creditCardId(transaction.getCreditCard() != null ? transaction.getCreditCard().getId() : null)
                .creditCardName(transaction.getCreditCard() != null ? transaction.getCreditCard().getCardName() : null)
                .isReimbursed(transaction.isReimbursed())
                .build();
    }

    private void updateCreditCardBill(com.tracker.expense.entity.CreditCard card, LocalDate date, java.math.BigDecimal amount) {
        String monthYear = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        com.tracker.expense.entity.CreditCardBill bill = creditCardBillRepository.findByCreditCardIdAndStatementMonth(card.getId(), monthYear)
                .orElse(com.tracker.expense.entity.CreditCardBill.builder()
                        .creditCard(card)
                        .statementMonth(monthYear)
                        .totalAmount(java.math.BigDecimal.ZERO)
                        .dueAmount(java.math.BigDecimal.ZERO)
                        .isPaid(false)
                        .build());
        
        bill.setTotalAmount(bill.getTotalAmount().add(amount));
        if (!bill.isPaid()) {
            bill.setDueAmount(bill.getDueAmount().add(amount));
        }
        
        creditCardBillRepository.save(bill);
    }

    private void revertCreditCardBill(com.tracker.expense.entity.CreditCard card, LocalDate date, java.math.BigDecimal amount) {
        String monthYear = date.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
        creditCardBillRepository.findByCreditCardIdAndStatementMonth(card.getId(), monthYear).ifPresent(bill -> {
            bill.setTotalAmount(bill.getTotalAmount().subtract(amount));
            if (!bill.isPaid()) {
                bill.setDueAmount(bill.getDueAmount().subtract(amount));
            }
            creditCardBillRepository.save(bill);
        });
    }
}
