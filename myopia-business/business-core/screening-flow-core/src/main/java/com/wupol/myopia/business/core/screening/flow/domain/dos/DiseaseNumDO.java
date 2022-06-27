package com.wupol.myopia.business.core.screening.flow.domain.dos;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 疾病数量实体
 *
 * @author hang.yuan 2022/5/23 18:33
 */
@Data
@Accessors(chain = true)
public class DiseaseNumDO {
    /**
     * 肝炎
     */
    private Integer hepatitis;
    /**
     * 肾炎
     */
    private Integer nephritis;
    /**
     * 心脏病
     */
    private Integer heartDisease;
    /**
     * 贫血
     */
    private Integer anemia;
    /**
     * 高血压
     */
    private Integer hypertension;
    /**
     * 糖尿病
     */
    private Integer diabetes;
    /**
     * 过敏性哮喘
     */
    private Integer allergicAsthma;
    /**
     * 身体残疾
     */
    private Integer physicalDisability;

}
