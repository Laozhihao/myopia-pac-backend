package com.wupol.myopia.business.aggregation.export.excel.imports;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.SchoolStudentImportEnum;
import com.wupol.myopia.business.aggregation.export.utils.CommonCheck;
import com.wupol.myopia.business.common.utils.constant.*;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.SchoolClass;
import com.wupol.myopia.business.core.school.domain.model.SchoolGrade;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolClassService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.school.util.SchoolUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 学校端导入学生
 *
 * @author Simple4H
 */
@Service
public class SchoolStudentExcelImportService {

    @Resource
    private SchoolService schoolService;
    @Resource
    private SchoolGradeService schoolGradeService;
    @Resource
    private StudentService studentService;
    @Resource
    private DistrictService districtService;
    @Resource
    private SchoolStudentService schoolStudentService;
    @Resource
    private SchoolClassService schoolClassService;

    private static final String ERROR_MSG = "在系统中重复";
    private static final Integer PASSPORT_LENGTH = 7;
    private static final Integer SNO_LENGTH = 25;

    /**
     * 导入学校学生
     * <p>
     * 这个方法有两个Map，第一个Map是判断数据是否重复，第二个Map是将删除的重新启用
     * </p>
     *
     * @param createUserId  创建人
     * @param multipartFile 文件
     * @param schoolId      学校Id
     */
    @Transactional(rollbackFor = Exception.class)
    public void importSchoolStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) {
        List<Map<Integer, String>> listMap = FileUtils.readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            throw new BusinessException("上传数据为空");
        }
        School school = schoolService.getById(schoolId);

        List<String> snos = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.SNO.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> idCards = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.ID_CARD.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> passports = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.PASSPORT.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());

        // 检查Excel中身份证、学号、护照是否重复
        CommonCheck.checkHaveDuplicate(idCards, snos, passports, true);

        // 获取已经存在的学校学生（判断是否重复）
        List<SchoolStudent> studentList = schoolStudentService.getAllStatusStudentByIdCardAndSnoAndPassport(idCards, snos, passports, schoolId);
        Map<String, SchoolStudent> snoMap = studentList.stream().filter(s -> StringUtils.isNotBlank(s.getSno()) && !CommonConst.STATUS_IS_DELETED.equals(s.getStatus())).collect(Collectors.toMap(SchoolStudent::getSno, Function.identity()));
        Map<String, SchoolStudent> idCardMap = studentList.stream().filter(s -> StringUtils.isNotBlank(s.getIdCard())).collect(Collectors.toMap(s -> StringUtils.upperCase(s.getIdCard()), Function.identity()));
        Map<String, SchoolStudent> passPortMap = studentList.stream().filter(s -> StringUtils.isNotBlank(s.getPassport())).collect(Collectors.toMap(s -> StringUtils.upperCase(s.getPassport()), Function.identity()));

        preHandleGraduateClass(listMap, idCardMap, passPortMap, schoolId, createUserId);

        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMap = schoolGradeService.getGradeAndClassMap(Lists.newArrayList(school.getId()));
        Map<String, Student> studentMap = studentService.getByIdCardsOrPassports(idCards, passports).stream().collect(Collectors.toMap(x -> StringUtils.upperCase(x.getIdCard()) + x.getPassport(), Function.identity()));
        List<SchoolStudent> schoolStudents = new ArrayList<>();
        List<Student> students = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            String sno = item.get(SchoolStudentImportEnum.SNO.getIndex());
            String name = item.get(SchoolStudentImportEnum.NAME.getIndex());
            String idCard = StringUtils.upperCase(item.get(SchoolStudentImportEnum.ID_CARD.getIndex()));
            String gender = item.get(SchoolStudentImportEnum.GENDER.getIndex());
            String birthday = item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex());
            String gradeName = item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex());
            String className = item.get(SchoolStudentImportEnum.CLASS_NAME.getIndex());
            String passport = StringUtils.upperCase(item.get(SchoolStudentImportEnum.PASSPORT.getIndex()));
            String phone = item.get(SchoolStudentImportEnum.PHONE.getIndex());

            // 获取已经存在的学生（含删除的），优先取身份证，再取护照号
            SchoolStudent schoolStudent = idCardMap.getOrDefault(idCard, passPortMap.getOrDefault(passport, new SchoolStudent()));

            // 数据校验
            validateData(snoMap, sno, name, idCard, gender, birthday, gradeName, className, passport, phone, schoolStudent);

            // 设置参数
            setSchoolStudentInfo(createUserId, schoolId, item, schoolStudent, schoolGradeMap, gradeName, className);
            schoolStudents.add(schoolStudent);
            students.add(buildStudent(schoolStudent, studentMap));
        }
        // 更新student表
        studentService.saveOrUpdateBatch(students);
        // 设置studentId
        Map<String, Student> newStudentMap = studentService.getByIdCardsOrPassports(idCards, passports).stream().collect(Collectors.toMap(x -> x.getIdCard() + x.getPassport(), Function.identity()));
        schoolStudents.forEach(schoolStudent -> schoolStudent.setStudentId(newStudentMap.get(schoolStudent.getIdCard() + schoolStudent.getPassport()).getId()));
        // 保存导入数据（新增学生和重新启用删除的学生）
        schoolStudentService.saveOrUpdateBatch(schoolStudents);
    }

    /**
     * 设置学生基本信息
     *
     * @param createUserId  创建人
     * @param schoolId      学校Id
     * @param item          导入信息
     * @param schoolStudent 学校端学生
     */
    private void setSchoolStudentInfo(Integer createUserId, Integer schoolId, Map<Integer, String> item, SchoolStudent schoolStudent, Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMap, String gradeName, String className) {
        schoolStudent.setName(item.get(SchoolStudentImportEnum.NAME.getIndex()))
                .setNation(NationEnum.getCodeByName(item.get(SchoolStudentImportEnum.NATION.getIndex())))
                .setGradeType(GradeCodeEnum.getByName(item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex())).getType())
                .setSno((item.get(SchoolStudentImportEnum.SNO.getIndex())))
                .setIdCard(StringUtils.upperCase(item.get(SchoolStudentImportEnum.ID_CARD.getIndex())))
                .setPassport(item.get(SchoolStudentImportEnum.PASSPORT.getIndex()))
                .setParentPhone(item.get(SchoolStudentImportEnum.PHONE.getIndex()))
                .setCreateUserId(createUserId)
                .setSchoolId(schoolId)
                .setStatus(CommonConst.STATUS_NOT_DELETED);
        schoolStudent.setProvinceCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.PROVINCE_NAME.getIndex()), null));
        schoolStudent.setCityCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.CITY_NAME.getIndex()), schoolStudent.getProvinceCode()));
        schoolStudent.setAreaCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.AREA_NAME.getIndex()), schoolStudent.getCityCode()));
        schoolStudent.setTownCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.TOWN_NAME.getIndex()), schoolStudent.getAreaCode()));
        schoolStudent.setAddress(item.get(SchoolStudentImportEnum.ADDRESS.getIndex()));
        schoolStudent.setUpdateTime(new Date());
        if (StringUtils.isNoneBlank(schoolStudent.getIdCard(), schoolStudent.getPassport())) {
            schoolStudent.setPassport(null);
        }
        if (Objects.nonNull(schoolStudent.getIdCard())) {
            schoolStudent.setBirthday(IdCardUtil.getBirthDay(schoolStudent.getIdCard()));
            schoolStudent.setGender(IdCardUtil.getGender(schoolStudent.getIdCard()));
        } else {
            // 出生日期
            schoolStudent.setBirthday(DateUtil.parse(item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE, DateFormatUtil.FORMAT_ONLY_DATE2));
            // 性别
            schoolStudent.setGender(GenderEnum.getType(item.get(SchoolStudentImportEnum.GENDER.getIndex())));
        }
        TwoTuple<Integer, Integer> gradeClassInfo = getSchoolStudentClassInfo(schoolId, schoolGradeMap, gradeName, className);
        schoolStudent.setGradeId(gradeClassInfo.getFirst());
        schoolStudent.setGradeName(gradeName);
        schoolStudent.setClassId(gradeClassInfo.getSecond());
        schoolStudent.setClassName(className);
        GradeCodeEnum gradeCodeEnum = GradeCodeEnum.getByName(gradeName);
        schoolStudent.setParticularYear(SchoolUtil.getParticularYear(gradeCodeEnum.getCode()));
        if (Objects.isNull(schoolStudent.getId())) {
            schoolStudent.setSourceClient(SourceClientEnum.SCHOOL.type);
        }
    }

    /**
     * 设置学生年级班级信息
     *
     * @param schoolId        学校Id
     * @param schoolGradeMaps 年级Map
     */
    public TwoTuple<Integer, Integer> getSchoolStudentClassInfo(Integer schoolId, Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps,
                                                                String gradeName, String className) {
        // 通过学校编号获取改学校的年级信息
        List<SchoolGradeExportDTO> schoolGradeExportList = schoolGradeMaps.get(schoolId);
        // 转换成年级Maps，年级名称作为Key
        Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportList.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
        // 年级信息
        SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(gradeName);
        Assert.notNull(schoolGradeExportDTO, "不存在该年级：" + gradeName);

        // 获取年级内的班级信息
        List<SchoolClassExportDTO> schoolClassExportList = schoolGradeExportDTO.getChild();
        if (CollUtil.isEmpty(schoolClassExportList)) {
            throw new BusinessException(gradeName + "不存在班级");
        }
        // 转换成班级Maps 把班级名称作为key
        Map<String, Integer> classExportMaps = schoolClassExportList.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
        Integer classId = classExportMaps.get(className);
        Integer gradeId = schoolGradeExportDTO.getId();
        Assert.notNull(classId, "不存在该班级:" + className);
        return new TwoTuple<>(gradeId, classId);
    }

    /**
     * 校验数据
     *
     * @param snoMap      学号Map
     * @param sno         学号
     * @param name        姓名
     * @param idCard      身份证
     * @param gender      性别
     * @param birthday    出生日期
     * @param gradeName   年级名称
     * @param className   班级名称
     * @param passport    护照信息
     * @param phone       手机号码
     * @param schoolStudent  通过证件获取的学生
     */
    private void validateData(Map<String, SchoolStudent> snoMap, String sno, String name, String idCard,
                              String gender, String birthday, String gradeName, String className, String passport, String phone,
                              SchoolStudent schoolStudent) {
        // 学籍号
        if (StringUtils.isBlank(sno) || sno.length() > SNO_LENGTH) {
            throw new BusinessException(name + "学籍号为空或超过长度限制");
        }
        // 姓名
        Assert.hasText(name, "学籍号为" + sno + "学生，姓名为空");
        // 身份证号码
        if (StringUtils.isAllBlank(idCard, passport) || StringUtils.isNoneBlank(idCard, passport)) {
            throw new BusinessException("学籍号为" + sno + "学生，身份证或护照不能为空，且二选一");
        }
        if (StringUtils.isNotBlank(idCard) && !IdcardUtil.isValidCard(idCard)) {
            throw new BusinessException("身份证号码错误：" + idCard);
        }
        if (StringUtils.isBlank(idCard)) {
            // 性别
            Assert.hasText(gender, "学籍号为" + sno + "的学生，性别为空");
            Assert.isTrue(GenderEnum.isGenderDesc(gender), "学籍号为" + sno + "的学生，性别描述错误");
            // 出生日期
            Assert.hasText(birthday, "学籍号为" + sno + "的学生，出生日期为空");
            DateUtil.checkBirthday(DateUtil.parse(birthday, DateFormatUtil.FORMAT_ONLY_DATE, DateFormatUtil.FORMAT_ONLY_DATE2));
        }
        // 年级
        Assert.hasText(gradeName, "学籍号为" + sno + "学生，年级为空");
        // 班级
        Assert.hasText(className, "学籍号为" + sno + "学生，班级为空");
        // 护照
        if (StringUtils.isNotBlank(passport) && passport.length() < PASSPORT_LENGTH) {
            throw new BusinessException("护照" + passport + "长度错误，需要" + PASSPORT_LENGTH + "个以上字符");
        }
        // 手机号码
        if (StringUtils.isNotBlank(phone) && !PhoneUtil.isPhone(phone)) {
            throw new BusinessException("学籍号为" + sno + "学生，手机号码为空或格式错误");
        }
        // 校验学籍号是否被占用（得放在身份证和护照号校验后面）
        validateSno(snoMap, sno, passport, idCard, schoolStudent);
    }

    /**
     * 校验学籍号是否被占用
     *
     * @param snoMap        系统学籍号Map
     * @param sno           当前学生学籍号
     * @param passport      当前学生护照号
     * @param idCard        当前学生身份证号
     * @param credentialSchoolStudent  通过证件获取的学生
     */
    private void validateSno(Map<String, SchoolStudent> snoMap, String sno, String passport, String idCard, SchoolStudent credentialSchoolStudent) {
        SchoolStudent schoolStudent = snoMap.get(sno);
        if (Objects.isNull(schoolStudent)) {
            return;
        }

        if (Objects.nonNull(credentialSchoolStudent) && (StringUtils.equals(sno, credentialSchoolStudent.getSno()))) {
            return;
        }

        Assert.isTrue((StringUtils.isNotBlank(idCard) && StringUtils.equals(idCard, schoolStudent.getIdCard())) ||
                (StringUtils.isNotBlank(passport) && StringUtils.equals(passport, schoolStudent.getPassport())), "学籍号" + sno + ERROR_MSG);
    }

    /**
     * 更新管理端的学生信息
     *
     * @param schoolStudent 学校端学生
     * @return 管理端学生
     */
    public Integer updateManagementStudent(SchoolStudent schoolStudent) {
        // 通过身份证在管理端查找学生
        Student managementStudent = studentService.getByIdCardAndPassport(schoolStudent.getIdCard(), schoolStudent.getPassport());
        // 如果为空新增，否则是更新
        if (Objects.isNull(managementStudent)) {
            Student student = new Student();
            BeanUtils.copyProperties(schoolStudent, student);
            studentService.saveStudent(student);
            return studentService.getByIdCardAndPassport(schoolStudent.getIdCard(), schoolStudent.getPassport()).getId();
        }
        buildStudent(managementStudent, schoolStudent);
        studentService.updateStudent(managementStudent);
        return managementStudent.getId();
    }

    /**
     * 构建管理端学生
     *
     * @param schoolStudent 学校学生
     * @param studentMap    管理端学生Map
     * @return 管理端学生
     */
    private Student buildStudent(SchoolStudent schoolStudent, Map<String, Student> studentMap) {
        // 通过身份证在管理端查找学生
        Student student = studentMap.get(schoolStudent.getIdCard() + schoolStudent.getPassport());
        // 如果为空新增，否则是更新
        if (Objects.isNull(student)) {
            student = new Student();
            BeanUtils.copyProperties(schoolStudent, student);
            return student.setId(null).setSourceClient(SourceClientEnum.SCHOOL.getType());
        }
        return buildStudent(student, schoolStudent);
    }

    /**
     * 构建管理端学生
     *
     * @param student       管理端学生
     * @param schoolStudent 学校学生
     * @return 管理端学生
     */
    private Student buildStudent(Student student, SchoolStudent schoolStudent) {
        student.setSchoolId(schoolStudent.getSchoolId());
        student.setSno(schoolStudent.getSno());
        student.setName(schoolStudent.getName());
        student.setGender(schoolStudent.getGender());
        student.setClassId(schoolStudent.getClassId());
        student.setGradeId(schoolStudent.getGradeId());
        student.setIdCard(schoolStudent.getIdCard());
        student.setBirthday(schoolStudent.getBirthday());
        student.setNation(schoolStudent.getNation());
        student.setParentPhone(schoolStudent.getParentPhone());
        student.setProvinceCode(schoolStudent.getProvinceCode());
        student.setCityCode(schoolStudent.getCityCode());
        student.setAreaCode(schoolStudent.getAreaCode());
        student.setTownCode(schoolStudent.getTownCode());
        student.setAddress(schoolStudent.getAddress());
        student.setPassport(schoolStudent.getPassport());
        student.setStatus(CommonConst.STATUS_NOT_DELETED);
        student.setUpdateTime(new Date());
        student.setGradeType(schoolStudent.getGradeType());
        return student;
    }

    /**
     * 预处理毕业班级年级
     */
    private void preHandleGraduateClass(List<Map<Integer, String>> listMap, Map<String, SchoolStudent> idCardMap, Map<String, SchoolStudent> passPortMap,
                                        Integer schoolId, Integer userId) {
        SchoolGrade schoolGrade = schoolGradeService.getByGradeCodeAndSchoolId(schoolId, GradeCodeEnum.GRADUATE.getCode());
        List<SchoolClassDTO> schoolClassList = schoolClassService.getVoBySchoolId(schoolId);
        Map<Integer, String> classMap = schoolClassList.stream()
                .collect(Collectors.toMap(SchoolClass::getId, SchoolClass::getName));

        if (Objects.isNull(schoolGrade)) {
            schoolGrade = new SchoolGrade();
            schoolGrade.setCreateUserId(userId);
            schoolGrade.setSchoolId(schoolId);
            schoolGrade.setGradeCode(GradeCodeEnum.GRADUATE.getCode());
            schoolGrade.setName(GradeCodeEnum.GRADUATE.getName());
            schoolGradeService.save(schoolGrade);
        }

        int currentYear = DateUtil.year(new Date());
        List<String> classNameList = new ArrayList<>();

        for (Map<Integer, String> item : listMap) {
            String gradeName = item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex());
            if (StringUtils.equals(gradeName, GradeCodeEnum.GRADUATE.getName())) {
                String idCard = item.get(SchoolStudentImportEnum.ID_CARD.getIndex());
                String passport = item.get(SchoolStudentImportEnum.PASSPORT.getIndex());
                SchoolStudent schoolStudent = idCardMap.getOrDefault(idCard, passPortMap.get(passport));
                String className;
                if (Objects.isNull(schoolStudent)) {
                    className = item.get(SchoolStudentImportEnum.CLASS_NAME.getIndex());
                } else {
                    // 如果学生的年级已经是毕业，则不处理
                    if (Objects.equals(schoolStudent.getGradeType(), SchoolAge.GRADUATE.getCode())) {
                        item.put(SchoolStudentImportEnum.CLASS_NAME.getIndex(), schoolStudent.getClassName());
                        continue;
                    } else {
                        className = currentYear + StrUtil.DASHED + classMap.get(schoolStudent.getClassId());
                    }
                }
                classNameList.add(className);
                item.put(SchoolStudentImportEnum.CLASS_NAME.getIndex(), className);
            }
        }

        SchoolGrade finalSchoolGrade = schoolGrade;
        List<SchoolClassDTO> graduateClass = schoolClassList.stream()
                .filter(s -> Objects.equals(s.getGradeId(), finalSchoolGrade.getId()))
                .collect(Collectors.toList());
        Map<String, Long> graduateClassCountMap = graduateClass.stream()
                .collect(Collectors.groupingBy(SchoolClass::getName, Collectors.counting()));

        List<SchoolClass> saveSchoolClassList = new ArrayList<>();
        for (String className : classNameList) {
            if (graduateClassCountMap.getOrDefault(className, 0L) == 0) {
                SchoolClass schoolClass = new SchoolClass();
                schoolClass.setGradeId(schoolGrade.getId());
                schoolClass.setCreateUserId(userId);
                schoolClass.setSchoolId(schoolId);
                schoolClass.setName(className);
                saveSchoolClassList.add(schoolClass);
            }
        }
        schoolClassService.saveBatch(saveSchoolClassList);
    }
}
