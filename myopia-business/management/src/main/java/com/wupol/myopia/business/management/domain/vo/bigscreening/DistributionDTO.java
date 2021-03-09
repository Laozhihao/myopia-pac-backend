package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class DistributionDTO {

    /**
     * num
     */
    private NumDTO num;
    /**
     * gender
     */
    private GenderDTO gender;
    /**
     * age
     */
    private AgeDTO age;
    /**
     * schoolAge
     */
    private SchoolAgeDTO schoolAge;
    /**
     * statisticDistrict
     */
    private List<StatisticDistrictDTO> statisticDistrict;
}
