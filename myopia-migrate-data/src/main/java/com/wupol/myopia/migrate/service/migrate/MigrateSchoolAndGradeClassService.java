package com.wupol.myopia.migrate.service.migrate;

import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.migrate.constant.GradeCodeEnum;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import com.wupol.myopia.migrate.domain.model.SysSchool;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.service.SysGradeClassService;
import com.wupol.myopia.migrate.service.SysSchoolService;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 迁移学校、年级、班级数据
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@Service
public class MigrateSchoolAndGradeClassService {

    @Autowired
    private SysSchoolService sysSchoolService;
    @Autowired
    private SysGradeClassService sysGradeClassService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private SysStudentEyeService sysStudentEyeService;

    /**
     * 迁移学校、年级、班级数据
     *
     * @return com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO
     **/
    @Transactional(rollbackFor = Exception.class)
    public SchoolAndGradeClassDO migrateSchoolAndGradeClass() {
        Map<String, Integer> schoolMap = new HashMap<>(50);
        Map<String, Integer> gradeMap = new HashMap<>(200);
        Map<String, Integer> classMap = new HashMap<>(3000);
        List<SysSchool> sysSchoolList = sysSchoolService.findByList(new SysSchool());
        sysSchoolList.forEach(sysSchool -> {
            // 没有数据的不迁移
            if (sysStudentEyeService.count(new SysStudentEye().setSchoolId(sysSchool.getSchoolId())) <= 0) {
                return;
            }
            // TODO：把sysSchool转为school
            SaveSchoolRequestDTO schoolDTO = new SaveSchoolRequestDTO();
            schoolDTO.setCreateUserId(1);
            schoolDTO.setGovDeptId(1);
            schoolDTO.initCooperationInfo();
            schoolDTO.setStatus(schoolDTO.getCooperationStopStatus());
            // 迁移学校
            schoolService.saveSchool(schoolDTO);
            Integer schoolId = schoolDTO.getId();
            String sysSchoolId = sysSchool.getSchoolId();
            schoolMap.put(sysSchoolId, schoolId);
            // 迁移年级、班级
            List<SysGradeClass> gradeAndClassList = sysGradeClassService.findByList(new SysGradeClass().setSchoolId(sysSchoolId));
            Map<String, List<SysGradeClass>> gradeClassMap = gradeAndClassList.stream().collect(Collectors.groupingBy(SysGradeClass::getGrade));
            gradeClassMap.forEach((gradeName, classList) -> {
                // 年级
                SchoolGrade schoolGrade = new SchoolGrade().setSchoolId(schoolId).setName(gradeName).setCreateUserId(1).setGradeCode(GradeCodeEnum.getCodeBySort(classList.get(0).getSort()));
                schoolGradeService.save(schoolGrade);
                gradeMap.put(sysSchoolId + gradeName, schoolGrade.getId());
                // 班级
                List<SchoolClass> schoolClassList = classList.stream()
                        .map(x -> new SchoolClass().setSchoolId(schoolId).setCreateUserId(1).setGradeId(schoolGrade.getId()).setName(x.getClazz()).setSeatCount(x.getClazzNum().intValue()))
                        .collect(Collectors.toList());
                schoolClassService.saveBatch(schoolClassList);
                Map<String, Integer> newClassMap = schoolClassList.stream().collect(Collectors.toMap(x -> sysSchoolId + gradeName + x.getName(), SchoolClass::getId));
                classMap.putAll(newClassMap);
            });
        });
        return new SchoolAndGradeClassDO(schoolMap, gradeMap, classMap);
    }

}
