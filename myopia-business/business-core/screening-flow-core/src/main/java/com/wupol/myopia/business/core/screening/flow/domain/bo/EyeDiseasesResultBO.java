package com.wupol.myopia.business.core.screening.flow.domain.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 其他眼病
 *
 * @author Simple4H
 */
@Getter
@Setter
public class EyeDiseasesResultBO {

    /**
     * 0 为左眼 1 为右眼
     */
    private Integer lateriality;

    /**
     * 眼部疾病
     */
    private List<String> eyeDiseases;
}
