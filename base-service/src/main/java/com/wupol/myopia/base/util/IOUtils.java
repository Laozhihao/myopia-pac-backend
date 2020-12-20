package com.wupol.myopia.base.util;

import java.io.File;

/**
 * @Author HaoHao
 * @Date 2020/12/21
 **/
public class IOUtils {

    public static String getTempSubPath(String sub) {
        String tmpdir = getTempPath();
        String subDirStr = tmpdir + File.separator + sub;
        File subDirFile = new File(subDirStr);
        subDirFile.mkdirs();
        return subDirStr;
    }

    public static String getTempPath() {
        String tmpdir = System.getProperty("java.io.tmpdir");
        File tempDir = new File(tmpdir);
        if (tempDir.exists() && tempDir.isDirectory() && tempDir.canWrite()) {
            return tmpdir;
        } else {
            throw new RuntimeException("系统临时目录不存在或不可写");
        }
    }
}
