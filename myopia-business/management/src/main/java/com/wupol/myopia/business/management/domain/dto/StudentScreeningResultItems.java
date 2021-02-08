package com.wupol.myopia.business.management.domain.dto;
import com.google.common.collect.Lists;

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
public class StudentScreeningResultItems {

    /**
     * 详情
     */
    private List<StudentResultDetails> details;
}
