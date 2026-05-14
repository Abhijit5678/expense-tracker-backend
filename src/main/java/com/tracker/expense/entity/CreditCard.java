package com.tracker.expense.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "credit_cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String cardName; // e.g. Chase Sapphire

    private Integer statementDay; // The day of the month when the statement is generated
    private Integer dueDay; // The day of the month when the bill is due

}
