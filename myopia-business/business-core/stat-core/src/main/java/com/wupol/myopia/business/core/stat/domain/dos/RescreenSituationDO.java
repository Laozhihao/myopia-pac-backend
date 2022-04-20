package com.wupol.myopia.business.core.stat.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 复测情况
 *
 * @author hang.yuan 2022/4/13 15:21
 */
@Data
@Accessors(chain = true)
public class RescreenSituationDO implements Serializable {

    /**
     * 复测人数（默认0）
     */
    private Integer retestNum;

    /**
     * 复测率
     */
    private String retestRatio;

    /**
     * 戴镜复测人数（默认0）
     */
    private Integer wearingGlassRetestNum;

    /**
     * 戴镜复测率
     */
    private String wearingGlassRetestRatio;

    /**
     * 非戴镜复测人数（默认0）
     */
    private Integer withoutGlassRetestNum;

    /**
     * 非戴镜复测率
     */
    private String withoutGlassRetestRatio;

    /**
     * 复测项次（默认0）
     */
    private Integer rescreeningItemNum;

    /**
     * 错误项次数（默认0）
     */
    private Integer errorItemNum;

    /**
     * 发生率
     */
    private String incidence;
}
