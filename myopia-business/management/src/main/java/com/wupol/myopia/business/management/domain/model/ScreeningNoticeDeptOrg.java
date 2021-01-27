package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 筛查通知通知到的部门或者机构表
 *
 * @Author HaoHao
 * @Date 2021-01-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_screening_notice_dept_org")
public class ScreeningNoticeDeptOrg implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 筛查通知--筛查通知表id
     */
    private Integer screeningNoticeId;

    /**
     * 筛查通知--接收对象所在的区域id
     */
    private Integer districtId;

    /**
     * 筛查通知--接收通知对象的id（机构id 或者 部门id）
     */
    private Integer acceptOrgId;

    /**
     * 筛查通知--操作状态（0未读 1 是已读 2是已创建）
     */
    private Integer operationStatus;

    /**
     * 筛查通知--操作人id（查看或者编辑的人id）
     */
    private Integer operatorId;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;


}
