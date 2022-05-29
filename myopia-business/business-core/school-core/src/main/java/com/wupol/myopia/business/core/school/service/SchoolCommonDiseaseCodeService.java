package com.wupol.myopia.business.core.school.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolCommonDiseaseCodeMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolCommonDiseaseCode;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

/**
 * 学校常见病编码
 *
 * @Author HaoHao
 * @Date 2022-05-27
 */
@Service
public class SchoolCommonDiseaseCodeService extends BaseService<SchoolCommonDiseaseCodeMapper, SchoolCommonDiseaseCode> {

    /**
     * 获取学校常见病编码
     *
     * @param districtId 行政区域ID
     * @param schoolId   学校ID
     * @param year       年份
     * @return java.lang.String
     **/
    public String getSchoolCommonDiseaseCode(Integer districtId, Integer schoolId, int year) {
        SchoolCommonDiseaseCode schoolCommonDiseaseCode = findOne(new SchoolCommonDiseaseCode().setDistrictId(districtId).setSchoolId(schoolId).setYear(year));
        if (Objects.nonNull(schoolCommonDiseaseCode)) {
            return schoolCommonDiseaseCode.getCode();
        }
        return createSchoolCommonDiseaseCode(districtId, schoolId, year);
    }

    /**
     * 生成学校常见病编码
     *
     * @param districtId 行政区域ID
     * @param schoolId   学校ID
     * @param year       年份
     * @return java.lang.String
     **/
    public String createSchoolCommonDiseaseCode(Integer districtId, Integer schoolId, int year) {
        SchoolCommonDiseaseCode schoolCommonDiseaseCode = new SchoolCommonDiseaseCode().setDistrictId(districtId).setYear(year);
        int total = count(schoolCommonDiseaseCode);
        Assert.isTrue(total < 99, "该区域下" + year + "年筛查常见病的学校数量，超过最大限制");
        String code = String.format("%02d", total + 1);
        save(schoolCommonDiseaseCode.setCode(code).setSchoolId(schoolId));
        return code;
    }
}
