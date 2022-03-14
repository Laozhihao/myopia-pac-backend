package com.wupol.myopia.business.aggregation.student.service;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.model.ResultNoticeConfig;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.common.service.ResourceFileService;
import com.wupol.myopia.business.core.school.domain.dto.SaveSchoolRequestDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * 多端管理学校
 *
 * @author Simple4H
 */
@Service
public class SchoolFacade {

    @Resource
    private SchoolService schoolService;
    @Resource
    private SchoolGradeService schoolGradeService;


    @Resource
    private DistrictService districtService;

    @Resource
    private StudentService studentService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Resource
    private ResourceFileService resourceFileService;

    /**
     * 获取学校详情
     *
     * @param id                 学校ID
     * @param isSchoolManagement 是否学校管理端
     * @return SchoolResponseDTO
     */
    public SchoolResponseDTO getBySchoolId(Integer id, boolean isSchoolManagement) {
        SchoolResponseDTO responseDTO = new SchoolResponseDTO();
        School school = schoolService.getBySchoolId(id);
        BeanUtils.copyProperties(school, responseDTO);
        // 填充地址
        responseDTO.setAddressDetail(districtService.getAddressDetails(school.getProvinceCode(), school.getCityCode(), school.getAreaCode(), school.getTownCode(), school.getAddress()));
        int studentCount;
        if (isSchoolManagement) {
            studentCount = schoolStudentService.count(new SchoolStudent().setSchoolId(school.getId()).setStatus(CommonConst.STATUS_NOT_DELETED));
        } else {
            studentCount = studentService.count(new Student().setSchoolId(school.getId()).setStatus(CommonConst.STATUS_NOT_DELETED));
        }
        // 统计学生数
        responseDTO.setStudentCount(studentCount);
        ResultNoticeConfig resultNoticeConfig = school.getResultNoticeConfig();
        if (Objects.nonNull(resultNoticeConfig) && Objects.nonNull(resultNoticeConfig.getQrCodeFileId())) {
            responseDTO.setNoticeResultFileUrl(resourceFileService.getResourcePath(resultNoticeConfig.getQrCodeFileId()));
        }
        return responseDTO;
    }

    /**
     * 更新学校
     *
     * @param schoolRequestDTO 学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolResponseDTO updateSchool(SaveSchoolRequestDTO schoolRequestDTO) {
        Integer schoolId = schoolRequestDTO.getId();
        if (schoolService.checkSchoolName(schoolRequestDTO.getName(), schoolId)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        District district = districtService.getById(schoolRequestDTO.getDistrictId());
        schoolRequestDTO.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        //更新学校
        schoolService.updateById(schoolRequestDTO);
        // 新增
        schoolService.generateGradeAndClass(schoolRequestDTO.getId(), schoolRequestDTO.getCreateUserId(), schoolRequestDTO.getBatchSaveGradeList());
        // 同步到oauth机构状态
        if (Objects.nonNull(schoolRequestDTO.getStatus())) {
            oauthServiceClient.updateOrganization(new Organization(schoolRequestDTO.getId(), SystemCode.SCHOOL_CLIENT,
                    UserType.OTHER, schoolRequestDTO.getStatus()));
        }
        // 更新筛查计划中的学校
        screeningPlanSchoolService.updateSchoolNameBySchoolId(schoolId, schoolRequestDTO.getName());
        School newSchool = schoolService.getById(schoolId);
        SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
        BeanUtils.copyProperties(newSchool, schoolResponseDTO);
        schoolResponseDTO.setDistrictName(districtService.getDistrictName(newSchool.getDistrictDetail()));
        schoolResponseDTO.setAddressDetail(districtService.getAddressDetails(newSchool.getProvinceCode(), newSchool.getCityCode(), newSchool.getAreaCode(), newSchool.getTownCode(), newSchool.getAddress()));
        // 判断是否能更新
        schoolResponseDTO.setCanUpdate(newSchool.getGovDeptId().equals(schoolRequestDTO.getGovDeptId()));
        schoolResponseDTO.setStudentCount(schoolRequestDTO.getStudentCount())
                .setScreeningCount(schoolRequestDTO.getScreeningCount())
                .setCreateUser(schoolRequestDTO.getCreateUser());
        return schoolResponseDTO;
    }
}
