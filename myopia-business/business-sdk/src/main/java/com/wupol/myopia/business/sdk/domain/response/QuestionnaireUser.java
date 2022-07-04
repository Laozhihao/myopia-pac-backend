package com.wupol.myopia.business.sdk.domain.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 问卷系统用户
 *
 * @Author wulizhou
 * @Date 2022/6/30 12:15
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Accessors(chain = true)
public class QuestionnaireUser {

    /**
     * 用户ID
     */
    private Integer id;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    private Integer orgId;

    /**
     * 真实姓名
     */
    private String realName;

}
