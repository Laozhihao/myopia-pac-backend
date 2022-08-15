package com.wupol.myopia.business.core.questionnaire.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesDataDO;
import com.wupol.myopia.business.core.questionnaire.domain.dto.EditQuestionnaireRequestDTO;
import com.wupol.myopia.business.core.questionnaire.domain.mapper.QuestionnaireQuestionMapper;
import com.wupol.myopia.business.core.questionnaire.domain.model.QuestionnaireQuestion;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @Author Simple4H
 * @Date 2022-07-06
 */
@Service
public class QuestionnaireQuestionService extends BaseService<QuestionnaireQuestionMapper, QuestionnaireQuestion> {

    /**
     * 插入题目
     */
    public void insert(Integer questionnaireId, List<EditQuestionnaireRequestDTO.Detail> details, Integer pid) {
        if (CollectionUtils.isEmpty(details)) {
            return;
        }
        details.forEach(detail -> {
            QuestionnaireQuestion question = new QuestionnaireQuestion();
            question.setQuestionnaireId(questionnaireId);
            question.setQuestionId(detail.getId());
            question.setPid(pid);
            question.setSerialNumber(detail.getSerialNumber());
            question.setRequired(detail.getRequired());
            question.setSort(1);
            question.setIsNotShowNumber(detail.getIsNotShowNumber());
            question.setJumpIds(detail.getJumpIds());
            question.setIsLogic(detail.getIsLogic());
            List<QesDataDO> qesData = detail.getQesData();
            if (!CollectionUtils.isEmpty(qesData) &&
                    qesData.stream().map(QesDataDO::getOptionId).filter(StringUtils::isNotBlank).count() != qesData.size()) {
                throw new BusinessException("选项Id异常");
            }
            question.setQesData(qesData);
            question.setIsHidden(detail.getIsHidden());
            baseMapper.insert(question);
            List<EditQuestionnaireRequestDTO.Detail> questionList = detail.getQuestionList();
            if (!CollectionUtils.isEmpty(questionList)) {
                insert(questionnaireId, questionList, question.getId());
            }
        });
    }

    /**
     * 通过序号获取问题
     */
    public List<QuestionnaireQuestion> getBySerialNumbers(Integer questionnaireId, Collection<String> serialNumber) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        wrapper.in(QuestionnaireQuestion::getSerialNumber, serialNumber);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 通过问卷Id获取QuestionnaireQuestion
     *
     * @param questionnaireId
     *
     * @return
     */
    public List<QuestionnaireQuestion> listByQuestionnaireId(Integer questionnaireId) {
        return this.list(new LambdaQueryWrapper<QuestionnaireQuestion>()
                .eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                .orderByAsc(QuestionnaireQuestion::getId));
    }

    /**
     * 通过问卷、问题Id获取
     *
     * @return QuestionnaireQuestion
     */
    public QuestionnaireQuestion getByQuestionnaireIdAndQuestionId(Integer questionnaireId, Integer questionId) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        wrapper.in(QuestionnaireQuestion::getQuestionId, questionId);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 查询问题列表
     */
    public List<QuestionnaireQuestion> getByQuestionnaireIdSerialNumber(Integer questionnaireId, String serialNumber, Integer questionId) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId)
                .like(QuestionnaireQuestion::getSerialNumber, serialNumber)
                .ne(QuestionnaireQuestion::getQuestionId, questionId);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 删除逻辑
     */
    public void deletedLogic(Integer questionnaireId, Integer questionId) {
        baseMapper.deletedLogic(questionnaireId, questionId);
    }

    /**
     * 通过问卷、问题Id获取
     *
     * @return QuestionnaireQuestion
     */
    public List<QuestionnaireQuestion> getByQuestionnaireIdAndQuestionIds(Integer questionnaireId, List<Integer> questionIds) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionnaireQuestion::getQuestionnaireId, questionnaireId);
        wrapper.in(QuestionnaireQuestion::getQuestionId, questionIds);
        return baseMapper.selectList(wrapper);
    }

    /**
     * 通过Pid获取
     */
    public List<QuestionnaireQuestion> getByPids(List<Integer> pids) {
        LambdaQueryWrapper<QuestionnaireQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(QuestionnaireQuestion::getPid, pids);
        return baseMapper.selectList(wrapper);
    }

}
