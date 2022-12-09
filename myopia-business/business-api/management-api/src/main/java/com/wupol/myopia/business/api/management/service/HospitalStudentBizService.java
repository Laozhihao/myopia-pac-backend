package com.wupol.myopia.business.api.management.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentRequestDTO;
import com.wupol.myopia.business.core.hospital.domain.dto.HospitalStudentResponseDTO;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.vo.SchoolGradeClassVO;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医院学生
 *
 * @author Simple4H
 */
@Service
public class HospitalStudentBizService {

    @Resource
    private HospitalStudentService hospitalStudentService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolClassService schoolClassService;

    @Resource
    private DistrictService districtService;

    @Resource
    private MedicalReportService medicalReportService;

    @Resource
    private PreschoolCheckRecordService preschoolCheckRecordService;

    /**
     * 获取医院学生
     *
     * @param pageRequest 分页请求
     * @param requestDTO  条件
     * @return IPage<HospitalStudentResponseDTO>
     */
    public IPage<HospitalStudentResponseDTO> getHospitalStudent(PageRequest pageRequest, HospitalStudentRequestDTO requestDTO) {
        IPage<HospitalStudentResponseDTO> responseDTOIPage = hospitalStudentService.getByList(pageRequest, requestDTO);
        List<HospitalStudentResponseDTO> hospitalStudentList = responseDTOIPage.getRecords();
        if (CollectionUtils.isEmpty(hospitalStudentList)) {
            return responseDTOIPage;
        }
        // 获取学校
        Map<Integer, String> schoolMap = schoolService.getSchoolMap(hospitalStudentList, HospitalStudent::getSchoolId);

        // 获取年级
        Map<Integer, SchoolGrade> gradeMap = schoolGradeService.getGradeMapByIds(hospitalStudentList, HospitalStudent::getGradeId);

        // 获取班级
        Map<Integer, SchoolClass> classMap = schoolClassService.getClassMapByIds(hospitalStudentList,HospitalStudent::getClassId);

        // 获取学生列表
        List<Integer> studentIds = hospitalStudentList.stream().map(HospitalStudent::getStudentId).collect(Collectors.toList());

        // 获取报告
        List<ReportAndRecordDO> reportList = medicalReportService.getByStudentIdsAndHospitalId(studentIds, requestDTO.getHospitalId());
        LinkedHashMap<Integer, Long> reportMap = reportList.stream().collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId, LinkedHashMap::new, Collectors.counting()));

        // 获取学生检查数
        Map<Integer, Integer> studentCheckCount = preschoolCheckRecordService.getStudentCheckCount(requestDTO.getHospitalId(), studentIds);

        hospitalStudentList.forEach(hospitalStudent -> {
            hospitalStudent.setSchoolName(schoolMap.get(hospitalStudent.getSchoolId()));
            hospitalStudent.setGradeName(Objects.isNull(gradeMap.get(hospitalStudent.getGradeId())) ? null : gradeMap.get(hospitalStudent.getGradeId()).getName());
            hospitalStudent.setClassName(Objects.isNull(classMap.get(hospitalStudent.getClassId())) ? null : classMap.get(hospitalStudent.getClassId()).getName());
            if (Objects.nonNull(hospitalStudent.getBirthday())) {
                hospitalStudent.setBirthdayInfo(DateUtil.getAgeInfo(hospitalStudent.getBirthday(), new Date()));
            }
            hospitalStudent.setReportCount(reportMap.getOrDefault(hospitalStudent.getStudentId(), 0L));
            hospitalStudent.setPreschoolCheckCount(studentCheckCount.getOrDefault(hospitalStudent.getStudentId(), 0));
        });
        return responseDTOIPage;
    }

    /**
     * 通过Id获取患者
     *
     * @param id 医院学生Id
     * @return HospitalStudentResponseDTO
     */
    public HospitalStudentResponseDTO getByHospitalStudentId(Integer id) {
        HospitalStudentResponseDTO hospitalStudent = hospitalStudentService.getByHospitalStudentId(id);
        SchoolGradeClassVO schoolGradeClassVO = schoolService.getBySchoolIdAndGradeIdAndClassId(hospitalStudent.getSchoolId(),
                hospitalStudent.getGradeId(), hospitalStudent.getClassId());
        if (Objects.nonNull(schoolGradeClassVO)) {
            hospitalStudent.setSchoolName(schoolGradeClassVO.getSchoolName());
            hospitalStudent.setGradeName(schoolGradeClassVO.getGradeName());
            hospitalStudent.setClassName(schoolGradeClassVO.getClassName());
            hospitalStudent.setSchoolName(schoolGradeClassVO.getSchoolName());
        }
        if (Objects.nonNull(hospitalStudent.getBirthday())) {
            hospitalStudent.setBirthdayInfo(DateUtil.getAgeInfo(hospitalStudent.getBirthday(), new Date()));
        }
        if (Objects.nonNull(hospitalStudent.getCommitteeCode())) {
            hospitalStudent.setCommitteeLists(districtService.getDistrictPositionDetail(hospitalStudent.getCommitteeCode()));
        }
        return hospitalStudent;
    }
}
