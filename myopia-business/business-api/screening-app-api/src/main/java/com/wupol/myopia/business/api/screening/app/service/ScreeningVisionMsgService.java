package com.wupol.myopia.business.api.screening.app.service;

import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.MsgContentUtil;
import com.wupol.myopia.business.core.school.domain.dto.StudentBasicInfoDTO;
import com.wupol.myopia.business.core.school.service.StudentService;
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
    private StatConclusionService statConclusionService;

    /**
     * 每天发送警告短信
     */
    public void sendWarningMsg() {
        // 找出需要发送短信的数据
        Set<Integer> studentIdSet = statConclusionService.getNeedToSendWarningMsgStudentIds();
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


    /**
     * 查找某天前的学生异常的记录,进行重新检查是否需要短信提醒.
     * 短信重新提醒的条件:
     *  1.无医院就诊记录
     *  2.最新的一次筛查结果视力达到短信提醒视力异常的标准
     * @param dayOfYear 一年的第几天
     */
    public void repeatNoticeWarningMsg(int dayOfYear) {
        List<WarningMsg> warningMsgs = warningMsgService.needRepeatNoticeMsg(dayOfYear);
        // 过滤掉正常的数据,过滤掉正常数据后,不会插入到warningMsg表中,这意味着,该学生这段时间检查的异常短信发送的生命周期结束
        warningMsgs = filterNormalVision(warningMsgs);
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
            String content = MsgContentUtil.getMsgContent(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION, studentBasicInfoDTO.getStudentName());
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
    private List<WarningMsg> filterNormalVision(List<WarningMsg> warningMsgs) {
        List<Integer> studentIdList = warningMsgs.stream().map(WarningMsg::getStudentId).collect(Collectors.toList());
        Map<Integer, Boolean> studentIdVisionWarningMap = statConclusionService.getByStudentIds(studentIdList);
        return warningMsgs.stream().filter(warningMsg -> studentIdVisionWarningMap.get(warningMsg.getStudentId())).collect(Collectors.toList());
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
            warningMsg.setMsgTemplateId(1);
            warningMsg.setStudentId(studentId);
            warningMsg.setSendTimes(0);
            warningMsgList.add(warningMsg);
        }
        return warningMsgList;
    }
}