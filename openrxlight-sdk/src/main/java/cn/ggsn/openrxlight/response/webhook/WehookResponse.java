package cn.ggsn.openrxlight.response.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WehookResponse {
    private Integer success;

    public boolean isSuccess() {
        return success != null && success == 1;
    }
}
