package cn.ggsn.rxlight.orders.domain;

import lombok.Data;

@Data
public class DiscountedFee {
    private Integer discountChargedFee; // 充电费用，在元的基础上乘以100，以分为最小单位

    private Integer discountServiceFee; // 服务费用，在元的基础上乘以100，以分为最小单位

    private Integer discountTotalFee; // 总费用，在元的基础上乘以100，以分为最小单位
}
