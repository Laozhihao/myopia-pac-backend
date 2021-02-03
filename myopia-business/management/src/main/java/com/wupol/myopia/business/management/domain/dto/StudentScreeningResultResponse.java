package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.VisionScreeningResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生筛查档案
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentScreeningResultResponse {

    private Integer total;

    private List<VisionScreeningResult> items;
}
