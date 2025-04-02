package app.web.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailEvent {

    private String userEmail;

    private String signalTitle;

    private String adminResponse;

}
