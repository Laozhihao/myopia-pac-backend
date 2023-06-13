package com.wupol.myopia.business.api.management.service.report.plan.myopia;

import com.wupol.framework.core.util.CollectionUtils;
import com.wupol.myopia.base.util.BigDecimalUtil;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.common.RadioAndCount;
import com.wupol.myopia.business.api.management.domain.dto.report.vision.plan.myopia.*;
import com.wupol.myopia.business.api.management.service.report.CommonReportService;
import com.wupol.myopia.business.api.management.util.RadioAndCountUtil;
import com.wupol.myopia.business.common.utils.constant.CommonConst;
import com.wupol.myopia.business.common.utils.constant.GenderEnum;
import com.wupol.myopia.business.core.school.domain.model.School;
import com.wupol.myopia.business.core.school.service.SchoolService;
import com.wupol.myopia.business.core.screening.flow.domain.model.StatConclusion;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 近视情况
 *
 * @author Simple4H
 */
@Service
public class MyopiaPlanReportService {

    @Resource
    private CommonReportService commonReportService;

    @Resource
    private SchoolService schoolService;


    /**
     * 近视情况
     *
     * @param statConclusions 筛查数据
     * @return MyopiaSituation
     */
    public MyopiaSituation getMyopiaSituation(List<StatConclusion> statConclusions) {

        List<StatConclusion> primaryStatConclusion = commonReportService.getPrimaryStatConclusion(statConclusions);
        if (CollectionUtils.isEmpty(primaryStatConclusion)) {
            return null;
        }
        MyopiaSituation myopiaSituation = new MyopiaSituation();
        myopiaSituation.setValidCount((long) primaryStatConclusion.size());
        RadioAndCount myopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(primaryStatConclusion);
        myopiaSituation.setMyopiaCount(myopiaRadioAndCount.getCount());
        myopiaSituation.setMyopiaRadio(myopiaRadioAndCount.getRadio());


        myopiaSituation.setModule1(generateModule1(primaryStatConclusion));
        myopiaSituation.setModule2(generateModule2(primaryStatConclusion));
        myopiaSituation.setModule21(generateModule21(primaryStatConclusion));
        myopiaSituation.setModule22(generateModule22(primaryStatConclusion));
//        myopiaSituation.setModule3();
//        myopiaSituation.setModule4();
//        myopiaSituation.setModule5();
//        myopiaSituation.setModule6();
//        myopiaSituation.setModule7();
        return myopiaSituation;
    }

    /**
     * 小学及以上教育阶段不同性别近视情况
     *
     * @param statConclusions 筛查数据
     * @return Module1
     */
    private Module1 generateModule1(List<StatConclusion> statConclusions) {
        Module1 module1 = new Module1();
        module1.setTable(getModule1TableItemList(statConclusions));
        return module1;
    }

    /**
     * 获取列表
     *
     * @param statConclusions 筛查数据
     * @return List<Module1.TableItem>
     */
    private List<Module1.TableItem> getModule1TableItemList(List<StatConclusion> statConclusions) {
        Map<Integer, List<StatConclusion>> genderMap = statConclusions.stream().collect(Collectors.groupingBy(StatConclusion::getGender));
        List<Module1.TableItem> itemList = GenderEnum.genderList().stream().map(genderEnum -> getModule1TableItem(genderEnum.desc, genderMap.getOrDefault(genderEnum.type, new ArrayList<>()))).collect(Collectors.toList());
        itemList.add(getModule1TableItem(CommonConst.TOTAL, statConclusions));
        return itemList;
    }

    /**
     * 获取表格
     *
     * @param desc            描述
     * @param statConclusions 筛查数据
     * @return Module1.TableItem
     */
    private Module1.TableItem getModule1TableItem(String desc, List<StatConclusion> statConclusions) {
        Module1.TableItem tableItem = new Module1.TableItem();
        tableItem.setDesc(desc);
        tableItem.setValidCount((long) statConclusions.size());
        RadioAndCount myopiaRadioAndCount = RadioAndCountUtil.getMyopiaRadioAndCount(statConclusions);
        Long myopiaCount = myopiaRadioAndCount.getCount();
        tableItem.setMyopiaCount(myopiaCount);
        tableItem.setMyopiaRadio(myopiaRadioAndCount.getRadio());

        RadioAndCount lightMyopiaRadioAndCount = RadioAndCountUtil.getLightMyopiaRadioAndCount(statConclusions);
        Long lightMyopiaCount = lightMyopiaRadioAndCount.getCount();
        tableItem.setLightMyopiaCount(lightMyopiaCount);
        tableItem.setLightMyopiaRadio(lightMyopiaRadioAndCount.getRadio());
        tableItem.setLightMyopiaProportion(BigDecimalUtil.divide(lightMyopiaCount, myopiaCount));

        RadioAndCount highMyopiaRadioAndCount = RadioAndCountUtil.getHighMyopiaRadioAndCount(statConclusions);
        tableItem.setHighMyopiaRadio(highMyopiaRadioAndCount.getRadio());
        Long highMyopiaCount = highMyopiaRadioAndCount.getCount();
        tableItem.setHighMyopiaCount(highMyopiaCount);
        tableItem.setHighMyopiaProportion(BigDecimalUtil.divide(highMyopiaCount, myopiaCount));
        return tableItem;
    }

    /**
     * 小学及以上教育阶段各年级学生近视情况
     *
     * @param statConclusions 筛查结果
     * @return Module2
     */
    private Module2 generateModule2(List<StatConclusion> statConclusions) {
        Module2 module2 = new Module2();
        module2.setTable(new Module2.TableItem().getGradeTableList(statConclusions));
        return module2;
    }

    /**
     * 小学及以上教育阶段各年级学生近视分布情况
     *
     * @param statConclusions 筛查结果
     * @return Module2
     */
    private Module21 generateModule21(List<StatConclusion> statConclusions) {
        Module21 module21 = new Module21();
        module21.setTable(new Module21.TableItem().getGradeTableList(statConclusions));
        return module21;
    }

    /**
     * 各学校近视率情况
     *
     * @param statConclusions 筛查结果
     * @return Module2
     */
    private Module22 generateModule22(List<StatConclusion> statConclusions) {
        Map<Integer, School> schoolMap = schoolService.getSchoolMap(statConclusions, StatConclusion::getSchoolId);
        Module22 module22 = new Module22();
        module22.setTable(new Module22.TableItem().getSchoolTableList(statConclusions, schoolMap));
        return module22;
    }
}
