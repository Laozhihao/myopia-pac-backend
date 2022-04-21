package com.wupol.myopia.migrate.service.migrate;

import com.wupol.myopia.migrate.domain.dos.PlanAndStudentDO;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningOrgAndStaffDO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 迁移数据服务
 * TODO: 1.给表sys_student_eye表的dept_id字段加索引  2.上线后需要重置获取筛查机构、学校、筛查人员账号密码，修改筛查机构类型，新增筛查人员  3.创建时间一致性
 * TODO：4.学校补充街道、为其他类型的学校改为正确类型、修改学校属性（默认为公办）、片区（默认中片）、监测点（默认城区）
 * TODO：5.oauth和business建立分布式事务
 * TODO：6.
 *
 * @Author HaoHao
 * @Date 2022/1/6
 **/
@Slf4j
@Service
public class MigrateDataHandler {

    @Autowired
    private MigrateScreeningDataService migrateScreeningDataService;
    @Autowired
    private MigrateScreeningOrganizationService migrateScreeningOrganizationService;
    @Autowired
    private MigrateSchoolAndGradeClassService migrateSchoolAndGradeClassService;
    @Autowired
    private MigratePlanService migratePlanService;
    @Autowired
    private MigrateStudentService migrateStudentService;


    /**
     * 迁移数据
     *
     * @return void
     **/
    @Transactional(rollbackFor = Exception.class)
    public void migrateData() {
        log.info("==  开始-迁移数据.....  ==");
        // 1.迁移学校、年级、班级
        SchoolAndGradeClassDO schoolAndGradeClassDO = migrateSchoolAndGradeClassService.migrateSchoolAndGradeClass();
        // 2.迁移筛查机构、筛查人员
        List<ScreeningOrgAndStaffDO> screeningOrgAndStaffList = migrateScreeningOrganizationService.migrateScreeningOrgAndScreeningStaff();
        // 3.创建计划并绑定筛查学校
        List<PlanAndStudentDO> planAndStudentList = migratePlanService.createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrgAndStaffList);
        // 4.迁移学生（上传到对应筛查计划下）
        List<ScreeningDataDO> screeningDataList = migrateStudentService.migrateStudentByPlan(schoolAndGradeClassDO, planAndStudentList);
        // 5.迁移学生筛查数据
        migrateScreeningDataService.migrateScreeningDataBySchool(screeningDataList);
        log.info("==  完成-迁移数据-完成  ==");
    }

}
