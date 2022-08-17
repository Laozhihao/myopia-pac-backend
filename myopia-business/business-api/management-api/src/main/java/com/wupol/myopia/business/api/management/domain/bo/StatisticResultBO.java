package com.wupol.myopia.business.api.management.domain.bo;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 结果统计流转实体
 *
 * @author hang.yuan 2022/4/18 18:08
 */
@Data
@Accessors(chain = true)
public class StatisticResultBO implements Serializable {


    /**
     * 筛查通知ID
     */
    private Integer screeningNoticeId;

    /**
     * 筛查类型
     */
    private Integer screeningType;

    /**
     * 区域ID
     */
    private Integer districtId;

    /**
     * 计划学生数
     */
    private Integer planStudentCount;

    /**
     * 筛查结果结论
     */
    private List<StatConclusion> statConclusions;

    /**
     * 筛查任务ID
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划ID
     */
    private Integer screeningPlanId;

    /**
     * 是否合计
     */
    private Boolean isTotal;

    /**
     * 学校ID
     */
    private Integer schoolId;

    /**
     * 筛查机构ID
     */
    private Integer screeningOrgId;
    /**
     * 学校类型
     */
    private Integer schoolType;

    /**
     * 参与筛查的学生
     */
    private List<ScreeningPlanSchoolStudent> planSchoolStudentList;

}
