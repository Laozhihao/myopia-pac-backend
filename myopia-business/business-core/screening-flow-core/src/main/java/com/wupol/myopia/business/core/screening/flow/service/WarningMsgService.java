package com.wupol.myopia.business.core.screening.flow.service;


import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.DateUtil;
import com.wupol.myopia.business.common.utils.constant.MsgTemplateEnum;
import com.wupol.myopia.business.common.utils.util.TwoTuple;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.WarningMsgMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.domain.model.VisionScreeningResult;
import com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg.STATUS_READY_TO_SEND;
import static com.wupol.myopia.business.core.screening.flow.domain.model.WarningMsg.STATUS_SEND_CANCEL;

/**
 * 视力警告信息
 * @Author jacob
 * @Date 2021-06-08
 */
@Service
@Slf4j
public class WarningMsgService extends BaseService<WarningMsgMapper, WarningMsg> {

    @Autowired
    private WarningMsgMapper warningMsgMapper;
    @Autowired
    private VistelToolsService vistelToolsService;

    /**
     * 获取今天需要发送短信的数据
     *
     * @return
     */
    public List<WarningMsg> needNoticeMsg() {
        return warningMsgMapper.selectNeedToNotice(null,DateUtil.getDayOfYear(new Date(),0), STATUS_READY_TO_SEND);
    }

    /**
     * 增加一条数据
     *
     * @param warningMsgs
     */
    public void addNewOne(List<WarningMsg> warningMsgs) {
        for (WarningMsg warningMsgNextTime : warningMsgs) {
            // 目前只有定时任务使用这个方法,不存在并发时的更新丢失情况, 如若避免更新丢失,可以加上where send_times = @lastTimes
            warningMsgNextTime.setUpdateTime(new Date()).setCreateTime(new Date()).setSendTimes(warningMsgNextTime.getSendTimes() + 1);
        }
        saveBatch(warningMsgs);
    }

    /**
     * 发送短信
     *
     * @param content
     * @param phoneNums
     */
    public boolean sendMsg(String content, List<String> phoneNums) {
        Set<Boolean> resultSet = phoneNums.stream().map(phoneNum -> {
            MsgData msgData = new MsgData(phoneNum, "+86", content);
            //发送短信
            try {
                SmsResult smsResult = vistelToolsService.sendMsg(msgData);
                log.info("发送学生视力预警短信{}，发送内容:{}", (smsResult.isSuccessful() != null && smsResult.isSuccessful()) ? "成功" : "失败", JSON.toJSONString(msgData));
                return smsResult.isSuccessful();
            } catch (Exception exception) {
                log.error("发送学生视力预警短信时,短信服务器异常,发送内容:{}", JSON.toJSONString(msgData), exception);
                return false;
            }
        }).collect(Collectors.toSet());
        return resultSet.contains(false);
    }

    /**
     * 取消短信 分步操作,尽量走索引,避免全表扫描
     * @param studentId
     */
    public void cancelMsg(Integer studentId) {
        if (studentId == null) {
            return;
        }
        //找出明天所有待发送的短信
        List<WarningMsg> warningMsgs = warningMsgMapper.selectNeedToNotice(studentId,DateUtil.getDayOfYear(new Date(),1),STATUS_READY_TO_SEND);
        if (CollectionUtils.isEmpty(warningMsgs)) {
            //明天没有数据
            return;
        }
        //更改状态
        List<WarningMsg> updateWarningMsgList = warningMsgs.stream().map(warningMsg -> warningMsg.setSendStatus(STATUS_SEND_CANCEL).setUpdateTime(new Date())).collect(Collectors.toList());
        updateBatchById(updateWarningMsgList);
    }

    /**
     * 处理预警短信的新增或者取消问题
     * @param visionScreeningResultStatConclusionTwoTuple
     * @throws IOException
     */
    @Async
    public void dealMsg(TwoTuple<VisionScreeningResult, StatConclusion> visionScreeningResultStatConclusionTwoTuple) {
        VisionScreeningResult visionScreeningResult = visionScreeningResultStatConclusionTwoTuple.getFirst();
        StatConclusion statConclusion = visionScreeningResultStatConclusionTwoTuple.getSecond();
        synchronized (WarningMsgService.class) {
            if (statConclusion.getIsVisionWarning() !=null && statConclusion.getIsVisionWarning()) {
                //如果视力过低的话,就通知
                createAndInsertNewOne(visionScreeningResult.getStudentId());
            } else {
                //视力检查正常的话,将明天的数据关闭
                cancelMsg(visionScreeningResult.getStudentId());
            }
        }
    }

    /**
     * 为该学生当天的检查创建一条隔天待发送的短信
     * @param studentId
     */
    private void createAndInsertNewOne(Integer studentId) {
        Date currentDateTime = new Date();
        String tmrDayOfYear = DateUtil.getDayOfYear(currentDateTime, 1);
        WarningMsg warningMsg = new WarningMsg();
        warningMsg.setStudentId(studentId)
                .setUpdateTime(currentDateTime)
                .setSendDayOfYear(tmrDayOfYear)
                .setSendStatus(STATUS_READY_TO_SEND)
                .setMsgTemplateId(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION.getMsgCode());
        saveOrUpdate(warningMsg, new LambdaQueryWrapper<WarningMsg>().eq(WarningMsg::getStudentId,studentId).eq(WarningMsg::getSendDayOfYear,tmrDayOfYear));
    }

    /**
     *  需要重复推送的短信(目前的周期是30天) 限制次数是5次
     * @return
     */
    public List<WarningMsg> needRepeatNoticeMsg(int offsetDays) {
        String dayOfYear = DateUtil.getDayOfYear(new Date(),offsetDays);
        LambdaQueryWrapper<WarningMsg> warningMsgLambdaQueryWrapper = new LambdaQueryWrapper<>();
        warningMsgLambdaQueryWrapper.between(WarningMsg::getSendTimes,1,5);
        warningMsgLambdaQueryWrapper.eq(WarningMsg::getSendDayOfYear,dayOfYear);
        return list(warningMsgLambdaQueryWrapper);
    }

}
