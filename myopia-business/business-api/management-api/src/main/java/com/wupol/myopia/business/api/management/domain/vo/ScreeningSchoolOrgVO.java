package com.wupol.myopia.business.api.management.domain.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 筛查机构（学校）
 *
 * @author hang.yuan 2022/9/27 16:46
 */
@Data
@Accessors(chain = true)
public class ScreeningSchoolOrgVO implements Serializable {

    /**
     * 筛查机构Id(学校ID)
     */
    private Integer id;
    /**
     * 是否已有任务
     */
    private Boolean alreadyHaveTask;

    /**
     * 筛查机构名称(学校名称)
     */
    private String name;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 是否存在任务中
     */
    private Boolean isAlreadyExistsTask;
}
