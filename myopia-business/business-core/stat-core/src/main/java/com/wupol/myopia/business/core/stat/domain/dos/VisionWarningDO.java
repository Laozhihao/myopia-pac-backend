package com.wupol.myopia.business.core.stat.domain.dos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 视力预警
 *
 * @author hang.yuan 2022/4/13 15:28
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class VisionWarningDO implements Serializable,FrontTableId {

    /**
     * 小学及以上--视力预警人数
     */
    private Integer visionWarningNum;

    /**
     * 视力预警比例
     */
    private String visionWarningRatio;

    /**
     * 小学及以上--零级预警人数（默认0）
     */
    private Integer visionLabel0Num;

    /**
     * 小学及以上--零级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel0Ratio;

    /**
     * 小学及以上--一级预警人数（默认0）
     */
    private Integer visionLabel1Num;

    /**
     * 小学及以上--一级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel1Ratio;

    /**
     * 小学及以上--二级预警人数（默认0）
     */
    private Integer visionLabel2Num;

    /**
     * 小学及以上--二级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel2Ratio;

    /**
     * 小学及以上--三级预警人数（默认0）
     */
    private Integer visionLabel3Num;

    /**
     * 小学及以上--三级预警比例（均为整数，如10.01%，数据库则是1001）
     */
    private String visionLabel3Ratio;

    @Override
    public Integer getSerialVersionUID() {
        return 5;
    }
}
