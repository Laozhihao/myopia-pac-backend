package com.wupol.myopia.business.core.questionnaire.domain.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.wupol.myopia.business.core.questionnaire.domain.dos.*;
import com.wupol.myopia.business.core.questionnaire.domain.handle.QesDataDoHandler;
import com.wupol.myopia.business.core.questionnaire.domain.model.Question;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @Classname QuestionResponse
 * @Description
 * @Date 2022/7/11 14:38
 * @Created by limy
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionResponse extends Question implements Serializable {

    private static final long serialVersionUID = 3390658266197220844L;
    /**
     * 是否必填
     */
    private Boolean required;

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
     * 题目
     */
    private List<QuestionResponse> questionList;

    /**
     * 传染病表格
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<InfectiousDiseaseTable> infectiousDiseaseTable;

    /**
     * 学校教室环境卫生表格
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ClassroomItemTable> classroomItemTables;

    /**
     * 学校教师
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SchoolTeacherTable> schoolTeacherTables;

    /**
     * 是否隐藏
     */
    private Boolean isHidden;

}
