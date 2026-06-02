package cn.ggsn.rxlight.ai.claw.impl.lark.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileMessage {
    @JsonProperty("file_key")
    private String fileKey;
}
