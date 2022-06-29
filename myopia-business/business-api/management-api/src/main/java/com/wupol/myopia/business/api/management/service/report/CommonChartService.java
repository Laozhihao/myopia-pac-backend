package com.wupol.myopia.business.api.management.service.report;

import com.google.common.collect.Lists;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.common.ChartDetail;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.school.AstigmatismTable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * CommonChartService
 *
 * @author Simple4H
 */
@Service
public class CommonChartService {

    public List<ChartDetail> astigmatismTableChartDetail(List<AstigmatismTable> tables) {
        return Lists.newArrayList(
                new ChartDetail("近视前期", tables.stream().map(AstigmatismTable::getEarlyMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("低度近视", tables.stream().map(AstigmatismTable::getLightMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("高度近视", tables.stream().map(AstigmatismTable::getHighMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("近视", tables.stream().map(AstigmatismTable::getMyopiaProportion).collect(Collectors.toList())),
                new ChartDetail("散光", tables.stream().map(AstigmatismTable::getAstigmatismProportion).collect(Collectors.toList()))
        );
    }
}
