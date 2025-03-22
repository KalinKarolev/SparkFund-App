package app.spark.repostiroty;

import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SparkRepository extends JpaRepository<Spark, UUID> {

    List<Spark> findAllByStatusOrderByCreatedOnDesc(SparkStatus status);

    List<Spark> findAllByOrderByCreatedOnDesc();

    @Query("SELECT s FROM Spark s WHERE s.currentAmount >= s.goalAmount AND s.status = 'ACTIVE'")
    List<Spark> findAllWhereStatusActiveAndCurrentAmountIsGreaterThanOrEqualToGoalAmount();
}
