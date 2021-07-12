package com.wupol.myopia.business.api.management.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class ContrastTypeYearItemsDTO {
    private int year;
    private List<YearItemDTO> yearItemDTOList;

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode(callSuper = false)
    @Accessors(chain = true)
    public static class YearItemDTO {
        private int id;
        private String title;
        private Long startTime;
        private Long endTime;
    }
}
