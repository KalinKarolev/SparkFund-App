package app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TotalDonationsInfo {

    private BigDecimal totalMoneyRaised;

    private int totalSparksFunded;

    private String firstDonorImage;

    private String firstDonorName;

    private BigDecimal firstDonorDonations;

    private String secondDonorImage;

    private String secondDonorName;

    private BigDecimal secondDonorDonations;

    private String thirdDonorImage;

    private String thirdDonorName;

    private BigDecimal thirdDonorDonations;

}
