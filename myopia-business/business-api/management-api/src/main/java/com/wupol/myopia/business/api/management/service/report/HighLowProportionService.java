package com.wupol.myopia.business.api.management.service.report;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.KindergartenScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.PrimaryScreeningInfoTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary.WarningTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.HighLowProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.MaxMinProportion;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.RefractiveTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeLowVision;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeRefractive;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.GradeWarning;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.AgeWearingTable;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.CommonLowVisionTable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * 比分比
 *
 * @author Simple4H
 */
@Service
public class HighLowProportionService {

    public HighLowProportion schoolAgeLowVisionTableHP(List<CommonLowVisionTable> tables, Function<CommonLowVisionTable, Float> comparingFunction) {
        return getProportion(tables, comparingFunction);
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
    public HighLowProportion ageLowVisionTableHP(List<CommonLowVisionTable> tables, Function<CommonLowVisionTable, Float> comparingFunction) {
        return getProportion(tables, comparingFunction);
    }

    private HighLowProportion getProportion(List<CommonLowVisionTable> tables, Function<CommonLowVisionTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new HighLowProportion();
        }
        if (tables.size() == 1) {
            CommonLowVisionTable min = tables.get(0);
            return new HighLowProportion(min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        CommonLowVisionTable max = tables.get(tables.size() - 1);
        CommonLowVisionTable min = tables.get(0);
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
    public MaxMinProportion ageWearingTableHP(String proportion, List<AgeWearingTable> tables, Function<AgeWearingTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return new MaxMinProportion();
        }
        if (tables.size() == 1) {
            AgeWearingTable min = tables.get(0);
            return new MaxMinProportion(proportion, min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        AgeWearingTable max = tables.get(tables.size() - 1);
        AgeWearingTable min = tables.get(0);
        return new MaxMinProportion(proportion, max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }

    /**
     * 年龄-戴眼
     */
    public HighLowProportion lowVisionTableHP(List<CommonLowVisionTable> tables, Function<CommonLowVisionTable, Float> comparingFunction) {
        return getProportion(tables, comparingFunction);
    }

    public MaxMinProportion getMaxMinProportion(String proportion, List<GradeRefractive.Table> tables, Function<GradeRefractive.Table, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return null;
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
            return null;
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
            return null;
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

    /**
     * 小学-学校
     */
    public MaxMinProportion primaryMyopiaMaxMinProportion(String proportion, List<PrimaryScreeningInfoTable> tables, Function<PrimaryScreeningInfoTable, Float> comparingFunction) {
        if (CollectionUtils.isEmpty(tables)) {
            return null;
        }
        if (tables.size() == 1) {
            PrimaryScreeningInfoTable min = tables.get(0);
            return new MaxMinProportion(proportion, min.getName(), String.valueOf(comparingFunction.apply(min)), min.getName(), String.valueOf(comparingFunction.apply(min)));
        }
        tables.sort(Comparator.comparing(comparingFunction));
        PrimaryScreeningInfoTable max = tables.get(tables.size() - 1);
        PrimaryScreeningInfoTable min = tables.get(0);
        return new MaxMinProportion(proportion, max.getName(), String.valueOf(comparingFunction.apply(max)), min.getName(), String.valueOf(comparingFunction.apply(min)));
    }
}
