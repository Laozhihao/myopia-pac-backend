package com.wupol.myopia.business.core.system.domain.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.Date;

/**
 * APP版本管理表
 *
 * @Author HaoHao
 * @Date 2021-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("m_app_version")
public class AppVersion implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 包名，例如：com.xbt.eyeproject
     */
    private String packageName;

    /**
     * 渠道，例如：Official-官方、HaiKou-海口、KunMing-昆明、JinCheng-晋城、YunCheng-运城
     */
    private String channel;

    /**
     * APP版本，例如：v1.2
     */
    private String version;

    /**
     * 版本号，例如：10
     */
    private Integer buildCode;

    /**
     * 是否强制更新
     */
    private Boolean isForceUpdate;

    /**
     * 是否自动下载
     */
    private Boolean isAutoUpdate;

    /**
     * apk资源文件ID
     */
    private Integer apkFileResourceId;

    /**
     * 第三方下载安装包二维码图片文件ID
     */
    private Integer thirdpartyQrCodeFileId;

    /**
     * apk文件名
     */
    private String apkFileName;

    /**
     * apk大小，单位：b
     */
    private Long apkFileSize;

    /**
     * 状态，0-启用、1-停用（默认）
     */
    private Integer status;

    /**
     * 备注
     */
    private String remark;

    /**
     * 创建者ID
     */
    private Integer createUserId;

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
     * apk下载地址
     */
    @TableField(exist = false)
    private String apkUrl;

}
