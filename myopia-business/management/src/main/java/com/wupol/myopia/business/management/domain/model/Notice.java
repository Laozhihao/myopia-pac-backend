package com.wupol.myopia.business.management.domain.model;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * 消息表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_notice")
public class Notice {
    /**
     * id
     */
    private Integer id;

    /**
     * 创建人
     */
    private Integer createUserId;

    /**
     * 关联ID
     */
    private Integer linkId;

    /**
     * 通知的userId
     */
    private Integer noticeUserId;

    // 类型 0-站内信 1-筛查通知
    private Byte type;

    /**
     * 状态 0-未读 1-已读 2-删除
     */
    private Byte status;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 文件url
     */
    private String downloadUrl;

    /**
     * 创建时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm")
    private Date updateTime;
}