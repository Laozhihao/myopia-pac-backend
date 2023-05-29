package com.wupol.myopia.migrate.service.migrate;

import com.alibaba.fastjson.JSON;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.constant.CooperationTypeEnum;
import com.wupol.myopia.base.constant.StatusConstant;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import com.wupol.myopia.migrate.domain.model.SysSchool;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.migrate.service.SysSchoolService;
import com.wupol.myopia.migrate.service.SysStudentEyeService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.validation.constraints.NotBlank;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 迁移学校、年级、班级数据
 * 注意：
 *      1.学校没有街道信息、默认公办、中片区、城区监测点
 *      2.没有区/县/镇行政区域信息，会报错
 *      3.不在预期内的学校类型，会归类为其他，TODO：需要到管理平台编辑
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
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public SchoolAndGradeClassDO migrateSchoolAndGradeClass() {
        log.info("==  学校-开始.....  ==");
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
            School school = saveSchool(sysSchool);
            Integer schoolId = school.getId();
            Integer schoolType = school.getType();
            String sysSchoolId = sysSchool.getSchoolId();
            schoolMap.put(sysSchoolId, schoolId);
            // 迁移年级、班级（根据年级编码排序）
            List<SysGradeClass> gradeAndClassList = sysStudentEyeService.getAllGradeAndClassBySchoolId(sysSchoolId);
            gradeAndClassList.forEach(x -> x.setGradeCode(getGradeCode(x.getGrade(), schoolType)));
            Map<String, Map<String, List<SysGradeClass>>> gradeClassMap = gradeAndClassList.stream().collect(Collectors.groupingBy(SysGradeClass::getGradeCode, Collectors.groupingBy(SysGradeClass::getGrade)));
            gradeClassMap.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEachOrdered(y -> {
                String gradeCode = y.getKey();
                y.getValue().forEach((gradeName, classList) -> {
                    // 年级
                    Integer gradeId = saveGrade(schoolId, gradeName, gradeCode);
                    gradeMap.put(sysSchoolId + gradeName, gradeId);
                    // 班级（存在同名的则不新增）
                    Map<@NotBlank(message = "班级名称不能为空") String, Integer> existClassMap = schoolClassService.getByGradeId(gradeId).stream().collect(Collectors.toMap(SchoolClass::getName, SchoolClass::getId));
                    List<SchoolClass> schoolClassList = classList.stream()
                            .filter(x -> Objects.isNull(existClassMap.get(x.getClazz())))
                            .map(x -> new SchoolClass().setSchoolId(schoolId).setCreateUserId(1).setGradeId(gradeId).setName(x.getClazz()))
                            .collect(Collectors.toList());
                    schoolClassService.saveOrUpdateBatch(schoolClassList);
                    Map<String, Integer> newClassMap = schoolClassList.stream().collect(Collectors.toMap(x -> sysSchoolId + gradeName + x.getName(), SchoolClass::getId));
                    classMap.putAll(newClassMap);
                });
            });
        });
        log.info("==  学校-完成  ==");
        return new SchoolAndGradeClassDO(schoolMap, gradeMap, classMap);
    }

    /**
     * 保存学校
     *
     * @param sysSchool 学校信息
     * @return java.lang.Integer
     **/
    private School saveSchool(SysSchool sysSchool) {
        // 存在同名的学校，则不新增
        School school = schoolService.findOne(new School().setName(sysSchool.getName()));
        if (Objects.nonNull(school)) {
            return school;
        }
        SaveSchoolRequestDTO schoolDTO = getSaveSchoolRequestDTO(sysSchool);
        schoolService.saveSchool(schoolDTO);
        return schoolDTO;
    }

    /**
     * 保存年级
     *
     * @param schoolId 学校ID
     * @param gradeName 年级名称
     * @param gradeCode 年级编码
     * @return java.lang.Integer
     **/
    private Integer saveGrade(Integer schoolId, String gradeName, String gradeCode) {
        // 存在同名年级则不新增
        SchoolGrade existGrade = schoolGradeService.findOne(new SchoolGrade().setSchoolId(schoolId).setName(gradeName));
        if (Objects.nonNull(existGrade)) {
            return existGrade.getId();
        }
        SchoolGrade schoolGrade = new SchoolGrade()
                .setSchoolId(schoolId)
                .setName(gradeName)
                .setCreateUserId(1)
                .setGradeCode(gradeCode);
        schoolGradeService.save(schoolGrade);
        return schoolGrade.getId();
    }

    private static String getGradeCode(String gradeName, Integer schoolType) {
        String gradeCode = GradeCodeEnum.getByName(gradeName.trim()).getCode();
        // 非规范名称，降级处理
        if (GradeCodeEnum.UNKNOWN.getCode().equals(gradeCode)) {
            if ("七年级".equals(gradeName)) {
                return GradeCodeEnum.ONE_JUNIOR_SCHOOL.getCode();
            } else if ("八年级".equals(gradeName)) {
                return GradeCodeEnum.TWO_JUNIOR_SCHOOL.getCode();
            } else if ("九年级".equals(gradeName)) {
                return GradeCodeEnum.THREE_JUNIOR_SCHOOL.getCode();
            } else if (gradeName.contains("高一")) {
                return GradeCodeEnum.ONE_HIGH_SCHOOL.getCode();
            } else if (gradeName.contains("高二")) {
                return GradeCodeEnum.TWO_HIGH_SCHOOL.getCode();
            } else if (gradeName.contains("高三")) {
                return GradeCodeEnum.THREE_HIGH_SCHOOL.getCode();
            } else if ("大".equals(gradeName) && SchoolEnum.TYPE_KINDERGARTEN.getType().equals(schoolType)) {
                return GradeCodeEnum.THREE_KINDERGARTEN.getCode();
            } else if ("中".equals(gradeName) && SchoolEnum.TYPE_KINDERGARTEN.getType().equals(schoolType)) {
                return GradeCodeEnum.TWO_KINDERGARTEN.getCode();
            } else if ("小".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.ONE_KINDERGARTEN.getCode();
            } else if ("一".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.ONE_PRIMARY_SCHOOL.getCode();
            } else if ("二".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.TWO_PRIMARY_SCHOOL.getCode();
            } else if ("三".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.THREE_PRIMARY_SCHOOL.getCode();
            } else if ("四".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.FOUR_PRIMARY_SCHOOL.getCode();
            } else if ("五".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.FIVE_PRIMARY_SCHOOL.getCode();
            } else if ("六".equals(gradeName) && SchoolEnum.TYPE_PRIMARY.getType().equals(schoolType)) {
                return GradeCodeEnum.SIX_PRIMARY_SCHOOL.getCode();
            } else {
                throw new BusinessException("无效年级名称：" + gradeName);
            }
        }
        return gradeCode;
    }

    /**
     * 获取保存学校的实体
     *
     * @param sysSchool 学校信息
     * @return com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO
     **/
    private SaveSchoolRequestDTO getSaveSchoolRequestDTO(SysSchool sysSchool) {
        Assert.hasText(sysSchool.getRegion(), sysSchool.getName() + "的区/镇/县为空");
        Long areaDistrictCode = districtService.getCodeByName(getRegion(sysSchool.getRegion()), null);
        Assert.notNull(areaDistrictCode, "无效行政区域地址");
        District areaDistrict = districtService.getByCode(areaDistrictCode);
        List<District> districtDetail = districtService.getDistrictPositionDetail(areaDistrictCode);
        SaveSchoolRequestDTO schoolDTO = new SaveSchoolRequestDTO();
        schoolDTO.setCreateUserId(1)
                .setGovDeptId(1)
                .setDistrictAreaCode(areaDistrictCode)
                .setDistrictDetail(JSON.toJSONString(districtDetail))
                .setDistrictId(areaDistrict.getId())
                .setType(getSchoolType(sysSchool.getState(), sysSchool.getName().trim()))
                // 默认为：0-公办学校
                .setKind(SchoolEnum.KIND_1.getType())
                // 默认为：2-中片区、1-城区（监测点）
                .setAreaType(2)
                .setMonitorType(1)
                .setSchoolNo(schoolService.getLatestSchoolNo(areaDistrictCode.toString(), 2, 1))
                .setName(sysSchool.getName())
                //新增筛查类型
                .setScreeningTypeConfig(String.valueOf(sysSchool.getScreeningType()))
                //省级code
                .setDistrictProvinceCode(sysSchool.getProvinceCode());
        Date date = new Date();
        schoolDTO.setCooperationType(CooperationTypeEnum.COOPERATION_TYPE_TRY_OUT.getType())
                .setCooperationTimeType(CooperationTimeTypeEnum.COOPERATION_TIME_TYPE_30_DAY.getType())
                .setCooperationStartTime(DateUtils.addDays(date, -30))
                .setCooperationEndTime(date)
                .setStatus(StatusConstant.DISABLE);
        return schoolDTO;
    }

    private static String getRegion(String region) {
        Map<String, String> nameMap = new HashMap<>(2);
        nameMap.put("康定县", "康定市");
        return Optional.ofNullable(nameMap.get(region)).orElse(region);
    }

    private static Integer getSchoolType(String sysSchoolState, String schoolName) {
        if (!StringUtils.hasText(sysSchoolState)) {
            return getSchoolTypeBySchoolName(schoolName);
        }
        sysSchoolState = sysSchoolState.trim();
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
        } else if ("幼儿园,小学".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        } else if ("小学,初中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_9.getType();
        } else if ("小学,初中,高中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_12.getType();
        } else if ("初中,高中".equals(sysSchoolState)) {
            return SchoolEnum.TYPE_INTEGRATED_MIDDLE.getType();
        }
        return getSchoolTypeBySchoolName(schoolName);
    }

    private static Integer getSchoolTypeBySchoolName(String schoolName) {
        if (schoolName.contains("小学")) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        } else if (schoolName.contains("幼儿园")) {
            return SchoolEnum.TYPE_KINDERGARTEN.getType();
        } else if (schoolName.contains("小")) {
            return SchoolEnum.TYPE_PRIMARY.getType();
        }
        return SchoolEnum.TYPE_OTHER.getType();
    }

}
