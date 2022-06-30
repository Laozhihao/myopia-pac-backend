package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.StudentDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.StudentCommonDiseaseId;
import com.wupol.myopia.business.core.school.service.SchoolCommonDiseaseCodeService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentCommonDiseaseIdService;
import com.wupol.myopia.business.core.screening.flow.domain.dos.DiseasesHistoryDO;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaData;
import com.wupol.myopia.business.core.screening.flow.domain.dos.SaprodontiaDataDO;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 档案卡业务类
 *
 * @Author HaoHao
 * @Date 2022/5/6
 **/
@Service
public class ArchiveService {

    @Autowired
    private ScreeningPlanService screeningPlanService;
    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;
    @Autowired
    private VisionScreeningResultService visionScreeningResultService;
    @Autowired
    private StudentFacade studentFacade;
    @Autowired
    private StudentCommonDiseaseIdService studentCommonDiseaseIdService;
    @Autowired
    private SchoolCommonDiseaseCodeService schoolCommonDiseaseCodeService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private SchoolGradeService schoolGradeService;

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
        School school = schoolService.getById(classWithSchoolAndGradeName.getSchoolId());
        List<Integer> planStudentIds = visionScreeningResultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentService.getByIds(planStudentIds).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        return visionScreeningResultList.stream()
                .map(visionScreeningResult -> generateArchiveCard(visionScreeningResult, getStudentDTO(planSchoolStudentMap.get(visionScreeningResult.getScreeningPlanSchoolStudentId()), classWithSchoolAndGradeName, school), screeningPlan, school))
                .collect(Collectors.toList());
    }

    /**
     * 获取学生信息
     *
     * @param planStudent   筛查计划学生
     * @param classWithSchoolAndGradeName   班级信息（带学校、年级名称）
     * @param school    学校
     * @return com.wupol.myopia.business.core.school.domain.dto.StudentDTO
     **/
    private StudentDTO getStudentDTO(ScreeningPlanSchoolStudent planStudent, SchoolClassDTO classWithSchoolAndGradeName, School school) {
        StudentDTO studentDTO = new StudentDTO()
                .setSchoolName(classWithSchoolAndGradeName.getSchoolName())
                .setGradeName(classWithSchoolAndGradeName.getGradeName())
                .setClassName(classWithSchoolAndGradeName.getName())
                .setSchoolDistrictName(school.getDistrictDetail());
        studentDTO.setName(planStudent.getStudentName())
                .setBirthday(planStudent.getBirthday())
                .setIdCard(planStudent.getIdCard())
                .setGender(planStudent.getGender())
                .setSno(planStudent.getStudentNo())
                .setParentPhone(planStudent.getParentPhone())
                .setNation(planStudent.getNation())
                .setPassport(planStudent.getPassport())
                .setGradeType(planStudent.getGradeType())
                .setGradeId(classWithSchoolAndGradeName.getGradeId())
                .setClassId(classWithSchoolAndGradeName.getId())
                .setSchoolId(school.getId())
                .setId(planStudent.getStudentId());
        return studentDTO;
    }

    /**
     * 生成档案卡
     *
     * @param visionScreeningResult 筛查结果
     * @param studentDTO            学生信息
     * @param screeningPlan         筛查计划
     * @param school                学校
     * @return 学生档案卡实体类
     */
    private CommonDiseaseArchiveCard generateArchiveCard(VisionScreeningResult visionScreeningResult, StudentDTO studentDTO, ScreeningPlan screeningPlan, School school) {
        CardInfoVO studentInfo = studentFacade.getCardInfo(studentDTO);
        // 民族特殊处理，不在常见民族列表的设为其他（前端展示需要）
        NationEnum nationEnum = NationEnum.COMMON_NATION.stream().filter(nation -> nation.getCode().equals(studentInfo.getNation())).findFirst().orElse(NationEnum.OTHER);
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
                .setCommonDiseaseIdInfo(getStudentCommonDiseaseIdInfo(studentDTO, screeningPlan, school));
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
     * @param school        学校
     * @return com.wupol.myopia.business.core.screening.flow.domain.vo.StudentCommonDiseaseIdInfo
     **/
    private StudentCommonDiseaseIdInfo getStudentCommonDiseaseIdInfo(StudentDTO studentDTO, ScreeningPlan screeningPlan, School school) {
        // TODO: 1. 减少数据库查询，在循环外查询数据库
        List<District> districtList = JSON.parseObject(school.getDistrictDetail(), new TypeReference<List<District>>(){});
        StudentCommonDiseaseId studentCommonDiseaseId = studentCommonDiseaseIdService.getStudentCommonDiseaseIdInfo(school.getDistrictId(), school.getId(), studentDTO.getGradeId(), studentDTO.getId(), screeningPlan.getStartTime());
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
                .setAreaType(school.getAreaType())
                .setMonitorType(school.getMonitorType());
    }

}
