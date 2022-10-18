package com.wupol.myopia.business.aggregation.student.domain.builder;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.aggregation.student.constant.RefractionSituationEnum;
import com.wupol.myopia.business.aggregation.student.constant.VisionSituationEnum;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.VisionUtil;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolStudentQueryDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentQueryBO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.domain.vo.SchoolStudentListVO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.experimental.UtilityClass;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校学生信息
 *
 * @author hang.yuan 2022/9/29 13:05
 */
@UtilityClass
public class SchoolStudentInfoBuilder {


    /**
     * 设置学生信息
     *
     * @param countMap
     * @param visitMap
     * @param studentPlanMap
     * @param student
     */
    public void setStudentInfo(Map<Integer, Integer> countMap, Map<Integer, List<ReportAndRecordDO>> visitMap,
                               Map<Integer, List<ScreeningPlanSchoolStudent>> studentPlanMap,
                               StudentDTO student) {
        // 筛查次数
        student.setScreeningCount(countMap.getOrDefault(student.getId(), 0));
        // 筛查码
        student.setScreeningCodes(StudentBizBuilder.getScreeningCodesByPlan(studentPlanMap.get(student.getId())));
        // 就诊次数
        student.setNumOfVisits(Objects.nonNull(visitMap.get(student.getId())) ? visitMap.get(student.getId()).size() : 0);
        // 问卷次数
        student.setQuestionnaireCount(0);
        //近视矫正
        student.setCorrection(student.getVisionCorrection());
    }

    /**
     * 构建学生信息
     *
     * @param schoolStudent
     */
    public SchoolStudentListVO buildSchoolStudentListVO(SchoolStudent schoolStudent){
        SchoolStudentListVO schoolStudentListVO = new SchoolStudentListVO()
                .setGradeName(schoolStudent.getGradeName())
                .setClassName(schoolStudent.getClassName());

        schoolStudentListVO.setId(schoolStudent.getId())
                .setStudentId(schoolStudent.getStudentId())
                .setSno(schoolStudent.getSno())
                .setName(schoolStudent.getName())
                .setVisionLabel(schoolStudent.getVisionLabel())
                .setCorrection(schoolStudent.getVisionCorrection());

        if (Objects.nonNull(schoolStudent.getParticularYear())){
            schoolStudentListVO.setYearStr(schoolStudent.getParticularYear().toString());
        }

        if (Objects.equals(schoolStudent.getGradeType(), SchoolAge.KINDERGARTEN.getCode())){
            schoolStudentListVO.setRefraction(VisionUtil.getRefractionSituation(schoolStudent.getIsAnisometropia(),schoolStudent.getIsRefractiveError(),schoolStudent.getVisionLabel()));
        }else {
            schoolStudentListVO.setRefraction(VisionUtil.getRefractionSituation(schoolStudent.getMyopiaLevel(),schoolStudent.getHyperopiaLevel(),schoolStudent.getAstigmatismLevel(),schoolStudent.getScreeningMyopia()));
        }

        schoolStudentListVO.setVision(VisionUtil.getVisionSituation(schoolStudent.getGlassesType(),schoolStudent.getGradeType(),schoolStudent.getLowVision()));
        return schoolStudentListVO;
    }

    /**
     * 构建学校学生查询条件对象
     *
     * @param studentQueryDTO
     */
    public SchoolStudentQueryBO builderSchoolStudentQueryBO(SchoolStudentQueryDTO studentQueryDTO) {
        SchoolStudentQueryBO schoolStudentQueryBO = new SchoolStudentQueryBO()
                .setName(studentQueryDTO.getName())
                .setSno(studentQueryDTO.getSno())
                .setGradeId(studentQueryDTO.getGradeId())
                .setClassId(studentQueryDTO.getClassId())
                .setSchoolId(studentQueryDTO.getSchoolId())
                .setVisionLabels(studentQueryDTO.getVisionLabels());

        if (CollUtil.isEmpty(schoolStudentQueryBO.getVisionLabels()) && Objects.nonNull(studentQueryDTO.getVisionLabel())){
            schoolStudentQueryBO.setVisionLabels(studentQueryDTO.getVisionLabel().toString());
        }

        setSchoolStudentQueryBO(schoolStudentQueryBO,studentQueryDTO);

        return schoolStudentQueryBO;
    }

    private void setSchoolStudentQueryBO(SchoolStudentQueryBO schoolStudentQueryBO,SchoolStudentQueryDTO studentQueryDTO){
        schoolStudentQueryBO.setYear(studentQueryDTO.getYear());
        schoolStudentQueryBO.setGlassesType(studentQueryDTO.getGlassesType());

        if (Objects.nonNull(studentQueryDTO.getVisionType())){
            VisionSituationEnum visionSituationEnum = VisionSituationEnum.getByCode(studentQueryDTO.getVisionType());
            switch (visionSituationEnum){
                case NORMAL:
                    schoolStudentQueryBO.setLowVisionList(Lists.newArrayList(LowVisionLevelEnum.ZERO.getCode()));
                    break;
                case LOW_VISION_KINDERGARTEN:
                    schoolStudentQueryBO.setLowVisionList(LowVisionLevelEnum.lowVisionLevelCodeList());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.kindergartenCode());
                    break;
                case LOW_VISION_PRIMARY_ABOVE:
                    schoolStudentQueryBO.setLowVisionList(LowVisionLevelEnum.lowVisionLevelCodeList());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;
                default:
                    break;
            }

        }

        if (Objects.nonNull(studentQueryDTO.getRefractionType())){
            RefractionSituationEnum refractionSituationEnum = RefractionSituationEnum.getByCode(studentQueryDTO.getRefractionType());
            switch (refractionSituationEnum){

                case MYOPIA:
                    schoolStudentQueryBO.setMyopiaList(Lists.newArrayList(
                            MyopiaLevelEnum.MYOPIA_LEVEL_LIGHT.getCode(),
                            MyopiaLevelEnum.MYOPIA_LEVEL_HIGH.getCode()));
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;
                case HYPEROPIA:
                    schoolStudentQueryBO.setHyperopiaList(Lists.newArrayList(
                            HyperopiaLevelEnum.HYPEROPIA_LEVEL_LIGHT.getCode(),
                            HyperopiaLevelEnum.HYPEROPIA_LEVEL_MIDDLE.getCode(),
                            HyperopiaLevelEnum.HYPEROPIA_LEVEL_HIGH.getCode()
                    ));
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;
                case ASTIGMATISM:
                    schoolStudentQueryBO.setAstigmatismList(Lists.newArrayList(
                            AstigmatismLevelEnum.ASTIGMATISM_LEVEL_LIGHT.getCode(),
                            AstigmatismLevelEnum.ASTIGMATISM_LEVEL_MIDDLE.getCode(),
                            AstigmatismLevelEnum.ASTIGMATISM_LEVEL_HIGH.getCode()
                    ));
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;

                case MYOPIA_LEVEL_EARLY:
                case MYOPIA_LEVEL_LIGHT:
                case MYOPIA_LEVEL_HIGH:
                    schoolStudentQueryBO.setMyopiaLevel(refractionSituationEnum.getType());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;

                case HYPEROPIA_LEVEL_LIGHT:
                case HYPEROPIA_LEVEL_MIDDLE:
                case HYPEROPIA_LEVEL_HIGH:
                    schoolStudentQueryBO.setHyperopiaLevel(refractionSituationEnum.getType());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;

                case ASTIGMATISM_LEVEL_LIGHT:
                case ASTIGMATISM_LEVEL_MIDDLE:
                case ASTIGMATISM_LEVEL_HIGH:
                    schoolStudentQueryBO.setAstigmatismLevel(refractionSituationEnum.getType());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.primaryAndAboveCode());
                    break;
                case INSUFFICIENT:
                    schoolStudentQueryBO.setVisionLabels(refractionSituationEnum.getType().toString());
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.kindergartenCode());
                    break;
                case REFRACTIVE_ERROR:
                    schoolStudentQueryBO.setRefractiveError(Boolean.TRUE);
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.kindergartenCode());
                    break;
                case ANISOMETROPIA:
                    schoolStudentQueryBO.setAnisometropia(Boolean.TRUE);
                    schoolStudentQueryBO.setGradeTypeList(SchoolAge.kindergartenCode());
                    break;
                case NORMAL:
                default:
                    schoolStudentQueryBO.setMyopiaLevel(MyopiaLevelEnum.ZERO.getCode());
                    schoolStudentQueryBO.setHyperopiaLevel(HyperopiaLevelEnum.ZERO.getCode());
                    schoolStudentQueryBO.setAstigmatismLevel(AstigmatismLevelEnum.ZERO.getCode());
                    break;
            }
        }


    }

    /**
     * 校验学校学生信息
     * @param schoolStudent 学校学生
     */
    public static void validSchoolStudent(SchoolStudent schoolStudent) {
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getSno()),"学号不能为空");
        Assert.isTrue(StrUtil.isNotBlank(schoolStudent.getName()),"姓名不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGender()),"性别不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getGradeId()),"年级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getClassId()),"班级不能为空");
        Assert.isTrue(Objects.nonNull(schoolStudent.getBirthday()),"出生日期不能为空");
    }

    /**
     * 检查参数
     * @param schoolStudent
     * @param schoolStudentList
     * @param function
     * @param errorMsg
     */
    public static void checkParam(SchoolStudent schoolStudent, List<SchoolStudent> schoolStudentList, Function<SchoolStudent,String> function, String errorMsg) {
        if (StrUtil.isNotBlank(getValue(schoolStudent,function))){
            List<SchoolStudent> schoolStudents = schoolStudentList.stream().filter(student -> Objects.equals(getValue(student,function),getValue(schoolStudent,function))).collect(Collectors.toList());
            if(CollUtil.isNotEmpty(schoolStudents)){
                throw new BusinessException(errorMsg);
            }
        }
    }


    /**
     * 获取参数值
     * @param t
     * @param function
     */
    public static <T,V> V getValue(T t,Function<T,V> function){
        return Optional.ofNullable(t).map(function).orElse(null);
    }

}
