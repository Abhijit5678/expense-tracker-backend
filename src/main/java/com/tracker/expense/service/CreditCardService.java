package com.tracker.expense.service;

import com.tracker.expense.dto.CreditCardRequest;
import com.tracker.expense.dto.CreditCardResponse;
import com.tracker.expense.entity.CreditCard;
import com.tracker.expense.entity.CreditCardBill;
import com.tracker.expense.entity.User;
import com.tracker.expense.exception.ResourceNotFoundException;
import com.tracker.expense.repository.CreditCardBillRepository;
import com.tracker.expense.repository.CreditCardRepository;
import com.tracker.expense.repository.UserRepository;
import com.tracker.expense.security.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditCardService {

    private final CreditCardRepository creditCardRepository;
    private final CreditCardBillRepository creditCardBillRepository;
    private final UserRepository userRepository;

    private User getCurrentUser() {
        String username = SecurityUtil.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    public CreditCardResponse addCreditCard(CreditCardRequest request) {
        User currentUser = getCurrentUser();
        
        CreditCard card = CreditCard.builder()
                .cardName(request.getCardName())
                .statementDay(request.getStatementDay())
                .dueDay(request.getDueDay())
                .user(currentUser)
                .build();
        return mapToResponse(creditCardRepository.save(card));
    }

    public List<CreditCardResponse> getAllCreditCards() {
        User currentUser = getCurrentUser();
        return creditCardRepository.findByUser(currentUser).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markBillAsPaid(Long cardId, String statementMonth) {
        User currentUser = getCurrentUser();
        
        CreditCard card = creditCardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Credit Card not found"));
        
        // Verify that the card belongs to the current user
        if (!card.getUser().getId().equals(currentUser.getId())) {
            throw new ResourceNotFoundException("Credit Card not found");
        }
        
        CreditCardBill bill = creditCardBillRepository.findByCreditCardIdAndStatementMonth(cardId, statementMonth)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found"));
        
        bill.setPaid(true);
        bill.setDueAmount(BigDecimal.ZERO);
        creditCardBillRepository.save(bill);
    }

    private CreditCardResponse mapToResponse(CreditCard card) {
        String currentMonthYear = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
        Optional<CreditCardBill> billOpt = creditCardBillRepository.findByCreditCardIdAndStatementMonth(card.getId(), currentMonthYear);
        
        LocalDate dueDate = null;
        if (card.getDueDay() != null) {
            YearMonth nextMonth = YearMonth.now().plusMonths(1);
            int day = Math.min(card.getDueDay(), nextMonth.lengthOfMonth());
            dueDate = nextMonth.atDay(day);
        }

        if (billOpt.isPresent()) {
            CreditCardBill bill = billOpt.get();
            return CreditCardResponse.builder()
                    .id(card.getId())
                    .cardName(card.getCardName())
                    .statementDay(card.getStatementDay())
                    .dueDay(card.getDueDay())
                    .currentStatementMonth(bill.getStatementMonth())
                    .totalAmount(bill.getTotalAmount())
                    .dueAmount(bill.getDueAmount())
                    .dueDate(dueDate)
                    .isPaid(bill.isPaid())
                    .build();
        }

        return CreditCardResponse.builder()
                .id(card.getId())
                .cardName(card.getCardName())
                .statementDay(card.getStatementDay())
                .dueDay(card.getDueDay())
                .currentStatementMonth(currentMonthYear)
                .totalAmount(BigDecimal.ZERO)
                .dueAmount(BigDecimal.ZERO)
                .dueDate(dueDate)
                .isPaid(true)
                .build();
    }
}
