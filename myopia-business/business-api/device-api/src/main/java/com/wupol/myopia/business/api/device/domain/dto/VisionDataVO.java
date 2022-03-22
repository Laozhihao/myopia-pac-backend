package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据实体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class VisionDataVO {

    private String uid;

    /**
     * 筛查学生Id
     */
    private Integer planStudentId;

    /**
     * 左裸眼视力
     */
    private String leftNakedVision;

    /**
     * 右裸眼视力
     */
    private String rightNakedVision;

    /**
     * 左矫正视力
     */
    private String leftCorrectedVision;

    /**
     * 右矫正视力
     */
    private String rightCorrectedVision;

    /**
     * 筛查时间
     */
    private Long screeningTime;
}
