package com.tracker.expense.dto;

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
public class CreditCardResponse {
    private Long id;
    private String cardName;
    private Integer statementDay;
    private Integer dueDay;
    
    // Details for current bill
    private String currentStatementMonth;
    private BigDecimal totalAmount;
    private BigDecimal dueAmount;
    private LocalDate dueDate;
    private boolean isPaid;
}
