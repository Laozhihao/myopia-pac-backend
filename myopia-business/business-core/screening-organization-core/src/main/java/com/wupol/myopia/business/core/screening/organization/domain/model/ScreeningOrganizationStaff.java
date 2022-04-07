package com.wupol.myopia.business.core.screening.organization.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.util.Assert;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 机构-人员表
 *
 * @Author HaoHao
 * @Date 2020-12-22
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_organization_staff")
public class ScreeningOrganizationStaff implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自动创建的筛查人员默认账号密码
     */
    public static final String AUTO_CREATE_STAFF_DEFAULT_PASSWORD = "scry12345678";

    /**
     * 自动创建筛查人员名称
     */
    public static final String AUTO_CREATE_STAFF_DEFAULT_NAME = "筛查人员TA";

    /**
     * 筛查人员类型(普通筛查人员)
     */
    public static final int GENERAL_SCREENING_PERSONNEL = 0;

    /**
     * 筛查人员类型(自动创建筛查人员)
     */
    public static final int AUTO_CREATE_SCREENING_PERSONNEL = 1;


    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer govDeptId;

    /**
     * 筛查机构表id
     */
    private Integer screeningOrgId;

    /**
     * 用户id
     */
    private Integer userId;

    /**
     * 创建人ID
     */
    private Integer createUserId;

    /**
     * 说明
     */
    private String remark;

    /**
     * 签名id
     */
    private Integer signFileId;


    /**
     * 筛查人员类型（0普通筛查人员，1自动生成的筛查人员）
     */
    private Integer type;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 是否为自动生成的筛查人员
     *
     * @return boolean
     **/
    public static boolean isAutoCreateScreeningStaff(Integer type) {
        Assert.notNull(type, "筛查人员类型为空");
        return type == AUTO_CREATE_SCREENING_PERSONNEL;
    }


}
