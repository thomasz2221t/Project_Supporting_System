package pl.polsl.projectmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PresenceRequestDto {

    private Long presenceId;
    private Boolean wasPresent;
}
