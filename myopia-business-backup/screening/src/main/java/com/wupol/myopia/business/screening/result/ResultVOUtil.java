package com.wupol.myopia.business.screening.result;


import com.wupol.myopia.business.screening.enums.ErrorEnum;

/**
 * Created by 武帅龙
 * 2017-12-10 18:03
 */
public class ResultVOUtil {

    public static ResultVO success(Object object) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(200);
        resultVO.setMessage("成功");
        resultVO.setData(object);
        return resultVO;
    }
    public static ResultVO success(Object object, Integer totalElements, Long totalPages) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(200);
        resultVO.setMessage("成功");
        resultVO.setData(object);
        return resultVO;
    }

    public static ResultVO success() {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(200);
        resultVO.setMessage("成功");
        return resultVO;
    }
     public static ResultVO error() {
         ResultVO resultVO = new ResultVO();
         resultVO.setCode(1);
         resultVO.setMessage("失败");
         return resultVO;
     }
    public static ResultVO error(Integer code, String message) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(code);
        resultVO.setMessage(message);
        return resultVO;
    }
    public static ResultVO error(ErrorEnum errorEnum) {
        ResultVO resultVO = new ResultVO();
        resultVO.setCode(errorEnum.getCode());
        resultVO.setMessage(errorEnum.getMessage());
        return resultVO;
    }
}
