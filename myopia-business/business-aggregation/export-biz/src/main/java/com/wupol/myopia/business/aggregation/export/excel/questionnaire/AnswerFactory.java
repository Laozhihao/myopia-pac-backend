package com.wupol.myopia.business.aggregation.export.excel.questionnaire;

import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.export.excel.questionnaire.answer.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 问卷Excel导出门面
 *
 * @author hang.yuan 2022/7/18 11:03
 */
@Service
public class AnswerFactory {

    @Autowired
    private List<Answer> answerList;

    /**
     * 获取用户类型实例
     * @param userType 用户类型
     */
    public Answer getAnswerService(Integer userType){
        return answerList.stream()
                .filter(service->Objects.equals(service.getUserType(),userType))
                .findFirst().orElseThrow(()->new BusinessException(String.format("不存在此类型实例,userType=%s",userType)));
    }

}
