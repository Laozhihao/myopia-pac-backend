package com.wupol.myopia.business.aggregation.screening.service;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.google.common.collect.Lists;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.GradeClassesDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentDTO;
import com.wupol.myopia.business.core.screening.flow.domain.dto.ScreeningStudentQueryDTO;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
import com.wupol.myopia.business.core.screening.flow.util.StatUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 筛查学生
 *
 * @author Simple4H
 */
@Slf4j
@Service
public class ScreeningPlanSchoolStudentFacadeService {

    @Autowired
    private ScreeningPlanSchoolStudentService screeningPlanSchoolStudentService;

    @Autowired
    private SchoolGradeService schoolGradeService;

    @Autowired
    private SchoolClassService schoolClassService;

    @Autowired
    private DistrictService districtService;

    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId 筛查计划
     * @param schoolId        学校Id
     * @return List<SchoolGradeVO>
     */
    public List<SchoolGradeVO> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId,Boolean isData) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId, null);
        List<SchoolGradeVO> schoolGradeVoS = getSchoolGradeVOS(gradeClasses);
        if (Objects.equals(Boolean.TRUE,isData)) {
            return getDataSchoolGradeList(screeningPlanId, schoolId, schoolGradeVoS);
        }
        return schoolGradeVoS;
    }

    /**
     * 获取有数据的学校年级和学校班级集合
     * @param screeningPlanId
     * @param schoolId
     * @param schoolGradeVoS
     */
    private List<SchoolGradeVO> getDataSchoolGradeList(Integer screeningPlanId, Integer schoolId, List<SchoolGradeVO> schoolGradeVoS) {
        List<VisionScreeningResult> visionScreeningResultList = visionScreeningResultService.getByPlanIdAndSchoolId(screeningPlanId, schoolId);
        if (CollUtil.isEmpty(visionScreeningResultList)){
            return Lists.newArrayList();
        }
        Set<Integer> planSchoolStudentIds = visionScreeningResultList.stream().map(VisionScreeningResult::getScreeningPlanSchoolStudentId).collect(Collectors.toSet());
        List<ScreeningPlanSchoolStudent> screeningPlanSchoolStudentList = screeningPlanSchoolStudentService.getByIds(Lists.newArrayList(planSchoolStudentIds));
        Set<Integer> gradeIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getGradeId).collect(Collectors.toSet());
        Set<Integer> classIds = screeningPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getClassId).collect(Collectors.toSet());
        return schoolGradeVoS.stream()
                .filter(schoolGradeVO -> gradeIds.contains(schoolGradeVO.getId()))
                .map(schoolGradeVO -> getSchoolGradeVO(classIds, schoolGradeVO))
                .collect(Collectors.toList());
    }

    /**
     * 获取有数据的学校年级
     * @param classIds
     * @param schoolGradeVO
     */
    private SchoolGradeVO getSchoolGradeVO(Set<Integer> classIds, SchoolGradeVO schoolGradeVO) {
        List<SchoolClassDTO> schoolClassList = schoolGradeVO.getClasses();
        if (CollUtil.isNotEmpty(schoolClassList)){
            List<SchoolClassDTO> collect = schoolClassList.stream().filter(schoolClassDTO -> classIds.contains(schoolClassDTO.getId())).collect(Collectors.toList());
            schoolGradeVO.setClasses(collect);
        }
        return schoolGradeVO;
    }

    /**
     * 分页获取筛查计划的学校学生数据
     *
     * @param query       查询条件
     * @param pageRequest 分页请求
     * @return IPage<ScreeningStudentDTO>
     */
    public IPage<ScreeningStudentDTO> getPage(ScreeningStudentQueryDTO query, PageRequest pageRequest) {
        Assert.notNull(query.getScreeningPlanId(), "筛查计划ID不能为空");
        Assert.notNull(query.getSchoolId(), "筛查学校ID不能为空");
        if (StringUtils.hasLength(query.getGradeIds())) {
            query.setGradeList(Stream.of(StringUtils.commaDelimitedListToStringArray(query.getGradeIds())).map(Integer::parseInt).collect(Collectors.toList()));
        }
        IPage<ScreeningStudentDTO> studentDTOIPage = screeningPlanSchoolStudentService.selectPageByQuery(pageRequest.toPage(), query);
        List<ScreeningStudentDTO> screeningStudentDTOS = studentDTOIPage.getRecords();
        if (CollectionUtils.isEmpty(screeningStudentDTOS)) {
            return studentDTOIPage;
        }
        List<VisionScreeningResult> resultList  = visionScreeningResultService.getByPlanStudentIds(screeningStudentDTOS.stream().map(ScreeningStudentDTO::getPlanStudentId).collect(Collectors.toList()));
        Map<Integer,VisionScreeningResult> firstScreeningResultMap = resultList.stream().filter(visionScreeningResult -> Boolean.FALSE.equals(visionScreeningResult.getIsDoubleScreen())).collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));
        Map<Integer,VisionScreeningResult> reScreeningResultMap= resultList.stream().filter(visionScreeningResult -> Boolean.TRUE.equals(visionScreeningResult.getIsDoubleScreen())).collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        //作者：钓猫的小鱼。  描述：给学生扩展类赋值
        studentDTOIPage.getRecords().forEach(studentDTO -> {
            studentDTO.setNationDesc(NationEnum.getNameByCode(studentDTO.getNation()))
                        .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));
            setStudentEyeInfo(studentDTO, firstScreeningResultMap, reScreeningResultMap);
        });
        return studentDTOIPage;
    }

    /**
    * @Description:  给学生扩展类赋值
    * @Param: [studentEyeInfor]
    * @return: void
    * @Author: 钓猫的小鱼
    * @Date: 2022/1/5
    */
    public void setStudentEyeInfo(ScreeningStudentDTO studentEyeInfo, Map<Integer, VisionScreeningResult> firstScreeningResultMap, Map<Integer, VisionScreeningResult> reScreeningResultMap) {
        VisionScreeningResult visionScreeningResult = Optional.ofNullable(firstScreeningResultMap).map(x -> x.get(studentEyeInfo.getPlanStudentId())).orElse(null);
        studentEyeInfo.setHasScreening(Objects.nonNull(visionScreeningResult))
                //是否戴镜情况
                .setGlassesTypeDes(EyeDataUtil.glassesTypeString(visionScreeningResult))
                //裸视力
                .setNakedVision(EyeDataUtil.visionRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.visionLeftDataToStr(visionScreeningResult))
                //矫正 视力
                .setCorrectedVision(EyeDataUtil.correctedRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.correctedLeftDataToStr(visionScreeningResult))
                //球镜
                .setRSph(EyeDataUtil.computerRightSphNULL(visionScreeningResult))
                .setLSph(EyeDataUtil.computerLeftSphNull(visionScreeningResult))
                //柱镜
                .setRCyl(EyeDataUtil.computerRightCylNull(visionScreeningResult))
                .setLCyl(EyeDataUtil.computerLeftCylNull(visionScreeningResult))
                //眼轴
                .setAxial(EyeDataUtil.computerRightAxial(visionScreeningResult)+"/"+EyeDataUtil.computerLeftAxial(visionScreeningResult));
        //是否有做复测
        if (Objects.isNull(visionScreeningResult)) {
            studentEyeInfo.setIsDoubleScreen(Boolean.FALSE);
            studentEyeInfo.setDataIntegrity(CommonConst.DATA_INTEGRITY_MISS);
            return;
        }
        VisionScreeningResult reScreeningResult = Optional.ofNullable(reScreeningResultMap).map(x -> x.get(studentEyeInfo.getPlanStudentId())).orElse(null);
        studentEyeInfo.setIsDoubleScreen(Objects.nonNull(reScreeningResult));

        //是否数据完整性
        boolean completedData = StatUtil.isCompletedData(visionScreeningResult.getVisionData(), visionScreeningResult.getComputerOptometry());
        studentEyeInfo.setDataIntegrity(Objects.equals(completedData,Boolean.TRUE)?CommonConst.DATA_INTEGRITY_FINISH:CommonConst.DATA_INTEGRITY_MISS);
    }

    /**
     * 获取计划中的学校年级情况(有数据)
     *
     * @param planId         筛查计划
     * @param schoolId       学校Id
     * @param isKindergarten 是否幼儿园
     *
     * @return List<SchoolGradeVO>
     */
    public List<SchoolGradeVO> getByPlanIdAndSchoolIdAndId(Integer planId, Integer schoolId, Boolean isKindergarten) {
        List<Integer> planStudentIds = visionScreeningResultService.getByPlanStudentIdPlanIdAndSchoolId(planId, schoolId);
        if (CollectionUtils.isEmpty(planStudentIds)) {
            return Collections.emptyList();
        }
        List<String> kindergartenGradeName = GradeCodeEnum.kindergartenSchoolName();
        List<GradeClassesDTO> gradeClassesDTOS = screeningPlanSchoolStudentService.getByPlanIdAndSchoolIdAndId(planId, schoolId, planStudentIds);
        if(Objects.isNull(isKindergarten)) {
            return getSchoolGradeVOS(gradeClassesDTOS);
        }
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeClassesDTOS, GradeClassesDTO::getGradeId);
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(gradeClassesDTOS, GradeClassesDTO::getClassId);
        gradeClassesDTOS.forEach(s->{
            s.setGradeName(gradeMap.getOrDefault(s.getGradeId(), new SchoolGrade()).getName());
            s.setClassName(classMap.getOrDefault(s.getClassId(), new SchoolClass()).getName());
        });
        return getSchoolGradeVOS(gradeClassesDTOS.stream()
                .filter(grade -> Boolean.TRUE.equals(isKindergarten) == kindergartenGradeName.contains(grade.getGradeName()))
                .collect(Collectors.toList()));
    }

    /**
     * 封装年级信息
     *
     * @param gradeClasses 年级
     * @return List<SchoolGradeVO>
     */
    public List<SchoolGradeVO> getSchoolGradeVOS(List<GradeClassesDTO> gradeClasses) {
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(gradeClasses.stream().map(GradeClassesDTO::getGradeId).collect(Collectors.toList()));
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(gradeClasses.stream().map(GradeClassesDTO::getClassId).collect(Collectors.toList()));


        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(gradeId -> getSchoolGradeVO(gradeMap, classMap, graderIdClasses, gradeId)).collect(Collectors.toList());
    }

    /**
     * 获取学校年级信息
     * @param gradeMap
     * @param classMap
     * @param graderIdClasses
     * @param gradeId
     */
    private SchoolGradeVO getSchoolGradeVO(Map<Integer, SchoolGrade> gradeMap, Map<Integer, SchoolClass> classMap, Map<Integer, List<GradeClassesDTO>> graderIdClasses, Integer gradeId) {
        SchoolGradeVO vo = new SchoolGradeVO();
        vo.setUniqueId(UUID.randomUUID().toString());
        List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
        // 查询并设置年级名称
        vo.setId(gradeId)
                .setName(gradeMap.get(gradeId).getName());
        // 查询并设置班级名称
        vo.setClasses(gradeClassesDTOS.stream().map(dto -> getSchoolClassDTO(classMap, gradeId, dto)).collect(Collectors.toList()));
        return vo;
    }

    /**
     * 获取学校班级信息
     * @param classMap
     * @param gradeId
     * @param dto
     */
    private SchoolClassDTO getSchoolClassDTO(Map<Integer, SchoolClass> classMap, Integer gradeId, GradeClassesDTO dto) {
        SchoolClassDTO schoolClass = new SchoolClassDTO();
        schoolClass.setUniqueId(UUID.randomUUID().toString());
        schoolClass.setId(dto.getClassId())
                .setName(classMap.get(dto.getClassId()).getName())
                .setGradeId(gradeId);
        return schoolClass;
    }

    /**
     * 获取计划中的学校年级情况
     *
     * @param screeningPlanId 筛查计划
     * @param schoolId        学校Id
     * @return List<SchoolGradeVO>
     */
    public List<SchoolGradeVO> getGradeByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.getGradeByPlanIdAndSchoolId(screeningPlanId, schoolId);
        return getSchoolGradeVOS(gradeClasses);
    }

}
