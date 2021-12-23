package com.wupol.myopia.business.aggregation.student.service;

import com.wupol.myopia.base.constant.SystemCode;
import com.wupol.myopia.base.constant.UserType;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.domain.dto.SchoolResponseDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.Organization;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

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
    private DistrictService districtService;

    @Resource
    private StudentService studentService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Resource
    private SchoolStudentService schoolStudentService;

    @Resource
    private OauthServiceClient oauthServiceClient;

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
        return responseDTO;
    }

    /**
     * 更新学校
     *
     * @param school 学校实体类
     * @return 学校实体类
     */
    @Transactional(rollbackFor = Exception.class)
    public SchoolResponseDTO updateSchool(School school) {
        Integer schoolId = school.getId();
        if (schoolService.checkSchoolName(school.getName(), schoolId)) {
            throw new BusinessException("学校名称重复，请确认");
        }
        District district = districtService.getById(school.getDistrictId());
        school.setDistrictProvinceCode(Integer.valueOf(String.valueOf(district.getCode()).substring(0, 2)));
        // 设置学校状态
        school.setStatus(school.getCooperationStopStatus());
        schoolService.updateById(school);
        // 同步到oauth机构状态
        oauthServiceClient.updateOrganization(new Organization(school.getId(), SystemCode.SCHOOL_CLIENT,
                UserType.OTHER, school.getStatus()));
        // 更新筛查计划中的学校
        screeningPlanSchoolService.updateSchoolNameBySchoolId(schoolId, school.getName());
        School newSchool = schoolService.getById(schoolId);
        SchoolResponseDTO schoolResponseDTO = new SchoolResponseDTO();
        BeanUtils.copyProperties(newSchool, schoolResponseDTO);
        schoolResponseDTO.setDistrictName(districtService.getDistrictName(newSchool.getDistrictDetail()));
        schoolResponseDTO.setAddressDetail(districtService.getAddressDetails(newSchool.getProvinceCode(), newSchool.getCityCode(), newSchool.getAreaCode(), newSchool.getTownCode(), newSchool.getAddress()));
        // 判断是否能更新
        schoolResponseDTO.setCanUpdate(newSchool.getGovDeptId().equals(school.getGovDeptId()));
        schoolResponseDTO.setStudentCount(school.getStudentCount())
                .setScreeningCount(school.getScreeningCount())
                .setCreateUser(school.getCreateUser());
        return schoolResponseDTO;
    }
}
