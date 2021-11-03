package com.wupol.myopia.business.api.school.management.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dos.ReportAndRecordDO;
import com.wupol.myopia.business.core.hospital.service.MedicalReportService;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentListResponseDTO;
import com.wupol.myopia.business.core.school.management.domain.dto.SchoolStudentRequestDTO;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.screening.flow.domain.dto.StudentScreeningCountDTO;
import com.wupol.myopia.business.core.screening.flow.service.VisionScreeningResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学校端-学生
 *
 * @author Simple4H
 */
@Service
public class SchoolStudentBizService {

    @Autowired
    private SchoolStudentService schoolStudentService;

    @Autowired
    private VisionScreeningResultService visionScreeningResultService;

    @Autowired
    private MedicalReportService medicalReportService;

    /**
     * 获取学生列表
     *
     * @param pageRequest 分页请求
     * @param requestDTO  请求入参
     * @return IPage<SchoolStudentListResponseDTO>
     */
    public IPage<SchoolStudentListResponseDTO> getList(PageRequest pageRequest, SchoolStudentRequestDTO requestDTO) {

        IPage<SchoolStudentListResponseDTO> responseDTO = schoolStudentService.getList(pageRequest, requestDTO);
        List<SchoolStudentListResponseDTO> studentList = responseDTO.getRecords();

        // 学生Ids
        List<Integer> studentIds = studentList.stream().map(SchoolStudent::getStudentId).collect(Collectors.toList());

        // 筛查次数
        List<StudentScreeningCountDTO> studentScreeningCountVOS = visionScreeningResultService.countScreeningTime();
        Map<Integer, Integer> countMaps = studentScreeningCountVOS.stream().collect(Collectors
                .toMap(StudentScreeningCountDTO::getStudentId,
                        StudentScreeningCountDTO::getCount));

        // 获取就诊记录
        List<ReportAndRecordDO> visitLists = medicalReportService.getByStudentIds(studentIds);
        Map<Integer, List<ReportAndRecordDO>> visitMap = visitLists.stream()
                .collect(Collectors.groupingBy(ReportAndRecordDO::getStudentId));

        studentList.forEach(s -> {
            s.setScreeningCount(countMaps.getOrDefault(s.getStudentId(), 0));
            s.setNumOfVisits(Objects.nonNull(visitMap.get(s.getStudentId())) ? visitMap.get(s.getStudentId()).size() : 0);
        });

    }

}
