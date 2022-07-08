package com.wupol.myopia.business.aggregation.export.excel.domain;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 导入筛查学校学生构建
 *
 * @author hang.yuan 2022/7/7 00:39
 */
@UtilityClass
public class ImportScreeningSchoolStudentBuilder {


    /**
     * 校验数据
     *
     * @param listMap 数据集合
     * @param existPlanSchoolStudentList 存在的筛查计划学校学生集合
     * @param schoolGradeExportDTOList 学校班级和年级集合
     * @param school 学校
     */
    public static TwoTuple<UploadScreeningStudentVO,List<ImportScreeningSchoolStudentFailDTO>> validData(List<Map<Integer, String>> listMap,
                                     List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList,
                                     List<SchoolGradeExportDTO> schoolGradeExportDTOList,
                                     School school){

        UploadScreeningStudentVO uploadScreeningStudentVO  = new UploadScreeningStudentVO();

        int totalSize = listMap.size();
        uploadScreeningStudentVO.setTotalStudentNum(totalSize);

        // 转换成年级Maps，年级名称作为Key
        Map<String, SchoolGradeExportDTO> gradeMaps = schoolGradeExportDTOList.stream().collect(Collectors.toMap(SchoolGradeExportDTO::getName, Function.identity()));

        List<String> idCardList = Lists.newArrayList();
        List<String> passportList = Lists.newArrayList();
        List<String> snoList = Lists.newArrayList();
        getDuplicateInfo(listMap,idCardList,passportList,snoList);

        List<Map<Integer, String>> errorMapList = Lists.newArrayList();

        //失败数据移出
        Iterator<Map<Integer, String>> it = listMap.iterator();
        while (it.hasNext()){
            Map<Integer, String> item = it.next();
            List<String> errorItemList = Lists.newArrayList();
            // 必填项
            checkRequired(item,errorItemList);
            // 年级和班级
            checkGradeAndClass(item, gradeMaps,errorItemList);
            // 字段有效性
            checkValidity(item, existPlanSchoolStudentList, errorItemList,school.getId());
            // 检查重复
            checkHaveDuplicate(item,errorItemList,idCardList,passportList,snoList);
            if (CollectionUtil.isNotEmpty(errorItemList)){
                item.put(11,CollectionUtil.join(errorItemList,"; "));
                errorMapList.add(item);
                it.remove();
            }
        }

        int errorSize = 0;
        if (CollectionUtil.isNotEmpty(errorMapList)){
            errorSize = errorMapList.size();
        }
        uploadScreeningStudentVO.setFailStudentNum(errorSize);
        uploadScreeningStudentVO.setSuccessStudentNum(totalSize-errorSize);

        List<ImportScreeningSchoolStudentFailDTO> errorList =Lists.newArrayList();
        if (CollectionUtil.isNotEmpty(errorMapList)){
            errorList = errorMapList.stream().map(item -> {
                ImportScreeningSchoolStudentFailDTO failDTO = new ImportScreeningSchoolStudentFailDTO();
                failDTO.setScreeningCode(item.getOrDefault(ImportExcelEnum.SCREENING_CODE.getIndex(), null));
                failDTO.setIdCard(item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null));
                failDTO.setPassport(item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null));
                failDTO.setName(item.getOrDefault(ImportExcelEnum.NAME.getIndex(), null));
                failDTO.setGender(item.getOrDefault(ImportExcelEnum.GENDER.getIndex(), null));
                failDTO.setBirthday(item.getOrDefault(ImportExcelEnum.BIRTHDAY.getIndex(), null));
                failDTO.setNation(item.getOrDefault(ImportExcelEnum.NATION.getIndex(), null));
                failDTO.setGradeName(item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null));
                failDTO.setClassName(item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null));
                failDTO.setStudentNo(item.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null));
                failDTO.setPhone(item.getOrDefault(ImportExcelEnum.PHONE.getIndex(), null));
                failDTO.setErrorMsg(item.getOrDefault(11, null));
                return failDTO;
            }).collect(Collectors.toList());
        }
        return TwoTuple.of(uploadScreeningStudentVO,errorList);
    }

    /**
     * 获取重复数据（身份证号，护照，学号）
     *
     * @param listMap 集合
     * @param idCardList 重复身份证号集合
     * @param passportList 重复护照集合
     * @param snoList 重复学号集合
     */
    private static void getDuplicateInfo(List<Map<Integer, String>> listMap, List<String> idCardList, List<String> passportList, List<String> snoList) {
        List<String> idCards =Lists.newArrayList();
        List<String> passports =Lists.newArrayList();
        List<String> snos =Lists.newArrayList();
        listMap.forEach(item -> {
            String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
            if (StringUtils.isNotBlank(idCard)) {
                idCards.add(idCard);
            }

            String passport = item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null);
            if (StringUtils.isNotBlank(passport)) {
                passports.add(passport);
            }

            String sno = item.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null);
            if (StringUtils.isNotBlank(sno)) {
                snos.add(sno);
            }
        });
       idCardList.addAll(ListUtil.getDuplicateElements(idCards));
       passportList.addAll(ListUtil.getDuplicateElements(passports));
       snoList.addAll(ListUtil.getDuplicateElements(snos));

    }

    /**
     * 检查数据的有效性
     * @param item 数据
     * @param existPlanSchoolStudentList 存在的筛查学校学生数据集合
     * @param errorItemList 错误信息集合
     * @param schoolId 学校ID
     */
    private static void checkValidity(Map<Integer, String> item,
                                   List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList,
                                   List<String> errorItemList,
                                   Integer schoolId) {


        String idCard = item.get(ImportExcelEnum.ID_CARD.getIndex());
        String passport = item.get(ImportExcelEnum.PASSPORT.getIndex());
        String screeningCode = item.get(ImportExcelEnum.SCREENING_CODE.getIndex());
        String phone = item.get(ImportExcelEnum.PHONE.getIndex());
        String sno = item.get(ImportExcelEnum.STUDENT_NO.getIndex());
        // 唯一标志
        if (StringUtils.isAllBlank(idCard, passport, screeningCode)) {
            errorItemList.add("身份证、护照、编码不能都为空");
        }
        //护照
        if (StringUtils.isNotBlank(passport) && passport.length() < 7) {
            errorItemList.add("护照号错误");
        }

        // 身份证
        if (StringUtils.isNotBlank(idCard) && !IdcardUtil.isValidCard(idCard)) {
            errorItemList.add("身份证号错误");
        }
        // 手机号码
        if (StringUtils.isNotBlank(phone) && !PhoneUtil.isPhone(phone)) {
            errorItemList.add("手机号码错误");
        }
        //学号
        if (Objects.equals(Boolean.TRUE,checkSno(existPlanSchoolStudentList,sno,idCard,passport,schoolId))) {
            errorItemList.add("学号重复");
        }

    }

    /**
     * 检查学号是否被使用
     * @param existPlanSchoolStudentList 存在的筛查学校学生数据集合
     * @param sno 学生号
     * @param idCard 身份证号
     * @param passport 护照
     * @param schoolId 学校ID
     */
    private static Boolean checkSno(List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList, String sno, String idCard, String passport, Integer schoolId) {
        if (org.springframework.util.CollectionUtils.isEmpty(existPlanSchoolStudentList) || StringUtils.isBlank(sno)) {
            return Boolean.FALSE;
        }
        Predicate<ScreeningPlanSchoolStudent>  predicate = s -> Objects.equals(sno, s.getStudentNo())
                && Objects.equals(schoolId, s.getSchoolId())
                && ((StringUtils.isNotBlank(idCard) && !Objects.equals(idCard, s.getIdCard()))
                || (StringUtils.isNotBlank(passport) && !Objects.equals(passport, s.getPassport())));
        // 学号是否被使用
        List<ScreeningPlanSchoolStudent> collect = existPlanSchoolStudentList.stream().filter(predicate).collect(Collectors.toList());
        return CollectionUtil.isNotEmpty(collect);
    }

    /**
     * 检查年级和班级
     * @param item 数据
     * @param gradeMaps 年级班级数据
     * @param errorItemList 错误信息集合
     */
    private static void checkGradeAndClass(Map<Integer, String> item,
                                             Map<String, SchoolGradeExportDTO> gradeMaps,
                                             List<String> errorItemList) {


        //检查班级和年级
        String gradeName = item.get(ImportExcelEnum.GRADE.getIndex());
        String className = item.get(ImportExcelEnum.CLASS.getIndex());

        // 年级信息
        SchoolGradeExportDTO schoolGradeExportDTO = gradeMaps.get(gradeName);
        if (Objects.isNull(schoolGradeExportDTO)){
            errorItemList.add("年级不存在");
        }

        if (Objects.nonNull(schoolGradeExportDTO)){
            // 获取年级内的班级信息
            List<SchoolClassExportDTO> classExportVOS = schoolGradeExportDTO.getChild();
            Map<String, Integer> classExportMaps=Maps.newHashMap();
            if (CollectionUtil.isNotEmpty(classExportVOS)){
                classExportMaps = classExportVOS.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
            }
            // 转换成班级Maps 把班级名称作为key
            Integer classId = classExportMaps.get(className);
            if (Objects.isNull(classId)){
                errorItemList.add("班级不存在");
            }
        }else {
            errorItemList.add("班级不存在");
        }
    }

    /**
     * 检查重复
     *
     * @param item 数据
     * @param errorItemList 错误集合
     * @param idCardList 身份证号重复集合
     * @param passportList 护照重复集合
     * @param snoList 学号重复集合
     */
    private static void checkHaveDuplicate(Map<Integer, String> item,List<String> errorItemList,List<String> idCardList, List<String> passportList, List<String> snoList){
        String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
        String passport = item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null);
        String sno = item.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null);

        if (StrUtil.isNotBlank(idCard) && idCardList.contains(idCard)){
            errorItemList.add("身份证号与其他重复");
        }
        if (StrUtil.isNotBlank(passport) && passportList.contains(passport)){
            errorItemList.add("护照与其他重复");
        }
        if (StrUtil.isNotBlank(sno) && snoList.contains(sno)){
            errorItemList.add("学号与其他重复");
        }
    }
    /**
     * 检查必须项
     * @param item 数据
     * @param errorItemList 错误信息集合
     */
    private static void checkRequired(Map<Integer, String> item,List<String> errorItemList){
        List<String> errorList = Lists.newArrayList();
        String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
        String passPort = item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null);
        if (Objects.isNull(idCard) && Objects.isNull(passPort)){
            errorList.add(ImportExcelEnum.ID_CARD.getName()+"/"+ImportExcelEnum.PASSPORT+": 二选一必填");
        }

        if (Objects.isNull(item.getOrDefault(ImportExcelEnum.NAME.getIndex(), null))){
            errorList.add(ImportExcelEnum.NAME.getName());
        }
        if (Objects.isNull(item.getOrDefault(ImportExcelEnum.GENDER.getIndex(), null))){
            errorList.add(ImportExcelEnum.GENDER.getName());
        }
        if (Objects.isNull(item.getOrDefault(ImportExcelEnum.BIRTHDAY.getIndex(), null))){
            errorList.add(ImportExcelEnum.BIRTHDAY.getName());
        }
        if (Objects.isNull(item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null))){
            errorList.add(ImportExcelEnum.GRADE.getName());
        }
        if (Objects.isNull(item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null))){
            errorList.add(ImportExcelEnum.GRADE.getName());
        }

        if (CollectionUtil.isNotEmpty(errorList)){
            errorItemList.add("必填项为空:" + CollectionUtil.join(errorList, "、"));
        }
    }

}
