package com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.business.aggregation.export.excel.domain.AnswerDataBO;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.questionnaire.domain.model.UserQuestionRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 政府答案实现
 *
 * @author hang.yuan 2022/8/25 10:40
 */
@Service
public class GovernmentAnswerImpl extends AbstractUserAnswer {

    @Autowired
    private DistrictService districtService;

    @Override
    public Integer getUserType() {
        return UserType.QUESTIONNAIRE_GOVERNMENT.getType();
    }

    @Override
    public List<UserQuestionRecord> getAnswerData(AnswerDataBO answerDataBO) {
        List<UserQuestionRecord> userQuestionRecordList = answerDataBO.getUserQuestionRecordList();
        ExportCondition exportCondition = answerDataBO.getExportCondition();
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordList;
        }

        List<Integer> districtIdList = filterDistrict(exportCondition.getDistrictId());
        Stream<UserQuestionRecord> stream = userQuestionRecordList.stream();
        if (Objects.nonNull(districtIdList)) {
            stream = stream.filter(userQuestionRecord -> {
                        if (Objects.isNull(userQuestionRecord)){
                            return false;
                        }
                        return districtIdList.contains(Integer.valueOf(userQuestionRecord.getDistrictCode().toString().substring(0,6)));
                    });
        }

        return stream.collect(Collectors.toList());
    }


    @Override
    protected List<Integer> filterDistrict(Integer districtId) {
        List<Integer> districtIdList = super.filterDistrict(districtId);
        if (Objects.isNull(districtIdList)){
            return districtIdList;
        }
        List<District> districtList = districtService.getDistrictByIds(districtIdList);
        Set<Integer> districtCodes = districtList.stream().map(district -> district.getCode().toString().substring(0, 6)).map(Integer::valueOf).collect(Collectors.toSet());
        return Lists.newArrayList(districtCodes);
    }
}
