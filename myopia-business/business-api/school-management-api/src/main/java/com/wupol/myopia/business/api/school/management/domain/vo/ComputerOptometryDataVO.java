package com.wupol.myopia.business.api.school.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 电脑验光
 *
 * @author hang.yuan
 * @date 2022/9/13
 */
@Data
@Accessors(chain = true)
public class ComputerOptometryDataVO implements Serializable{

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer eyeType;
    /**
     * 轴位
     */
    private String axial;
    /**
     * 球镜
     */
    private String sph;
    /**
     * 柱镜
     */
    private String cyl;

}