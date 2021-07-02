package com.wupol.myopia.business.api.screening.app.service;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.MsgContentUtil;
import com.wupol.myopia.business.core.hospital.domain.model.MedicalRecord;
import com.wupol.myopia.business.core.hospital.domain.query.MedicalRecordQuery;
import com.wupol.myopia.business.core.hospital.service.MedicalRecordService;
import com.wupol.myopia.business.core.school.domain.dto.StudentBasicInfoDTO;
import com.wupol.myopia.business.core.school.service.StudentService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.screening.flow.service.WarningMsgService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 筛查视力预警短信
 *
 * @Author Jacob
 * @Date 2021-06-15
 */
@Service
@Slf4j
public class ScreeningVisionMsgService {

    @Autowired
    private WarningMsgService warningMsgService;
    @Autowired
    private StudentService studentService;
    @Autowired
    private MedicalRecordService medicalRecordService;
    @Autowired
    private StatConclusionService statConclusionService;
    /**
     * 每天发送警告短信
     */
    public void sendWarningMsg() {
        // 找出需要发送短信的数据
        Set<Integer> studentIdSet = getNeedToSendWarningMsgStudentIds();
        if (CollectionUtils.isEmpty(studentIdSet)) {
            //没有短信消息需要发送
            log.info("{} 没有学生的警告短信需要重新发送.", DateUtil.getNowDateTimeStr());
            return;
        }
        // 初始化数据
        List<WarningMsg> warningMsgs = getInitWarningMsgList(studentIdSet);
        // 处理短信
        dealMsg(warningMsgs);
        // 更新状态
        warningMsgService.saveBatch(warningMsgs);
    }

    private Set<Integer> getNeedToSendWarningMsgStudentIds() {
            //今天10点到昨天10点
            Date yesterdayDateTime = DateUtil.getSpecialDateTime(10,0,-1);
            Date todayDateTime = DateUtil.getTodayTime(10, 0);
            //获取特定时间的statconclusions
            List<StatConclusion> statConclusions = statConclusionService.getStatConclusionByDateTimeRange(yesterdayDateTime,todayDateTime);
            //选择出视力异常的数据
            return getVisionExceptionStudentIds(statConclusions,todayDateTime);
    }

    /**
     * 获取视力异常的学生id
     * @param statConclusions
     * @param todayDateTime
     * @return
     */
    private Set<Integer> getVisionExceptionStudentIds(List<StatConclusion> statConclusions, Date todayDateTime) {
        //每个studentId排序取最新的状态进行判断getValidDistrictTree
        Map<Integer, Date> studentIdVisionWarnMsgDateMap = this.getStudentNeedWarningMsgMap(statConclusions);
        //过滤掉不需要视力警告
        Set<MedicalRecordQuery> medicalRecordQueries = new HashSet<>();
        studentIdVisionWarnMsgDateMap.forEach((studentId,visionWarningUpdateTime) -> {
            if (visionWarningUpdateTime != null) {
                MedicalRecordQuery MedicalRecordQuery = new MedicalRecordQuery();
                MedicalRecordQuery.setStudentId(studentId);
                MedicalRecordQuery.setStartDate(visionWarningUpdateTime);
                MedicalRecordQuery.setEndDate(todayDateTime);
                medicalRecordQueries.add(MedicalRecordQuery);
            }
        });
        Set<Integer> studentIds = medicalRecordQueries.stream().map(MedicalRecord::getStudentId).collect(Collectors.toSet());
        //查找studentId在出现异常时间 到 现在 是否具有就诊记录
        Set<Integer> medicalRecordStudentIds = medicalRecordService.getMedicalRecordStudentIds(medicalRecordQueries);
        studentIds.removeAll(medicalRecordStudentIds);
        return studentIds;
    }

    /**
     * 获取studentId是否需要通知短信的Map, key = studentId, value = 视力异常的更新时间
     * @param statConclusions
     * @return
     */
    private Map<Integer, Date> getStudentNeedWarningMsgMap(List<StatConclusion> statConclusions) {
        return statConclusions.stream().filter(statConclusion -> statConclusion.getStudentId() != null).collect(
                Collectors.groupingBy(StatConclusion::getStudentId, Collectors.collectingAndThen(Collectors.maxBy(Comparator.comparing(StatConclusion::getVisionWarningUpdateTime))
                        , statConclusionOptional -> {
                            if (!statConclusionOptional.isPresent()) {
                                return null;
                            }
                            StatConclusion statConclusion = statConclusionOptional.get();
                            Boolean isVisionWarning = statConclusion.getIsVisionWarning();
                            if (isVisionWarning != null && isVisionWarning) {
                                return statConclusion.getVisionWarningUpdateTime();
                            }
                            return null;
                        }))
        );
    }


    /**
     * 查找某天前的学生异常的记录,进行重新检查是否需要短信提醒.
     * 短信重新提醒的条件:
     *  1.无医院就诊记录
     *  2.最新的一次筛查结果视力达到短信提醒视力异常的标准
     * @param dayOffset 日期偏移量
     */
    public void repeatNoticeWarningMsg(int dayOffset) {
        List<WarningMsg> warningMsgs = warningMsgService.needRepeatNoticeMsg(dayOffset);
        // 过滤掉正常的数据,过滤掉正常数据后,不会插入到warningMsg表中,这意味着,该学生这段时间检查的异常短信发送的生命周期结束
        warningMsgs = filterNormalVision(warningMsgs,dayOffset);
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //没有发送消息的任务
            log.info("今天{}没有学生需要重复警告短信需要重新发送.", DateUtil.getNowDateTimeStr());
            return;
        }
        // 设置基础数据
        this.setWarningMsg(warningMsgs);
        // 处理短信事宜
        dealMsg(warningMsgs);
        // 保存
        warningMsgService.saveBatch(warningMsgs);
    }

    /**
     * 设置WarningMsg的数据
     * @param warningMsgs
     */
    private void setWarningMsg(List<WarningMsg> warningMsgs) {
        Date date = new Date();
        for (WarningMsg warningMsg : warningMsgs) {
            // 目前只有定时任务使用这个方法,不存在并发时的更新丢失情况, 如若避免更新丢失,可以加上where send_times = @lastTimes
            warningMsg.setUpdateTime(new Date()).setUpdateTime(date).setCreateTime(date).setSendTimes(warningMsg.getSendTimes() + 1);
        }
    }

    /**
     * 处理短信
     *
     * @param warningMsgs
     */
    public void dealMsg(List<WarningMsg> warningMsgs) {
        if (CollectionUtils.isEmpty(warningMsgs)) {
            return;
        }
        // 查找学生名字及对应的电话号码
        Map<Integer, StudentBasicInfoDTO> studentPhonesMap = studentService.getPhonesMap(warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toSet()));
        for (WarningMsg warningMsg : warningMsgs) {
            // 获取学生数据
            StudentBasicInfoDTO studentBasicInfoDTO = studentPhonesMap.get(warningMsg.getStudentId());
            // 设置短信内容
            String content = MsgContentUtil.getMsgContent(MsgTemplateEnum.getTemplateByCode(warningMsg.getMsgTemplateId()), studentBasicInfoDTO.getStudentName());
            // 设置短信号码
            warningMsg.setPhoneNumbers(studentBasicInfoDTO.getPhoneNums());
            // 发送短信并记录
            this.sendMsgAndSetRecordInWarningMsg(warningMsg,content);
        }
    }

    /**
     * 发送短信并做记录
     * @param warningMsg
     * @param content
     */
    private void sendMsgAndSetRecordInWarningMsg(WarningMsg warningMsg, String content) {
        //先设置今天的天数
        warningMsg.setSendDayOfYear(DateUtil.getDayOfYear(new Date(), 0));
        //获取电话号码
        List<String> phoneNumbers = warningMsg.getPhoneNumbers();
        if (CollectionUtils.isEmpty(phoneNumbers)) {
            log.warn("发送视力预警短信的时候没有找到该学生的电话号码,studentId = {}", warningMsg.getStudentId());
            warningMsg.setSendStatus(WarningMsg.STATUS_SEND_CANCEL);
            return;
        }
        boolean isFail = warningMsgService.sendMsg(content, phoneNumbers);
        warningMsg.setSendStatus(isFail ? WarningMsg.STATUS_SEND_FAILURE : WarningMsg.STATUS_SEND_SUCCESS);
        //设置发送日期: 有发送的,无论发送失败或者都有发送日期
        warningMsg.setSendTime(new Date());
    }


    /**
     * 过滤掉
     * 1.已经具有正常的数据
     * 2.医院就诊有数据(无视医院就诊的结果)
     * @param warningMsgs
     * @return
     */
    private List<WarningMsg> filterNormalVision(List<WarningMsg> warningMsgs,int dayOffset) {
        Set<Integer> studentIdList = warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toSet());
        Date todayTime = DateUtil.getTodayTime(10, 30);
        Date thirdtyDaysAgoTime = DateUtil.offsetDay(todayTime,dayOffset);
        //过滤掉有就诊记录的数据
        Set<Integer> medicalRecordStudentIds = medicalRecordService.getMedicalRecordStudentIds(studentIdList, todayTime, thirdtyDaysAgoTime);
        studentIdList.removeAll(medicalRecordStudentIds);
        //过滤掉已经正常的数据
        Set<Integer> hasNormalVisionStudentIds = statConclusionService.getHasNormalVisionStudentIds(studentIdList);
        studentIdList.removeAll(hasNormalVisionStudentIds);
        if (CollectionUtils.isEmpty(studentIdList)) {
            return Collections.emptyList();
        }
        return warningMsgs.stream().filter(warningMsg -> studentIdList.contains(warningMsg.getStudentId())).collect(Collectors.toList());
    }

    /**
     * 视力警告数据
     * @param studentIdSet
     */
    private List<WarningMsg> getInitWarningMsgList(Set<Integer> studentIdSet) {
        if (CollectionUtils.isEmpty(studentIdSet)) {
            return Collections.emptyList();
        }
        List<WarningMsg> warningMsgList = new ArrayList<>(studentIdSet.size());
        for (Integer studentId: studentIdSet) {
            WarningMsg warningMsg = new WarningMsg();
            //模板待修改
            warningMsg.setMsgTemplateId(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION.getMsgCode());
            warningMsg.setStudentId(studentId);
            warningMsg.setSendTimes(0);
            warningMsgList.add(warningMsg);
        }
        return warningMsgList;
    }
}