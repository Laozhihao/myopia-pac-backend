package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * 法里奥设备上传实体
 *
 * @author Simple4H
 */
@Getter
@Setter
public class FkrRequestDTO {

    /**
     * 设备编号
     */
    private String deviceSN;

    /**
     * 用户Id
     */
    private String uid;

    /**
     * 左眼球镜
     */
    private String leftSph;

    /**
     * 左眼柱镜
     */
    private String leftCyl;

    /**
     * 左眼轴位
     */
    private String leftAxial;

    /**
     * 右眼球镜
     */
    private String rightSph;

    /**
     * 右眼柱镜
     */
    private String rightCyl;

    /**
     * 右眼轴位
     */
    private String rightAxial;
}
