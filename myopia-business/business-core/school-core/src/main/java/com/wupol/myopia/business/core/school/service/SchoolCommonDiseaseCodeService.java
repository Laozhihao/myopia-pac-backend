package com.wupol.myopia.business.core.school.service;

import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.core.school.domain.mapper.SchoolCommonDiseaseCodeMapper;
import com.wupol.myopia.business.core.school.domain.model.SchoolCommonDiseaseCode;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
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
     * @param areaDistrictShortCode 区/县行政区域编码（6位）
     * @param schoolId   学校ID
     * @param screeningPlanStartTime 年份
     * @return java.lang.String
     **/
    public String getSchoolCommonDiseaseCode(String areaDistrictShortCode, Integer schoolId, Date screeningPlanStartTime) {
        int year = DateUtil.getSchoolYear(screeningPlanStartTime);
        return getSchoolCommonDiseaseCode(areaDistrictShortCode, schoolId, year);
    }

    /**
     * 获取学校常见病编码
     *
     * @param areaDistrictShortCode 区/县行政区域编码（6位）
     * @param schoolId   学校ID
     * @param year       年份
     * @return java.lang.String
     **/
    public String getSchoolCommonDiseaseCode(String areaDistrictShortCode, Integer schoolId, int year) {
        SchoolCommonDiseaseCode schoolCommonDiseaseCode = findOne(new SchoolCommonDiseaseCode().setAreaDistrictShortCode(areaDistrictShortCode).setSchoolId(schoolId).setYear(year));
        if (Objects.nonNull(schoolCommonDiseaseCode)) {
            return schoolCommonDiseaseCode.getCode();
        }
        return createSchoolCommonDiseaseCode(areaDistrictShortCode, schoolId, year);
    }

    /**
     * 生成学校常见病编码
     *
     * @param areaDistrictShortCode 区/县行政区域编码（6位）
     * @param schoolId   学校ID
     * @param year       年份
     * @return java.lang.String
     **/
    public String createSchoolCommonDiseaseCode(String areaDistrictShortCode, Integer schoolId, int year) {
        SchoolCommonDiseaseCode schoolCommonDiseaseCode = new SchoolCommonDiseaseCode().setAreaDistrictShortCode(areaDistrictShortCode).setYear(year);
        int total = count(schoolCommonDiseaseCode);
        Assert.isTrue(total < 99, "该区域下" + year + "年筛查常见病的学校数量，超过最大限制");
        String code = String.format("%02d", total + 1);
        save(schoolCommonDiseaseCode.setCode(code).setSchoolId(schoolId));
        return code;
    }
}
