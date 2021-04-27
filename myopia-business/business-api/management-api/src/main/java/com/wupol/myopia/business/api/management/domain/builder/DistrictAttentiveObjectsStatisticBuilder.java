package com.wupol.myopia.business.api.management.domain.builder;

import com.wupol.myopia.business.common.utils.constant.WarningLevel;
import com.wupol.myopia.business.common.utils.util.MathUtil;
import com.wupol.myopia.business.core.school.domain.dto.StudentExtraDTO;
import com.wupol.myopia.business.core.stat.domain.model.DistrictAttentiveObjectsStatistic;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
public class DistrictAttentiveObjectsStatisticBuilder {

    public static DistrictAttentiveObjectsStatistic build(Integer districtId, Integer isTotal, List<StudentExtraDTO> studentDTOList) {
        Map<Integer, Long> visionLabelNumberMap = studentDTOList.stream().filter(vo -> Objects.nonNull(vo.getVisionLabel())).collect(Collectors.groupingBy(StudentExtraDTO::getVisionLabel, Collectors.counting()));
        DistrictAttentiveObjectsStatistic statistic = new DistrictAttentiveObjectsStatistic();
        Integer visionLabel0Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ZERO.code, 0L).intValue();
        Integer visionLabel1Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.ONE.code, 0L).intValue();
        Integer visionLabel2Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.TWO.code, 0L).intValue();
        Integer visionLabel3Numbers = visionLabelNumberMap.getOrDefault(WarningLevel.THREE.code, 0L).intValue();
        Integer keyWarningNumbers = visionLabel0Numbers + visionLabel1Numbers + visionLabel2Numbers + visionLabel3Numbers;
        Integer totalStudentNumbers = studentDTOList.size();
        statistic.setDistrictId(districtId).setIsTotal(isTotal)
                .setVisionLabel0Numbers(visionLabel0Numbers).setVisionLabel0Ratio(MathUtil.divide(visionLabel0Numbers, totalStudentNumbers))
                .setVisionLabel1Numbers(visionLabel1Numbers).setVisionLabel1Ratio(MathUtil.divide(visionLabel1Numbers, totalStudentNumbers))
                .setVisionLabel2Numbers(visionLabel2Numbers).setVisionLabel2Ratio(MathUtil.divide(visionLabel2Numbers, totalStudentNumbers))
                .setVisionLabel3Numbers(visionLabel3Numbers).setVisionLabel3Ratio(MathUtil.divide(visionLabel3Numbers, totalStudentNumbers))
                .setKeyWarningNumbers(keyWarningNumbers).setStudentNumbers(totalStudentNumbers);
        return statistic;
    }
}
