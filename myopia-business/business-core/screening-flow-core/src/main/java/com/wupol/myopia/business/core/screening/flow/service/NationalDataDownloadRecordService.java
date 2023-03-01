package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.constant.NationalDataDownloadStatusEnum;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.NationalDataDownloadRecordMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.NationalDataDownloadRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Objects;

/**
 * @author Simple4H
 */
@Service
public class NationalDataDownloadRecordService extends BaseService<NationalDataDownloadRecordMapper, NationalDataDownloadRecord> {

    /**
     * 获取列表
     *
     * @param pageRequest 分页
     * @param schoolId    学校Id
     *
     * @return IPage<DataSubmit>
     */
    public IPage<NationalDataDownloadRecord> getList(PageRequest pageRequest, Integer schoolId,Integer screeningPlanId) {
        LambdaQueryWrapper<NationalDataDownloadRecord> queryWrapper = new LambdaQueryWrapper<NationalDataDownloadRecord>().eq(NationalDataDownloadRecord::getSchoolId, schoolId).orderByDesc(NationalDataDownloadRecord::getCreateTime);
        if (Objects.nonNull(screeningPlanId)) {
            queryWrapper.eq(NationalDataDownloadRecord::getScreeningPlanId, screeningPlanId);
        }
        return baseMapper.selectPage(pageRequest.getPage(), queryWrapper);
    }

    /**
     * 创建数据上报记录
     *
     * @param schoolId 学校Id
     *
     * @return id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createNewDataSubmit(Integer schoolId,Integer screeningPlanId) {
        NationalDataDownloadRecord nationalDataDownloadRecord = new NationalDataDownloadRecord();
        nationalDataDownloadRecord.setScreeningPlanId(screeningPlanId);
        nationalDataDownloadRecord.setSchoolId(schoolId);
        nationalDataDownloadRecord.setRemark(DatePattern.PURE_DATE_FORMAT.format(new Date()) + CommonConst.FILE_NAME);
        nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.CREATE.getType());
        save(nationalDataDownloadRecord);
        return nationalDataDownloadRecord.getId();
    }

    /**
     * 创建或者更新上报记录
     * @param screeningPlanId 筛查计划id
     * @param schoolId 学校id
     * @return id
     */
    @Transactional(rollbackFor = Exception.class)
    public Integer createOrUpdateDataSubmit(Integer screeningPlanId, Integer schoolId) {
        NationalDataDownloadRecord nationalDataDownloadRecord = findOne(new NationalDataDownloadRecord().setScreeningPlanId(screeningPlanId).setSchoolId(schoolId));
        if (Objects.isNull(nationalDataDownloadRecord)){
            return createNewDataSubmit(schoolId,screeningPlanId);
        }
        nationalDataDownloadRecord.setRemark(DatePattern.PURE_DATE_FORMAT.format(new Date()) + CommonConst.FILE_NAME);
        nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.CREATE.getType());
        nationalDataDownloadRecord.setUpdateTime(new Date());
        updateById(nationalDataDownloadRecord);
        return nationalDataDownloadRecord.getId();
    }
}
