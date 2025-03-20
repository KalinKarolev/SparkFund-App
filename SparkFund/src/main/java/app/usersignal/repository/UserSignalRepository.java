package app.usersignal.repository;

import app.usersignal.model.UserSignal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserSignalRepository extends JpaRepository<UserSignal, UUID> {
}
