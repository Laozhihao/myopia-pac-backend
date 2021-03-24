package com.wupol.myopia.business.management.service;

import com.wupol.myopia.base.cache.RedisUtil;
import com.wupol.myopia.business.management.util.S3Utils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author HaoHao
 * @Date 2021/3/20
 **/
@Log4j2
@Service
public class ReportService {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DistrictService districtService;
    @Autowired
    private NoticeService noticeService;
    @Autowired
    private S3Utils s3Utils;
    @Autowired
    private StatConclusionService statConclusionService;
    @Autowired
    private SchoolService schoolService;
    @Autowired
    private ScreeningOrganizationService screeningOrganizationService;

}
