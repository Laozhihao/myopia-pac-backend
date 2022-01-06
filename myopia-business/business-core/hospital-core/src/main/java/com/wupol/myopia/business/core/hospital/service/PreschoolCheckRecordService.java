package com.wupol.myopia.business.core.hospital.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.wupol.myopia.base.service.BaseService;
import com.wupol.myopia.business.common.utils.domain.query.PageRequest;
import com.wupol.myopia.business.core.hospital.domain.dto.PreschoolCheckRecordDTO;
import com.wupol.myopia.business.core.hospital.domain.mapper.PreschoolCheckRecordMapper;
import com.wupol.myopia.business.core.hospital.domain.model.PreschoolCheckRecord;
import com.wupol.myopia.business.core.hospital.domain.query.PreschoolCheckRecordQuery;
import org.springframework.stereotype.Service;


/**
 * @Author wulizhou
 * @Date 2022-01-04
 */
@Service
public class PreschoolCheckRecordService extends BaseService<PreschoolCheckRecordMapper, PreschoolCheckRecord> {

    /**
     * 获取眼保健详情
     * @param id
     * @return
     */
    public PreschoolCheckRecordDTO getDetails(Integer id) {
        PreschoolCheckRecordDTO details = baseMapper.getDetails(id);
        // TODO wulizhou
        details.setConclusion("正常");
        details.setDoctorsName("");
        return details;
    }

    /**
     * 获取眼保健列表
     * @param pageRequest
     * @param query
     * @return
     */
    public IPage<PreschoolCheckRecordDTO> getList(PageRequest pageRequest, PreschoolCheckRecordQuery query) {
        IPage<PreschoolCheckRecordDTO> records = baseMapper.getListByCondition(pageRequest.toPage(), query);
        // TODO wulizhou
        records.getRecords().forEach(record -> {
            record.setConclusion("正常");
            record.setReferralConclusion("sdfsdf");
            record.setDoctorsName("");
        });
        return records;
    }

}
