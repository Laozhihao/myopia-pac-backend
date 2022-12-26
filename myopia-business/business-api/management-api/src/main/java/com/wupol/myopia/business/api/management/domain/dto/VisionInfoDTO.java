package com.wupol.myopia.business.api.management.domain.dto;

import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * 学生视力情况
 * @Author wulizhou
 * @Date 2022/12/26 12:28
 */
@Data
@Builder
@Accessors(chain = true)
public class VisionInfoDTO {

    /**
     * 整体视力程度情况
     */
    private MyopiaLevelDTO general;

    /**
     * 视力程度情况（性别）
     */
    private List<MyopiaLevelDTO> genderVision;

    /**
     * 视力程度情况（年级）
     */
    private List<MyopiaLevelDTO> gradeVision;

    /**
     * 视力程度情况（班级）
     */
    private Map<String, List<MyopiaInfoDTO>> classVision;

}
