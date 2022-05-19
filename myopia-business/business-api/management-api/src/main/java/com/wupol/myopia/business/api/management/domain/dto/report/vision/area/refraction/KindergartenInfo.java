package com.wupol.myopia.business.api.management.domain.dto.report.vision.area.refraction;

import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.kindergarten.SexRefractive;
import lombok.Getter;
import lombok.Setter;

/**
 * 幼儿园
 *
 * @author Simple4H
 */
@Getter
@Setter
public class KindergartenInfo {

    /**
     * 不同性别屈光筛查情况
     */
    private SexRefractive sexRefractive;

    /**
     * 不同学龄段屈光筛查情况
     */
    private SchoolAgeRefractive schoolAgeRefractive;

    /**
     * 不同年龄段屈光筛查情况
     */
    private AgeRefractive ageRefractive;

    /**
     * 历年屈光情况趋势分析
     */
    private HistoryRefractive historyRefractive;
}
