package com.wupol.myopia.business.api.management.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 常见病项目占比
 *
 * @author hang.yuan
 * @date 2022/6/6
 */
@Data
public class CommonDiseasesItemVO {
    /**
     * 数量
     */
    private Integer num;
    /**
     * 占比
     */
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal ratio;

    public CommonDiseasesItemVO(Integer num, BigDecimal ratio) {
        this.num = num;
        this.ratio = ratio;
    }
}
