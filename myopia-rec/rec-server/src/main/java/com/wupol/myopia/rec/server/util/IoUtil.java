package com.wupol.myopia.rec.server.util;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.wupol.myopia.rec.server.exception.BusinessException;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.nio.file.Paths;
import java.util.Objects;

/**
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@UtilityClass
public class IoUtil {

    public static String getTempSubPath(String sub) {
        String tmpdir = getTempPath();
        String subDirStr =  Paths.get(tmpdir,sub).toString();
        File subDirFile = new File(subDirStr);
        if (FileUtil.exist(subDirFile)){
            return subDirStr;
        }
        FileUtil.mkdir(subDirFile);
        return subDirStr;
    }

    public static String getTempPath() {
        String tmpdir = Objects.equals(Boolean.TRUE,windowsSystem())?"C:\\tmp":"/tmp";
        if (!FileUtil.exist(tmpdir)){
            FileUtil.mkdir(tmpdir);
        }
        File tempDir = new File(tmpdir);

        if (tempDir.isDirectory() && tempDir.canWrite()) {
            return tmpdir;
        } else {
            throw new BusinessException("系统临时目录不存在或不可写");
        }
    }

    /**
     * 判断是否windows系统
     */
    public static Boolean windowsSystem(){
        String system = System.getProperty("os.name").toLowerCase();
        return StrUtil.isNotBlank(system) &&  !system.contains("mac") && !system.contains("linux");
    }
}
