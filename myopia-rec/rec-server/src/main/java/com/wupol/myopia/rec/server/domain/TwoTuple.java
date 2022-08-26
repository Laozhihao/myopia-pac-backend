package com.wupol.myopia.rec.server.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author hang.yuan
 * @date 2022/8/12
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TwoTuple<A, B> {

    private A first;

    private B second;

    public static <A, B> TwoTuple<A, B> of(A first, B second) {
        return new TwoTuple<>(first, second);
    }

}