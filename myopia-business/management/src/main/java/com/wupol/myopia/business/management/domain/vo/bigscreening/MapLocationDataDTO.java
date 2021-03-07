package com.wupol.myopia.business.management.domain.vo.bigscreening;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @Description
 * @Date 2021/3/7 20:47
 * @Author by Jacob
 */
@Data
public class MapLocationDataDTO {

    private String name;
    /**
     * value
     */
    private Integer value;
    /**
     * coords
     */
    private List<List<Double>> coords;
}
