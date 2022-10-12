package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.collection.CollUtil;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.vo.SchoolStudentQuerySelectVO;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校学生
 *
 * @author hang.yuan 2022/10/12 09:57
 */
@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class SchoolStudentFacade {

    private final SchoolGradeService schoolGradeService;

    /**
     * 获取学校学生查询条件下拉框值
     * @param schoolId
     */
    public SchoolStudentQuerySelectVO getSelectValue(Integer schoolId) {

        TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove = kindergartenAndPrimaryAbove(schoolId);

        SchoolStudentQuerySelectVO schoolStudentQuerySelectVO = new SchoolStudentQuerySelectVO(Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList());

        //年份
        schoolStudentQuerySelectVO.setYearList(Lists.newArrayList(new SchoolStudentQuerySelectVO.SelectValue(2022,"2022")));

        //戴镜类型
        ImmutableMap<Integer, String> typeDescriptionMap = WearingGlassesSituation.getTypeDescriptionMap();
        List<SchoolStudentQuerySelectVO.SelectValue> glassesTypeList = typeDescriptionMap.entrySet().stream().map(entry -> new SchoolStudentQuerySelectVO.SelectValue(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.setGlassesTypeList(glassesTypeList);

        //视力类型
        visionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);


        //屈光类型
        refractionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);

        return schoolStudentQuerySelectVO;
    }

    /**
     * 屈光类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void refractionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        SchoolStudentQuerySelectVO.SelectValue normal = new SchoolStudentQuerySelectVO.SelectValue(0, "正常");
        schoolStudentQuerySelectVO.getRefractionTypeList().add(normal);

        SchoolStudentQuerySelectVO.SelectValue myopiaLevelEarly = new SchoolStudentQuerySelectVO.SelectValue(1, "近视前期");
        SchoolStudentQuerySelectVO.SelectValue myopia = new SchoolStudentQuerySelectVO.SelectValue(2, "近视");
        SchoolStudentQuerySelectVO.SelectValue myopiaLevelLight = new SchoolStudentQuerySelectVO.SelectValue(3, "低度近视");
        SchoolStudentQuerySelectVO.SelectValue myopiaLevelHigh = new SchoolStudentQuerySelectVO.SelectValue(4, "高度近视");

        SchoolStudentQuerySelectVO.SelectValue hyperopia = new SchoolStudentQuerySelectVO.SelectValue(5, "远视");
        SchoolStudentQuerySelectVO.SelectValue hyperopiaLevelLight = new SchoolStudentQuerySelectVO.SelectValue(6, "低度远视");
        SchoolStudentQuerySelectVO.SelectValue hyperopiaLevelMiddle = new SchoolStudentQuerySelectVO.SelectValue(7, "中度远视");
        SchoolStudentQuerySelectVO.SelectValue hyperopiaLevelHigh = new SchoolStudentQuerySelectVO.SelectValue(8, "重度远视");

        SchoolStudentQuerySelectVO.SelectValue astigmatism = new SchoolStudentQuerySelectVO.SelectValue(9, "散光");
        SchoolStudentQuerySelectVO.SelectValue astigmatismLevelLight = new SchoolStudentQuerySelectVO.SelectValue(10, "低度散光");
        SchoolStudentQuerySelectVO.SelectValue astigmatismLevelMiddle = new SchoolStudentQuerySelectVO.SelectValue(11, "中度散光");
        SchoolStudentQuerySelectVO.SelectValue astigmatismLevelHigh = new SchoolStudentQuerySelectVO.SelectValue(12, "重度散光");

        SchoolStudentQuerySelectVO.SelectValue insufficient = new SchoolStudentQuerySelectVO.SelectValue(13, "远视储备不足");
        SchoolStudentQuerySelectVO.SelectValue refractiveError = new SchoolStudentQuerySelectVO.SelectValue(14, "屈光不足");
        SchoolStudentQuerySelectVO.SelectValue anisometropia = new SchoolStudentQuerySelectVO.SelectValue(15, "屈光参差");



        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.TRUE)  ){
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelEarly);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopia);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelHigh);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopia);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelMiddle);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelHigh);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatism);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelMiddle);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelHigh);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(insufficient);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(refractiveError);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(anisometropia);
            return;
        }

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.FALSE)){
            schoolStudentQuerySelectVO.getRefractionTypeList().add(insufficient);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(refractiveError);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(anisometropia);
            return;
        }

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.TRUE)){
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelEarly);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopia);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(myopiaLevelHigh);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopia);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelMiddle);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(hyperopiaLevelHigh);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatism);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelLight);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelMiddle);
            schoolStudentQuerySelectVO.getRefractionTypeList().add(astigmatismLevelHigh);
            insufficient = null;
            refractiveError = null;
            anisometropia = null;
        }
    }

    /**
     * 视力类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void visionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {

        SchoolStudentQuerySelectVO.SelectValue normal = new SchoolStudentQuerySelectVO.SelectValue(0, "正常");
        schoolStudentQuerySelectVO.getVisionTypeList().add(normal);

        SchoolStudentQuerySelectVO.SelectValue primaryAbove = new SchoolStudentQuerySelectVO.SelectValue(1, "视力低下");
        SchoolStudentQuerySelectVO.SelectValue kindergarten = new SchoolStudentQuerySelectVO.SelectValue(2, "视力低常");

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.TRUE)  ){
            schoolStudentQuerySelectVO.getVisionTypeList().add(primaryAbove);
            schoolStudentQuerySelectVO.getVisionTypeList().add(kindergarten);
            return;
        }

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.FALSE)){
            schoolStudentQuerySelectVO.getVisionTypeList().add(kindergarten);
            primaryAbove = null;
            return;
        }

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(),Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(),Boolean.TRUE)){
            schoolStudentQuerySelectVO.getVisionTypeList().add(primaryAbove);
            kindergarten = null;
        }

    }

    /**
     * 判断学校的学龄段（幼儿园/小学及以上）
     * @param schoolId
     */
    private TwoTuple<Boolean,Boolean> kindergartenAndPrimaryAbove(Integer schoolId) {
        List<SchoolGrade> schoolGradeList = schoolGradeService.getBySchoolId(schoolId);
        if (CollUtil.isEmpty(schoolGradeList)){
            return TwoTuple.of(Boolean.FALSE,Boolean.FALSE);
        }
        List<String> kindergartenSchoolCode = GradeCodeEnum.kindergartenSchoolCode();
        List<String> primaryAbove = GradeCodeEnum.primaryAbove();
        List<SchoolGrade> kindergartenList = schoolGradeList.stream().filter(schoolGrade -> kindergartenSchoolCode.contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        List<SchoolGrade> primaryAboveList = schoolGradeList.stream().filter(schoolGrade -> primaryAbove.contains(schoolGrade.getGradeCode())).collect(Collectors.toList());
        return TwoTuple.of(CollUtil.isNotEmpty(kindergartenList),CollUtil.isNotEmpty(primaryAboveList));
    }
}
