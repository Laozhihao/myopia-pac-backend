package com.wupol.myopia.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * 白名单配置
 * @Author HaoHao
 * @Date 2020/12/24
 **/
@Data
@Configuration
@ConfigurationProperties(prefix = "whitelist")
public class WhiteListConfig {
    private List<String> urls;
}
