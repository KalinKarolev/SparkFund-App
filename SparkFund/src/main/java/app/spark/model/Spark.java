package app.spark.model;

import app.donation.model.Donation;
import app.user.model.User;
import jakarta.persistence.*;
import lombok.*;

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

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String firstPictureUrl;

    private String secondPictureUrl;

    private String thirdPictureUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SparkStatus sparkStatus;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SparkCategory sparkCategory;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "spark")
    @OrderBy("createdOn DESC")
    private List<Donation> donations;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;
}
