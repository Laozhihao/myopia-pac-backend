package com.wupol.myopia.business.core.screening.flow.domain.model;

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
 * 数据报送
 *
 * @Author Simple4H
 * @Date 2022-11-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_national_data_download_record")
public class NationalDataDownloadRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学校Id
     */
    private Integer schoolId;

    /**
     * 说明
     */
    private String remark;

    /**
     * 成功匹配
     */
    private Integer successMatch;

    /**
     * 失败匹配
     */
    private Integer failMatch;

    /**
     * 文件Id
     */
    private Integer fileId;

    /**
     * 状态 0-创建 1-进行中 2-成功 3-失败
     */
    private Integer status;

    /**
     * 筛查计划id
     */
    private Integer screeningPlanId;

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


}
