package com.wupol.myopia.business.aggregation.student.service;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.school.domain.vo.SchoolStudentQuerySelectVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校学生
 *
 * @author hang.yuan 2022/10/12 09:57
 */
@Service
public class SchoolStudentFacade {


    /**
     * 获取学校学生查询条件下拉框值
     * @param schoolId
     */
    public SchoolStudentQuerySelectVO getSelectValue(Integer schoolId) {

        SchoolStudentQuerySelectVO schoolStudentQuerySelectVO = new SchoolStudentQuerySelectVO(Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList(),Lists.newArrayList());

        //年份
        schoolStudentQuerySelectVO.setYearList(Lists.newArrayList(new SchoolStudentQuerySelectVO.SelectValue(2022,"2022")));

        //戴镜类型
        ImmutableMap<Integer, String> typeDescriptionMap = WearingGlassesSituation.getTypeDescriptionMap();
        List<SchoolStudentQuerySelectVO.SelectValue> glassesTypeList = typeDescriptionMap.entrySet().stream().map(entry -> new SchoolStudentQuerySelectVO.SelectValue(entry.getKey(), entry.getValue())).collect(Collectors.toList());
        schoolStudentQuerySelectVO.setGlassesTypeList(glassesTypeList);

        //视力类型
        schoolStudentQuerySelectVO.getVisionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(0,"正常"));
        schoolStudentQuerySelectVO.getVisionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(1,"视力低下"));
        schoolStudentQuerySelectVO.getVisionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(2,"视力低常"));

        //屈光类型
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(0,"正常"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(1,"近视前期"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(2,"近视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(3,"低度近视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(4,"高度近视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(5,"远视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(6,"低度远视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(7,"中度远视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(8,"重度远视"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(9,"散光"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(10,"低度散光"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(11,"中度散光"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(12,"重度散光"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(13,"远视储备不足"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(14,"屈光不足"));
        schoolStudentQuerySelectVO.getRefractionTypeList().add(new SchoolStudentQuerySelectVO.SelectValue(15,"屈光参差"));

        return schoolStudentQuerySelectVO;
    }
}
