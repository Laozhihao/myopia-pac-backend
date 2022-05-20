package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction.primary;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.primary.GenderWearingGlasses;
import lombok.Getter;
import lombok.Setter;

/**
 * 小学及以上
 *
 * @author Simple4H
 */
@Getter
@Setter
public class PrimaryInfo {

    /**
     * 男女屈光筛查情况
     */
    private GenderRefraction genderRefraction;

    /**
     * 不同学龄段屈光筛查情况
     */
    private SchoolAgeRefraction schoolAgeRefraction;

    /**
     * 不同年龄段屈光筛查情况
     */
    private AgeRefraction ageRefraction;

    /**
     * 男女近视矫正情况
     */
    private GenderWearingGlasses genderWearingGlasses;

    /**
     * 不同学龄段近视矫正情况
     */
    private SchoolAgeWearingGlasses schoolAgeWearingGlasses;

    /**
     * 不同年龄段近视矫正情况
     */
    private AgeWearingGlasses ageWearingGlasses;

    /**
     * 历年屈光情况趋势分析
     */
    private HistoryRefraction historyRefraction;


}
