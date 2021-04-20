package com.wupol.myopia.business.screening.domain.vo;


import lombok.Data;
import java.util.Date;

@Data

public class EyeDiseaseVO {

    private String id;

    private String eye;

    private String name;

    private Date createTime;

}
