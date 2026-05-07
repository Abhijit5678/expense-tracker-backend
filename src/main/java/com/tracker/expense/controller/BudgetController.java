package com.tracker.expense.controller;

import com.tracker.expense.dto.BudgetRequest;
import com.tracker.expense.dto.BudgetResponse;
import com.tracker.expense.dto.ErrorResponse;
import com.tracker.expense.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Budget", description = "APIs for setting monthly budgets and tracking spending against them")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/budget")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @Operation(
            summary = "Set or update monthly budget",
            description = "Creates or updates the budget for a given month (format: yyyy-MM).",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Budget set successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid request",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<BudgetResponse> setBudget(@Valid @RequestBody BudgetRequest request) {
        return new ResponseEntity<>(budgetService.setBudget(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get budget for a month",
            description = "Returns the configured budget amount for the given month."
    )
    @GetMapping
    public ResponseEntity<BudgetResponse> getBudget(
            @Parameter(description = "Month in yyyy-MM format", example = "2025-04") @RequestParam String monthYear) {
        return ResponseEntity.ok(budgetService.getBudget(monthYear));
    }

    @Operation(
            summary = "Check budget status",
            description = "Returns budget amount, current spent amount, remaining, and whether budget is exceeded."
    )
    @GetMapping("/status")
    public ResponseEntity<BudgetResponse> checkBudgetStatus(
            @Parameter(description = "Month in yyyy-MM format", example = "2025-04") @RequestParam String monthYear) {
        return ResponseEntity.ok(budgetService.checkBudgetStatus(monthYear));
    }
}
