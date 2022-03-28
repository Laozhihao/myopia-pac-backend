package com.wupol.myopia.migrate.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.aggregation.export.excel.imports.PlanStudentExcelImportService;
import com.wupol.myopia.business.aggregation.screening.service.VisionScreeningBizService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.core.device.domain.model.DeviceReportTemplate;
import com.wupol.myopia.business.core.device.service.DeviceReportTemplateService;
import com.wupol.myopia.business.core.device.service.ScreeningOrgBindDeviceReportService;
import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ComputerOptometryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningPlanDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.VisionDataDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.organization.constant.ScreeningOrgConfigTypeEnum;
import com.wupol.myopia.business.core.screening.organization.domain.model.ScreeningOrganization;
import com.wupol.myopia.business.core.screening.organization.service.ScreeningOrganizationService;
import com.wupol.myopia.migrate.constant.GradeCodeEnum;
import com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO;
import com.wupol.myopia.migrate.domain.model.SysDept;
import com.wupol.myopia.migrate.domain.model.SysGradeClass;
import com.wupol.myopia.migrate.domain.model.SysSchool;
import com.wupol.myopia.migrate.domain.model.SysStudentEye;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2022/1/6
 **/
@Slf4j
@Service
public class MigrateDataService {

    private static final int SYS_STUDENT_ID_KEY = -1;

    @Autowired
    private SysStudentEyeService sysStudentEyeService;
    @Autowired
    private SysDeptService sysDeptService;
    @Autowired
    private SysSchoolService sysSchoolService;
    @Autowired
    private SysGradeClassService sysGradeClassService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private SchoolClassService schoolClassService;
    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private PlanStudentExcelImportService planStudentExcelImportService;
    @Resource
    private OauthServiceClient oauthServiceClient;
    @Resource
    private DeviceReportTemplateService deviceReportTemplateService;
    @Resource
    private ScreeningOrgBindDeviceReportService screeningOrgBindDeviceReportService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VirtualStudentService virtualStudentService;
    @Autowired
    private VisionScreeningBizService visionScreeningBizService;


    @Transactional(rollbackFor = Exception.class)
    public void migrateData() {
        log.info("====================  开始-迁移数据.....  ====================");
        SchoolAndGradeClassDO schoolAndGradeClassDO = migrateSchoolAndGradeClass();
        migrateScreeningOrgAndScreeningStaff(schoolAndGradeClassDO);
        log.info("====================  完成-迁移数据.....  ====================");
    }

    /**
     * 迁移学校、年级、班级数据
     *
     * @return com.wupol.myopia.migrate.domain.dos.SchoolAndGradeClassDO
     **/
    private SchoolAndGradeClassDO migrateSchoolAndGradeClass() {
        Map<String, Integer> schoolMap = new HashMap<>(50);
        Map<String, Integer> gradeMap = new HashMap<>(200);
        Map<String, Integer> classMap = new HashMap<>(3000);
        List<SysSchool> sysSchoolList = sysSchoolService.findByList(new SysSchool());
        sysSchoolList.forEach(sysSchool -> {
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
            List<SysGradeClass> gradeAndClassList = sysGradeClassService.findByList(new SysGradeClass().setSchoolId(sysSchoolId));
            Map<String, List<SysGradeClass>> gradeClassMap = gradeAndClassList.stream().collect(Collectors.groupingBy(SysGradeClass::getGrade));
            gradeClassMap.forEach((gradeName, classList) -> {
                // 迁移年级
                SchoolGrade schoolGrade = new SchoolGrade().setSchoolId(schoolId).setName(gradeName).setCreateUserId(1).setGradeCode(GradeCodeEnum.getCodeBySort(classList.get(0).getSort()));
                schoolGradeService.save(schoolGrade);
                gradeMap.put(sysSchoolId + gradeName, schoolGrade.getId());
                // 迁移班级
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

    private void migrateScreeningOrgAndScreeningStaff(SchoolAndGradeClassDO schoolAndGradeClassDO) {
        List<SysDept> deptList = sysDeptService.findByList(new SysDept());
        deptList.forEach(sysDept -> {
            // 迁移筛查机构
            // TODO：把sysDept转为screeningOrganization、没有数据的不迁移
            ScreeningOrganization screeningOrganization = new ScreeningOrganization().setCreateUserId(1)
                    .setGovDeptId(1)
                    .setConfigType(ScreeningOrgConfigTypeEnum.CONFIG_TYPE_1.getType());
            screeningOrganization.initCooperationInfo();
            screeningOrganization.setStatus(screeningOrganization.getCooperationStopStatus());
            saveScreeningOrganization(screeningOrganization);
            // 迁移筛查人员

            // 迁移筛查数据和学生
            migrateStudentAndScreeningResult(schoolAndGradeClassDO, sysDept.getDeptId(), screeningOrganization, null, null);
        });
    }

    /**
     * 保存筛查机构
     *
     * @param screeningOrganization 筛查机构
     * @return UsernameAndPasswordDTO 账号密码
     */
    private void saveScreeningOrganization(ScreeningOrganization screeningOrganization) {
        String name = screeningOrganization.getName();
        if (StringUtils.isBlank(name)) {
            throw new BusinessException("名字不能为空");
        }
        if (Boolean.TRUE.equals(screeningOrganizationService.checkScreeningOrgName(name, null))) {
            throw new BusinessException("筛查机构名称不能重复");
        }
        screeningOrganizationService.save(screeningOrganization);
        // 同步到oauth机构状态
        oauthServiceClient.addOrganization(new Organization(screeningOrganization.getId(), SystemCode.MANAGEMENT_CLIENT,
                UserType.SCREENING_ORGANIZATION_ADMIN, screeningOrganization.getStatus()));
        // 为筛查机构新增设备报告模板
        DeviceReportTemplate template = deviceReportTemplateService.getSortFirstTemplate();
        screeningOrgBindDeviceReportService.orgBindReportTemplate(template.getId(), screeningOrganization.getId(), screeningOrganization.getName());
        screeningOrganizationService.generateAccountAndPassword(screeningOrganization, ScreeningOrganizationService.PARENT_ACCOUNT, null);
    }

    private void migrateStudentAndScreeningResult(SchoolAndGradeClassDO schoolAndGradeClassDO, String oldOrgId, ScreeningOrganization screeningOrganization, Integer screeningStaffId, String screeningStaffName) {
        // 获取该筛查机构下所有筛查数据
        // TODO: 给表sys_student_eye表的dept_id字段加索引、通过SQL仅获取需要的字段、根据时间升序
        List<SysStudentEye> sysStudentEyeList = sysStudentEyeService.findByList(new SysStudentEye().setDeptId(oldOrgId));
        // 按年分组
        Map<String, List<SysStudentEye>> oneYearStudentEyeMap = sysStudentEyeList.stream().collect(Collectors.groupingBy(x -> DateUtil.format(x.getCreateTime(), DateFormatUtil.FORMAT_ONLY_YEAR)));
        // 同年的按上下半年分为两组
        oneYearStudentEyeMap.forEach((year, studentEyeList) -> {
            Date startDate = DateUtil.parse(year + "-01-01", DateFormatUtil.FORMAT_ONLY_DATE);
            Date endDate = DateUtil.parse(year + "-07-01", DateFormatUtil.FORMAT_ONLY_DATE);
            Map<Boolean, List<SysStudentEye>> halfYearStudentEyeMap = studentEyeList.stream().collect(Collectors.partitioningBy(x -> x.getCreateTime().after(startDate) && x.getCreateTime().before(endDate)));
            createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrganization, startDate, getDateByDateStr(year + "06-30"), year + "年上半年", halfYearStudentEyeMap.get(true), screeningStaffId, screeningStaffName);
            createPlanAndBindSchool(schoolAndGradeClassDO, screeningOrganization, endDate, getDateByDateStr(year + "12-31"), year + "年下半年", halfYearStudentEyeMap.get(false), screeningStaffId, screeningStaffName);
        });

    }

    private Date getDateByDateStr(String dateStr) {
        return DateUtil.parse(dateStr, DateFormatUtil.FORMAT_ONLY_DATE);
    }

    private void createPlanAndBindSchool(SchoolAndGradeClassDO schoolAndGradeClassDO, ScreeningOrganization screeningOrganization, Date startDate, Date endDate,
                                         String titlePrefix, List<SysStudentEye> halfYearStudentEyeMap, Integer screeningStaffId, String screeningStaffName) {
        // 创建筛查计划，同时为计划绑定学校
        ScreeningPlanDTO screeningPlanDTO = new ScreeningPlanDTO();
        screeningPlanDTO.setTitle(titlePrefix + screeningOrganization.getName() + "筛查计划")
                .setScreeningOrgId(screeningOrganization.getId())
                .setCreateUserId(1)
                .setDistrictId(screeningOrganization.getDistrictId())
                .setCreateTime(startDate)
                .setStartTime(startDate)
                .setEndTime(endDate)
                .setReleaseStatus(CommonConst.STATUS_RELEASE)
                .setReleaseTime(startDate)
                .setOperatorId(1)
                .setOperateTime(startDate);
        // TODO: 设置学校名称
        Map<String, List<SysStudentEye>> halfYearStudentEyeGroupBySchoolIdMap = halfYearStudentEyeMap.stream().collect(Collectors.groupingBy(SysStudentEye::getSchoolId));
        List<ScreeningPlanSchool> planSchoolList = halfYearStudentEyeGroupBySchoolIdMap.keySet().stream()
                .map(oldSchoolId -> schoolAndGradeClassDO.getSchoolMap().get(oldSchoolId))
                .distinct()
                .map(newSchoolId -> new ScreeningPlanSchool().setSchoolId(newSchoolId).setScreeningOrgId(screeningOrganization.getId())
                        .setQualityControllerCommander(screeningStaffName).setQualityControllerName(screeningStaffName))
                .collect(Collectors.toList());
        screeningPlanDTO.setSchools(planSchoolList);
        screeningPlanService.saveOrUpdateWithSchools(1, screeningPlanDTO, false);
        // 迁移学生
        migrateStudent(schoolAndGradeClassDO, screeningOrganization, screeningStaffId, screeningPlanDTO, halfYearStudentEyeGroupBySchoolIdMap);
    }

    private void migrateStudent(SchoolAndGradeClassDO schoolAndGradeClassDO, ScreeningOrganization screeningOrganization, Integer screeningStaffId,
                                ScreeningPlanDTO screeningPlan, Map<String, List<SysStudentEye>> halfYearStudentEyeGroupBySchoolIdMap) {
        // 逐个学校迁移
        halfYearStudentEyeGroupBySchoolIdMap.forEach((oldSchoolId, oneSchoolHalfYearStudentEyeList) -> {
            Integer newSchoolId = schoolAndGradeClassDO.getSchoolMap().get(oldSchoolId);
            // TODO: 验证取最新一条的代码是否可行
            // 获取每个学生半年内最新一条筛查记录
            Comparator<? super SysStudentEye> comparator = Comparator.comparing(SysStudentEye::getCreateTime);
            Map<String, SysStudentEye> latestSysStudentEyeMap = oneSchoolHalfYearStudentEyeList.stream().collect(Collectors.toMap(SysStudentEye::getStudentId, Function.identity(), BinaryOperator.maxBy(comparator)));
            List<String> eyeIdList = latestSysStudentEyeMap.values().stream().map(SysStudentEye::getEyeId).collect(Collectors.toList());
            List<SysStudentEye> sysStudentEyes = sysStudentEyeService.listByIds(eyeIdList);
            // 转为Map，对于没有IdCard的走虚拟学生
            Map<Boolean, List<SysStudentEye>> isHasIdCardSysStudentEyeMap = sysStudentEyes.stream()
                    .collect(Collectors.partitioningBy(x -> StringUtils.isNotBlank(x.getStudentIdcard()) && IdcardUtil.isValidCard(x.getStudentIdcard())));
            List<Map<Integer, String>> noIdCardStudentInfoList = getNoIdCardStudentInfoList(isHasIdCardSysStudentEyeMap.get(false), newSchoolId, schoolAndGradeClassDO.getGradeMap(), schoolAndGradeClassDO.getClassMap(), screeningPlan);
            List<Map<Integer, String>> hasIdCardStudentInfoList = isHasIdCardSysStudentEyeMap.get(true).stream().map(SysStudentEye::convertToMap).collect(Collectors.toList());
            hasIdCardStudentInfoList.addAll(noIdCardStudentInfoList);
            // 批量插入（模拟通过Excel上传）
            planStudentExcelImportService.insertByUpload(1, hasIdCardStudentInfoList, screeningPlan, newSchoolId);
            screeningPlanService.updateStudentNumbers(1, screeningPlan.getId(), screeningPlanSchoolStudentService.getCountByScreeningPlanId(screeningPlan.getId()));
            // 迁移筛查数据
            Map<String, String> sysStudentIdAndScreeningCodeMap = noIdCardStudentInfoList.stream().collect(Collectors.toMap(x -> x.get(SYS_STUDENT_ID_KEY), x -> x.get(ImportExcelEnum.SCREENING_CODE.getIndex())));
            migrateScreeningResult(sysStudentEyes, newSchoolId, screeningOrganization.getId(), screeningStaffId, screeningPlan.getId(), sysStudentIdAndScreeningCodeMap);
        });
    }

    private List<Map<Integer, String>> getNoIdCardStudentInfoList(List<SysStudentEye> noIdCardStudentEyeList, Integer schoolId,
                                                                  Map<String, Integer> gradeMap, Map<String, Integer> classMap, ScreeningPlan screeningPlan) {
        return noIdCardStudentEyeList.stream().map(sysStudentEye -> {
            Map<Integer, String> noIdCardStudent = sysStudentEye.convertToMap();
            Integer gradeId = gradeMap.get(sysStudentEye.getSchoolId() + sysStudentEye.getSchoolGrade());
            Integer classId = classMap.get(sysStudentEye.getSchoolId() + sysStudentEye.getSchoolGrade() + sysStudentEye.getSchoolClazz());
            ScreeningPlanSchoolStudent screeningPlanSchoolStudent = virtualStudentService.createVirtualStudent(screeningPlan, schoolId, gradeId, sysStudentEye.getSchoolGrade(), classId, sysStudentEye.getSchoolClazz());
            noIdCardStudent.put(ImportExcelEnum.SCREENING_CODE.getIndex(), String.valueOf(screeningPlanSchoolStudent.getScreeningCode()));
            noIdCardStudent.put(SYS_STUDENT_ID_KEY, sysStudentEye.getStudentId());
            return noIdCardStudent;
        }).collect(Collectors.toList());
    }

    // TODO：独立一个类
    private void migrateScreeningResult(List<SysStudentEye> sysStudentEyeList, Integer schoolId, Integer screeningOrgId, Integer userId, Integer planId,
                                        Map<String, String> sysStudentIdAndScreeningCodeMap) {
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.findByList(new ScreeningPlanSchoolStudent().setScreeningPlanId(planId).setSchoolId(schoolId));
        Map<String, Integer> certificateAndPlanStudentIdMap = planStudentList.stream().collect(Collectors.toMap(x -> StringUtils.isNotBlank(x.getIdCard()) ? x.getIdCard() : String.valueOf(x.getScreeningCode()), ScreeningPlanSchoolStudent::getId));
        // 遍历逐个学生迁移筛查数据
        sysStudentEyeList.forEach(sysStudentEye -> {
            String planStudentId = String.valueOf(getPlanStudentId(sysStudentEye, sysStudentIdAndScreeningCodeMap, certificateAndPlanStudentIdMap));
            VisionDataDTO visionDataDTO = new VisionDataDTO();
            visionDataDTO.setSchoolId(String.valueOf(schoolId))
                    .setDeptId(screeningOrgId)
                    .setCreateUserId(userId)
                    .setPlanStudentId(planStudentId)
                    .setIsState(0);
            visionDataDTO.setLeftNakedVision(new BigDecimal(sysStudentEye.getLLsl()))
                    .setLeftCorrectedVision(new BigDecimal(sysStudentEye.getLJzsl()))
                    .setRightNakedVision(new BigDecimal(sysStudentEye.getRLsl()))
                    .setRightCorrectedVision(new BigDecimal(sysStudentEye.getRJzsl()))
                    .setGlassesType(StringUtils.isNotBlank(sysStudentEye.getGlasses()) ? sysStudentEye.getGlasses() : WearingGlassesSituation.NOT_WEARING_GLASSES_TYPE)
                    .setIsCooperative(0);
            visionScreeningBizService.saveOrUpdateStudentScreenData(visionDataDTO);
            ComputerOptometryDTO computerOptometryDTO = new ComputerOptometryDTO();
            computerOptometryDTO.setSchoolId(String.valueOf(schoolId))
                    .setDeptId(screeningOrgId)
                    .setCreateUserId(userId)
                    .setPlanStudentId(planStudentId)
                    .setIsState(0);
            computerOptometryDTO.setLSph(new BigDecimal(sysStudentEye.getLSph()))
                    .setLCyl(new BigDecimal(sysStudentEye.getLCyl()))
                    .setLAxial(new BigDecimal(sysStudentEye.getLAxial()))
                    .setRSph(new BigDecimal(sysStudentEye.getRSph()))
                    .setRCyl(new BigDecimal(sysStudentEye.getRCyl()))
                    .setRAxial(new BigDecimal(sysStudentEye.getRAxial()));
            visionScreeningBizService.saveOrUpdateStudentScreenData(computerOptometryDTO);
            // TODO: 生物测量 、其他眼病
        });
    }

    private Integer getPlanStudentId(SysStudentEye sysStudentEye, Map<String, String> sysStudentIdAndScreeningCodeMap, Map<String, Integer> certificateAndPlanStudentIdMap) {
        if (StringUtils.isNotBlank(sysStudentEye.getStudentIdcard()) && IdcardUtil.isValidCard(sysStudentEye.getStudentIdcard())) {
            return certificateAndPlanStudentIdMap.get(sysStudentEye.getStudentIdcard());
        }
        return certificateAndPlanStudentIdMap.get(sysStudentIdAndScreeningCodeMap.get(sysStudentEye.getStudentId()));
    }
}
