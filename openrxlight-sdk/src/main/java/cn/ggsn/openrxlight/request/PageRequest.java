package cn.ggsn.openrxlight.request;

import java.util.TreeMap;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.collect.Maps;

import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PageRequest {
    private Integer pageNo;
    private Integer pageSize;

    protected PageRequest(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    protected PageRequest() {
        this.pageNo = 1;
        this.pageSize = 10;
    }

    public TreeMap<String, String> getQueryParams() {
        TreeMap<String, String> result = Maps.newTreeMap();
        result.put("page_no", String.valueOf(this.pageNo));
        result.put("page_size", String.valueOf(this.pageSize));
        return result;
    }
}
