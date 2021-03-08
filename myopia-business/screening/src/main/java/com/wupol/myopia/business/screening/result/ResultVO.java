package com.wupol.myopia.business.screening.result;

import lombok.Data;

/**
 * Created by 廖师兄
 * 2017-12-10 18:02
 */
@Data
public class ResultVO<T> {

    private Integer code;

    private String message;

    private T data;

}
