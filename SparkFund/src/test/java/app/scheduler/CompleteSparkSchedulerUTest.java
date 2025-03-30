package app.scheduler;

import app.spark.model.Spark;
import app.spark.service.SparkService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompleteSparkSchedulerUTest {

    @Mock
    private SparkService sparkService;

    @InjectMocks
    private CompleteSparkScheduler scheduler;

    @Test
    void givenSparksForCompletion_whenCompleteSparks_thenCompleteThem() {
        Spark spark1 = Spark.builder()
                .id(UUID.randomUUID())
                .build();
        Spark spark2 = Spark.builder()
                .id(UUID.randomUUID())
                .build();
        List<Spark> sparks = List.of(spark1, spark2);

        when(sparkService.findSparksForCompletion()).thenReturn(sparks);

        scheduler.completeSparks();

        verify(sparkService, times(1)).findSparksForCompletion();
        verify(sparkService, times(1)).completeSpark(spark1);
        verify(sparkService, times(1)).completeSpark(spark2);
    }

    @Test
    void givenNoSparksForCompletion_whenCompleteSparks_thenDoNothing() {
        when(sparkService.findSparksForCompletion()).thenReturn(Collections.emptyList());

        scheduler.completeSparks();

        verify(sparkService, times(1)).findSparksForCompletion();
        verify(sparkService, never()).completeSpark(any());
    }
}
