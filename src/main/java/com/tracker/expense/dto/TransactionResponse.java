package com.tracker.expense.dto;

import com.tracker.expense.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private Long id;
    private BigDecimal amount;
    private TransactionType type;
    private String category;
    private LocalDate transactionDate;
    private String description;
    private com.tracker.expense.entity.PaymentMethod paymentMethod;
    private Long creditCardId;
    private String creditCardName;
    private boolean isReimbursed;
}
