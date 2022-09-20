package com.wupol.myopia.business.core.hospital.domain.model;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.List;

/**
 * 眼底检查
 *
 * @author Simple4H
 */
@Getter
@Setter
@Accessors(chain = true)
public class FundusMedicalRecord implements Serializable {

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 影像FileIds列表
     */
    private List<Integer> imageIdList;

    /**
     * 影像列表
     */
    private List<String> imageUrlList;
}
