package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.api.management.validator.AddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.QueryValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateStatusValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateValidatorGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * APP的apk版本管理实体
 *
 * @Author HaoHao
 * @Date 2021-11-23
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class AppVersionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID主键
     */
    @NotNull(message = "ID不能为空", groups = {UpdateValidatorGroup.class, UpdateStatusValidatorGroup.class})
    private Integer id;

    /**
     * 包名，例如：com.xbt.eyeproject
     */
    @NotBlank(message = "packageName不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class, QueryValidatorGroup.class})
    private String packageName;

    /**
     * 渠道，例如：Official-官方、HaiKou-海口、KunMing-昆明、JinCheng-晋城、YunCheng-运城
     */
    @NotBlank(message = "channel不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class, QueryValidatorGroup.class})
    private String channel;

    /**
     * apk版本，例如：v1.2
     */
    @NotBlank(message = "version不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class})
    private String version;

    /**
     * 版本号，例如：10
     */
    @NotBlank(message = "buildCode不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class})
    private String buildCode;

    /**
     * 是否强制更新，0-否（默认）、1-是
     */
    @NotNull(message = "isForceUpdate不能为空", groups = AddValidatorGroup.class)
    private Boolean isForceUpdate;

    /**
     * 是否自动下载，0-否（默认）、1-是
     */
    @NotNull(message = "isAutoUpdate不能为空", groups = AddValidatorGroup.class)
    private Boolean isAutoUpdate;

    /**
     * 状态，0-启用、1-停用（默认）
     */
    @NotNull(message = "status不能为空", groups = UpdateStatusValidatorGroup.class)
    @Min(value = 0)
    @Max(value = 1)
    private Integer status;

    /**
     * 备注
     */
    private String remark;
}
