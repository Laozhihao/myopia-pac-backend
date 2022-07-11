package com.wupol.myopia.business.aggregation.export.excel.domain;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdcardUtil;
import cn.hutool.core.util.PhoneUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.wupol.myopia.base.util.DateFormatUtil;
import com.wupol.myopia.business.aggregation.export.excel.constant.ImportExcelEnum;
import com.wupol.myopia.business.common.utils.util.ListUtil;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.school.domain.dto.SchoolClassExportDTO;
import com.wupol.myopia.business.core.school.domain.dto.SchoolGradeExportDTO;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.screening.flow.domain.model.ScreeningPlanSchoolStudent;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
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

        List<String> screeningCodeList = existPlanSchoolStudentList.stream().map(ScreeningPlanSchoolStudent::getScreeningCode).map(Objects::toString).collect(Collectors.toList());
        List<Map<Integer, String>> errorMapList = Lists.newArrayList();

        //失败数据移出
        Iterator<Map<Integer, String>> it = listMap.iterator();
        while (it.hasNext()){
            Map<Integer, String> item = it.next();
            List<String> errorItemList = Lists.newArrayList();
            checkProcess(item,errorItemList,screeningCodeList,idCardList,passportList,snoList,existPlanSchoolStudentList,gradeMaps,school.getId());
            if (CollectionUtil.isNotEmpty(errorItemList)){
                List<String> requiredList = errorItemList.stream().filter(error -> error.contains("必填项为空")).map(s -> s.split(":")[1]).collect(Collectors.toList());
                String requiredStr=StrUtil.EMPTY;
                if (CollectionUtil.isNotEmpty(requiredList)){
                    requiredStr = "必填项为空:"+CollectionUtil.join(requiredList,"、");
                }
                List<String> notRequiredList = errorItemList.stream().filter(error -> !error.contains("必填项为空")).collect(Collectors.toList());
                if (StrUtil.isNotBlank(requiredStr)){
                    notRequiredList.add(requiredStr);
                }
                item.put(11,CollectionUtil.join(notRequiredList, "; "));
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
     * 每行数据校验处理
     *
     * @param item 数据
     * @param errorItemList 错误集合
     * @param screeningCodeList 筛查编码集合
     * @param idCardList 重复身份证号集合
     * @param passportList 重复护照集合
     * @param snoList 重复学号集合
     * @param existPlanSchoolStudentList 筛查计划学校学生集合
     * @param gradeMaps 年级和班级集合
     * @param schoolId 学校ID
     */
    private static void checkProcess(Map<Integer, String> item,
                                     List<String> errorItemList,
                                     List<String> screeningCodeList,
                                     List<String> idCardList, List<String> passportList, List<String> snoList,
                                     List<ScreeningPlanSchoolStudent> existPlanSchoolStudentList,
                                     Map<String, SchoolGradeExportDTO> gradeMaps,
                                     Integer schoolId){
        String screeningCode = item.getOrDefault(ImportExcelEnum.SCREENING_CODE.getIndex(), null);
        String idCard = item.getOrDefault(ImportExcelEnum.ID_CARD.getIndex(), null);
        String passport = item.getOrDefault(ImportExcelEnum.PASSPORT.getIndex(), null);
        String name = item.getOrDefault(ImportExcelEnum.NAME.getIndex(), null);
        String gender = item.getOrDefault(ImportExcelEnum.GENDER.getIndex(), null);
        String birthdayStr = item.getOrDefault(ImportExcelEnum.BIRTHDAY.getIndex(), null);
        String gradeName = item.getOrDefault(ImportExcelEnum.GRADE.getIndex(), null);
        String className = item.getOrDefault(ImportExcelEnum.CLASS.getIndex(), null);
        String studentNo = item.getOrDefault(ImportExcelEnum.STUDENT_NO.getIndex(), null);
        String phone = item.getOrDefault(ImportExcelEnum.PHONE.getIndex(), null);
        //筛查编号
        if (Objects.nonNull(screeningCode) && !screeningCodeList.contains(screeningCode)){
            //校验编码是否存在于系统
            errorItemList.add("编码错误");
        }

        if (StringUtils.isAllBlank(idCard,passport)){
            errorItemList.add("身份证号和护照，二选一必填");
        }

        if (StringUtils.isAllBlank(idCard,passport,screeningCode)){
            errorItemList.add("身份证号、护照、编码不能都为空");
        }

        boolean isIdCard = StringUtils.isNotBlank(idCard);
        //身份证号/护照
        if (isIdCard){
            if (!IdcardUtil.isValidCard(idCard)){
                errorItemList.add("身份证号错误");
            }
            if (idCardList.contains(idCard)){
                errorItemList.add("身份证号与其他重复");
            }
        }else {
            boolean isPassport = StrUtil.isNotBlank(passport);
            if (isPassport && passport.length() < 7) {
                errorItemList.add("护照错误");
            }
            if (isPassport && passportList.contains(passport)){
                errorItemList.add("护照与其他重复");
            }
        }

        //姓名
        if (StrUtil.isBlank(name)){
            errorItemList.add("必填项为空:姓名");
        }

        //性别
        if (!isIdCard && StrUtil.isBlank(gender)){
            errorItemList.add("必填项为空:性别");
        }

        //出生日期
        if (!isIdCard && StrUtil.isBlank(birthdayStr)){
            errorItemList.add("必填项为空:出生日期");
        }

        if (StrUtil.isNotBlank(birthdayStr)){
            Date birthday =null;
            try {
                birthday = DateFormatUtil.parseDate(birthdayStr, DateFormatUtil.FORMAT_ONLY_DATE2);
            }catch (Exception e){
                errorItemList.add("出生日期格式错误");
            }
            // 1970-01-01 00:00:02 毫秒时间戳
            Date beforeDate = new Date(-28798000L);
            Date afterDate = new Date();
            if (Objects.nonNull(birthday) && (birthday.before(beforeDate) || birthday.after(afterDate))) {
                errorItemList.add("出生日期超出限制错误");
            }
        }

        SchoolGradeExportDTO schoolGradeExportDTO=null;
        //年级
        if (StrUtil.isNotBlank(gradeName)){
            schoolGradeExportDTO = gradeMaps.get(gradeName);
            if (Objects.isNull(schoolGradeExportDTO)){
                errorItemList.add("年级不存在");
            }
        }else {
            errorItemList.add("必填项为空:年级");
        }

        if (StrUtil.isNotBlank(className) ){
            if(Objects.nonNull(schoolGradeExportDTO)){
                // 获取年级内的班级信息
                List<SchoolClassExportDTO> classExportDTOList = schoolGradeExportDTO.getChild();
                Map<String, Integer> classExportMaps=Maps.newHashMap();
                if (CollectionUtil.isNotEmpty(classExportDTOList)){
                    classExportMaps = classExportDTOList.stream().collect(Collectors.toMap(SchoolClassExportDTO::getName, SchoolClassExportDTO::getId));
                }
                // 转换成班级Maps 把班级名称作为key
                Integer classId = classExportMaps.get(className);
                if (Objects.isNull(classId)){
                    errorItemList.add("班级不存在");
                }
            }else {
                errorItemList.add("班级不存在");
            }

        }else {
            errorItemList.add("必填项为空:班级");
        }

        //学号
        if (StrUtil.isNotBlank(studentNo) ){
            if (Objects.equals(Boolean.TRUE,checkSno(existPlanSchoolStudentList,studentNo,idCard,passport,schoolId))) {
                errorItemList.add("学号错误");
            }
            if (snoList.contains(studentNo)){
                errorItemList.add("学号与其他重复");
            }
        }
        //手机号码
        if (StrUtil.isNotBlank(phone) && !PhoneUtil.isPhone(phone)){
            errorItemList.add("手机号码错误");
        }

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

}
