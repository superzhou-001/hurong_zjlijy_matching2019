package hry.mall.order.model.vo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author luyue
 * @date 2019/12/6 16:39
 */
public class CoinVo   implements Serializable {
    public String coinCode;//币种
    public BigDecimal count;//个数
    public String picturePath;//图片路径

    public String getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(String coinCode) {
        this.coinCode = coinCode;
    }

    public BigDecimal getCount() {
        return count;
    }

    public void setCount(BigDecimal count) {
        this.count = count;
    }

    public String getPicturePath() {
        return picturePath;
    }

    public void setPicturePath(String picturePath) {
        this.picturePath = picturePath;
    }
}
