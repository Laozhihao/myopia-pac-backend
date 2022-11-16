package com.wupol.myopia.business.aggregation.export.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.CurrentUserUtil;
import com.wupol.myopia.business.aggregation.export.ExportStrategy;
import com.wupol.myopia.business.aggregation.export.pdf.constant.ArchiveExportTypeEnum;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.ExportTypeConst;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.SchoolAge;
import com.wupol.myopia.business.common.utils.util.MaskUtil;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.StudentCommonDiseaseId;
import com.wupol.myopia.business.core.school.facade.SchoolBizFacade;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.StudentCommonDiseaseIdService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseasesHistoryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaData;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ArchiveExportCondition;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.core.screening.flow.domain.dto.SaprodontiaStat;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlan;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CardInfoVO;
import com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard;
import com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCommonDiseaseIdInfo;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.validation.Valid;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 档案卡业务类
 *
 * @Author HaoHao
 * @Date 2022/5/6
 **/
@Slf4j
@Service
public class ArchiveService {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StudentCommonDiseaseIdService studentCommonDiseaseIdService;
    @Autowired
    private SchoolGradeService schoolGradeService;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private SchoolBizFacade schoolBizFacade;
    @Autowired
    private ExportStrategy exportStrategy;

    /**
     * 导出档案卡/监测表
     *
     * @param archiveExportCondition 导出条件
     * @return 链接地址
     **/
    public String exportArchive(@Valid ArchiveExportCondition archiveExportCondition) throws IOException {
        log.info("导出档案卡/监测表：{}", JSON.toJSONString(archiveExportCondition));
        // 构建导出条件
        Integer type = archiveExportCondition.getType();
        ExportCondition exportCondition = new ExportCondition()
                .setNotificationId(archiveExportCondition.getNoticeId())
                .setDistrictId(archiveExportCondition.getDistrictId())
                .setPlanId(archiveExportCondition.getPlanId())
                .setApplyExportFileUserId(CurrentUserUtil.getCurrentUser().getId())
                .setSchoolId(archiveExportCondition.getSchoolId())
                .setGradeId(archiveExportCondition.getGradeId())
                .setClassId(archiveExportCondition.getClassId())
                .setPlanStudentIds(archiveExportCondition.getPlanStudentIdsStr())
                .setType(type);

        ArchiveExportTypeEnum archiveExportType = ArchiveExportTypeEnum.getByType(type);
        // 同步导出
        if (Boolean.FALSE.equals(archiveExportType.getAsyncExport())) {
            return exportStrategy.syncExport(exportCondition, archiveExportType.getServiceClassName());
        }
        // 异步导出
        exportStrategy.doAsyncExport(exportCondition, archiveExportType.getServiceClassName());
        return null;
    }


    /**
     * 获取档案卡/监测表数据 TODO：整合获取其他类型档案卡数据接口
     *
     * @param archiveRequestParam 请求参数
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard>
     **/
    public List<CommonDiseaseArchiveCard> getArchiveData(ArchiveRequestParam archiveRequestParam) {
        ScreeningPlan screeningPlan = screeningPlanService.getById(archiveRequestParam.getPlanId());
        Assert.notNull(screeningPlan, "无法找到该筛查计划");

        // 获取当前班级所有计划学生ID
        Set<Integer> planStudentIds = getPlanStudentIds(archiveRequestParam.getPlanId(), archiveRequestParam.getClassId(), archiveRequestParam.getPlanStudentIds());
        if (CollectionUtils.isEmpty(planStudentIds)) {
            return Collections.emptyList();
        }
        // 获取所有学生的筛查结果（初筛）
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(planStudentIds);
        // 生成档案卡数据
        SchoolClassDTO classWithSchoolAndGradeName = schoolGradeService.getClassWithSchoolAndGradeName(archiveRequestParam.getClassId());
        return generateArchiveCardBatch(visionScreeningResultList, screeningPlan, classWithSchoolAndGradeName);
    }


    /**
     * 获取监测表数据
     * @param exportCondition 导出条件
     */
    public List<CommonDiseaseArchiveCard> getArchiveData(ExportCondition exportCondition){
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getReleasePlanByNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(), exportCondition.getTaskId(), exportCondition.getPlanId());

        if (CollUtil.isEmpty(screeningPlanList)){
            throw new BusinessException("无法找到该筛查计划");
        }

        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        Map<Integer, ScreeningPlan> screeningPlanMap = screeningPlanList.stream().collect(Collectors.toMap(ScreeningPlan::getId, Function.identity(), (v1, v2) -> v2));

        List<ScreeningPlanSchoolStudent> planSchoolStudentList = screeningPlanSchoolStudentService.getByPlanIdOrSchoolId(Lists.newArrayList(planIds),exportCondition.getSchoolId());

        List<Integer> districtIds = districtService.filterDistrict(exportCondition.getDistrictId());
        if (CollUtil.isNotEmpty(districtIds)){
            planSchoolStudentList = planSchoolStudentList.stream()
                    .filter(planSchoolStudent -> districtIds.contains(planSchoolStudent.getSchoolDistrictId()))
                    .collect(Collectors.toList());
        }

        Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = planSchoolStudentList.stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));

        //班级信息
        Set<Integer> classIds = planSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toSet());
        List<SchoolClassDTO> schoolClassDTOList = schoolBizFacade.getClassWithSchoolAndGradeName(Lists.newArrayList(classIds));
        Map<Integer, SchoolClassDTO> schoolClassDtoMap = schoolClassDTOList.stream().collect(Collectors.toMap(SchoolClass::getId, Function.identity(), (v1, v2) -> v2));

        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdAndIsDoubleScreenBatch(Lists.newArrayList(planIds),Boolean.FALSE,exportCondition.getSchoolId());

       return generateArchiveCardBatch(visionScreeningResultList, planSchoolStudentMap, schoolClassDtoMap, screeningPlanMap);
    }

    /**
     * 获取当前班级所有计划学生ID
     *
     * @param planId 筛查学生ID
     * @param classId 班级ID
     * @param planStudentIds 筛查学生ID集
     * @return java.util.Set<java.lang.Integer>
     **/
    private Set<Integer> getPlanStudentIds(Integer planId, Integer classId, Set<Integer> planStudentIds) {
        if (!CollectionUtils.isEmpty(planStudentIds)) {
            return planStudentIds;
        }
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(planId,null,null, classId);
        return planStudentList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
    }

    /**
     * 批量生成学生档案卡
     *
     * @param visionScreeningResultList     筛查结果列表
     * @param screeningPlan                 筛查计划
     * @param classWithSchoolAndGradeName   班级信息（带学校、年级名称）
     * @return 学生档案卡实体类list
     */
    private List<CommonDiseaseArchiveCard> generateArchiveCardBatch(List<VisionScreeningResult> visionScreeningResultList, ScreeningPlan screeningPlan, SchoolClassDTO classWithSchoolAndGradeName) {
        if (CollectionUtils.isEmpty(visionScreeningResultList)) {
            return Collections.emptyList();
        }
        // 查询学生信息
        List<Integer> planStudentIds = visionScreeningResultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentService.getByIds(planStudentIds).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        return visionScreeningResultList.stream()
                .map(visionScreeningResult -> generateArchiveCard(visionScreeningResult, getStudentDTO(planSchoolStudentMap.get(visionScreeningResult.getScreeningPlanSchoolStudentId()), classWithSchoolAndGradeName), screeningPlan))
                .collect(Collectors.toList());
    }

    private List<CommonDiseaseArchiveCard> generateArchiveCardBatch(List<VisionScreeningResult> visionScreeningResultList,
                                                                    Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap,
                                                                    Map<Integer, SchoolClassDTO> schoolClassDtoMap,
                                                                    Map<Integer, ScreeningPlan> screeningPlanMap) {
        if (CollectionUtils.isEmpty(visionScreeningResultList)) {
            return Collections.emptyList();
        }

        return visionScreeningResultList.stream()
                .map(visionScreeningResult -> {
                    ScreeningPlanSchoolStudent screeningPlanSchoolStudent = planSchoolStudentMap.get(visionScreeningResult.getScreeningPlanSchoolStudentId());
                    SchoolClassDTO schoolClassDTO = schoolClassDtoMap.get(screeningPlanSchoolStudent.getClassId());
                    ScreeningPlan screeningPlan = screeningPlanMap.get(visionScreeningResult.getPlanId());
                    return generateArchiveCard(visionScreeningResult, getStudentDTO(screeningPlanSchoolStudent, schoolClassDTO), screeningPlan);
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取学生信息
     *
     * @param planStudent   筛查计划学生
     * @param schoolClassDTO   班级信息（带学校、年级名称）
     * @return com.wupol.myopia.business.core.school.domain.dto.StudentDTO
     **/
    private StudentDTO getStudentDTO(ScreeningPlanSchoolStudent planStudent, SchoolClassDTO schoolClassDTO) {
        StudentDTO studentDTO = new StudentDTO()
                .setSchoolName(schoolClassDTO.getSchoolName())
                .setSchoolId(schoolClassDTO.getSchoolId())
                .setGradeName(schoolClassDTO.getGradeName())
                .setClassName(schoolClassDTO.getName())
                .setSchoolDistrictName(schoolClassDTO.getSchoolDistrictDetail())
                .setSchoolDistrictId(schoolClassDTO.getSchoolDistrictId())
                .setSchoolAreaType(schoolClassDTO.getSchoolAreaType())
                .setSchoolMonitorType(schoolClassDTO.getSchoolMonitorType());

        studentDTO.setName(planStudent.getStudentName())
                .setBirthday(planStudent.getBirthday())
                .setIdCard(planStudent.getIdCard())
                .setGender(planStudent.getGender())
                .setSno(planStudent.getStudentNo())
                .setParentPhone(planStudent.getParentPhone())
                .setNation(planStudent.getNation())
                .setPassport(planStudent.getPassport())
                .setGradeType(planStudent.getGradeType())
                .setGradeId(schoolClassDTO.getGradeId())
                .setClassId(schoolClassDTO.getId())
                .setId(planStudent.getStudentId());
        return studentDTO;
    }

    /**
     * 生成档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param studentDTO            学生信息
     * @param screeningPlan         筛查计划
     * @return 学生档案卡实体类
     */
    private CommonDiseaseArchiveCard generateArchiveCard(VisionScreeningResult visionScreeningResult, StudentDTO studentDTO, ScreeningPlan screeningPlan) {
        CardInfoVO studentInfo = getCardInfo(studentDTO);
        // 民族特殊处理，不在常见民族列表的设为其他（前端展示需要）
        NationEnum nationEnum = NationEnum.COMMON_NATION.stream().filter(nation -> Objects.equals(nation.getCode(),studentInfo.getNation())).findFirst().orElse(NationEnum.OTHER);
        studentInfo.setScreeningDate(visionScreeningResult.getCreateTime())
                .setNation(Optional.ofNullable(studentDTO.getNation()).map(x -> nationEnum.getCode()).orElse(null))
                .setNationDesc(Optional.ofNullable(studentDTO.getNation()).map(x -> nationEnum.getName()).orElse(null));
        return new CommonDiseaseArchiveCard()
                .setStudentInfo(studentInfo)
                .setBloodPressureData(visionScreeningResult.getBloodPressureData())
                .setComputerOptometryData(visionScreeningResult.getComputerOptometry())
                .setVisionData(visionScreeningResult.getVisionData())
                .setDiseasesHistoryData(Optional.ofNullable(visionScreeningResult.getDiseasesHistoryData()).map(DiseasesHistoryDO::getDiseases).orElse(Collections.emptyList()))
                .setSaprodontiaData(getSaprodontiaData(visionScreeningResult.getSaprodontiaData()))
                .setSpineData(visionScreeningResult.getSpineData())
                .setHeightAndWeightData(visionScreeningResult.getHeightAndWeightData())
                .setPrivacyData(visionScreeningResult.getPrivacyData())
                .setCommonDiseaseIdInfo(getStudentCommonDiseaseIdInfo(studentDTO, screeningPlan))
                .setOtherEyeDiseases(visionScreeningResult.getOtherEyeDiseases());
    }

    /**
     * 设置学生基本信息
     *
     * @param studentInfo 学生
     * @return 学生档案卡基本信息
     */
    public CardInfoVO getCardInfo(StudentDTO studentInfo) {
        CardInfoVO cardInfoVO = new CardInfoVO();
        cardInfoVO.setName(studentInfo.getName());
        cardInfoVO.setBirthday(studentInfo.getBirthday());
        cardInfoVO.setIdCard(StringUtils.isNotBlank(studentInfo.getIdCard()) ? MaskUtil.maskIdCard(studentInfo.getIdCard()) : MaskUtil.maskPassport(studentInfo.getPassport()));
        cardInfoVO.setGender(studentInfo.getGender());
        cardInfoVO.setAge(DateUtil.ageOfNow(studentInfo.getBirthday()));
        cardInfoVO.setSno(studentInfo.getSno());
        cardInfoVO.setParentPhone(studentInfo.getParentPhone());
        cardInfoVO.setSchoolName(studentInfo.getSchoolName());
        cardInfoVO.setSchoolId(studentInfo.getSchoolId());
        cardInfoVO.setClassName(studentInfo.getClassName());
        cardInfoVO.setGradeName(studentInfo.getGradeName());
        cardInfoVO.setDistrictName(districtService.getDistrictName(studentInfo.getSchoolDistrictName()));
        cardInfoVO.setNation(studentInfo.getNation());
        cardInfoVO.setNationDesc(NationEnum.getNameByCode(studentInfo.getNation()));
        cardInfoVO.setPassport(studentInfo.getPassport());
        cardInfoVO.setSchoolType(SchoolAge.get(studentInfo.getGradeType()).type);
        return cardInfoVO;
    }

    /**
     * 龋齿
     *
     * @param saprodontiaDataDO 龋齿筛查数据
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaData
     **/
    private SaprodontiaData getSaprodontiaData(SaprodontiaDataDO saprodontiaDataDO) {
        if (Objects.isNull(saprodontiaDataDO)) {
            return null;
        }
        SaprodontiaData saprodontiaData = new SaprodontiaData();
        BeanUtils.copyProperties(saprodontiaDataDO, saprodontiaData);
        saprodontiaData.setSaprodontiaStat(SaprodontiaStat.parseFromSaprodontiaDataDO(saprodontiaDataDO));
        return saprodontiaData;
    }

    /**
     * 获取学生常见病ID信息
     *
     * @param studentDTO    学生信息
     * @param screeningPlan 筛查计划
     * @return com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCommonDiseaseIdInfo
     **/
    private StudentCommonDiseaseIdInfo getStudentCommonDiseaseIdInfo(StudentDTO studentDTO, ScreeningPlan screeningPlan) {
        // TODO: 1. 减少数据库查询，在循环外查询数据库
        List<District> districtList = JSON.parseObject(studentDTO.getSchoolDistrictName(), new TypeReference<List<District>>(){});
        StudentCommonDiseaseId studentCommonDiseaseId = studentCommonDiseaseIdService.getStudentCommonDiseaseIdInfo(studentDTO.getSchoolDistrictId(),studentDTO.getSchoolId(), studentDTO.getGradeId(), studentDTO.getId(), screeningPlan.getStartTime());
        String commonDiseaseId = studentCommonDiseaseId.getCommonDiseaseId();
        Assert.isTrue(districtList.size() > 1, "学校行政区域无效");
        String cityCode = String.valueOf(districtList.get(1).getCode());
        // 用于判断是否为直辖市
        int index = cityCode.indexOf("000");
        return new StudentCommonDiseaseIdInfo()
                .setCommonDiseaseId(commonDiseaseId)
                .setProvinceName(districtList.get(0).getName())
                .setProvinceCode(commonDiseaseId.substring(0, 2))
                .setCityName(index > 4 ? districtList.get(0).getName() : districtList.get(1).getName())
                .setCityCode(commonDiseaseId.substring(2, 4))
                .setAreaName(index > 4 ? districtList.get(1).getName() : districtList.get(2).getName())
                .setAreaCode(commonDiseaseId.substring(5, 7))
                .setSchoolName(studentDTO.getSchoolName())
                .setSchoolCode(commonDiseaseId.substring(8, 10))
                .setGradeName(studentDTO.getGradeName())
                .setGradeCode(GradeCodeEnum.getByName(studentDTO.getGradeName()).getCode())
                .setStudentCode(studentCommonDiseaseId.getCommonDiseaseCode())
                .setAreaType(studentDTO.getSchoolAreaType())
                .setMonitorType(studentDTO.getSchoolMonitorType());
    }

    /**
     * 档案卡数据校验
     * @param exportCondition 导出条件
     */
    public void archiveDataValidate(ExportCondition exportCondition) {
        //筛查记录
        screeningRecord(exportCondition);
        //按区域
        districtStatistics(exportCondition);
        //按学校
        schoolStatistics(exportCondition);
    }

    /**
     * 按学校监测表rec导出数据校验
     * @param exportCondition 导出条件
     */
    private void schoolStatistics(ExportCondition exportCondition) {
        if (!Objects.equals(exportCondition.getExportType(),ExportTypeConst.SCHOOL_STATISTICS_REC)){
            return;
        }

        List<ScreeningPlan> screeningPlanList = screeningPlanService.getReleasePlanByNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId());
        if (CollUtil.isEmpty(screeningPlanList)){
            throw new BusinessException("暂无筛查计划");
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdsAndIsDoubleScreenAndDistrictIds(Lists.newArrayList(planIds), Boolean.FALSE,null,exportCondition.getSchoolId());
        exceptionInfo(CollUtil.isEmpty(visionScreeningResultList));
    }

    /**
     * 按区域监测表rec导出数据校验
     * @param exportCondition 导出条件
     */
    private void districtStatistics(ExportCondition exportCondition) {
        if (!Objects.equals(exportCondition.getExportType(),ExportTypeConst.DISTRICT_STATISTICS_REC)){
            return;
        }
        List<ScreeningPlan> screeningPlanList = screeningPlanService.getReleasePlanByNoticeIdOrTaskIdOrPlanId(exportCondition.getNotificationId(),exportCondition.getTaskId(),exportCondition.getPlanId());
        if (CollUtil.isEmpty(screeningPlanList)){
            throw new BusinessException("暂无筛查计划");
        }
        Set<Integer> planIds = screeningPlanList.stream().map(ScreeningPlan::getId).collect(Collectors.toSet());
        List<Integer> districtIdList = districtService.filterDistrict(exportCondition.getDistrictId());
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdsAndIsDoubleScreenAndDistrictIds(Lists.newArrayList(planIds), Boolean.FALSE,districtIdList,null);
        exceptionInfo(CollUtil.isEmpty(visionScreeningResultList));
    }

    /**
     * 筛查记录监测表rec导出数据校验
     * @param exportCondition 导出条件
     */
    private void screeningRecord(ExportCondition exportCondition) {
        if (!Objects.equals(exportCondition.getExportType(), ExportTypeConst.SCREENING_RECORD_REC)){
            return;
        }
        if (Objects.isNull(exportCondition.getPlanId())){
            throw new BusinessException("筛查计划ID不能为空");
        }
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdAndIsDoubleScreen(exportCondition.getPlanId(), Boolean.FALSE,exportCondition.getSchoolId());
        exceptionInfo(CollUtil.isEmpty(visionScreeningResultList));
    }

    /**
     * 异常处理
     * @param condition 条件
     */
    private void exceptionInfo(Boolean condition){
        if (Objects.equals(condition,Boolean.TRUE)){
            throw new BusinessException("暂无数据");
        }
    }

}
