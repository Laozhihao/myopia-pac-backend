package com.wupol.myopia.migrate.controller;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.migrate.service.migrate.MigrateDataHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Author HaoHao
 * @Date 2022/3/23
 **/
@ResponseResultBody
@RestController
@RequestMapping("/migrate/data")
public class MigrateDataController {

    @Autowired
    private MigrateDataHandler migrateDataHandler;
    @Autowired
    private RedisUtil redisUtil;

    /**
     * 数据迁移
     *
     * @return void
     **/
    @GetMapping()
    public void migrateData() {
        // 加锁，超过半个小时自动解锁
        Assert.isTrue(redisUtil.tryLock("MIGRATE_DATA", 0, 30 * 60L), "正在迁移中，请勿重复提交！");
        try {
            migrateDataHandler.migrateData();
        } catch (Exception e) {
            throw new BusinessException("数据迁移失败", e);
        } finally {
            // 解说
            redisUtil.unlock("MIGRATE_DATA");
        }
    }
}
