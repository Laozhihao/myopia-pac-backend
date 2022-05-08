package com.wupol.myopia.business.api.management.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.wupol.myopia.business.aggregation.student.service.StudentFacade;
import com.wupol.myopia.business.api.management.domain.dto.ArchiveRequestParam;
import com.wupol.myopia.business.core.common.domain.model.District;
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
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
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
    @Resource
    private StudentService studentService;
    @Autowired
    private StudentFacade studentFacade;

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
        return generateArchiveCardBatch(visionScreeningResultList);
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

    /**
     * 获取屈光数据
     *
     * @param computerOptometryDO 屈光检查数据
     * @return com.wupol.myopia.business.core.screening.flow.domain.dos.ArchiveComputerOptometryDO
     **/
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
        archiveVisionDataDO.setSignPicUrl(studentFacade.getSignPicUrl(1651));
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
        List<District> districtList = JSON.parseObject(studentDTO.getSchoolDistrictName(), new TypeReference<List<District>>(){});

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
