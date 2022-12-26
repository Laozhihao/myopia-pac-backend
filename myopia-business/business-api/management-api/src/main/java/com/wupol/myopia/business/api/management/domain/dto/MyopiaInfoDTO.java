package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 学生近视情况
 * @Author wulizhou
 * @Date 2022/12/26 12:27
 */
@Data
@Builder
@Accessors(chain = true)
public class MyopiaInfoDTO {

    /**
     * 近视情况（性别）
     */
    private List<MyopiaDTO> genderMyopia;

    /**
     * 男生占总体近视率
     */
    private Float maleGeneralMyopiaRatio;

    /**
     * 女生占总体近视率
     */
    private Float femaleGeneralMyopiaRatio;

    /**
     * 学生近视监测结果（年级）
     */
    private List<GenderMyopiaInfoDTO> gradeMyopia;

    /**
     * 学生近视监测结果（班级）
     */
    private Map<String, List<GenderMyopiaInfoDTO>> classMyopia;

}
