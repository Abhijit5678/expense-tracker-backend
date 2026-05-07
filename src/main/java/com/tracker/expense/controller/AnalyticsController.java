package com.tracker.expense.controller;

import com.tracker.expense.dto.AnalyticsSummary;
import com.tracker.expense.dto.CategoryExpense;
import com.tracker.expense.dto.MonthlyTrend;
import com.tracker.expense.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Analytics", description = "Dashboard analytics: income/expense summary, category breakdown, and monthly trends")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Operation(
            summary = "Get financial summary",
            description = "Returns total income, total expense (excluding reimbursed lends), and net balance."
    )
    @GetMapping("/summary")
    public ResponseEntity<AnalyticsSummary> getSummary() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }

    @Operation(
            summary = "Get category-wise expense breakdown",
            description = "Returns aggregated expenses grouped by category. Excludes reimbursed transactions."
    )
    @GetMapping("/category")
    public ResponseEntity<List<CategoryExpense>> getCategoryWiseExpense() {
        return ResponseEntity.ok(analyticsService.getCategoryWiseExpense());
    }

    @Operation(
            summary = "Get monthly expense trend",
            description = "Returns month-by-month expense totals sorted by date. Used for bar chart visualization."
    )
    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyTrend>> getMonthlyTrend() {
        return ResponseEntity.ok(analyticsService.getMonthlyTrend());
    }
}
