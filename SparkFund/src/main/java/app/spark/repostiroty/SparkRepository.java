package app.spark.repostiroty;

import app.spark.model.Spark;
import app.spark.model.SparkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SparkRepository extends JpaRepository<Spark, UUID> {

    List<Spark> findAllByStatus(SparkStatus status);
}
