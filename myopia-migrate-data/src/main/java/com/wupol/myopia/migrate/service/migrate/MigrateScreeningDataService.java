package com.wupol.myopia.migrate.service.migrate;

import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 迁移筛查数据
 * 注意：
 *      1.如何合并生物测量数据？
 *      2.如何合并常见病筛查数据？新的筛查计划？
 *
 * @Author HaoHao
 * @Date 2022/3/30
 **/
@Log4j2
@Service
public class MigrateScreeningDataService {

    @Autowired
    private ScreeningDataService screeningDataService;

    /**
     * 逐个学校迁移筛查数据
     *
     * @param screeningDataList 筛查数据（多个计划的）
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void migrateScreeningDataBySchool(List<ScreeningDataDO> screeningDataList) {
        log.info("==  迁移筛查数据-开始.....  ==");
        screeningDataList.forEach(screeningDataDO -> screeningDataService.migrateScreeningDataOfSameSchool(screeningDataDO.getSysStudentEyeList(),
                screeningDataDO.getSchoolId(),
                screeningDataDO.getScreeningOrgId(),
                screeningDataDO.getScreeningStaffUserId(),
                screeningDataDO.getPlanId()));
        log.info("==  迁移筛查数据-完成  ==");
    }

}
