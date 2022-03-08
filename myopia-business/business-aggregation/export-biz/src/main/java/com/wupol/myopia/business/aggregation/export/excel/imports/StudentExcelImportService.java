package com.wupol.myopia.business.aggregation.export.excel.imports;

import cn.hutool.core.util.IdcardUtil;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.base.util.RegularUtils;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.constant.SourceClientEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
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
import org.springframework.transaction.annotation.Transactional;
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
    private CommonImportService commonImportService;


    /**
     * 导入学生
     *
     * @param createUserId  创建人userID
     * @param multipartFile 导入文件
     */
    @Transactional(rollbackFor = Exception.class)
    public void importStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) {
        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);
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

        // 收集护照
        List<String> passports = listMap.stream().map(s -> s.get(9 - offset)).filter(Objects::nonNull).peek(passport -> {
            if (passport.length() < 7) {
                throw new BusinessException("护照" + passport + "异常");
            }
        }).collect(Collectors.toList());

        // 数据预校验
        preCheckStudent(schools, idCards);

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(schools.stream().map(School::getId).collect(Collectors.toList()));
        schoolGradeService.packageGradeInfo(grades);

        // 通过学校编号分组
        Map<String, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolNo));

        // 通过身份证获取学生
        Map<String, Student> studentMap = new HashMap<>();
        Map<String, Student> passportMap = new HashMap<>();

        // 通过护照获取学生
        Map<String, Integer> deletedIdCardMap = new HashMap<>();
        Map<String, Integer> deletedPassportMap = new HashMap<>();

        if (!CollectionUtils.isEmpty(idCards)) {
            studentMap = studentService.getByIdCardsAndStatus(idCards).stream().collect(Collectors.toMap(Student::getIdCard, Function.identity()));

            // 通过身份证获取已经删除的学生
            List<Student> deleteIdCardStudent = studentService.getDeleteStudentByIdCard(idCards);
            deletedIdCardMap = deleteIdCardStudent.stream().collect(Collectors.toMap(Student::getIdCard, Student::getId));
        }
        if (!CollectionUtils.isEmpty(passports)) {
            passportMap = studentService.getByPassportAndStatus(passports).stream().collect(Collectors.toMap(Student::getPassport, Function.identity()));

            // 通过护照获取已经删除的学生
            List<Student> deletePassportStudent = studentService.getDeletedByPassportAndStatus(passports);
            deletedPassportMap = deletePassportStudent.stream().collect(Collectors.toMap(Student::getPassport, Student::getId));
        }

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
            String passport = item.get(9 - offset);
            if (Objects.nonNull(idCard) && Objects.nonNull(studentMap.get(idCard))) {
                throw new BusinessException("身份证" + idCard + "在系统中重复");
            }
            if (Objects.nonNull(passport) && Objects.nonNull(passportMap.get(passport))) {
                throw new BusinessException("护照" + passport + "在系统中重复");
            }
            setStudentInfo(createUserId, offset, item, student, idCard, passport);
            setStudentSchoolInfo(schoolId, isSameSchool, offset, schoolNo, schoolMap, schoolGradeMaps, item, student);
            student.checkStudentInfo();
            DateUtil.checkBirthday(student.getBirthday());
            importList.add(student);
        }
        // 将删除的学生重新启用
        for (Student student : importList) {
            if (Objects.nonNull(student.getIdCard()) && Objects.nonNull(deletedIdCardMap.get(student.getIdCard()))) {
                student.setId(deletedIdCardMap.get(student.getIdCard()));
                student.setStatus(CommonConst.STATUS_NOT_DELETED);
            }
            if (Objects.nonNull(student.getPassport()) && Objects.nonNull(deletedPassportMap.get(student.getPassport()))) {
                student.setId(deletedPassportMap.get(student.getPassport()));
                student.setStatus(CommonConst.STATUS_NOT_DELETED);
            }
        }
        studentService.saveOrUpdateBatch(importList);
        // 插入学校端
        commonImportService.insertSchoolStudent(importList, SourceClientEnum.MANAGEMENT.type);
    }

    /**
     * 设置学生基本信息
     *
     * @param createUserId 创建人
     * @param offset       偏移量
     * @param item         导入信息
     * @param student      学生
     * @param idCard       身份证
     */
    private void setStudentInfo(Integer createUserId, int offset, Map<Integer, String> item, Student student, String idCard, String passport){
        student.setName(item.get(0))
                .setGender(Objects.nonNull(item.get(1)) ? GenderEnum.getType(item.get(1)) : IdCardUtil.getGender(idCard))

                .setNation(NationEnum.getCode(item.get(3))).setGradeType(GradeCodeEnum.getByName(item.get(5 - offset)).getType())
                .setSno((item.get(7 - offset)))
                .setIdCard(idCard)
                .setParentPhone(item.get(10 - offset))
                .setCreateUserId(createUserId)
                .setPassport(passport);
        student.setProvinceCode(districtService.getCodeByName(item.get(11 - offset)));
        student.setCityCode(districtService.getCodeByName(item.get(12 - offset)));
        student.setAreaCode(districtService.getCodeByName(item.get(13 - offset)));
        student.setTownCode(districtService.getCodeByName(item.get(14 - offset)));
        student.setAddress(item.get(15 - offset));
        if (StringUtils.isNoneBlank(idCard, passport)) {
            student.setPassport(null);
        }
        try {
            student.setBirthday(Objects.nonNull(item.get(2)) ? DateFormatUtil.parseDate(item.get(2), DateFormatUtil.FORMAT_ONLY_DATE2) : IdCardUtil.getBirthDay(idCard));
        } catch (ParseException e) {
            throw new BusinessException("生日格式异常");
        }
    }

    /**
     * 学生学生学校信息
     *
     * @param schoolId        学校Id
     * @param isSameSchool    是否相同学校
     * @param offset          偏移量
     * @param schoolNo        学校编号
     * @param schoolMap       学校Map
     * @param schoolGradeMaps 班级Map
     * @param item            导入信息
     * @param student         学生
     */
    private void setStudentSchoolInfo(Integer schoolId, boolean isSameSchool, int offset, String schoolNo, Map<String, Integer> schoolMap, Map<String, List<SchoolGradeExportDTO>> schoolGradeMaps, Map<Integer, String> item, Student student) {
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
    }


    /**
     * 前置校验
     *
     * @param schools 学校列表
     * @param idCards 身份证信息
     */
    private void preCheckStudent(List<School> schools, List<String> idCards) {
        Assert.isTrue(!CollectionUtils.isEmpty(schools), "学校编号异常");

        if (CollectionUtils.isEmpty(idCards)) {
            return;
        }
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
        Assert.isTrue(StringUtils.isBlank(item.get(8 - offset)) || (StringUtils.isNotBlank(item.get(8 - offset)) && Pattern.matches(RegularUtils.REGULAR_ID_CARD, item.get(8 - offset))), "学生身份证" + item.get(8 - offset) + "异常");
        Assert.isTrue(StringUtils.isBlank(item.get(10 - offset)) || Pattern.matches(RegularUtils.REGULAR_MOBILE, item.get(10 - offset)), "学生手机号码" + item.get(10 - offset) + "异常");
    }
}
