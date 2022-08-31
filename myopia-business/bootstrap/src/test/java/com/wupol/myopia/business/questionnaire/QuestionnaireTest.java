package com.wupol.myopia.business.questionnaire;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.bootstrap.MyopiaBusinessApplication;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QuestionnaireQuestionRecDataBO;
import com.wupol.myopia.business.core.questionnaire.facade.QuestionnaireFacade;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;

/**
 * 问卷测试
 *
 * @author hang.yuan 2022/8/25 11:51
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = MyopiaBusinessApplication.class)
public class QuestionnaireTest {

    @Autowired
    private QuestionnaireFacade questionnaireFacade;
    @Test
    public void questionnaireBaseInfoDataTest(){
        List<Integer> questionnaireIds =Lists.newArrayList(5);
        List<QuestionnaireQuestionRecDataBO> dataBuildList = questionnaireFacade.getDataBuildList(questionnaireIds);
        for (QuestionnaireQuestionRecDataBO questionnaireQuestionRecDataBO : dataBuildList) {
            System.out.println(JSON.toJSONString(questionnaireQuestionRecDataBO, true));
        }

        Assert.assertTrue(true);
    }
}
