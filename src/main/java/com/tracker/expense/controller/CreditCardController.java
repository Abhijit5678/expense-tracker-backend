package com.tracker.expense.controller;

import com.tracker.expense.dto.CreditCardRequest;
import com.tracker.expense.dto.CreditCardResponse;
import com.tracker.expense.dto.ErrorResponse;
import com.tracker.expense.service.CreditCardService;
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

import java.util.List;

@Tag(name = "Credit Cards", description = "APIs for managing credit cards, tracking outstanding bills, and marking bills as paid")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/api/credit-cards")
@RequiredArgsConstructor
public class CreditCardController {

    private final CreditCardService creditCardService;

    @Operation(
            summary = "Add a new credit card",
            description = "Registers a new credit card for tracking liabilities.",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Credit card added",
                            content = @Content(schema = @Schema(implementation = CreditCardResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Validation error",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping
    public ResponseEntity<CreditCardResponse> addCreditCard(@Valid @RequestBody CreditCardRequest request) {
        return new ResponseEntity<>(creditCardService.addCreditCard(request), HttpStatus.CREATED);
    }

    @Operation(
            summary = "Get all credit cards with current bill status",
            description = "Returns all registered cards along with their current month's outstanding balance, due amount, and due date."
    )
    @GetMapping
    public ResponseEntity<List<CreditCardResponse>> getAllCreditCards() {
        return ResponseEntity.ok(creditCardService.getAllCreditCards());
    }

    @Operation(
            summary = "Mark a credit card bill as paid",
            description = "Sets the bill for a given statement month as paid, clearing the due amount.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bill marked as paid"),
                    @ApiResponse(responseCode = "404", description = "Bill not found for given card and month",
                            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
            }
    )
    @PostMapping("/{id}/pay-bill")
    public ResponseEntity<Void> markBillAsPaid(
            @Parameter(description = "Credit card ID") @PathVariable Long id,
            @Parameter(description = "Statement month in yyyy-MM format", example = "2025-04") @RequestParam String statementMonth) {
        creditCardService.markBillAsPaid(id, statementMonth);
        return ResponseEntity.ok().build();
    }
}
