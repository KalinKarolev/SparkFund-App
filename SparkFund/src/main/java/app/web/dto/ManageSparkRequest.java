package app.web.dto;

import app.spark.model.SparkCategory;
import app.spark.model.SparkStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.URL;

import java.math.BigDecimal;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManageSparkRequest {

    @NotBlank(message = "Spark title should not be empty")
    @Size(min = 1, max = 50, message = "Spark title must be between 1 and 50 symbols")
    private String title;

    @NotBlank(message = "Spark description should not be empty")
    @Size(min = 50, max = 1000, message = "Spark description must be between 50 and 1000 symbols")
    private String description;

    @NotNull
    private BigDecimal goalAmount;

    private BigDecimal currentAmount;

    @NotNull
    private SparkCategory category;

    @NotNull
    private SparkStatus status;

    @NotBlank(message = "Each Spark should have at least one picture")
    @URL
    private String firstPictureUrl;

    @URL
    private String secondPictureUrl;

    @URL
    private String thirdPictureUrl;
}
