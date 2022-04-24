package com.wupol.myopia.business.core.system.constants;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.ScreeningTypeEnum;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

/**
 * 模板常量
 *
 * @author Simple4H
 */
@UtilityClass
public final class TemplateConstants {

    /**
     * 学生档案卡-屈光档案(全国)
     */
    public final Integer GLOBAL_TEMPLATE = 1;
    /**
     * 学生档案卡-海南省学生眼疾病筛查单
     */
    public final Integer HAI_NAN_TEMPLATE = 2;
    /**
     * 学生档案卡-近视筛查结果记录表
     */
    public final Integer SCREENING_TEMPLATE = 3;


    /**
     * 模版类型-档案卡
     */
    public final Integer TYPE_TEMPLATE_STUDENT_ARCHIVES = 1;
    /**
     * 模版类型-筛查报告
     */
    public final Integer TYPE_TEMPLATE_SCREENING_REPORT = 2;


    /**
     * 业务类型-视力筛查
     */
    public final Integer TEMPLATE_BIZ_TYPE_VISION = 1;
    /**
     * 业务类型-常见病筛查
     */
    public final Integer TEMPLATE_BIZ_TYPE_COMMON_DISEASE = 2;


    /**
     * 根据筛查类型获取模板业务类型
     *
     * @param screeningType 筛查类型
     * @return java.lang.Integer
     **/
    public Integer getTemplateBizTypeByScreeningType(Integer screeningType) {
        Assert.notNull(screeningType, "筛查类型不能为空");
        if (ScreeningTypeEnum.VISION.getType().equals(screeningType)) {
            return TEMPLATE_BIZ_TYPE_VISION;
        }
        if (ScreeningTypeEnum.COMMON_DISEASE.getType().equals(screeningType)) {
            return TEMPLATE_BIZ_TYPE_COMMON_DISEASE;
        }
        throw new BusinessException("未知筛查类型");
    }
}
