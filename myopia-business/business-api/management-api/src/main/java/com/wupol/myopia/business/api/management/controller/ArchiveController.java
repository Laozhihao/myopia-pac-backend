package com.wupol.myopia.business.api.management.controller;

import com.alibaba.fastjson.JSONObject;
import com.wupol.myopia.base.domain.ApiResult;
import com.wupol.myopia.base.handler.ResponseResultBody;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveExportCondition;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.*;
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
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2022/4/25
 **/
@Log4j2
@CrossOrigin
@Validated
@ResponseResultBody
@RestController
@RequestMapping("/management/archive")
public class ArchiveController {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Resource
    private StudentService studentService;
    @Autowired
    private StudentFacade studentFacade;

    /**
     * 导出档案卡/监测表
     *
     * @param archiveExportCondition 导出条件
     * @return com.wupol.myopia.base.domain.ApiResult<java.lang.String>
     **/
    @GetMapping("/export")
    public ApiResult<String> exportArchive(@Valid ArchiveExportCondition archiveExportCondition) {
        log.info("export success!");
        log.info(JSONObject.toJSONString(archiveExportCondition));
        return ApiResult.success();
    }

    /**
     * 获取档案卡/监测表数据
     *
     * @param archiveRequestParam 请求参数
     * @return java.util.List<com.wupol.myopia.business.core.screening.flow.domain.vo.CommonDiseaseArchiveCard>
     **/
    @GetMapping("/data")
    public List<CommonDiseaseArchiveCard> getArchiveData(@Valid ArchiveRequestParam archiveRequestParam) {
        log.info(JSONObject.toJSONString(archiveRequestParam));

        ScreeningPlan screeningPlan = screeningPlanService.getById(archiveRequestParam.getPlanId());
        Assert.notNull(screeningPlan, "无法找到该筛查计划");

        Set<Integer> planStudentIds = getPlanStudentIds(archiveRequestParam);
        if (CollectionUtils.isEmpty(planStudentIds)) {
            return Collections.emptyList();
        }
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByScreeningPlanSchoolStudentIds(planStudentIds);
        return generateArchiveCardBatch(visionScreeningResultList);
    }

    private Set<Integer> getPlanStudentIds(ArchiveRequestParam archiveRequestParam) {
        if (!CollectionUtils.isEmpty(archiveRequestParam.getPlanStudentIds())) {
            return archiveRequestParam.getPlanStudentIds();
        }
        List<ScreeningPlanSchoolStudent> planStudentList = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndGradeIdAndClassId(archiveRequestParam.getPlanId(),
                null, null, archiveRequestParam.getClassId());
        return planStudentList.stream().map(ScreeningPlanSchoolStudent::getId).collect(Collectors.toSet());
    }

    /**
     * 批量生成学生档案卡
     *
     * @param visionScreeningResultList 筛查结果列表
     * @return 学生档案卡实体类list
     */
    private List<CommonDiseaseArchiveCard> generateArchiveCardBatch(List<VisionScreeningResult> visionScreeningResultList) {
        if (CollectionUtils.isEmpty(visionScreeningResultList)) {
            return Collections.emptyList();
        }
        // 查询学生信息 TODO：应该取planStudent表学生数据
        List<Integer> studentIdList = visionScreeningResultList.stream().map(VisionScreeningResult::getStudentId).collect(Collectors.toList());
        Map<Integer, StudentDTO> studentMap = studentService.getStudentInfoList(studentIdList).stream().collect(Collectors.toMap(Student::getId, Function.identity()));

        return visionScreeningResultList.stream()
                .map(visionScreeningResult -> generateArchiveCard(visionScreeningResult, studentMap.get(visionScreeningResult.getStudentId())))
                .collect(Collectors.toList());
    }

    /**
     * 生成档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param studentDTO           学生信息
     * @return 学生档案卡实体类
     */
    private CommonDiseaseArchiveCard generateArchiveCard(VisionScreeningResult visionScreeningResult, StudentDTO studentDTO) {
        CardInfoVO studentInfo = studentFacade.getCardInfo(studentDTO);
        studentInfo.setScreeningDate(visionScreeningResult.getCreateTime());
        return new CommonDiseaseArchiveCard().setStudentInfo(studentInfo)
                .setBloodPressureData(visionScreeningResult.getBloodPressureData())
                .setComputerOptometryData(getComputerOptometryData(visionScreeningResult.getComputerOptometry()))
                .setVisionData(getVisionDataData(visionScreeningResult.getVisionData()))
                .setDiseasesHistoryData(Optional.ofNullable(visionScreeningResult.getDiseasesHistoryData()).map(DiseasesHistoryDO::getDiseases).orElse(Collections.emptyList()))
                .setSaprodontiaData(getSaprodontiaData(visionScreeningResult.getSaprodontiaData()))
                .setSpineData(visionScreeningResult.getSpineData())
                .setHeightAndWeightData(visionScreeningResult.getHeightAndWeightData())
                .setPrivacyData(visionScreeningResult.getPrivacyData())
                .setCommonDiseaseIdInfo(getStudentCommonDiseaseIdInfo(studentDTO));
    }

    private ArchiveComputerOptometryDO getComputerOptometryData(ComputerOptometryDO computerOptometryDO) {
        // TODO：合并 getVisionDataData()
        if (Objects.isNull(computerOptometryDO)) {
            return null;
        }
        ArchiveComputerOptometryDO archiveComputerOptometryDO = new ArchiveComputerOptometryDO();
        BeanUtils.copyProperties(computerOptometryDO, archiveComputerOptometryDO);
        archiveComputerOptometryDO.setSignPicUrl(studentFacade.getSignPicUrl(computerOptometryDO.getCreateUserId()));
        return archiveComputerOptometryDO;
    }

    private ArchiveVisionDataDO getVisionDataData(VisionDataDO visionDataDO) {
        if (Objects.isNull(visionDataDO)) {
            return null;
        }
        ArchiveVisionDataDO archiveVisionDataDO = new ArchiveVisionDataDO();
        BeanUtils.copyProperties(visionDataDO, archiveVisionDataDO);
        archiveVisionDataDO.setSignPicUrl(studentFacade.getSignPicUrl(visionDataDO.getCreateUserId()));
        return archiveVisionDataDO;
    }

    private SaprodontiaData getSaprodontiaData(SaprodontiaDataDO saprodontiaDataDO) {
        if (Objects.isNull(saprodontiaDataDO)) {
            return null;
        }
        SaprodontiaData saprodontiaData = new SaprodontiaData();
        BeanUtils.copyProperties(saprodontiaDataDO, saprodontiaData);
        saprodontiaData.setSaprodontiaStat(SaprodontiaStat.parseFromSaprodontiaDataDO(saprodontiaDataDO));
        return saprodontiaData;
    }


    private StudentCommonDiseaseIdInfo getStudentCommonDiseaseIdInfo(StudentDTO studentDTO) {
        return new StudentCommonDiseaseIdInfo().setCommonDiseaseId(4201202102010001L)
                .setProvinceName("广东省")
                .setProvinceCode("32")
                .setCityName("广州市")
                .setCityCode("56")
                .setAreaName("天河区")
                .setAreaCode("12")
                .setSchoolName(studentDTO.getSchoolName())
                .setSchoolCode("01")
                .setGradeName(studentDTO.getGradeName())
                .setGradeCode(GradeCodeEnum.getByName(studentDTO.getGradeName()).getCode())
                .setStudentCode("0001")
                .setAreaType(1)
                .setMonitorType(1);
    }


}
