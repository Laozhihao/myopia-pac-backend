package com.wupol.myopia.business.common.utils.util;

/**
 * TwoTuple
 *
 * @author Simple4H
 */
public class TwoTuple<A, B> {

    private A first;

    private B second;

    public TwoTuple() {
    }

    public TwoTuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    public static <A, B> TwoTuple<A, B> of(A first, B second) {
        return new TwoTuple<>(first, second);
    }

    public A getFirst() {
        return first;
    }

    public void setFirst(A first) {
        this.first = first;
    }

    public B getSecond() {
        return second;
    }

    public void setSecond(B second) {
        this.second = second;
    }

    @Override
    public String toString() {
        return "TwoTuple{" +
                "first=" + first +
                ", second=" + second +
                '}';
    }
}
