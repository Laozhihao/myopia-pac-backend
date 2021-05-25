package com.wupol.myopia.business.api.management.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @Classname BigScreeningProperties
 * @Description 大屏展示的配置类
 * @Date 2021/5/25 12:25 下午
 * @Author Jacob
 * @Version
 */
@Configuration
@EnableConfigurationProperties(BigScreeningProperties.class)
@ConfigurationProperties(prefix = BigScreeningProperties.PROPERTIES_PREFIX)
@Getter
@Setter
public class BigScreeningProperties {

    public static final String PROPERTIES_PREFIX = "vistel.myopia.big-screening";
    /**
     * 是否开启debug模式,debug模式开启后
     * 1.关闭定时任务统计大屏数据
     * 2.访问大屏数据实时计算,并且不使用缓存
     * 默认false
     */
    boolean debug;
}
