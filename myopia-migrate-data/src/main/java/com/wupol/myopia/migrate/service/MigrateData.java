package com.wupol.myopia.migrate.service;

import com.wupol.myopia.business.core.school.service.SchoolService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2022/1/6
 **/
@Slf4j
@Service
public class MigrateData {

    @Autowired
    private SchoolService schoolService;

    public void action() {
        log.debug(schoolService.getById(1).getName());
    }
}
