package com.wupol.myopia.business.api.management.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.api.management.domain.dto.AppVersionDTO;
import com.wupol.myopia.business.api.management.validator.AddValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateStatusValidatorGroup;
import com.wupol.myopia.business.api.management.validator.UpdateValidatorGroup;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.system.domain.model.AppVersion;
import com.wupol.myopia.business.core.system.service.AppVersionService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.groups.Default;

/**
 * @Author HaoHao
 * @Date 2021-11-23
 */
@Validated
@ResponseResultBody
@CrossOrigin
@RestController
@RequestMapping("/management/app/version")
public class AppVersionController {

    @Autowired
    private AppVersionService appVersionService;
    @Autowired
    private ResourceFileService resourceFileService;

    /**
     * 获取app版本列表（分页）
     *
     * @param appVersion 查询条件
     * @param pageRequest 分页参数
     * @return com.baomidou.mybatisplus.core.metadata.IPage<com.wupol.myopia.business.core.system.domain.model.AppVersion>
     **/
    @GetMapping("list")
    public IPage<AppVersion> getAppVersionList(AppVersion appVersion, @Validated PageRequest pageRequest) {
        return appVersionService.findByPage(appVersion, pageRequest.getCurrent(), pageRequest.getSize());
    }

    /**
     * 新增app版本
     *
     * @param apkFile apk文件
     * @param appVersionDTO app版本信息
     * @return void
     **/
    @PostMapping
    public void addAppVersion(@NotNull(message = "apkFile不能为空") MultipartFile apkFile,
                                 @Validated(value = AddValidatorGroup.class) AppVersionDTO appVersionDTO) throws UtilException {
        // 上传
        ResourceFile resourceFile = resourceFileService.uploadFileAndSave(apkFile, "apk");
        // 保存
        AppVersion appVersion = new AppVersion();
        BeanUtils.copyProperties(appVersionDTO, appVersion);
        appVersion.setApkFileResourceId(resourceFile.getId())
                .setApkFileName(apkFile.getOriginalFilename())
                .setApkFileSize(apkFile.getSize())
                .setCreateUserId(CurrentUserUtil.getCurrentUser().getId());
        appVersionService.save(appVersion);
    }

    /**
     * 更新apk版本信息
     *
     * @param appVersionDTO apk版本信息
     * @return void
     **/
    @PutMapping
    public void updateAppVersion(@RequestBody @Validated(value = UpdateValidatorGroup.class) AppVersionDTO appVersionDTO) {
        AppVersion appVersion = new AppVersion();
        BeanUtils.copyProperties(appVersionDTO, appVersion);
        appVersionService.updateById(appVersion);
    }

    /**
     * 更新app版本状态
     *
     * @param appVersionDTO 状态信息
     * @return void
     **/
    @PutMapping("/status")
    public void updateAppVersionStatus(@RequestBody @Validated(value = {UpdateStatusValidatorGroup.class, Default.class}) AppVersionDTO appVersionDTO) {
        appVersionService.updateById(new AppVersion().setId(appVersionDTO.getId()).setStatus(appVersionDTO.getStatus()));
    }
}
