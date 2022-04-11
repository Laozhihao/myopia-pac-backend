package com.wupol.myopia.migrate.service.migrate;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 迁移学校、年级、班级数据
 * 注意：
 *      1.学校没有街道信息、默认公办、中片区、城区监测点
 *      2.没有区/县/镇行政区域信息，会报错
 *      3.不在预期内的学校类型，会归类为其他
 *      4.年级的类型为空或不在预期内，会报错（影响统计分析）
 *
 * @Author HaoHao
 * @Date 2022/3/31
 **/
@Log4j2
@Service
public class MigrateSchoolAndGradeClassService {

    @Autowired
    private SysSchoolService sysSchoolService;
    @Autowired
    private SysGradeClassService sysGradeClassService;
    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private DistrictService districtService;

    /**
     * 迁移学校、年级、班级数据
     *
     * @return com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO
     **/
    @Transactional(rollbackFor = Exception.class)
    public SchoolAndGradeClassDO migrateSchoolAndGradeClass() {
        log.info("====================  ......【1】开始-迁移-学校.....  ====================");
        Map<String, Integer> schoolMap = new HashMap<>(50);
        Map<String, Integer> gradeMap = new HashMap<>(200);
        Map<String, Integer> classMap = new HashMap<>(3000);
        List<SysSchool> sysSchoolList = sysSchoolService.findByList(new SysSchool());
        sysSchoolList.forEach(sysSchool -> {
            // 没有数据的不迁移
            if (sysStudentEyeService.count(new SysStudentEye().setSchoolId(sysSchool.getSchoolId())) <= 0) {
                return;
            }
            // 迁移学校
            SaveSchoolRequestDTO schoolDTO = getSaveSchoolRequestDTO(sysSchool);
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
        log.info("====================  ......【1】完成-迁移-学校.....  ====================");
        return new SchoolAndGradeClassDO(schoolMap, gradeMap, classMap);
    }

    /**
     * 获取保存学校的实体
     *
     * @param sysSchool 学校信息
     * @return com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO
     **/
    private SaveSchoolRequestDTO getSaveSchoolRequestDTO(SysSchool sysSchool) {
        Assert.hasText(sysSchool.getRegion(), sysSchool.getName() + "的区/镇/县为空");
        Long areaDistrictCode = districtService.getCodeByName(sysSchool.getRegion());
        District areaDistrict = districtService.getByCode(areaDistrictCode);
        List<District> districtDetail = districtService.getDistrictPositionDetail(areaDistrictCode);
        SaveSchoolRequestDTO schoolDTO = new SaveSchoolRequestDTO();
        schoolDTO.setCreateUserId(1)
                .setGovDeptId(1)
                .setDistrictAreaCode(areaDistrictCode)
                .setDistrictDetail(JSON.toJSONString(districtDetail))
                .setDistrictId(areaDistrict.getId())
                .setType(getSchoolType(sysSchool.getState()))
                // 默认为：0-公办学校
                .setKind(SchoolEnum.KIND_1.getType())
                // 默认为：2-中片区、1-城区（监测点）
                .setAreaType(2)
                .setMonitorType(1)
                .setSchoolNo(schoolService.getLatestSchoolNo(areaDistrictCode.toString(), 2, 1))
                .setName(sysSchool.getName());
        schoolDTO.initCooperationInfo();
        schoolDTO.setStatus(schoolDTO.getCooperationStopStatus());
        return schoolDTO;
    }

    private Integer getSchoolType(String sysSchoolState) {
        if ("幼儿园".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_KINDERGARTEN.getType();
        } else if ("小学".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        } else if ("初中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_MIDDLE.getType();
        } else if ("高中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_HIGH.getType();
        } else if ("职高".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_VOCATIONAL.getType();
        } else if ("小学,初中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_9.getType();
        } else if ("小学,初中,高中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_12.getType();
        } else if ("初中,高中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_INTEGRATED_MIDDLE.getType();
        }
        return SchoolEnum.TYPE_OTHER.getType();
    }

}
