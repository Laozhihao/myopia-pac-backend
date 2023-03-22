package com.wupol.myopia.business.core.system.service;

import com.vistel.Interface.exception.UtilException;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.domain.model.ResourceFile;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.system.domain.mapper.AppVersionMapper;
import com.wupol.myopia.business.core.system.domain.model.AppChannel;
import com.wupol.myopia.business.core.system.domain.model.AppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;
import java.util.Optional;

/**
 * @Author HaoHao
 * @Date 2021-11-23
 */
@Service
public class AppVersionService extends BaseService<AppVersionMapper, AppVersion> {

    @Autowired
    private ResourceFileService resourceFileService;
    @Autowired
    private AppChannelService appChannelService;

    /**
     * 获取最新的一个版本
     *
     * @param packageName 包名
     * @param channel 渠道
     * @return com.wupol.myopia.business.core.system.domain.model.AppVersion
     **/
    public AppVersion getLatestVersionByPackageNameAndChannel(String packageName, String channel) {
        Assert.hasText(packageName, "packageName不能为空");
        Assert.hasText(channel, "channel不能为空");
        // 降级处理，如果找不到channel，就默认为官方的
        AppChannel appChannel = appChannelService.findOne(new AppChannel().setEnName(channel).setStatus(0));
        channel = Optional.ofNullable(appChannel).map(AppChannel::getEnName).orElse("Official");
        AppVersion appVersion = baseMapper.selectLatestVersionByPackageNameAndChannel(new AppVersion().setPackageName(packageName).setChannel(channel));
        if (Objects.isNull(appVersion)) {
            return null;
        }
        String apkUrl = resourceFileService.getResourcePath(appVersion.getApkFileResourceId());
        return appVersion.setApkUrl(apkUrl);
    }

    /**
     * 设置apk文件信息
     *
     * @param appVersion    APP版本学校
     * @param apkFile       apk文件
     * @throws UtilException
     */
    public void setApkFileInfo(AppVersion appVersion, MultipartFile apkFile) throws UtilException {
        ResourceFile resourceFile = resourceFileService.uploadFileAndSave(apkFile, "apk");
        appVersion.setApkFileResourceId(resourceFile.getId())
                .setApkFileName(apkFile.getOriginalFilename())
                .setApkFileSize(apkFile.getSize());
    }
}
