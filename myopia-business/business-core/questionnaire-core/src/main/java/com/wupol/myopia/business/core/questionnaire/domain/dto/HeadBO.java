package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 表头信息实体
 *
 * @author hang.yuan 2022/7/22 23:22
 */
@Data
@Accessors(chain = true)
public class HeadBO {
    /**
     * 最后问题ID
     */
    private Integer lastQuestionId;
    /**
     * 表头问题层级集合
     */
    private List<String> questionDepthList;
    /**
     * 表头层级深度
     */
    private Integer depth;
    /**
     * 问题排序
     */
    private Integer sort;

    public List<String> getQuestionHead(){
        if (CollectionUtils.isEmpty(questionDepthList)){
            return Lists.newArrayList();
        }
        int size = questionDepthList.size();
        if (size < depth){
            String s = questionDepthList.get(size - 1);
            for (int i = 0; i < (depth - size); i++) {
                questionDepthList.add(s);
            }
        }
        return questionDepthList;
    }
}
