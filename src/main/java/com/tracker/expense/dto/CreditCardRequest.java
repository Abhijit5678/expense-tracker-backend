package com.tracker.expense.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreditCardRequest {
    @NotBlank(message = "Card name is required")
    private String cardName;
    @NotNull(message = "Statement day is required")
    private Integer statementDay;
    @NotNull(message = "Due day is required")
    private Integer dueDay;
}
