package cn.ggsn.openrxlight.response.accounts;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonNaming(com.fasterxml.jackson.databind.PropertyNamingStrategies.SnakeCaseStrategy.class)
public class AccountInfo {
    private UUID id;
    private Integer accountType;
    private String displayName;
    private List<String> commands;
}
