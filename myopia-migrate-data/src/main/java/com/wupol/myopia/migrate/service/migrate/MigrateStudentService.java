package com.wupol.myopia.migrate.service.migrate;

import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.migrate.domain.dos.PlanAndStudentDO;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.dos.ScreeningDataDO;
import com.wupol.myopia.migrate.domain.model.SysStudentEyeSimple;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 迁移学生数据
 * 注意：
 *      1.没有身份证号码的学生走虚拟
 *
 * @Author HaoHao
 * @Date 2022/4/10
 **/
@Log4j2
@Service
public class MigrateStudentService {

    @Autowired
    private StudentDataService studentDataService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 逐个计划迁移学生数据（上传到对应筛查计划下）
     *
     * @param schoolAndGradeClassDO     学校、年级和班级信息
     * @param planAndStudentList        计划和学生信息list
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningDataDO>
     **/
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public List<ScreeningDataDO> migrateStudentByPlan(SchoolAndGradeClassDO schoolAndGradeClassDO, List<PlanAndStudentDO> planAndStudentList) {
        log.info("==  迁移学生-开始.....  ==");
        List<ScreeningDataDO> screeningDataList = new ArrayList<>();
        // 逐个计划迁移
        planAndStudentList.forEach(planAndStudentDO -> {
            log.info("迁移学生-计划：{}", planAndStudentDO.getScreeningPlan().getTitle());
            // 逐个学校迁移
            screeningDataList.addAll(migrateStudentBySchool(schoolAndGradeClassDO, planAndStudentDO));
        });
        log.info("==  迁移学生-完成  ==");
        return screeningDataList;
    }

    /**
     * 逐个学校迁移学生数据
     *
     * @param schoolAndGradeClassDO 学校、年级和班级信息
     * @param planAndStudentDO      计划和学生信息
     * @return java.util.List<com.wupol.myopia.migrate.domain.dos.ScreeningDataDO>
     **/
    private List<ScreeningDataDO> migrateStudentBySchool(SchoolAndGradeClassDO schoolAndGradeClassDO, PlanAndStudentDO planAndStudentDO) {
        List<ScreeningDataDO> screeningDataList = new ArrayList<>();
        ScreeningPlan screeningPlan = planAndStudentDO.getScreeningPlan();
        Map<String, List<SysStudentEyeSimple>> pendingMigrateStudentEyeMap = planAndStudentDO.getCurrentPlanStudentGroupBySchoolIdMap();
        Integer screeningStaffUserId = planAndStudentDO.getScreeningStaffUserId();
        // 逐个学校迁移
        pendingMigrateStudentEyeMap.forEach((oldSchoolId, oneSchoolHalfYearStudentEyeList) -> {
            log.info("迁移学生-学校：{}", oneSchoolHalfYearStudentEyeList.get(0).getSchoolName());
            Integer newSchoolId = schoolAndGradeClassDO.getSchoolMap().get(oldSchoolId);
            // 计划中不存在该学校则不迁移数据
            if (Objects.isNull(newSchoolId)) {
                return;
            }
            // 筛查数据已迁移完成的，不再处理，节省时间
            if (visionScreeningResultService.count(new VisionScreeningResult().setPlanId(screeningPlan.getId()).setSchoolId(newSchoolId)) > 0) {
                log.warn("{}的所有学生的筛查数据，都已经迁移完成，需要再处理，id={}", oneSchoolHalfYearStudentEyeList.get(0).getSchoolName(), newSchoolId);
                return;
            }
            studentDataService.migrateStudent(screeningDataList, oneSchoolHalfYearStudentEyeList, screeningPlan, schoolAndGradeClassDO, newSchoolId, screeningStaffUserId);
        });
        return screeningDataList;
    }

}
