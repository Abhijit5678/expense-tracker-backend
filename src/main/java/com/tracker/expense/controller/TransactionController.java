package com.tracker.expense.controller;

import com.tracker.expense.dto.TransactionRequest;
import com.tracker.expense.dto.TransactionResponse;
import com.tracker.expense.dto.ErrorResponse;
import com.tracker.expense.entity.TransactionType;
import com.tracker.expense.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Transactions", description = "APIs for managing income and expense transactions")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(
            summary = "Add a new transaction",
            description = "Creates a new income or expense transaction. Automatically updates budget and credit card bill if applicable.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transaction created successfully",
                            content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation failed",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @PostMapping
    public ResponseEntity<TransactionResponse> addTransaction(@Valid @RequestBody TransactionRequest request) {
        return new ResponseEntity<>(transactionService.createTransaction(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all transactions",
            description = "Returns all transactions sorted by date descending. Reimbursed lends are included but can be filtered client-side.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of transactions returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            }
    )
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return ResponseEntity.ok(transactionService.getAllTransactions());
    }

    @Operation(
            summary = "Filter transactions",
            description = "Filter transactions by type, category, and date range. All filters are optional."
    )
    @GetMapping("/filter")
    public ResponseEntity<List<TransactionResponse>> filterTransactions(
            @Parameter(description = "Transaction type: INCOME or EXPENSE") @RequestParam(required = false) TransactionType type,
            @Parameter(description = "Category name e.g. Food, Travel, Salary") @RequestParam(required = false) String category,
            @Parameter(description = "Start date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(transactionService.filterTransactions(type, category, startDate, endDate));
    }

    @Operation(
            summary = "Update a transaction",
            description = "Updates an existing transaction by ID. Automatically adjusts budget and credit card bill.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction updated"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> updateTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id,
            @Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.ok(transactionService.updateTransaction(id, request));
    }

    @Operation(
            summary = "Delete a transaction",
            description = "Permanently deletes a transaction and reverts its budget / credit card bill impact.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Transaction not found",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "Transaction ID") @PathVariable Long id) {
        transactionService.deleteTransaction(id);
        return ResponseEntity.noContent().build();
    }
}
