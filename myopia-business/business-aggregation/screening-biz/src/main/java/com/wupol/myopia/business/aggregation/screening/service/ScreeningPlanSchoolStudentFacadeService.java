package com.wupol.myopia.business.aggregation.screening.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wupol.framework.core.util.StringUtils;
import com.wupol.myopia.business.aggregation.screening.domain.vos.SchoolGradeVO;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.WearingGlassesSituation;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
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

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        List<GradeClassesDTO> gradeClasses = screeningPlanSchoolStudentService.selectSchoolGradeVoByPlanIdAndSchoolId(screeningPlanId, schoolId);
        //2. 根据年级分组
        Map<Integer, List<GradeClassesDTO>> graderIdClasses = gradeClasses.stream().collect(Collectors.groupingBy(GradeClassesDTO::getGradeId));
        //3. 组装SchoolGradeVo数据
        return graderIdClasses.keySet().stream().map(gradeId -> {
            SchoolGradeVO vo = new SchoolGradeVO();
            vo.setUniqueId(UUID.randomUUID().toString());
            List<GradeClassesDTO> gradeClassesDTOS = graderIdClasses.get(gradeId);
            // 查询并设置年级名称
            vo.setId(gradeId)
                    .setName(schoolGradeService.getGradeNameById(gradeId));
            // 查询并设置班级名称
            vo.setClasses(gradeClassesDTOS.stream().map(dto -> {
                SchoolClassDTO schoolClass = new SchoolClassDTO();
                schoolClass.setUniqueId(UUID.randomUUID().toString());
                schoolClass.setId(dto.getClassId())
                        .setName(schoolClassService.getClassNameById(dto.getClassId()))
                        .setGradeId(gradeId);
                return schoolClass;
            }).collect(Collectors.toList()));
            return vo;
        }).collect(Collectors.toList());
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
        Page<ScreeningStudentDTO> page = (Page<ScreeningStudentDTO>) pageRequest.toPage();
        if (StringUtils.hasLength(query.getGradeIds())) {
            query.setGradeList(Stream.of(StringUtils.commaDelimitedListToStringArray(query.getGradeIds())).map(Integer::parseInt).collect(Collectors.toList()));
        }
        IPage<ScreeningStudentDTO> studentDTOIPage = screeningPlanSchoolStudentService.selectPageByQuery(page, query);

        // 设置民族、地址 最初的写发
//        studentDTOIPage.getRecords().forEach(studentDTO ->
//                studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
//                        .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()))
//
//        );
        List<ScreeningStudentDTO> screeningStudentDTOS = studentDTOIPage.getRecords();
        List<Integer> ids = screeningStudentDTOS.stream().map(ScreeningStudentDTO :: getId).collect(Collectors.toList());
        List<VisionScreeningResult> visionScreeningResults =  visionScreeningResultService.getByStudentIds(query.getScreeningPlanId(),ids);
        //由于数据只有1条，所以进行分组
        Map<Integer,List<VisionScreeningResult>> visionScreeningResultsGroup = visionScreeningResults.stream().collect(Collectors.groupingBy(VisionScreeningResult::getStudentId));
        //作者：钓猫的小鱼。  描述：给学生扩展类赋值
        studentDTOIPage.getRecords().forEach(studentDTO -> {
            studentDTO.setNationDesc(NationEnum.getName(studentDTO.getNation()))
                        .setAddress(districtService.getAddressDetails(studentDTO.getProvinceCode(), studentDTO.getCityCode(), studentDTO.getAreaCode(), studentDTO.getTownCode(), studentDTO.getAddress()));
            setStudentEyeInfor(studentDTO,visionScreeningResultsGroup);
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
    public void setStudentEyeInfor(ScreeningStudentDTO studentEyeInfor,Map<Integer,List<VisionScreeningResult>> visionScreeningResultsGroup){
        VisionScreeningResult visionScreeningResult = EyeDataUtil.getVisionScreeningResult(studentEyeInfor,visionScreeningResultsGroup);

        studentEyeInfor.setGlassesTypeDes(EyeDataUtil.glassesType(visionScreeningResult));//是否戴镜情况

        String nakedVision = EyeDataUtil.visionRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.visionLeftDataToStr(visionScreeningResult);
        studentEyeInfor.setNakedVision(nakedVision);//裸视力

        String correctedVision = EyeDataUtil.correcteRightDataToStr(visionScreeningResult)+"/"+EyeDataUtil.correcteLeftDataToStr(visionScreeningResult);
        studentEyeInfor.setCorrectedVision(correctedVision);//矫正 视力

        String sph = EyeDataUtil.computerRightSph(visionScreeningResult)+"/"+EyeDataUtil.computerLeftSph(visionScreeningResult);
        studentEyeInfor.setSph(sph);//球镜

        String cyl = EyeDataUtil.computerRightCyl(visionScreeningResult)+"/"+EyeDataUtil.computerLeftCyl(visionScreeningResult);
        studentEyeInfor.setCyl(cyl);//柱镜

        String axial = EyeDataUtil.computerRightAxial(visionScreeningResult)+"/"+EyeDataUtil.computerLeftAxial(visionScreeningResult);
        studentEyeInfor.setAxial(axial);//眼轴

    }


}
