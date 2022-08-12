package com.wupol.myopia.business.api.management.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.core.questionnaire.domain.dos.Option;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import com.wupol.myopia.business.core.questionnaire.service.QuestionService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题库管理
 *
 * @author Simple4H
 */
@Service
public class QuestionBizService {

    @Resource
    private QuestionService questionService;

    /**
     * 保存问题
     *
     * @param question 问题
     */
    @Transactional(rollbackFor = Exception.class)
    public void saveQuestion(Question question) {

        question.setTitle(StringUtils.deleteWhitespace(CharMatcher.javaIsoControl().removeFrom(question.getTitle())));


        // 判断问题是否已经存在
        Question questionByTitle = questionService.findOne(new Question().setTitle(question.getTitle()));
        if (Objects.nonNull(questionByTitle)) {
            throw new BusinessException("该问题已经存在");
        }

        //选项Id唯一判断
        List<String> optionIdsByQuestion = getOptionIdByQuestion(question);
        checkDuplicate(optionIdsByQuestion);
        // 集合的交集
        Collection<String> intersection = CollUtil.intersection(optionIdsByQuestion, getAllOptionIds());
        if (CollUtil.isNotEmpty(intersection)) {
            throw new BusinessException("新问题的选项Id与已有问题的选项Id重复");
        }
        questionService.save(question);
    }

    /**
     * 获取问题的所有选项Id
     *
     * @return 选项Id
     */
    public List<String> getAllOptionIds() {
        List<String> addIds = new ArrayList<>();
        List<Question> allQuestion = questionService.getAllQuestion();
        allQuestion.forEach(question -> addIds.addAll(getOptionIdByQuestion(question)));
        checkDuplicate(addIds);
        return addIds;
    }

    private List<String> getOptionIdByQuestion(Question question) {
        // 获取问题下选项的Id
        // 获取选项的Id
        List<Option> options = question.getOptions();
        List<String> optionIds = options.stream().map(Option::getId).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(ListUtil.getDuplicateElements(optionIds))) {
            throw new BusinessException("问题选项Id重复");
        }

        // 获取选项的填空option
        List<String> ids = new ArrayList<>();
        options.forEach(s -> {
            JSONObject option = s.getOption();
            if (Objects.nonNull(option)) {
                int size = option.size();
                for (int i = 1; i <= size; i++) {
                    ids.add(option.getJSONObject(String.valueOf(i)).get("id").toString());
                }
            }
        });

        // 合并两个list
        return Lists.newArrayList(Iterables.concat(ids, optionIds));
    }

    /**
     * 检查是否重复
     *
     * @param ids 选项Id
     */
    private void checkDuplicate(List<String> ids) {
        HashSet<String> setIds = new HashSet<>(ids);
        if (ids.size() > setIds.size()) {
            throw new BusinessException("选项Id重复");
        }
    }
}
