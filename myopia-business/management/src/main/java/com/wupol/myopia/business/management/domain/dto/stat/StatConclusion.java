package com.wupol.myopia.business.management.domain.dto.stat;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

/**
 * 筛查数据结论
 */
@Data
@Builder
public class StatConclusion {

    /** id */
    private Integer id;

    /** 源筛查数据id */
    private Integer resultId;

    /** 任务id */
    private Integer taskId;

    /** 所属地区id */
    private Integer districtId;

    /** 学龄 */
    private Integer schoolAge;

    /** 性别 */
    private Integer gender;

    /** 预警级别 */
    private Integer warningLevel;

    /** 左眼视力 */
    private Float visionL;

    /** 右眼视力 */
    private Float visionR;

    /** 是否视力低下 */
    private Boolean isLowVision;

    /** 是否屈光不正 */
    private Boolean isRefractiveError;

    /** 是否近视 */
    private Boolean isMyopia;

    /** 是否远视 */
    private Boolean isHyperopia;

    /** 是否散光 */
    private Boolean isAstigmatism;

    /** 是否戴镜 */
    private Boolean isWearingGlasses;

    /** 是否建议就诊 */
    private Boolean isRecommendVisit;

    /** 是否复测 */
    private Boolean isRescreen;

    /** 复测错误项次 */
    private Integer rescreenErrorNum;

    /** 是否有效数据 */
    private Boolean isValid;

    /** 创建时间 */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
