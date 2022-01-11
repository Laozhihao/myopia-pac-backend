package com.wupol.myopia.business.api.management.service;

import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.myopia.base.constant.MonthAgeEnum;
import com.wupol.myopia.base.domain.vo.FamilyInfoVO;
import com.wupol.myopia.base.util.BusinessUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.core.hospital.domain.model.HospitalStudent;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.service.HospitalStudentService;
import com.wupol.myopia.business.core.hospital.service.PreschoolCheckRecordService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 孩子年龄到了后会短信或公众号提醒家长做保健
 *
 * @author Simple4H
 */
@Service
@Log4j2
public class PreSchoolNoticeService {

    @Resource
    private HospitalStudentService hospitalStudentService;

    @Resource
    private PreschoolCheckRecordService preschoolCheckRecordService;

    @Resource
    private VistelToolsService vistelToolsService;


    /**
     * 发送短信
     */
    public void timedTaskSendMsg() {
        // 获取学生
        List<HospitalStudent> hospitalStudents = hospitalStudentService.getByStudentType();
        if (CollectionUtils.isEmpty(hospitalStudents)) {
            return;
        }
        List<Integer> studentIds = hospitalStudents.stream().map(HospitalStudent::getStudentId).collect(Collectors.toList());

        // 获取学生报告
        List<PreschoolCheckRecord> reportList = preschoolCheckRecordService.getByStudentIds(studentIds);
        // 通过学生Id分组
        Map<Integer, List<PreschoolCheckRecord>> reportMap = reportList.stream().collect(Collectors.groupingBy(PreschoolCheckRecord::getStudentId));

        hospitalStudents.forEach(hospitalStudent -> {
            MonthAgeEnum monthAge = BusinessUtil.getMonthAgeByBirthday(hospitalStudent.getBirthday());
            if (Objects.isNull(monthAge)) {
                return;
            }
            List<PreschoolCheckRecord> studentReportList = reportMap.get(hospitalStudent.getStudentId());
            // 如果报告为空，说明学生没有检查，直接发送短信
            if (CollectionUtils.isEmpty(studentReportList)) {
                sendMsg(hospitalStudent, monthAge);
                return;
            }
            // 判断当前年龄段是否做过检查
            if (studentReportList.stream().map(PreschoolCheckRecord::getMonthAge).collect(Collectors.toList()).contains(monthAge.getId())) {
                return;
            }
            // 不存在年龄段发送短信
            sendMsg(hospitalStudent, monthAge);
        });
    }

    /**
     * 发送短信
     *
     * @param hospitalStudent 医院学生
     * @param monthAge        月龄段
     */
    private void sendMsg(HospitalStudent hospitalStudent, MonthAgeEnum monthAge) {
        String phone = getNoticePhone(hospitalStudent);
        if (StringUtils.isEmpty(phone)) {
            return;
        }
        String messageInfo = String.format(CommonConst.SEND_SMS_PRESCHOOL_NOTICE, packageStudentName(hospitalStudent.getName()), monthAge.getName());
        MsgData msgData = new MsgData(phone, "+86", messageInfo);
        vistelToolsService.sendMsg(msgData);
    }

    /**
     * 获取学生名称
     *
     * @param studentName 学生名称
     * @return 学生名称
     */
    private String packageStudentName(String studentName) {
        if (studentName.length() < 5) {
            return studentName;
        }
        return studentName.substring(0, 5) + "...";
    }

    /**
     * 获取通知的手机好吗
     *
     * @param hospitalStudent 医院患者
     * @return 手机号码
     */
    private String getNoticePhone(HospitalStudent hospitalStudent) {
        FamilyInfoVO familyInfo = hospitalStudent.getFamilyInfo();
        if (Objects.isNull(familyInfo)) {
            return null;
        }
        List<FamilyInfoVO.MemberInfo> member = familyInfo.getMember();
        if (CollectionUtils.isEmpty(member)) {
            return null;
        }
        String fatherPhone = getPhoneByMemberInfo(member.get(0));
        String motherPhone = getPhoneByMemberInfo(member.get(1));
        return StringUtils.isNotBlank(fatherPhone) ? fatherPhone : StringUtils.isNotBlank(motherPhone) ? motherPhone : StringUtils.EMPTY;
    }

    /**
     * 通过家庭信息获取电话
     *
     * @param memberInfo 家庭信息
     * @return 电话
     */
    private String getPhoneByMemberInfo(FamilyInfoVO.MemberInfo memberInfo) {
        if (Objects.isNull(memberInfo)) {
            return StringUtils.EMPTY;
        }
        return memberInfo.getPhone();
    }
}
