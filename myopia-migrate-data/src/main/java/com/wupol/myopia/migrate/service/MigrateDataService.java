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
public class MigrateDataService {

    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private SysStudentService sysStudentService;

    public void action() {
        log.debug(schoolService.getById(1).getName());
        log.debug(sysStudentEyeService.getById("1634776604969995901").getStudentName());
        log.debug(sysStudentService.getById("1634611634249358571").getStudentName());
    }
}
