package com.wupol.myopia.business.core.stat.domain.model;

import com.wupol.myopia.business.core.stat.domain.dos.RescreenSituationDO;
import com.wupol.myopia.business.core.stat.domain.dos.VisionAnalysis;
import com.wupol.myopia.business.core.stat.domain.dos.VisionWarningDO;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * 视力筛查结果统计
 *
 * @author hang.yuan 2022/4/7 17:53
 */
@Data
@Accessors(chain = true)
public class VisionScreeningResultStatistic implements Serializable {

    /**
     * 主键id
     */
    private Integer id;

    /**
     * 所属的通知id
     */
    private Integer screeningNoticeId;

    /**
     * 关联的任务id（is_total情况下，可能为0）
     */
    private Integer screeningTaskId;

    /**
     * 筛查计划Id （is_total情况下，可能为0）
     */
    private Integer screeningPlanId;

    /**
     * 筛查类型 （0-视力筛查、1-常见病筛查）
     */
    private Integer screeningType;

    /**
     * 是否合计数据
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
     * 学校类型 0-小学,1-初级中学,2-高级中学,3-完全中学,4-九年一贯制学校,5-十二年一贯制学校,6-职业高中,7其他
     */
    private Integer schoolType;


    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 筛查范围、所属的地区id
     */
    private Integer districtId;



    /**
     * 学校数
     */
    private Integer schoolNum;

    /**
     * 计划的学生数量（默认0）
     */
    private Integer planScreeningNum;

    /**
     * 实际筛查的学生数量（默认0）
     */
    private Integer realScreeningNum;

    /**
     * 完成率
     */
    private String finishRatio;

    /**
     * 纳入统计的实际筛查学生数量（默认0）
     */
    private Integer validScreeningNum;

    /**
     * 纳入统计的实际筛查学生比例
     */
    private String validScreeningRatio;


    /**
     * 视力分析
     */
    private VisionAnalysis visionAnalysis;

    /**
     * 复测情况
     */
    private RescreenSituationDO rescreenSituation;

    /**
     * 视力预警
     */
    private VisionWarningDO visionWarning;


}
