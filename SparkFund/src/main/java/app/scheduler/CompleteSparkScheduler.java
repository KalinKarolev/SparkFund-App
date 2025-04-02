package app.scheduler;

import app.spark.model.Spark;
import app.spark.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CompleteSparkScheduler {

    private final SparkService sparkService;


    public CompleteSparkScheduler(SparkService _sparkService) {
        sparkService = _sparkService;
    }

    @Scheduled(fixedRate = 10000)
    public void completeSparks(){
        List<Spark> sparksToBeCompleted = sparkService.findSparksForCompletion();
        if (sparksToBeCompleted.isEmpty()) {
            log.info("No Sparks found for completion");
        }
        for (Spark spark : sparksToBeCompleted) {
            sparkService.completeSpark(spark);
        }
    }
}
