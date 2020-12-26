package com.wupol.myopia.oauth.cache;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.oauth.domain.model.Permission;
import com.wupol.myopia.oauth.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 权限资源缓存
 *
 * @Author HaoHao
 * @Date 2020/12/26
 **/
@Component
public class PermissionResourceCache implements CommandLineRunner {

    @Autowired
    private PermissionService permissionService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 启动项目后，自动初始化缓存
     * @return void
     **/
    @Override
    public void run(String... args) {
        List<Permission> permissionList = permissionService.list();
        Map<String, List<Permission>> permissionMap = permissionList.stream()
                .collect(Collectors.groupingBy(x -> RedisConstant.SINGLE_SYSTEM_PERMISSION_KEY_PREFIX + x.getSystemCode().toString()));
        redisTemplate.opsForHash().putAll(RedisConstant.ALL_PERMISSION_KEY, permissionMap);
    }
}
