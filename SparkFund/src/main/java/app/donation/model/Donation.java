package app.donation.model;

import app.spark.model.Spark;
import app.wallet.model.Wallet;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Donation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 1000)
    private String message;

    @ManyToOne(fetch = FetchType.EAGER)
    private Spark spark;

    @ManyToOne(fetch = FetchType.EAGER)
    private Wallet wallet;

    @Column(nullable = false)
    private LocalDateTime createdOn;
}
