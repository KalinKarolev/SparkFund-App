package app.user.model;

import app.signal.model.Signal;
import app.spark.Spark;
import app.wallet.Wallet;
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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    private String firstName;

    private String lastName;

    private String profilePicture;

    @Column(unique = true)
    private String email;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus userStatus;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "creator")
    @OrderBy("createdOn DESC")
    private List<Spark> createdSparks;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER, mappedBy = "owner")
    private Wallet wallet;

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "creator")
    @OrderBy("createdOn DESC")
    private List<Signal> createdSignals;

    @Column(nullable = false)
    private LocalDateTime createdOn;

    private LocalDateTime updatedOn;

    private Boolean isAnonymousDonator;

}
