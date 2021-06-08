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
 * 
 * 数据异常告警短信
 * @Author jacob
 * @Date 2021-06-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_warning_msg")
public class WarningMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 通知id
     */
    private Integer noticeId;

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 计划id
     */
    private Integer planId;

    /**
     * 短信模板id
     */
    private Integer msgTemplateId;

    /**
     * 电话号码(发送的时候才记录)
     */
    private String phoneNumbers;

    /**
     * 短信参数
     */
    private String msgParams;

    /**
     * 发送状态,-1发送失败,0准备发送,1是发送成功,2是取消发送
     */
    private Integer sendStatus;

    /**
     * 待发送的时间
     */
    private Date sendTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 创建时间
     */
    private Date createTime;


}
