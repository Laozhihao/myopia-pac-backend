package com.wupol.myopia.business.common.utils.domain.query;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;

/**
 * 分页基类
 *
 * @author Simple4H
 */
@Setter
@Getter
@Accessors(chain = true)
public class PageRequest {

    @NotNull(message = "页码不能为空")
    private Integer current;

    @NotNull(message = "每页条数不能为空")
    private Integer size;

    public Page<?> toPage() {
        if (null == current) {
            current = 1;
        }
        if (null == size) {
            size = 10;
        }
        return new Page<>(getCurrent(), getSize());
    }
}
