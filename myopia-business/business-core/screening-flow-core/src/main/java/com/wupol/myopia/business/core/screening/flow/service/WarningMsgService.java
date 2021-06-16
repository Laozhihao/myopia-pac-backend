package com.wupol.myopia.business.core.screening.flow.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wupol.framework.api.service.VistelToolsService;
import com.wupol.framework.sms.domain.dto.MsgData;
import com.wupol.framework.sms.domain.dto.SmsResult;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.base.util.BeanCopyUtil;
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
        Date todayDate = DateUtil.getTodayDate(new Date());
        return warningMsgMapper.selectNeedToNotice(null,todayDate.getTime(), STATUS_READY_TO_SEND);
    }

    /**
     * 更新发送的状态
     *
     * @return
     * @throws IOException
     */
    public List<WarningMsg> updateStatus() throws IOException {
        LambdaQueryWrapper<WarningMsg> warningMsgLambdaQueryWrapper = new LambdaQueryWrapper<>();
        WarningMsg warningMsg = new WarningMsg();
        warningMsg.setSendStatus(STATUS_READY_TO_SEND);
        warningMsgLambdaQueryWrapper.setEntity(warningMsg);
        return findByList(warningMsg);
    }

    /**
     * 为这些数据增加新的30天的数据
     *
     * @param warningMsgs
     * @return
     * @throws IOException
     */
    public void updateStatus(List<WarningMsg> warningMsgs) {
        warningMsgs.forEach(warningMsg -> {
            //设置查询条件是updateTime 保持一致(类似compareAndSet)
            boolean isChanged = checkUpdateVersion(warningMsg);
            if (isChanged) {
                //表示这里已经被修改过了.(可能是筛查app更新数据,或者其他sql更新)
                return;
            }
            //插入一条新的数据
            WarningMsg warningMsgNextTime = BeanCopyUtil.copyBeanPropertise(warningMsg, WarningMsg.class);
            warningMsgNextTime.setSendStatus(STATUS_READY_TO_SEND).setUpdateTime(new Date()).setCreateTime(new Date()).setSendTime(new Date());
            super.save(warningMsgNextTime);
        });
    }


    /**
     * 检查要监视的数据有没有被动过
     *
     * @param warningMsg
     * @return
     */
    private boolean checkUpdateVersion(WarningMsg warningMsg) {
        //这里的条件是只要  学生id noticeID planId status, 主要是看状态有没有被人动过
        try {
            return findOne(warningMsg) == null;
        } catch (IOException e) {
            //todo 待辛谋等同学确定要不要去掉这个额问题
            log.error("异常", e);
            return false;
        }
    }

    /**
     * 更新状态
     *
     * @param id
     * @param sendStatus
     */
    public void updateStatus(Integer id, Integer sendStatus) {
        updateById(new WarningMsg().setSendStatus(sendStatus).setId(id));
    }

    /**
     * 为下一次增加一条
     *
     * @param warningMsgs
     */
    public void addNewOne(List<WarningMsg> warningMsgs) {
        warningMsgs.stream().forEach(warningMsgNextTime -> warningMsgNextTime.setUpdateTime(new Date()).setCreateTime(new Date()).setSendTime(new Date()));
        saveBatch(warningMsgs);
    }

    /**
     * 发送并更新状态
     *
     * @param warningMsg
     * @param content
     * @param phoneNums
     */
    public void sengMsgAndUpdateStatus(WarningMsg warningMsg, String content, List<String> phoneNums) {
        phoneNums.forEach(phoneNum -> {
            MsgData msgData = new MsgData(phoneNum, "+86", content);
            //发送短信
            SmsResult smsResult = vistelToolsService.sendMsg(msgData);
            //获取结果
            if (smsResult.isSuccessful()) {
                log.info("发送学生视力预警短信成功，发送内容:{}", JSONObject.toJSONString(msgData));
                //发送成功的话,更新状态
                updateStatus(warningMsg.getId(), WarningMsg.STATUS_SEND_SUCCESS);
            } else {
                updateStatus(warningMsg.getId(), WarningMsg.STATUS_SEND_FAILURE);
                //发送失败的话,更新状态
                log.error("发送学生视力预警短信成功，发送内容:{}, 异常信息:{}", JSON.toJSONString(msgData), JSON.toJSONString(smsResult));
            }
        });
    }


    /**
     * 发送并更新状态
     *
     * @param content
     * @param phoneNums
     */
    public boolean sendMsg(String content, List<String> phoneNums) {
        Set<Boolean> resultSet = phoneNums.stream().map(phoneNum -> {
            MsgData msgData = new MsgData(phoneNum, "+86", content);
            //发送短信
            SmsResult smsResult = vistelToolsService.sendMsg(msgData);
            log.info("发送学生视力预警短信{}，发送内容:{}", smsResult.isSuccessful() ? "成功" : "失败", JSONObject.toJSONString(msgData));
            return smsResult.isSuccessful();
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
        List<WarningMsg> warningMsgs = warningMsgMapper.selectNeedToNotice(studentId,null,STATUS_READY_TO_SEND);
        if (CollectionUtils.isEmpty(warningMsgs)) {
            return;
        }
        List<WarningMsg> updateWarningMsgList = warningMsgs.stream().filter(warningMsg -> warningMsg.getSendTime()
                .after(DateUtil.getTodayDate(new Date())))
                .map(warningMsg -> warningMsg.setSendStatus(STATUS_SEND_CANCEL)).collect(Collectors.toList());
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
            if ((statConclusion.getVisionL() <= 4.9 || statConclusion.getVisionR() <= 4.9) && statConclusion.getAge() >=6 ) {
                //如果视力过低的话,就通知
                createAndInsertNewOne(visionScreeningResult.getStudentId());
            } else {
                //视力检查正常的话,将当天之后(不包含当天)的所有通知都取消
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
        WarningMsg warningMsg = new WarningMsg();
        Date tmrDate = DateUtil.getOffsetDays(currentDateTime, 1);
        warningMsg.setCreateTime(currentDateTime)
                .setStudentId(studentId)
                .setSendTime(tmrDate)
                .setSendStatus(STATUS_READY_TO_SEND)
                .setMsgTemplateId(MsgTemplateEnum.TO_PARENTS_WARING_KIDS_VISION.getMsgCode());
        saveOrUpdate(warningMsg, new LambdaQueryWrapper<WarningMsg>().eq(WarningMsg::getStudentId,studentId).eq(WarningMsg::getSendTime,tmrDate));
    }

    /**
     *  需要重复推送的短信(目前的周期是30天)
     * @return
     */
    public List<WarningMsg> needRepeatNoticeMsg(int offsetDays) {
        String dayOfYear = DateUtil.getDayOfYear(new Date(),offsetDays);
        LambdaQueryWrapper<WarningMsg> warningMsgLambdaQueryWrapper = new LambdaQueryWrapper<>();
        warningMsgLambdaQueryWrapper.eq(WarningMsg::getSendDayOfYear,dayOfYear);//.eq(WarningMsg::getSendStatus,WarningMsg.STATUS_SEND_SUCCESS);
        return list(warningMsgLambdaQueryWrapper);
    }

}
