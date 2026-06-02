package cn.ggsn.openrxlight.model.system;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SystemData {
    private List<CarBrandTree> carBrandTrees;
    private List<CarType> carTypes;

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CarBrandTree {
        private Long brandId;
        private String name;
        private Integer status;
        private String icon;
        private List<CarType> children;
    }

    @Getter
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class CarType {
        private Long typeId;
        private String name;
        private Boolean enable;
        private String brandName;
    }
}
