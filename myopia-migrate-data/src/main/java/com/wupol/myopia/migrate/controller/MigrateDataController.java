package com.wupol.myopia.migrate.controller;

import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.migrate.service.MigrateDataService;
import org.springframework.beans.factory.annotation.Autowired;
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
    private MigrateDataService migrateDataService;

    /**
     * 数据迁移
     *
     * @return void
     **/
    @GetMapping()
    public void migrateData() {
        migrateDataService.action();
    }
}
