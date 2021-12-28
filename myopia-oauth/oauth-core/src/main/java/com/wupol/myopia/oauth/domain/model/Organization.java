package com.wupol.myopia.oauth.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


/**
 * org基本信息表
 *
 * @Author wulizhou
 * @Date 2021-12-06
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("o_organization")
@NoArgsConstructor
@AllArgsConstructor
public class Organization implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 机构组织ID（如政府部门ID、学校ID、医院ID）
     */
    @NotNull(message = "机构ID不能为空")
    private Integer orgId;

    /**
     * 系统编号
     */
    @NotNull(message = "systemCodeID不能为空")
    private Integer systemCode;

    /**
     * 用户类型：0-平台管理员、1-政府人员、2-筛查机构、3-医院管理员
     */
    @NotNull(message = "userType")
    private Integer userType;

    /**
     * 组织状态：0-启用 1-禁止 2-删除
     */
    @Min(0)
    @Max(2)
    private Integer status;

}
