package com.wupol.myopia.business.core.stat.domain.model;

import com.wupol.myopia.business.core.stat.domain.dos.CommonDiseaseDO;
import com.wupol.myopia.business.core.stat.domain.dos.QuestionnaireDO;
import com.wupol.myopia.business.core.stat.domain.dos.SaprodontiaDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.Valid;

/**
 * 常见病筛查结果统计
 *
 * @author hang.yuan 2022/4/7 17:54
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CommonDiseaseScreeningResultStatistic extends VisionScreeningResultStatistic {

    /**
     * 龋齿情况
     */
    @Valid
    private SaprodontiaDO saprodontia;

    /**
     *  常见病分析
     */
    private CommonDiseaseDO commonDisease;

    /**
     *  问卷情况
     */
    private QuestionnaireDO questionnaire;

}
