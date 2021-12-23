package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.cache.RedisConstant;
import com.wupol.myopia.base.constant.CooperationTimeTypeEnum;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelFileNameConstant;
import com.wupol.myopia.business.aggregation.export.excel.constant.ExcelNoticeKeyContentConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.hospital.domain.model.Hospital;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalAdmin;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.*;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolAdmin;
import com.wupol.myopia.business.core.school.service.SchoolAdminService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.oauth.sdk.client.OauthServiceClient;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * 导出学校
 *
 * @author Simple4H
 */
@Service("schoolExcelService")
public class ExportSchoolExcelService extends BaseExportExcelFileService {

    @Resource
    private DistrictService districtService;

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private SchoolAdminService schoolAdminService;

    @Resource
    private OauthServiceClient oauthServiceClient;

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        SchoolQueryDTO query = new SchoolQueryDTO();
        query.setDistrictId(exportCondition.getDistrictId());
        List<School> list = schoolService.getBy(query);

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Integer> schoolIds = list.stream().map(School::getId).collect(Collectors.toList());

        // 年级统计
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schoolIds);
        schoolGradeService.packageGradeInfo(grades);

        // 年级通过学校ID分组
        Map<Integer, List<SchoolGradeExportDTO>> gradeMaps = grades.stream()
                .collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        List<SchoolAdmin> schoolAdminList = schoolAdminService.getBySchoolIds(schoolIds);
        Map<Integer, List<Integer>> adminMap = schoolAdminList.stream().collect(Collectors.groupingBy(SchoolAdmin::getSchoolId, Collectors.mapping(SchoolAdmin::getUserId, Collectors.toList())));

        List<Integer> userIds = schoolAdminList.stream().map(SchoolAdmin::getUserId).collect(Collectors.toList());
        List<User> userLists = oauthServiceClient.getUserBatchByIds(userIds);
        Map<Integer, String> userMap = userLists.stream().collect(Collectors.toMap(User::getId, User::getUsername));

        List<SchoolExportDTO> exportList = new ArrayList<>();
        for (School item : list) {
            AtomicReference<String> account = new AtomicReference<>(StringUtils.EMPTY);
            adminMap.get(item.getId()).forEach(s -> account.set(account + userMap.get(s) + "、"));
            account.set(account.get().substring(0, account.get().length() - 1));

            SchoolExportDTO exportVo = new SchoolExportDTO()
                    .setNo(item.getSchoolNo())
                    .setName(item.getName())
                    .setKind(SchoolEnum.getKindName(item.getKind()))
                    .setAddress(districtService.getAddressDetails(item.getProvinceCode(), item.getCityCode(), item.getAreaCode(), item.getTownCode(), item.getAddress()))
                    .setType(SchoolEnum.getTypeName(item.getType()))
                    .setRemark(item.getRemark())
                    .setAccount(account.get())
                    .setCooperationType(CooperationTimeTypeEnum.getCooperationTimeTypeDesc(item.getCooperationType(), item.getCooperationTimeType(), item.getCooperationStartTime(), item.getCooperationEndTime()))
                    .setCooperationRemainTime(item.getCooperationRemainTime())
                    .setCooperationStartTime(Objects.nonNull(item.getCooperationStartTime()) ? DateFormatUtil.format(item.getCooperationStartTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                    .setCooperationEndTime(Objects.nonNull(item.getCooperationEndTime()) ? DateFormatUtil.format(item.getCooperationEndTime(), DateFormatUtil.FORMAT_TIME_WITHOUT_SECOND) : StringUtils.EMPTY)
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME));
            StringBuilder result = new StringBuilder();
            List<SchoolGradeExportDTO> exportGrade = gradeMaps.get(item.getId());
            if (!CollectionUtils.isEmpty(exportGrade)) {
                getSchoolGradeAndClass(result, exportGrade);
                exportVo.setClassName(result.toString());
            }
            if (Objects.nonNull(item.getLodgeStatus())) {
                exportVo.setLodgeStatus(SchoolEnum.getLodgeName(item.getLodgeStatus()));
            }
            exportList.add(exportVo);
        }
        return exportList;
    }

    @Override
    public Class getHeadClass() {
        return SchoolExportDTO.class;
    }

    @Override
    public String getNoticeKeyContent(ExportCondition exportCondition) {
        District district = districtService.checkAndGetDistrict(exportCondition.getDistrictId());
        return String.format(ExcelNoticeKeyContentConstant.SCHOOL_EXCEL_NOTICE_KEY_CONTENT, districtService.getTopDistrictName(district.getCode()));
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.checkAndGetDistrict(exportCondition.getDistrictId());
        // 设置文件名
        return ExcelFileNameConstant.SCHOOL_FILE_NAME + district.getName();
    }

    /**
     * 获取当前学校下的年级和班级
     *
     * @param result      结果
     * @param exportGrade 年级和班级信息
     */
    private void getSchoolGradeAndClass(StringBuilder result, List<SchoolGradeExportDTO> exportGrade) {
        exportGrade.forEach(grade -> {
            result.append(grade.getName()).append(": ");
            List<SchoolClassExportDTO> child = grade.getChild();
            if(Objects.isNull(child) || CollectionUtils.isEmpty(child)) {
                return;
            }
            for (int i = 0; i < child.size(); i++) {
                result.append(child.get(i).getName());
                if (i < child.size() - 1) {
                    result.append("、");
                } else {
                    result.append("。");
                }
            }
        });
    }

    @Override
    public String getLockKey(ExportCondition exportCondition) {
        return String.format(RedisConstant.FILE_EXPORT_EXCEL_SCHOOL,
                exportCondition.getApplyExportFileUserId(),
                exportCondition.getDistrictId());
    }
}
