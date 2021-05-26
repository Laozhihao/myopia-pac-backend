package com.wupol.myopia.business.core.system.config;

import com.wupol.myopia.business.core.system.constants.BigScreeningMapConstants;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Redis配置
 *
 * @Author HaoHao
 * @Date 2020/12/21
 **/
@Configuration
@EnableCaching
public class BigScreeningMapRedisConfig {

    private static final int EXPIRED_DAYS = 30;


    /**
     * cache配置:
     * 1.不缓存null
     * 2.默认过期时间1天
     * 3.默认分割符为 单冒号
     * @param redisConnectionFactory
     * @return
     */
    @Bean(BigScreeningMapConstants.BIG_SCREENING_MAP_CACHE_MANAGEMANT_BEAN_ID)
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                //变双冒号为单冒号
                .computePrefixWith(name -> name + ":")
                //默认30天
                .entryTtl(Duration.ofDays(EXPIRED_DAYS))
                //不保存空
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));

        return RedisCacheManager.RedisCacheManagerBuilder
                .fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
