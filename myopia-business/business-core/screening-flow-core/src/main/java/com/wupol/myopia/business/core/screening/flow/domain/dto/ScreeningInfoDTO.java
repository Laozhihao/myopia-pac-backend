package com.wupol.myopia.business.core.screening.flow.domain.dto;/*
 * @Author  钓猫的小鱼
 * @Date  2022/4/12 16:54
 * @Email: shuailong.wu@vistel.cn
 * @Des:
 */

import lombok.Data;

import java.util.List;

/**
 * 筛查信息扩展类：包括视力筛查/常见病筛查/复测筛查
 */
@Data
public class ScreeningInfoDTO {
    /**
     * 视力信息
     */
    private List<StudentResultDetailsDTO> vision;
    /**
     * 常见病
     */
    private CommonDiseasesDTO commonDiseases;
    /**
     * 复测信息
     */
    public ReScreenDTO rescreening;

}
