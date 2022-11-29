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
    public IPage<NationalDataDownloadRecord> getList(PageRequest pageRequest, Integer schoolId) {
        LambdaQueryWrapper<NationalDataDownloadRecord> queryWrapper = new LambdaQueryWrapper<NationalDataDownloadRecord>().eq(NationalDataDownloadRecord::getSchoolId, schoolId).orderByDesc(NationalDataDownloadRecord::getCreateTime);
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
    public Integer createNewDataSubmit(Integer schoolId) {
        NationalDataDownloadRecord nationalDataDownloadRecord = new NationalDataDownloadRecord();
        nationalDataDownloadRecord.setSchoolId(schoolId);
        nationalDataDownloadRecord.setRemark(DatePattern.PURE_DATE_FORMAT.format(new Date()) + CommonConst.FILE_NAME);
        nationalDataDownloadRecord.setStatus(NationalDataDownloadStatusEnum.CREATE.getType());
        save(nationalDataDownloadRecord);
        return nationalDataDownloadRecord.getId();
    }

}
