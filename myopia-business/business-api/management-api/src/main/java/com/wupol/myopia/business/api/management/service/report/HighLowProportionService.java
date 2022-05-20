package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.KindergartenScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.schoolage.SchoolAgeLowVisionTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeWarning;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.LowVisionTable;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 比分比
 *
 * @author Simple4H
 */
@Service
public class HighLowProportionService {

    /**
     * 视力低下学龄段不同程度
     *
     * @return HighLowProportion
     */
    public HighLowProportion levelSchoolAgeLowVision(List<StatConclusion> statConclusions, Integer lowVisionLevel) {
        HighLowProportion highLowProportion = new HighLowProportion();

        List<StatConclusion> lowStatList = statConclusions.stream().filter(s -> Objects.equals(s.getLowVisionLevel(), lowVisionLevel)).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(lowStatList)) {
            return new HighLowProportion();
        }

        Map<Integer, List<StatConclusion>> collectMap = lowStatList.stream().collect(Collectors.groupingBy(StatConclusion::getSchoolAge));

        Integer maxName = -1;
        Integer minName = -1;
        float maxProportion = 0f;
        float minProportion = 0f;
        for (Map.Entry<Integer, List<StatConclusion>> entry : collectMap.entrySet()) {
            List<StatConclusion> value = entry.getValue();
            float result = (float) value.size() / (float) lowStatList.size();
            if (result > maxProportion) {
                maxName = entry.getKey();
                maxProportion = result;
            }
            if (result < minProportion) {
                minName = entry.getKey();
                minProportion = result;
            }
        }

        highLowProportion.setMaxName(SchoolAge.getDesc(maxName));
        highLowProportion.setMaxProportion(new BigDecimal(maxProportion).setScale(2, RoundingMode.HALF_UP).toString());
        highLowProportion.setMinName(SchoolAge.getDesc(minName));
        highLowProportion.setMinProportion(BigDecimal.valueOf(minProportion).setScale(2, RoundingMode.HALF_UP).toString());
        return highLowProportion;
    }

    public HighLowProportion warningTableHP(List<WarningTable> tables, Function<WarningTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            WarningTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        WarningTable max = tables.get(tables.size() - 1);
        WarningTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 幼儿园-学校
     */
    public HighLowProportion kScreeningInfoTableHP(List<KindergartenScreeningInfoTable> tables, Function<KindergartenScreeningInfoTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            KindergartenScreeningInfoTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        KindergartenScreeningInfoTable max = tables.get(tables.size() - 1);
        KindergartenScreeningInfoTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 小学-学校
     */
    public HighLowProportion pScreeningInfoTableHP(List<PrimaryScreeningInfoTable> tables, Function<PrimaryScreeningInfoTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            PrimaryScreeningInfoTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        PrimaryScreeningInfoTable max = tables.get(tables.size() - 1);
        PrimaryScreeningInfoTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 小学-学校
     */
    public HighLowProportion ageLowVisionTableHP(List<SchoolAgeLowVisionTable> tables, Function<SchoolAgeLowVisionTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            SchoolAgeLowVisionTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        SchoolAgeLowVisionTable max = tables.get(tables.size() - 1);
        SchoolAgeLowVisionTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-屈光
     */
    public HighLowProportion ageRefractiveTableHP(List<RefractiveTable> tables, Function<RefractiveTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            RefractiveTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        RefractiveTable max = tables.get(tables.size() - 1);
        RefractiveTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-散光
     */
    public HighLowProportion ageAstigmatismTableHP(List<AstigmatismTable> tables, Function<AstigmatismTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            AstigmatismTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        AstigmatismTable max = tables.get(tables.size() - 1);
        AstigmatismTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-戴眼
     */
    public HighLowProportion ageWearingTableHP(List<AgeWearingTable> tables, Function<AgeWearingTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            AgeWearingTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        AgeWearingTable max = tables.get(tables.size() - 1);
        AgeWearingTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-戴眼
     */
    public HighLowProportion lowVisionTableHP(List<LowVisionTable> tables, Function<LowVisionTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            LowVisionTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        LowVisionTable max = tables.get(tables.size() - 1);
        LowVisionTable min = tables.get(0);
        return new HighLowProportion(max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    public MaxMinProportion getMaxMinProportion(String proportion, List<GradeRefractive.Table> tables, Function<GradeRefractive.Table, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            GradeRefractive.Table min = tables.get(0);
            return new MaxMinProportion(proportion, min.getClassName(), String.valueOf(comparingFunction.apply(min)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        GradeRefractive.Table max = tables.get(tables.size() - 1);
        GradeRefractive.Table min = tables.get(0);
        return new MaxMinProportion(proportion, max.getClassName(), String.valueOf(comparingFunction.apply(max)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
    }

    public MaxMinProportion getWarningMaxMinProportion(String proportion, List<GradeWarning.Table> tables, Function<GradeWarning.Table, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            GradeWarning.Table min = tables.get(0);
            return new MaxMinProportion(proportion, min.getClassName(), String.valueOf(comparingFunction.apply(min)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        GradeWarning.Table max = tables.get(tables.size() - 1);
        GradeWarning.Table min = tables.get(0);
        return new MaxMinProportion(proportion, max.getClassName(), String.valueOf(comparingFunction.apply(max)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
    }

    public MaxMinProportion getVisionMaxMinProportion(String proportion, List<GradeLowVision.Table> tables, Function<GradeLowVision.Table, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            GradeLowVision.Table min = tables.get(0);
            return new MaxMinProportion(proportion, min.getClassName(), String.valueOf(comparingFunction.apply(min)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        GradeLowVision.Table max = tables.get(tables.size() - 1);
        GradeLowVision.Table min = tables.get(0);
        return new MaxMinProportion(proportion, max.getClassName(), String.valueOf(comparingFunction.apply(max)), min.getClassName(), String.valueOf(comparingFunction.apply(min)));
    }
}
