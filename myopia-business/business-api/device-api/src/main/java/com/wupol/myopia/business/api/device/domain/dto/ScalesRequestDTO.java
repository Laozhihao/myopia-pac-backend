package com.wupol.myopia.business.api.device.domain.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * TODO:
 *
 * @author Simple4H
 */
@Getter
@Setter
public class ScalesRequestDTO {

    private String action;

    private String deviceID;

    private List<ScalesData> datas;

    @Getter
    @Setter
    static class ScalesData {

        private String UID;

        private String name;

        private String age;

        private String sex;

        private String headImgStr;

        private String occurTime;

        private BmiData BMI;
    }

    @Getter
    @Setter
    static class BmiData {
        private String height;

        private String weight;

        private String bmi;
    }


}
