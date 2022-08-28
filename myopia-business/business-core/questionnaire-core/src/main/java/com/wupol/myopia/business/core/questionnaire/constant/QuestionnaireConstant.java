package com.wupol.myopia.business.core.questionnaire.constant;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.QuestionnaireTypeEnum;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Objects;

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
    public static List<Integer> getStudentTypeList(){
        return Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType(), QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType(), QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType());
    }

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

    /**
     * 下拉
     */
    public static final String SELECT = "select";

    /**
     * 输入
     */
    public static final String INPUT = "input";

    /**
     * text
     */
    public static final String TEXT = "text";


    /**
     * 临时文件夹名称
     */
    public static final String EPI_DATA_FOLDER = "EpiData";
    /**
     * qes文件扩展名
     */
    public static final String QES = "qes";

    public static String getQesExtension(){
        return "."+QES;
    }

    /**
     * 文件扩展名
     */
    public static final String REC = ".rec";
    public static final String TXT = ".txt";
    public static final String  ZIP = ".zip";

    /**
     * 文件类型
     */
    public static final String EXCEL_FILE = "excel";
    public static final String REC_FILE = "rec";

    /**
     * 选项类型
     */
    public static final String  RADIO = "radio";
    public static final String  CHECKBOX = "checkbox";
    public static final String  RADIO_INPUT = "radio-input";
    public static final String  CHECKBOX_INPUT = "checkbox-input";

    public static final String  NUMBER="number";
    public static final String  QM = "QM";

    /**
     * 日期格式化样式
     */
    public static final String DATE_FORMAT ="yyyy/MM/dd";

    /**
     * 获取省、地市及区（县）管理部门学校卫生工作调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getAreaDistrictSchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType());
    }

    /**
     * 获取中小学校开展学校卫生工作情况调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getPrimarySecondarySchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType());
    }

    /**
     * 获取学生健康状况及影响因素调查表（小学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getPrimarySchool(String exportFile){
        if (Objects.equals(EXCEL_FILE,exportFile)) {
            return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType());
        }else {
            return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.PRIMARY_SCHOOL.getType(),QuestionnaireTypeEnum.VISION_SPINE.getType());
        }
    }

    /**
     * 获取学生健康状况及影响因素调查表（中学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getMiddleSchool(String exportFile){
        if (Objects.equals(EXCEL_FILE,exportFile)) {
            return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType());
        }else {
            return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.MIDDLE_SCHOOL.getType(),QuestionnaireTypeEnum.VISION_SPINE.getType());
        }
    }

    /**
     * 获取学生健康状况及影响因素调查表（大学版）问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getUniversitySchool(){
        return Lists.newArrayList(QuestionnaireTypeEnum.QUESTIONNAIRE_NOTICE.getType(),QuestionnaireTypeEnum.UNIVERSITY_SCHOOL.getType());
    }

    /**
     * 获取学生视力不良及脊柱弯曲异常影响因素专项调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getVisionSpine(){
        return Lists.newArrayList(QuestionnaireTypeEnum.VISION_SPINE_NOTICE.getType(),QuestionnaireTypeEnum.VISION_SPINE.getType());
    }


    /**
     * 获取学校环境健康影响因素调查表问卷
     * @return 问卷类型集合
     */
    public static List<Integer> getSchoolEnvironment(){
        return Lists.newArrayList(QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType());
    }

    /**
     * 获取rec导出类型
     */
    public static List<Integer> getRecExportType(){
        return Lists.newArrayList(QuestionnaireTypeEnum.AREA_DISTRICT_SCHOOL.getType(),
                QuestionnaireTypeEnum.PRIMARY_SECONDARY_SCHOOLS.getType(),
                QuestionnaireTypeEnum.SCHOOL_ENVIRONMENT.getType());
    }
}
