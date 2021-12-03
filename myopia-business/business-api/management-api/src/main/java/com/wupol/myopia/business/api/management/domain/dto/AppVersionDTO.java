package com.wupol.myopia.business.api.management.domain.dto;

import com.wupol.myopia.business.api.management.validator.AddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.QueryValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateStatusValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateValidatorGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

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
    @Length(max = 50, message = "包名超长")
    private String packageName;

    /**
     * 渠道，例如：Official-官方、HaiKou-海口、KunMing-昆明、JinCheng-晋城、YunCheng-运城
     */
    @NotBlank(message = "channel不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class, QueryValidatorGroup.class})
    @Length(max = 50, message = "渠道值超长")
    private String channel;

    /**
     * APP版本，例如：v1.2
     */
    @NotBlank(message = "version不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class})
    @Length(max = 30, message = "APP版本号超长")
    private String version;

    /**
     * 版本号，例如：10
     */
    @NotNull(message = "buildCode不能为空", groups = {AddValidatorGroup.class, UpdateValidatorGroup.class})
    @Max(value = 9999, message = "buildCode超过取值范围")
    private Integer buildCode;

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
     * 状态，0-停用（默认）、1-启用
     */
    @NotNull(message = "status不能为空", groups = UpdateStatusValidatorGroup.class)
    @Min(value = 0, message = "status值无效")
    @Max(value = 1, message = "status值无效")
    private Integer status;

    /**
     * 备注
     */
    @Length(max = 250, message = "更新内容超长")
    private String remark;
}
