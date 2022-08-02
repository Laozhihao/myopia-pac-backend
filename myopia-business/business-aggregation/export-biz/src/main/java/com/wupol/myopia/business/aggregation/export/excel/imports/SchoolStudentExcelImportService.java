package com.wupol.myopia.business.aggregation.export.excel.imports;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.SchoolStudentImportEnum;
import com.wupol.myopia.business.aggregation.export.utils.CommonCheck;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.FileUtils;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.common.service.DistrictService;
import com.wupol.myopia.business.core.school.constant.GradeCodeEnum;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.domain.model.Student;
import com.wupol.myopia.business.core.school.management.domain.model.SchoolStudent;
import com.wupol.myopia.business.core.school.management.service.SchoolStudentService;
import com.wupol.myopia.business.core.school.service.SchoolGradeService;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.school.service.StudentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.text.ParseException;
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

    private static final String ERROR_MSG = "在系统中重复";
    private static final Integer PASSPORT_LENGTH = 7;

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
            return;
        }
        School school = schoolService.getById(schoolId);

        // 收集身份证号码、学号
        List<String> idCards = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.ID_CARD.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> snos = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.SNO.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());


        //处理护照异常
        List<String> errorList = listMap.stream()
                .map(s -> s.get(SchoolStudentImportEnum.PASSPORT.getIndex()))
                .filter(Objects::nonNull)
                .filter(passport -> passport.length() < PASSPORT_LENGTH)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(errorList)){
            throw new BusinessException(String.format("护照异常:%s",errorList));
        }

        //护照正常的
        List<String> passports = listMap.stream()
                .map(s -> s.get(SchoolStudentImportEnum.PASSPORT.getIndex()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        CommonCheck.checkHaveDuplicate(idCards, snos, passports, true);

        // 获取已经存在的学校学生（判断是否重复）
        List<SchoolStudent> studentList = schoolStudentService.getByIdCardAndSnoAndPassports(idCards, snos, passports, schoolId);
        Map<String, SchoolStudent> snoMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getSno, Function.identity()));
        Map<String, SchoolStudent> idCardMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));
        Map<String, SchoolStudent> passPortMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getPassport, Function.identity()));

        // 获取已经删除的学生（重新启用删除的学生）
        List<SchoolStudent> deletedSchoolStudents = schoolStudentService.getDeletedByIdCard(idCards, passports, schoolId);
        Map<String, SchoolStudent> deletedIdCardStudentMap = deletedSchoolStudents.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));
        Map<String, SchoolStudent> deletedPassportStudentMap = deletedSchoolStudents.stream().collect(Collectors.toMap(SchoolStudent::getPassport, Function.identity()));

        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = schoolGradeService.getGradeAndClassMap(Lists.newArrayList(school.getId()));

        List<SchoolStudent> schoolStudents = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            SchoolStudent schoolStudent;
            // 查看是否已经删除，优先取身份证，再取护照号
            SchoolStudent deletedSchoolStudent = deletedIdCardStudentMap.get(item.get(SchoolStudentImportEnum.ID_CARD.getIndex()));
            deletedSchoolStudent = Objects.isNull(deletedSchoolStudent) ? deletedPassportStudentMap.get(item.get(SchoolStudentImportEnum.PASSPORT.getIndex())) : deletedSchoolStudent;
            if (Objects.isNull(deletedSchoolStudent)) {
                schoolStudent = new SchoolStudent();
            } else {
                schoolStudent = deletedSchoolStudent;
            }
            if (StringUtils.isBlank(item.get(SchoolStudentImportEnum.NAME.getIndex()))) {
                break;
            }
            schoolStudent.setStatus(CommonConst.STATUS_NOT_DELETED);

            checkIsExist(snoMap, idCardMap, passPortMap,
                    item.get(SchoolStudentImportEnum.SNO.getIndex()), item.get(SchoolStudentImportEnum.ID_CARD.getIndex()),
                    item.get(SchoolStudentImportEnum.GENDER.getIndex()), item.get(SchoolStudentImportEnum.PASSPORT.getIndex()));

            setSchoolStudentInfo(createUserId, schoolId, item, schoolStudent);
            String gradeName = item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex());
            String className = item.get(SchoolStudentImportEnum.CLASS_NAME.getIndex());
            TwoTuple<Integer, Integer> gradeClassInfo = getSchoolStudentClassInfo(schoolId, schoolGradeMaps, gradeName, className);
            schoolStudent.setGradeId(gradeClassInfo.getFirst());
            schoolStudent.setGradeName(gradeName);
            schoolStudent.setClassId(gradeClassInfo.getSecond());
            schoolStudent.setClassName(className);

            // 更新管理端
            schoolStudent.checkStudentInfo();
            DateUtil.checkBirthday(schoolStudent.getBirthday());
            Integer managementStudentId = updateManagementStudent(schoolStudent);
            schoolStudent.setStudentId(managementStudentId);
            schoolStudents.add(schoolStudent);
        }
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
    private void setSchoolStudentInfo(Integer createUserId, Integer schoolId, Map<Integer, String> item, SchoolStudent schoolStudent) {
        schoolStudent.setName(item.get(SchoolStudentImportEnum.NAME.getIndex()))
                .setGender(Objects.nonNull(item.get(SchoolStudentImportEnum.GENDER.getIndex())) ? GenderEnum.getType(item.get(SchoolStudentImportEnum.GENDER.getIndex())) : IdCardUtil.getGender(item.get(SchoolStudentImportEnum.ID_CARD.getIndex())))

                .setNation(NationEnum.getCode(item.get(SchoolStudentImportEnum.NATION.getIndex())))
                .setGradeType(GradeCodeEnum.getByName(item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex())).getType())
                .setSno((item.get(SchoolStudentImportEnum.SNO.getIndex())))
                .setIdCard(item.get(SchoolStudentImportEnum.ID_CARD.getIndex()))
                .setPassport(item.get(SchoolStudentImportEnum.PASSPORT.getIndex()))
                .setParentPhone(item.get(SchoolStudentImportEnum.PHONE.getIndex()))
                .setCreateUserId(createUserId)
                .setSchoolId(schoolId)
                .setStatus(CommonConst.STATUS_NOT_DELETED);
        schoolStudent.setProvinceCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.PROVINCE_NAME.getIndex())));
        schoolStudent.setCityCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.CITY_NAME.getIndex())));
        schoolStudent.setAreaCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.AREA_NAME.getIndex())));
        schoolStudent.setTownCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.TOWN_NAME.getIndex())));
        schoolStudent.setAddress(item.get(SchoolStudentImportEnum.ADDRESS.getIndex()));
        schoolStudent.setUpdateTime(new Date());
        if (StringUtils.isNoneBlank(schoolStudent.getIdCard(), schoolStudent.getPassport())) {
            schoolStudent.setPassport(null);
        }
        try {
            schoolStudent.setBirthday(Objects.nonNull(item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex())) ? DateFormatUtil.parseDate(item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2) : IdCardUtil.getBirthDay(item.get(SchoolStudentImportEnum.ID_CARD.getIndex())));
        } catch (ParseException e) {
            throw new BusinessException("生日格式异常");
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
        List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(schoolId);
        // 转换成年级Maps，年级名称作为Key
        Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
        // 年级信息
        SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(gradeName);
        Assert.notNull(schoolGradeExportDTO, "不存在该年级：" + gradeName);

        // 获取年级内的班级信息
        List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
        // 转换成班级Maps 把班级名称作为key
        Map<String, Integer> classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
        Integer classId = classExportMaps.get(className);
        Integer gradeId = schoolGradeExportDTO.getId();
        Assert.notNull(classId, "不存在该班级:" + className);
        return new TwoTuple<>(gradeId, classId);
    }

    /**
     * 学校端-学生是否存在
     *
     * @param snoMap      学号Map
     * @param idCardMap   身份证Map
     * @param passPortMap 护照
     * @param sno         学号
     * @param idCard      身份证
     * @param passport    护照信息
     */
    private void checkIsExist(Map<String, SchoolStudent> snoMap, Map<String, SchoolStudent> idCardMap,
                              Map<String, SchoolStudent> passPortMap, String sno, String idCard,
                              String gradeName, String passport) {

        if (StringUtils.isAllBlank(sno, idCard)) {
            throw new BusinessException("学号或身份证为空");
        }
        if (Objects.nonNull(snoMap.get(sno))) {
            throw new BusinessException("学号" + sno + ERROR_MSG);
        }
        if (Objects.nonNull(idCard) && Objects.nonNull(idCardMap.get(idCard))) {
            throw new BusinessException("身份证" + idCard + ERROR_MSG);
        }
        if (StringUtils.isBlank(gradeName)) {
            throw new BusinessException("身份证" + idCard + "年级不能为空");
        }
        if (Objects.nonNull(passport) && Objects.nonNull(passPortMap.get(passport))) {
            throw new BusinessException("护照" + passport + ERROR_MSG);
        }
    }

    /**
     * 更新管理端的学生信息
     *
     * @param schoolStudent 学校端学生
     * @return 管理端学生
     */
    public Integer updateManagementStudent(SchoolStudent schoolStudent) {
        // 通过身份证在管理端查找学生
        Student managementStudent = studentService.getByIdCardAndPassport(schoolStudent.getIdCard(), schoolStudent.getPassport(), null);

        // 如果为空新增，否则是更新
        if (Objects.isNull(managementStudent)) {
            Student student = new Student();
            BeanUtils.copyProperties(schoolStudent, student);
            studentService.saveStudent(student);
            return student.getId();
        }
        managementStudent.setSchoolId(schoolStudent.getSchoolId());
        managementStudent.setSno(schoolStudent.getSno());
        managementStudent.setName(schoolStudent.getName());
        managementStudent.setGender(schoolStudent.getGender());
        managementStudent.setClassId(schoolStudent.getClassId());
        managementStudent.setGradeId(schoolStudent.getGradeId());
        managementStudent.setIdCard(schoolStudent.getIdCard());
        managementStudent.setBirthday(schoolStudent.getBirthday());
        managementStudent.setNation(schoolStudent.getNation());
        managementStudent.setParentPhone(schoolStudent.getParentPhone());
        managementStudent.setProvinceCode(schoolStudent.getProvinceCode());
        managementStudent.setCityCode(schoolStudent.getCityCode());
        managementStudent.setAreaCode(schoolStudent.getAreaCode());
        managementStudent.setTownCode(schoolStudent.getTownCode());
        managementStudent.setAddress(schoolStudent.getAddress());
        managementStudent.setPassport(schoolStudent.getPassport());
        managementStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        managementStudent.setUpdateTime(new Date());
        studentService.updateStudent(managementStudent);
        return managementStudent.getId();
    }
}
