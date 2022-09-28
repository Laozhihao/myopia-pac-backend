package com.wupol.myopia.business.aggregation.screening.domain.vos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视力数据
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
@Accessors(chain = true)
public class VisionDataVO implements Serializable {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer eyeType;

    /**
     * 矫正视力
     */
    private String correctedVision;
    /**
     * 裸眼视力
     */
    private String nakedVision;
}