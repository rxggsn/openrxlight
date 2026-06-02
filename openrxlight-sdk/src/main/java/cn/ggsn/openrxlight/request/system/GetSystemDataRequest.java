package cn.ggsn.openrxlight.request.system;

import java.util.TreeMap;

import cn.ggsn.openrxlight.request.PageRequest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = false)
public class GetSystemDataRequest extends PageRequest {
    private Integer type;

    @Override
    public TreeMap<String, String> getQueryParams() {
        var params = super.getQueryParams();
        params.put("type", String.valueOf(this.getType()));
        return params;
    }

}
