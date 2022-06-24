package com.wupol.myopia.business.aggregation.screening.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
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
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import com.wupol.myopia.business.core.screening.flow.util.EyeDataUtil;
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
    public List<SchoolGradeVO> getSchoolGradeVoByPlanIdAndSchoolId(Integer screeningPlanId, Integer schoolId) {
        //1. 获取该计划学校的筛查学生所有年级、班级
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId, null);
        return getSchoolGradeVOS(gradeClasses);
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
        Map<Integer,VisionScreeningResult> visionScreeningResultsGroup = resultList.stream().filter(visionScreeningResult -> Boolean.FALSE.equals(visionScreeningResult.getIsDoubleScreen())).collect(Collectors.toMap(VisionScreeningResult::getScreeningPlanSchoolStudentId, Function.identity()));

        //作者：钓猫的小鱼。  描述：给学生扩展类赋值
        studentDTOIPage.getRecords().forEach(studentDTO -> {
            studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                        .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));
            setStudentEyeInfo(studentDTO, visionScreeningResultsGroup);
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
    public void setStudentEyeInfo(ScreeningStudentDTO studentEyeInfo, Map<Integer, VisionScreeningResult> visionScreeningResultsGroup) {
        VisionScreeningResult visionScreeningResult = Optional.ofNullable(visionScreeningResultsGroup).map(x -> x.get(studentEyeInfo.getPlanStudentId())).orElse(null);
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
            return;
        }
        int reScreeningCount = visionScreeningResultService.count(new VisionScreeningResult().setScreeningPlanSchoolStudentId(visionScreeningResult.getScreeningPlanSchoolStudentId()).setIsDoubleScreen(true));
        studentEyeInfo.setIsDoubleScreen(reScreeningCount > 0);
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
        return graderIdClasses.keySet().stream().map(gradeId -> {
            SchoolGradeVO vo = new SchoolGradeVO();
            vo.setUniqueId(UUID.randomUUID().toString());
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            // 查询并设置年级名称
            vo.setId(gradeId)
                    .setName(gradeMap.get(gradeId).getName());
            // 查询并设置班级名称
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClassDTO schoolClass = new SchoolClassDTO();
                schoolClass.setUniqueId(UUID.randomUUID().toString());
                schoolClass.setId(dto.getClassId())
                        .setName(classMap.get(dto.getClassId()).getName())
                        .setGradeId(gradeId);
                return schoolClass;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
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
