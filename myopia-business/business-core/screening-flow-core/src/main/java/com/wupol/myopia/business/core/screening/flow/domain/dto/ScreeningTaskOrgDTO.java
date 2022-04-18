package com.wupol.myopia.business.core.screening.flow.domain.dto;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningTaskOrg;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.List;

/**
 * 筛查任务机构
 * @Author Alix
 * @Date 2021/01/25
 **/

@Data
@Accessors(chain = true)
public class ScreeningTaskOrgDTO extends ScreeningTaskOrg {

    /** 筛查机构名称 */
    private String name;
    /**
     * 筛查任务--开始时间
     */
    private Date startTime;

    /**
     * 筛查任务--结束时间
     */
    private Date endTime;

    /**
     * 筛查学校数量
     */
    private Integer screeningSchoolNum;

    /**
     * 筛查情况
     */
    private String screeningSituation;

    /**
     * 问卷情况
     */
    private String questionnaire;

    /**
     * 筛查学校情况
     */
    private List<ScreeningPlanSchoolDTO> screeningPlanSchoolDTOS;
}