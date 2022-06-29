package com.wupol.myopia.business.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceHelper {
    /**
     * 构造方法
     */
    private ResourceHelper() {
        throw new UnsupportedOperationException();
    }

    /**
     * 以字符串方式获取资源
     *
     * @param clazz 类
     * @param name 资源名称
     * @return 字符串
     */
    public static <T> String getResourceAsString(Class<T> clazz, String name) {
        try (InputStream is = clazz.getResourceAsStream(name)) {
            assert is != null;
            return IOUtils.toString(is, String.valueOf(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(String.format("以字符串方式获取资源(%s)异常", name), e);
        }
    }
}
