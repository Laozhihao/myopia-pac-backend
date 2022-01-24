package com.wupol.myopia.business.aggregation.export.excel.imports;

import com.google.common.collect.Lists;
import com.wupol.myopia.base.exception.BusinessException;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.base.util.ListUtil;
import com.wupol.myopia.business.aggregation.export.excel.ExcelFacade;
import com.wupol.myopia.business.aggregation.export.excel.domain.SchoolStudentImportEnum;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.common.utils.constant.NationEnum;
import com.wupol.myopia.business.common.utils.util.IdCardUtil;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private ExcelFacade excelFacade;

    /**
     * 导入学校学生
     *
     * @param createUserId  创建人
     * @param multipartFile 文件
     * @param schoolId      学校Id
     * @throws ParseException 转换异常
     */
    @Transactional(rollbackFor = Exception.class)
    public void importSchoolStudent(Integer createUserId, MultipartFile multipartFile, Integer schoolId) throws ParseException {
        List<Map<Integer, String>> listMap = excelFacade.readExcel(multipartFile);
        if (CollectionUtils.isEmpty(listMap)) {
            return;
        }
        School school = schoolService.getById(schoolId);

        // 收集身份证号码、学号
        List<String> idCards = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.ID_CARD.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());
        List<String> snos = listMap.stream().map(s -> s.get(SchoolStudentImportEnum.SNO.getIndex())).filter(Objects::nonNull).collect(Collectors.toList());
        checkIdCard(idCards, snos);

        // 获取学校学生
        List<SchoolStudent> studentList = schoolStudentService.getByIdCardOrSno(idCards, snos, schoolId);
        Map<String, SchoolStudent> snoMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getSno, Function.identity()));
        Map<String, SchoolStudent> idCardMap = studentList.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));

        List<SchoolStudent> deletedSchoolStudents = schoolStudentService.getDeletedByIdCard(idCards, schoolId);
        Map<String, SchoolStudent> deletedStudentMap = deletedSchoolStudents.stream().collect(Collectors.toMap(SchoolStudent::getIdCard, Function.identity()));

        // 收集年级信息
        List<SchoolGradeExportDTO> grades = schoolGradeService.getBySchoolIds(Lists.newArrayList(school.getId()));
        schoolGradeService.packageGradeInfo(grades);

        // 年级信息通过学校Id分组
        Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps = grades.stream().collect(Collectors.groupingBy(SchoolGradeExportDTO::getSchoolId));

        List<SchoolStudent> schoolStudents = new ArrayList<>();
        for (Map<Integer, String> item : listMap) {
            SchoolStudent schoolStudent;
            SchoolStudent deletedSchoolStudent = deletedStudentMap.get(item.get(SchoolStudentImportEnum.ID_CARD.getIndex()));
            if (Objects.isNull(deletedSchoolStudent)) {
                schoolStudent = new SchoolStudent();
            } else {
                schoolStudent = deletedSchoolStudent;
            }
            if (StringUtils.isBlank(item.get(SchoolStudentImportEnum.NAME.getIndex()))) {
                break;
            }
            checkIsExist(snoMap, idCardMap, item.get(SchoolStudentImportEnum.SNO.getIndex()),
                    item.get(SchoolStudentImportEnum.ID_CARD.getIndex()), item.get(SchoolStudentImportEnum.GENDER.getIndex()),
                    item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex()), item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex()));

            setSchoolStudentInfo(createUserId, schoolId, item, schoolStudent);
            setSchoolStudentClassInfo(schoolId, schoolGradeMaps, item, schoolStudent);
            // 更新管理端
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
     * @throws ParseException 日期转换异常
     */
    private void setSchoolStudentInfo(Integer createUserId, Integer schoolId, Map<Integer, String> item, SchoolStudent schoolStudent) throws ParseException {
        schoolStudent.setName(item.get(SchoolStudentImportEnum.NAME.getIndex()))
                .setGender(Objects.nonNull(item.get(SchoolStudentImportEnum.GENDER.getIndex())) ? GenderEnum.getType(item.get(SchoolStudentImportEnum.GENDER.getIndex())) : IdCardUtil.getGender(item.get(SchoolStudentImportEnum.ID_CARD.getIndex())))
                .setBirthday(Objects.nonNull(item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex())) ? DateFormatUtil.parseDate(item.get(SchoolStudentImportEnum.BIRTHDAY.getIndex()), DateFormatUtil.FORMAT_ONLY_DATE2) : IdCardUtil.getBirthDay(item.get(SchoolStudentImportEnum.ID_CARD.getIndex())))
                .setNation(NationEnum.getCode(item.get(SchoolStudentImportEnum.NATION.getIndex())))
                .setGradeType(GradeCodeEnum.getByName(item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex())).getType())
                .setSno((item.get(SchoolStudentImportEnum.SNO.getIndex())))
                .setIdCard(item.get(SchoolStudentImportEnum.ID_CARD.getIndex()))
                .setParentPhone(item.get(SchoolStudentImportEnum.PHONE.getIndex()))
                .setCreateUserId(createUserId)
                .setSchoolId(schoolId)
                .setStatus(CommonConst.STATUS_NOT_DELETED);
        schoolStudent.setProvinceCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.PROVINCE_NAME.getIndex())));
        schoolStudent.setCityCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.CITY_NAME.getIndex())));
        schoolStudent.setAreaCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.AREA_NAME.getIndex())));
        schoolStudent.setTownCode(districtService.getCodeByName(item.get(SchoolStudentImportEnum.TOWN_NAME.getIndex())));
        schoolStudent.setAddress(item.get(SchoolStudentImportEnum.ADDRESS.getIndex()));
    }

    /**
     * 设置学生年级班级信息
     *
     * @param schoolId        学校Id
     * @param schoolGradeMaps 年级Map
     * @param item            导入信息
     * @param schoolStudent   学校端学生
     */
    private void setSchoolStudentClassInfo(Integer schoolId, Map<Integer, List<SchoolGradeExportDTO>> schoolGradeMaps, Map<Integer, String> item, SchoolStudent schoolStudent) {
        // 通过学校编号获取改学校的年级信息
        List<SchoolGradeExportDTO> schoolGradeExportVOS = schoolGradeMaps.get(schoolId);
        // 转换成年级Maps，年级名称作为Key
        Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportVOS.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));
        // 年级信息
        SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex()));
        Assert.notNull(schoolGradeExportDTO, "年级数据异常");
        // 设置年级ID
        schoolStudent.setGradeId(schoolGradeExportDTO.getId());
        schoolStudent.setGradeName(item.get(SchoolStudentImportEnum.GRADE_NAME.getIndex()));
        // 获取年级内的班级信息
        List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
        // 转换成班级Maps 把班级名称作为key
        Map<String, Integer> classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
        Integer classId = classExportMaps.get(item.get(SchoolStudentImportEnum.CLASS_NAME.getIndex()));
        Assert.notNull(classId, "班级数据为空");
        // 设置班级信息
        schoolStudent.setClassId(classId);
        schoolStudent.setClassName(item.get(SchoolStudentImportEnum.CLASS_NAME.getIndex()));
    }

    /**
     * 检查身份证、学号是否重复
     *
     * @param idCards 身份证
     * @param snoList 学号
     */
    private void checkIdCard(List<String> idCards, List<String> snoList) {
        if (CollectionUtils.isEmpty(idCards)) {
            throw new BusinessException("身份证为空");
        }
        if (CollectionUtils.isEmpty(snoList)) {
            throw new BusinessException("学号为空");
        }
        List<String> idCardDuplicate = ListUtil.getDuplicateElements(idCards);
        if (!CollectionUtils.isEmpty(idCardDuplicate)) {
            throw new BusinessException("身份证号码：" + String.join(",", idCardDuplicate) + "重复");
        }
        List<String> snoDuplicate = ListUtil.getDuplicateElements(snoList);
        if (!CollectionUtils.isEmpty(snoDuplicate)) {
            throw new BusinessException("学号：" + String.join(",", snoDuplicate) + "重复");
        }
    }

    /**
     * 学校端-学生是否存在
     *
     * @param snoMap    学号Map
     * @param idCardMap 身份证Map
     * @param sno       学号
     * @param idCard    身份证
     */
    private void checkIsExist(Map<String, SchoolStudent> snoMap, Map<String, SchoolStudent> idCardMap,
                              String sno, String idCard, String gender, String birthday, String gradeName) {

        if (StringUtils.isAllBlank(sno, idCard)) {
            throw new BusinessException("学号或身份证为空");
        }
        if (Objects.nonNull(snoMap.get(sno))) {
            throw new BusinessException("学号" + sno + "在系统中重复");
        }
        if (Objects.nonNull(idCardMap.get(idCard))) {
            throw new BusinessException("身份证" + idCard + "在系统中重复");
        }
        if (StringUtils.isBlank(gradeName)) {
            throw new BusinessException("身份证" + idCard + "年级不能为空");
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
        Student managementStudent = studentService.getAllByIdCard(schoolStudent.getIdCard());

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
        managementStudent.setStatus(CommonConst.STATUS_NOT_DELETED);
        studentService.updateStudent(managementStudent);
        return managementStudent.getId();
    }
}
