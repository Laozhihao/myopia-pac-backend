package com.wupol.myopia.business.core.questionnaire.constant;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * 问卷常量
 *
 * @author hang.yuan 2022/7/26 10:52
 */
@UtilityClass
public class QuestionnaireConstant {
    /**
     * 问卷类型学生类型组合数（小学版、中学版、大学版）
     * {@link QuestionnaireTypeEnum}
     */
    public static final Integer STUDENT_TYPE = 12;

    /**
     * 问卷类型学生类型组合描述
     */
    public static final String STUDENT_TYPE_DESC = "学生健康状况及影响因素调查表";

    /**
     * 问卷类型学生类型集合
     */
    public static final List<Integer> STUDENT_TYPE_LIST = Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType(), QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType(), QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType());

    /**
     * 父ID值
     */
    public static final Integer PID = -1;

    /**
     * 传染病表格
     */
    public static final String INFECTIOUS_DISEASE_TITLE = "infectious-disease-table";


    /**
     *传染病
     */
    public static final String INFECTIOUS_DISEASE_PREFIX = "传染病-";

    /**
     * 甲乙类
     */
    public static final String INFECTIOUS_DISEASE_ONE = "甲乙类";

    /**
     * 丙类
     */
    public static final String INFECTIOUS_DISEASE_TWO = "丙类";

    /**
     * 学校教室环境卫生
     */
    public static final String SCHOOL_CLASSROOM_TITLE = "school-classroom-table";

    /**
     * 学校教师
     */
    public static final String TEACHER_TABLE = "teacher-table";

    /**
     * id
     */
    public static final String ID = "id";

    /**
     * 数据类型
     */
    public static final String DATA_TYPE = "dataType";

    /**
     * 下拉key
     */
    public static final String DROP_SELECT_KEY = "dropSelectKey";

    /**
     * 是否必填
     */
    public static final String REQUIRED = "required";

    /**
     * maxLimit
     */
    public static final String MAX_LIMIT = "maxLimit";

    /**
     * minLimit
     */
    public static final String MIN_LIMIT = "minLimit";

    /**
     * range
     */
    public static final String RANGE = "range";

    /**
     * length
     */
    public static final String LENGTH = "length";

}
