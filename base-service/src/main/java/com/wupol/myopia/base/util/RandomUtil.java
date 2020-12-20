package com.wupol.myopia.base.util;

import lombok.experimental.UtilityClass;

import java.util.*;

@UtilityClass
public class RandomUtil {

    /**
     * 随机生成字符串
     * @param length 字符串长度，小于等于36
     * @return
     */
    public static String generateWord(Integer length) {
        String[] beforeShuffle = new String[] { "0", "1", "2", "3", "4", "5", "6", "7",
                "8", "9", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
                "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
                "W", "X", "Y", "Z" };
        List list = Arrays.asList(beforeShuffle);
        Collections.shuffle(list);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            sb.append(list.get(i));
        }
        String afterShuffle = sb.toString();
        return afterShuffle.substring(0, length > 36 ? 36 : length);
    }

    /**
     * 生成不重复的字符串集
     * @param wordLength 每个字符串的长度
     * @param size 个数
     * @return
     */
    public static Set<String> generateWordSet(Integer wordLength, Integer size) {
        Set<String> tokens = new HashSet<>();
        while (tokens.size() < size) {
            tokens.add(RandomUtil.generateWord(wordLength));
        }
        return tokens;
    }

    /**
     * 随机数字
     * @param length 随机数长度，不大于64
     * @return
     */
    public static String generateNumber(Integer length){
        length = length > 64 ? 64 : length;
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for(int i = 0; i < length; i++){
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

}
