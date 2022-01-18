package com.wupol.myopia.migrate.service;

import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2022/1/6
 **/
@Service
public class MigrateData {

    @Autowired
    private SchoolService schoolService;

    public void action() {
        schoolService.getById(1);
    }
}
