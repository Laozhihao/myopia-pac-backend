package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.base.util.DateUtil;
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
        int year = DateUtil.getYear(screeningPlan.getStartTime());
        SchoolClassDTO classWithSchoolAndGradeName = schoolGradeService.getClassWithSchoolAndGradeName(archiveRequestParam.getClassId());
        return generateArchiveCardBatch(visionScreeningResultList, year, classWithSchoolAndGradeName);
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
     * @param visionScreeningResultList 筛查结果列表
     * @return 学生档案卡实体类list
     */
    private List<CommonDiseaseArchiveCard> generateArchiveCardBatch(List<VisionScreeningResult> visionScreeningResultList, int year, SchoolClassDTO classWithSchoolAndGradeName) {
        if (CollectionUtils.isEmpty(visionScreeningResultList)) {
            return Collections.emptyList();
        }
        // 查询学生信息
        School school = schoolService.getById(classWithSchoolAndGradeName.getSchoolId());
        List<Integer> planStudentIds = visionScreeningResultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toList());
        Map<Integer, ScreeningPlanSchoolStudent> planSchoolStudentMap = screeningPlanSchoolStudentService.getByIds(planStudentIds).stream().collect(Collectors.toMap(ScreeningPlanSchoolStudent::getId, Function.identity()));
        return visionScreeningResultList.stream()
                .map(visionScreeningResult -> generateArchiveCard(visionScreeningResult, getStudentDTO(planSchoolStudentMap.get(visionScreeningResult.getScreeningPlanSchoolStudentId()), classWithSchoolAndGradeName, school), year, school))
                .collect(Collectors.toList());
    }

    private StudentDTO getStudentDTO(ScreeningPlanSchoolStudent planStudent, SchoolClassDTO classWithSchoolAndGradeName, School school) {
        StudentDTO studentDTO = new StudentDTO()
                .setSchoolName(classWithSchoolAndGradeName.getSchoolName())
                .setGradeName(classWithSchoolAndGradeName.getGradeName())
                .setClassName(classWithSchoolAndGradeName.getName())
                .setSchoolDistrictName(school.getDistrictDetail());
        studentDTO
                .setName(planStudent.getStudentName())
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
     * @param studentDTO           学生信息
     * @return 学生档案卡实体类
     */
    private CommonDiseaseArchiveCard generateArchiveCard(VisionScreeningResult visionScreeningResult, StudentDTO studentDTO, int year, School school) {
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
                .setCommonDiseaseIdInfo(getStudentCommonDiseaseIdInfo(studentDTO, year, school));
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

    private StudentCommonDiseaseIdInfo getStudentCommonDiseaseIdInfo(StudentDTO studentDTO, int year, School school) {
        // TODO: 减少数据库查询，在循环外查询数据库
        List<District> districtList = JSON.parseObject(school.getDistrictDetail(), new TypeReference<List<District>>(){});
        StudentCommonDiseaseId studentCommonDiseaseId = studentCommonDiseaseIdService.getStudentCommonDiseaseId(school.getDistrictId(), school.getId(), studentDTO.getGradeId(), studentDTO.getId(), year);
        String schoolCommonDiseaseCode = schoolCommonDiseaseCodeService.getSchoolCommonDiseaseCode(school.getDistrictId(), school.getId(), year);
        return new StudentCommonDiseaseIdInfo()
                .setCommonDiseaseId(studentCommonDiseaseId.getCommonDiseaseId())
                .setProvinceName(districtList.get(0).getName())
                .setProvinceCode(String.valueOf(districtList.get(0).getCode()).substring(0, 2))
                .setCityName(districtList.get(1).getName())
                .setCityCode(String.valueOf(districtList.get(1).getCode()).substring(2, 4))
                .setAreaName(districtList.get(2).getName())
                .setAreaCode(String.valueOf(districtList.get(2).getCode()).substring(4, 6))
                .setSchoolName(studentDTO.getSchoolName())
                .setSchoolCode(schoolCommonDiseaseCode)
                .setGradeName(studentDTO.getGradeName())
                .setGradeCode(GradeCodeEnum.getByName(studentDTO.getGradeName()).getCode())
                .setStudentCode(studentCommonDiseaseId.getCommonDiseaseCode())
                .setAreaType(school.getAreaType())
                .setMonitorType(school.getMonitorType());
    }


}
