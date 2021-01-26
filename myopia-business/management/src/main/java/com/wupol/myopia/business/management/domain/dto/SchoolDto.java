package com.wupol.myopia.business.management.domain.dto;

import com.wupol.myopia.business.management.domain.model.School;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 学校返回类
 *
 * @author Simple4H
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SchoolDto extends School {

    private String accountNo;

    /**
     * 筛查次数
     */
    private Integer screeningCount;

    /**
     * 是否能更新
     */
    private boolean canUpdate = false;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 行政区域名
     */
    private String districtName;
}
