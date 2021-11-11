package com.wupol.myopia.business.core.screening.flow.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 学生跟踪预警返回
 *
 * @author Simple4H
 */
@Getter
@Setter
public class StudentTrackWarningResponseDTO {

    private String sno;

    private Integer studentId;

    private Integer reportId;

    private String name;

    private Integer gender;

    private String className;

    private String gradeName;

    /**
     * 近视预警等级
     * {@link com.wupol.myopia.business.common.utils.constant.MyopiaLevelEnum}
     */
    private Integer myopiaLevel;

    /**
     * 远视预警等级
     * {@link com.wupol.myopia.business.common.utils.constant.HyperopiaLevelEnum}
     */
    private Integer hyperopiaLevel;

    /**
     * 散光预警等级
     * {@link com.wupol.myopia.business.common.utils.constant.AstigmatismLevelEnum}
     */
    private Integer astigmatismLevel;

    /**
     * 预警等级
     * {@link com.wupol.myopia.business.common.utils.constant.WarningLevel}
     */
    private Integer warningLevel;

    /**
     * 是否绑定公众号
     */
    private Boolean isBindMq;

    /**
     * 是否复查
     */
    private Boolean isReview;

    /**
     * 就诊结论(医生反馈)
     */
    private String visitResult;

    /**
     * 配镜建议
     */
    private Integer glassesSuggest;

    /**
     * 课桌型号
     */
    private List<Integer> deskType;

    /**
     * 课桌建议高度
     */
    private Integer deskAdviseHeight;

    /**
     * 课椅型号
     */
    private List<Integer> chairType;

    /**
     * 课椅建议高度
     */
    private Integer chairAdviseHeight;
}
