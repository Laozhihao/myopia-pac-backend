package com.wupol.myopia.business.aggregation.export.excel;

import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.pdf.constant.FileNameConstant;
import com.wupol.myopia.business.aggregation.export.pdf.domain.ExportCondition;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.common.constant.ExportAddressKey;
import com.wupol.myopia.business.core.common.domain.model.District;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.SchoolEnum;
import com.wupol.myopia.business.core.school.domain.dto.*;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchool;
import com.wupol.myopia.business.core.screening.flow.service.ScreeningPlanSchoolService;
import com.wupol.myopia.oauth.sdk.domain.response.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
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
    private StudentService studentService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private ScreeningPlanSchoolService screeningPlanSchoolService;

    @Override
    public List getExcelData(ExportCondition exportCondition) {

        SchoolQueryDTO query = new SchoolQueryDTO();
        query.setDistrictId(exportCondition.getDistrictId());
        List<School> list = schoolService.getBy(query);

        if (CollectionUtils.isEmpty(list)) {
            return new ArrayList<>();
        }

        List<Integer> schoolIds = list.stream().map(School::getId).collect(Collectors.toList());
        Set<Integer> createUserIds = list.stream().map(School::getCreateUserId).collect(Collectors.toSet());

        // 创建人姓名
        Map<Integer, User> userMap = getUserMapByIds(createUserIds);

        // 学生统计
        List<StudentCountDTO> studentCountVOS = studentService.countStudentBySchoolNo();
        Map<String, Integer> studentCountMaps = studentCountVOS.stream()
                .collect(Collectors.toMap(StudentCountDTO::getSchoolNo, StudentCountDTO::getCount));

        // 年级统计
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schoolIds);
        schoolGradeService.packageGradeInfo(grades);

        // 年级通过学校ID分组
        Map<Integer, List<SchoolGradeExportDTO>> gradeMaps = grades.stream()
                .collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        // 学校筛查次数
        List<ScreeningPlanSchool> planSchoolList = screeningPlanSchoolService.getBySchoolIds(schoolIds);
        Map<Integer, Long> planSchoolMaps = planSchoolList.stream()
                .collect(Collectors.groupingBy(ScreeningPlanSchool::getSchoolId, Collectors.counting()));

        List<SchoolExportDTO> exportList = new ArrayList<>();
        for (School item : list) {
            HashMap<String, String> addressMap = generateAddressMap(item);
            SchoolExportDTO exportVo = new SchoolExportDTO()
                    .setNo(item.getSchoolNo())
                    .setName(item.getName())
                    .setKind(SchoolEnum.getKindName(item.getKind()))
                    .setType(SchoolEnum.getTypeName(item.getType()))
                    .setStudentCount(studentCountMaps.getOrDefault(item.getSchoolNo(), 0))
                    .setDistrictName(districtService.getDistrictName(item.getDistrictDetail()))
                    .setAddress(item.getAddress())
                    .setRemark(item.getRemark())
                    .setScreeningCount(planSchoolMaps.getOrDefault(item.getId(), 0L))
                    .setCreateUser(userMap.get(item.getCreateUserId()).getRealName())
                    .setCreateTime(DateFormatUtil.format(item.getCreateTime(), DateFormatUtil.FORMAT_DETAIL_TIME))
                    .setProvince(addressMap.getOrDefault(ExportAddressKey.PROVIDE, StringUtils.EMPTY))
                    .setCity(addressMap.getOrDefault(ExportAddressKey.CITY, StringUtils.EMPTY))
                    .setArea(addressMap.getOrDefault(ExportAddressKey.AREA, StringUtils.EMPTY))
                    .setTown(addressMap.getOrDefault(ExportAddressKey.TOWN, StringUtils.EMPTY));
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
        return String.format(CommonConst.EXPORT_MESSAGE_CONTENT_SUCCESS,
                districtService.getTopDistrictName(district.getCode()) + FileNameConstant.SCHOOL_EXCEL_FILE_NAME,
                new Date());
    }

    @Override
    public String getFileName(ExportCondition exportCondition) {
        District district = districtService.checkAndGetDistrict(exportCondition.getDistrictId());
        // 设置文件名
        return "学校" + "-" + district.getName();
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
}
