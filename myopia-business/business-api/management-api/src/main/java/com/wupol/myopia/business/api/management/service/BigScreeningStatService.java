package com.wupol.myopia.business.api.management.service;

import com.wupol.myopia.business.api.management.domain.builder.BigScreenStatDataBuilder;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import com.wupol.myopia.business.core.screening.flow.service.StatConclusionService;
import com.wupol.myopia.business.core.stat.domain.dto.BigScreenStatDataDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author HaoHao
 * @Date 2021/4/27
 **/
@Service
public class BigScreeningStatService {

    @Autowired
    private StatConclusionService statConclusionService;

    /**
     * 获取通知
     * @param noticeId
     * @param noticeId
     * @return
     */
    public List<BigScreenStatDataDTO> getByNoticeIdAndDistrictIds(Integer noticeId) throws IOException {
        List<StatConclusion> statConclusionList = statConclusionService.findByList(new StatConclusion().setSrcScreeningNoticeId(noticeId).setIsRescreen(false));
        return this.getBigScreenStatDataDTOList(statConclusionList);
    }

    /**
     * 获取大屏统计的基础数据
     * @param statConclusionList
     * @return
     */
    private List<BigScreenStatDataDTO> getBigScreenStatDataDTOList(List<StatConclusion> statConclusionList) {
        return statConclusionList.stream().map(BigScreenStatDataBuilder::build).collect(Collectors.toList());
    }
}
