package com.wupol.myopia.business.core.screening.flow.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

import java.util.List;

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
    /**
     * 限制次数
     */
    private static final long LIMIT_TIMES = 5;

    private static final long serialVersionUID = 1L;
    /**
     * 可以准备发送
     */
    public static final Integer STATUS_READY_TO_SEND = 0;
    /**
     * 发送失败
     */
    public static final Integer STATUS_SEND_FAILURE = -1;
    /**
     * 发送成功
     */
    public static final Integer STATUS_SEND_SUCCESS = 1;
    /**
     * 取消发送
     */
    public static final Integer STATUS_SEND_CANCEL = 2;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 学生id
     */
    private Integer studentId;

    /**
     * 短信模板id
     */
    private Integer msgTemplateId;

    /**
     * 电话号码(发送的时候才记录)
     */
    @TableField(typeHandler = JacksonTypeHandler.class)
    private List<String> phoneNumbers;

    /**
     * 发送状态,-1发送失败,0准备发送,1是发送成功,2是取消发送
     */
    private Integer sendStatus;

    /**
     * 待发送的时间
     */
    private Date sendTime;
    /**
     * 是在一年的第几天发送
     */
    private String sendDayOfYear;
    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 发送的次数
     */
    private Integer sendTimes;

    /**
     * 创建时间
     */
    private Date createTime;


}
