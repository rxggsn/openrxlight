package cn.ggsn.openrxlight.request;

import java.util.TreeMap;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DatePageReq extends PageRequest {
    private String startDate;
    private String endDate;

    public DatePageReq(Integer pageNo, Integer pageSize, String startDate, String endDate) {
        super(pageNo, pageSize);
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Override
    public TreeMap<String, String> getQueryParams() {
        var params = super.getQueryParams();
        params.put("start_date", this.startDate);
        params.put("end_date", this.endDate);
        return params;
    }
}
