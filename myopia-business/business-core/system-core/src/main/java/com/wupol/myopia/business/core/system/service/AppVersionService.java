package com.wupol.myopia.business.core.system.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.system.domain.mapper.AppVersionMapper;
import com.wupol.myopia.business.core.system.domain.model.AppVersion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2021-11-23
 */
@Service
public class AppVersionService extends BaseService<AppVersionMapper, AppVersion> {

    @Autowired
    private ResourceFileService resourceFileService;

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
        AppVersion appVersion = baseMapper.selectLatestVersionByPackageNameAndChannel(new AppVersion().setPackageName(packageName).setChannel(channel));
        if (Objects.isNull(appVersion)) {
            return null;
        }
        String apkUrl = resourceFileService.getResourcePath(appVersion.getApkFileResourceId());
        return appVersion.setApkUrl(apkUrl);
    }
}
