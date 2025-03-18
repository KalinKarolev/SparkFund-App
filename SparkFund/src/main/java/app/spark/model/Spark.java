package app.spark.model;

import app.donation.model.Donation;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Spark {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false)
    private BigDecimal goalAmount;

    private BigDecimal currentAmount;

    @Column(nullable = false)
    private String firstPictureUrl;

    private String secondPictureUrl;

    private String thirdPictureUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SparkStatus status;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SparkCategory category;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "spark")
    @OrderBy("createdOn DESC")
    private List<Donation> donations;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
