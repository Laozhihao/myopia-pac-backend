package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.wupol.myopia.business.core.questionnaire.domain.dos.JumpIdsDO;
import com.wupol.myopia.business.core.questionnaire.domain.dos.QesSerialNumberDO;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname QuestionnaireInfoDTO
 * @Description
 * @Date 2022/7/8 11:30
 * @Created by limy
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionnaireInfoDTO extends Question implements Serializable {

    /**
     * 扩展id（中间表）
     */
    private Integer exId;

    /**
     * 扩展Pid（中间表）
     */
    private Integer exPid;

    /**
     * 是否不展示题目序号
     */
    private Boolean isNotShowNumber;

    /**
     * 是否逻辑题
     */
    private Boolean isLogic;

    /**
     * 跳转Id
     */
    private List<JumpIdsDO> jumpIds;

    /**
     * 是否必填
     */
    private Boolean required;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

    /**
     * qes
     */
    private List<QesSerialNumberDO> qesSerialNumber;

    /**
     * 子模块问题数组
     */
    private List<QuestionResponse> questionList;
}
