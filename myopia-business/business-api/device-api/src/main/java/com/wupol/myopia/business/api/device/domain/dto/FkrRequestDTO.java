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

    /**
     * 左眼-k1
     */
    private String leftK1;

    /**
     * 左眼-k2
     */
    private String leftK2;

    /**
     * 右眼-k1
     */
    private String rightK1;

    /**
     * 右眼-k2
     */
    private String rightK2;
}
