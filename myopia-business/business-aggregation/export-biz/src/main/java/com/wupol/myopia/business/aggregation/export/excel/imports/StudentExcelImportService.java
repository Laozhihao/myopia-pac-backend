package com.wupol.myopia.business.aggregation.export.excel.imports;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 导入学生
 *
 * @author Simple4H
 */
@Service
public class StudentExcelImportService {

    @Resource
    private SchoolService schoolService;

    @Resource
    private SchoolGradeService schoolGradeService;

    @Resource
    private StudentService studentService;

    @Resource
    private DistrictService districtService;

    @Resource
    private ExcelFacade excelFacade;


    /**
     * 导入学生
     *
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     * @throws BusinessException 异常
     */
    public void importStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) throws ParseException {
        List<Map<Integer, String>> listMap = excelFacade.readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }

        // 判断是否导入到同一个学校(同个学校时没有"学校编号"列，第5列，index=4)
        boolean isSameSchool = Objects.nonNull(schoolId);
        int offset = isSameSchool ? 1 : 0;

        // 收集学校编号
        List<School> schools;
        String schoolNo = null;
        if (isSameSchool) {
            School school = schoolService.getById(schoolId);
            schoolNo = school.getSchoolNo();
            schools = Collections.singletonList(schoolService.getById(schoolId));
        } else {
            List<String> schoolNos = listMap.stream().map(s -> s.get(4)).collect(Collectors.toList());
            schools = schoolService.getBySchoolNos(schoolNos);
        }
        Map<String, Integer> schoolMap = schools.stream().collect(Collectors.toMap(School::getSchoolNo, School::getId));

        // 收集身份证号码
        List<String> idCards = listMap.stream().map(s -> s.get(8 - offset)).filter(Objects::nonNull).collect(Collectors.toList());

        // 数据预校验
        preCheckStudent(schools, idCards);

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schools.stream().map(School::getId).collect(Collectors.toList()));
        schoolGradeService.packageGradeInfo(grades);

        // 通过学校编号分组
        Map<String, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolNo));

        // 通过身份证获取学生
        Map<String, Student> studentMap = studentService.getByIdCardsAndStatus(idCards).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));

        List<Student> importList = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            Student student = new Student();
            if (StringUtils.isBlank(item.get(0))) {
                break;
            }
            checkStudentInfo(item, offset);
            // Excel 格式： 姓名	性别	出生日期	民族   学校编号(同个学校时没有该列，后面的左移一列)   年级	班级	学号	身份证号	手机号码	省	市	县区	镇/街道	详细
            // 民族取值：1-汉族  2-蒙古族  3-藏族  4-壮族  5-回族  6-其他
            String idCard = item.get(8 - offset);
            if (Objects.nonNull(studentMap.get(idCard))) {
                throw new BusinessException("身份证" + idCard + "在系统中重复");
            }
            student.setName(item.get(0))
                    .setGender(Objects.nonNull(item.get(1)) ? GenderEnum.getType(item.get(1)) : IdCardUtil.getGender(idCard))
                    .setBirthday(Objects.nonNull(item.get(2)) ? DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2) : IdCardUtil.getBirthDay(idCard))
                    .setNation(NationEnum.getCode(item.get(3))).setGradeType(GradeCodeEnum.getByName(item.get(5 - offset)).getType())
                    .setSno((item.get(7 - offset)))
                    .setIdCard(idCard)
                    .setParentPhone(item.get(9 - offset))
                    .setCreateUserId(createUserId);
            student.setProvinceCode(districtService.getCodeByName(item.get(10 - offset)));
            student.setCityCode(districtService.getCodeByName(item.get(11 - offset)));
            student.setAreaCode(districtService.getCodeByName(item.get(12 - offset)));
            student.setTownCode(districtService.getCodeByName(item.get(13 - offset)));
            student.setAddress(item.get(14 - offset));
            // 通过学校编号获取改学校的年级信息
            List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(isSameSchool ? schoolNo : item.get(4));
            // 转换成年级Maps，年级名称作为Key
            Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
            // 年级信息
            SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(item.get(5 - offset));
            Assert.notNull(schoolGradeExportDTO, "年级数据异常");
            // 设置年级ID
            student.setGradeId(schoolGradeExportDTO.getId());
            // 获取年级内的班级信息
            List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
            // 转换成班级Maps 把班级名称作为key
            Map<String, Integer> classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
            Integer classId = classExportMaps.get(item.get(6 - offset));
            Assert.notNull(classId, "班级数据为空");
            // 设置班级信息
            student.setClassId(classId);
            if (isSameSchool) {
                student.setSchoolId(schoolId);
            } else {
                student.setSchoolId(schoolMap.get((item.get(4 - offset))));
            }
            importList.add(student);
        }
        // 通过身份证获取已经删除的学生
        List<Student> deleteStudent = studentService.getDeleteStudentByIdCard(idCards);
        Map<String, Integer> deletedMap = deleteStudent.stream().collect(Collectors.toMap(Student::getIdCard, Student::getId));
        importList.forEach(student -> {
            if (Objects.nonNull(deletedMap.get(student.getIdCard()))) {
                student.setId(deletedMap.get(student.getIdCard()));
                student.setStatus(CommonConst.STATUS_NOT_DELETED);
            }

        });
        studentService.saveOrUpdateBatch(importList);
    }

    /**
     * 前置校验
     *
     * @param schools 学校列表
     * @param idCards 身份证信息
     */
    private void preCheckStudent(List<School> schools, List<String> idCards) {
        Assert.isTrue(!CollectionUtils.isEmpty(schools), "学校编号异常");

        List<String> notLegalIdCards = new ArrayList<>();
        idCards.forEach(s -> {
            if (!IdcardUtil.isValidCard(s)) {
                notLegalIdCards.add(s);
            }
        });
        if (!com.wupol.framework.core.util.CollectionUtils.isEmpty(notLegalIdCards)) {
            throw new BusinessException("身份证格式错误：" + notLegalIdCards);
        }

        List<String> duplicateElements = ListUtil.getDuplicateElements(idCards);
        if (!CollectionUtils.isEmpty(duplicateElements)) {
            throw new BusinessException("身份证" + StringUtils.join(duplicateElements, ",") + "重复");
        }

        List<String> repeatIdCard = idCards.stream().filter(s -> StringUtils.isNotBlank(s) && !Pattern.matches(RegularUtils.REGULAR_ID_CARD, s)).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(repeatIdCard)) {
            throw new BusinessException("身份证" + StringUtils.join(repeatIdCard, ",") + "错误");
        }
    }

    /**
     * 检查学生信息是否完整
     *
     * @param item   学生信息
     * @param offset 偏移量(导入的为同一个学校的数据时，没有学校编号列，后面的左移一列)
     */
    private void checkStudentInfo(Map<Integer, String> item, int offset) {
        if (offset > 0) {
            Assert.isTrue(StringUtils.isNotBlank(item.get(4)), "学校编号不能为空");
        }
        Assert.isTrue(StringUtils.isNotBlank(item.get(5 - offset)), "学生年级不能为空");
        Assert.isTrue(StringUtils.isNotBlank(item.get(6 - offset)), "学生班级不能为空");
        Assert.isTrue(StringUtils.isNotBlank(item.get(8 - offset)) && Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8 - offset)), "学生身份证" + item.get(8 - offset) + "异常");
        Assert.isTrue(StringUtils.isBlank(item.get(9 - offset)) || Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(9 - offset)), "学生手机号码" + item.get(9 - offset) + "异常");
    }
}
