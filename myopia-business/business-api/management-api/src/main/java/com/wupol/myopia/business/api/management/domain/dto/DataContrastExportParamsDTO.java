package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DataContrastExportParamsDTO {
    private Integer contrastType;
    private List<Params> params;

    @Data
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public static class Params {
        private Integer contrastId;
        private Integer districtId;
        private Integer schoolAge;
        private Integer schoolId;
        private String schoolGradeCode;
        private String schoolClass;
    }
}
