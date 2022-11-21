package com.wupol.myopia.business.core.screening.flow.service;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.screening.flow.domain.mapper.DataSubmitMapper;
import com.wupol.myopia.business.core.screening.flow.domain.model.DataSubmit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author Simple4H
 */
@Service
public class DataSubmitService extends BaseService<DataSubmitMapper, DataSubmit> {

    /**
     * 获取列表
     *
     * @param pageRequest 分页
     * @param schoolId    学校Id
     *
     * @return IPage<DataSubmit>
     */
    public IPage<DataSubmit> getList(PageRequest pageRequest, Integer schoolId) {
        LambdaQueryWrapper<DataSubmit> queryWrapper = new LambdaQueryWrapper<DataSubmit>().eq(DataSubmit::getSchoolId, schoolId).orderByDesc(DataSubmit::getCreateTime);
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
        DataSubmit dataSubmit = new DataSubmit();
        dataSubmit.setSchoolId(schoolId);
        dataSubmit.setRemark(DatePattern.PURE_DATE_FORMAT.format(new Date()) + CommonConst.FILE_NAME);
        dataSubmit.setDownloadMessage(CommonConst.FILE_NAME);
        save(dataSubmit);
        return dataSubmit.getId();
    }

}
