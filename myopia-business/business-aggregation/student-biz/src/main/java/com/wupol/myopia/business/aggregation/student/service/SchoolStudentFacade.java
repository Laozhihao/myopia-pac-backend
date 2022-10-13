package com.wupol.myopia.business.aggregation.student.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.student.constant.RefractionSituationEnum;
import com.wupol.myopia.business.aggregation.student.constant.VisionSituationEnum;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.vo.SchoolStudentQuerySelectVO;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
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

        //戴镜类型
        ImmutableMap<Integer, String> typeDescriptionMap = WearingGlassesSituation.getTypeDescriptionMap();
        List<SchoolStudentQuerySelectVO.SelectValue> glassesTypeList = typeDescriptionMap.entrySet().stream().map(entry -> new SchoolStudentQuerySelectVO.SelectValue(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.setGlassesTypeList(glassesTypeList);

        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.FALSE)) {
            return schoolStudentQuerySelectVO;
        }
        //年份
        yearList(schoolStudentQuerySelectVO);

        //视力类型
        visionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);

        //屈光类型
        refractionTypeList(schoolStudentQuerySelectVO,kindergartenAndPrimaryAbove);

        return schoolStudentQuerySelectVO;
    }

    /**
     * 年份
     * @param schoolStudentQuerySelectVO
     */
    private void yearList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO) {
        schoolStudentQuerySelectVO.getYearList().add(new SchoolStudentQuerySelectVO.SelectValue(2022,"2022"));
    }

    /**
     * 获取现在的年份时间间隔
     */
    public List<Date> getNowTimeInterval() {
        List<Date> nowTimeInterval = Lists.newArrayList();
        int year = LocalDate.now().getYear();

        String frontYearFormat = "%s-2-1~%s-8-31";
        String[] frontYearArr = String.format(frontYearFormat, year, year).split("~");
        Date frontStartTime = DateUtil.parse(frontYearArr[0], DatePattern.NORM_DATE_PATTERN);
        Date frontEndTime = DateUtil.parse(frontYearArr[1], DatePattern.NORM_DATE_PATTERN);
        nowTimeInterval.add(frontStartTime);
        nowTimeInterval.add(frontEndTime);

        String afterYearFormat = "%s-9-1~%s-1-31";
        String[] afterYearArr = String.format(afterYearFormat, year, year + 1).split("~");
        Date afterStartTime = DateUtil.parse(afterYearArr[0], DatePattern.NORM_DATE_PATTERN);
        Date afterEndTime = DateUtil.parse(afterYearArr[1], DatePattern.NORM_DATE_PATTERN);
        nowTimeInterval.add(afterStartTime);
        nowTimeInterval.add(afterEndTime);
        return nowTimeInterval;
    }
    /**
     * 屈光类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void refractionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = getCondition(kindergartenAndPrimaryAbove);
        List<RefractionSituationEnum> refractionSituationEnumList = RefractionSituationEnum.listByCondition(condition);
        List<SchoolStudentQuerySelectVO.SelectValue> selectValueList = refractionSituationEnumList.stream().map(situationEnum -> new SchoolStudentQuerySelectVO.SelectValue(situationEnum.getCode(), situationEnum.getDesc())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.getRefractionTypeList().addAll(selectValueList);
    }


    /**
     * 视力类型下拉选择值
     * @param schoolStudentQuerySelectVO
     * @param kindergartenAndPrimaryAbove
     */
    private void visionTypeList(SchoolStudentQuerySelectVO schoolStudentQuerySelectVO, TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = getCondition(kindergartenAndPrimaryAbove);
        List<VisionSituationEnum> visionSituationEnumList = VisionSituationEnum.listByCondition(condition);
        List<SchoolStudentQuerySelectVO.SelectValue> selectValueList = visionSituationEnumList.stream().map(visionSituationEnum -> new SchoolStudentQuerySelectVO.SelectValue(visionSituationEnum.getCode(), visionSituationEnum.getDesc())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.getVisionTypeList().addAll(selectValueList);
    }

    /**
     * 获取条件值
     * @param kindergartenAndPrimaryAbove
     */
    private Boolean getCondition(TwoTuple<Boolean, Boolean> kindergartenAndPrimaryAbove) {
        Boolean condition = null;

        //幼儿园
        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.TRUE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.FALSE)) {
            condition = Boolean.TRUE;
        }
        //小学及以上
        if (Objects.equals(kindergartenAndPrimaryAbove.getFirst(), Boolean.FALSE) && Objects.equals(kindergartenAndPrimaryAbove.getSecond(), Boolean.TRUE)) {
            condition = Boolean.FALSE;
        }
        return condition;
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
