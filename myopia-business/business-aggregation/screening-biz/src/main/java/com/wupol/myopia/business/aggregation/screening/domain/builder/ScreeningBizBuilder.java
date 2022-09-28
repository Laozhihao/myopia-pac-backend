package com.wupol.myopia.business.aggregation.screening.domain.builder;

import cn.hutool.core.collection.CollUtil;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import com.wupol.myopia.business.core.screening.flow.constant.ScreeningConstant;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 筛查业务相关构建
 *
 * @author hang.yuan 2022/9/23 12:09
 */
@UtilityClass
public class ScreeningBizBuilder {

    /**
     * 获取筛查情况
     * @param screeningResultCount
     * @param screeningPlan
     */
    public String getSituation(Integer screeningResultCount, ScreeningPlan screeningPlan) {
        if (screeningPlan.getEndTime().getTime() <= System.currentTimeMillis()){
            return ScreeningConstant.END;
        }
        screeningResultCount = Optional.ofNullable(screeningResultCount).orElse(0);
        return screeningResultCount > 0 ? ScreeningConstant.IN_PROGRESS : ScreeningConstant.NOT_START;
    }

    /**
     * 获得问卷完成学校的状态
     *
     * @param plan
     * @param schoolId
     * @param userQuestionRecordMap
     */
    public String getCountBySchool(ScreeningPlan plan, Integer schoolId, Map<Integer, List<UserQuestionRecord>> userQuestionRecordMap) {
        if (plan.getEndTime().getTime() <= System.currentTimeMillis()) {
            return ScreeningConstant.END;
        }
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordMap.get(schoolId);
        return CollUtil.isNotEmpty(userQuestionRecordList) ? ScreeningConstant.IN_PROGRESS : ScreeningConstant.NOT_START;
    }
}
