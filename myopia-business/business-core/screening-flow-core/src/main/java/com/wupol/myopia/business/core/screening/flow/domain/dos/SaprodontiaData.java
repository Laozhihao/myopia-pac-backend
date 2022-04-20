package com.wupol.myopia.business.core.screening.flow.domain.dos;

import com.wupol.myopia.business.core.screening.flow.domain.dto.SaprodontiaStat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @Author HaoHao
 * @Date 2022/4/18
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class SaprodontiaData extends SaprodontiaDataDO {
    /**
     * 龋齿统计
     */
    private SaprodontiaStat saprodontiaStat;
}
