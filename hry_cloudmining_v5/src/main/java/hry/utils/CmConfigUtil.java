package hry.utils;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import hry.cm5.constant.ExchangeDataCacheRedis;
import hry.cm5.util.CmRedis;
import hry.core.constant.StringConstant;
import hry.manage.remote.model.FeixiaohaoPriceVo;
import hry.redis.common.utils.RedisService;
import hry.util.SpringUtil;
import hry.util.sys.ContextUtil;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CmConfigUtil {
    /**
     * 李金沅项目配置
     * */
    public final static String platformKey = "CMV5";//app_config 【typeKey】
    //项目基础币种配置
    public final static String baseCoinKey = "CMV5_base_coin_config";//app_config 【typeKey】
    //基础币种
    public final static String payCoinCode = "payCoinCode"; //支付/积分币种
    public final static String pricingCoinCode = "pricingCoinCode"; //矿机定价币种
    public final static String usCoinCode = "usCoinCode"; //US算力奖励币种
    public final static String uskcCoinCode = "uskcCoinCode"; //USKC算力奖励币种
    public final static String pipeCoinCode = "pipeCoinCode"; //管道算力奖励币种

    /***
     * 所有币种的key值
     * @return
     */
    public static  Set<String>  getAllConskeysList(){

        try (Jedis jedis = CmRedis.JEDIS_POOL.getResource()) {
            String cmKeyheader= ExchangeDataCacheRedis.getCoinConfig();
            String str=jedis.get(cmKeyheader);
            List<String> productList = JSON.parseArray(str, String.class);
            Set<String> set = new HashSet(productList);
            return set;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    /***
     * 余额  币种的位数
     * @param coinCode
     * @return
     */
    public static int getCoinKeepDecimal(String coinCode){
        int keepDecimalForCurrency=4;
        try (Jedis jedis = CmRedis.JEDIS_POOL.getResource()) {
            String coincodeKeyheader= ExchangeDataCacheRedis.getCoinConfig();
           String decialm= jedis.hget(coincodeKeyheader,coinCode);
           if(StringUtils.isNotEmpty(decialm)){
               return Integer.valueOf(decialm);
           }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return keepDecimalForCurrency;
    }

    /**
     * 获取币种对应的定价金额（默认: USDT）--- 李金沅项目
     * */
    public static BigDecimal getPricingPrice(String coinCode) {
        BigDecimal pricingPrice = new BigDecimal("0");
        RedisService redisService = (RedisService) ContextUtil.getBean("redisService");
        String coinJson = redisService.get("cm:coinCodeList");
        JSONArray jsonArray = JSON.parseArray(coinJson);
        for (Object obj : jsonArray) {
            JSONObject json = JSON.parseObject(obj.toString());
            if (coinCode.equals(json.getString("coinCode"))) {
                pricingPrice = new BigDecimal(json.getString("pricingPrice"));
                break;
            }
        }
        return pricingPrice;
    }

    /**
     * 获取项目基础配置币种---李金沅项目
     * */
    public static String getConfigCoinCode(String key) {
        RedisService redisService = (RedisService) ContextUtil.getBean("redisService");
        String jsonStr = redisService.get(StringConstant.CONFIG_CACHE + ":" +baseCoinKey);
        JSONArray obj = JSON.parseArray(jsonStr);
        for(Object o:obj) {
            JSONObject oo = JSON.parseObject(o.toString());
            if (oo.getString("configkey").equals(key)) {
                return oo.getString("value");
            }
        }
        return "";
    }

    /**
     * 获取币种对应人民币价钱
     * @param coinCode
     * @return
     */
    public static BigDecimal getCoinRate(String coinCode) {
        // TODO Auto-generated method stub
        // 获取 平台币
        String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");
        // 获取 平台币市价(RMB)
        String platformMarkPriceStr = BaseConfUtil.getConfigSingle("platformMarkPrice", "configCache:basefinanceConfig");
        BigDecimal platformMarkPrice = new BigDecimal(platformMarkPriceStr);
        BigDecimal codePrice = BigDecimal.ZERO;
        // 如果是平台币，取平台币市价(RMB)
        if (coinCode.equals(platCoin)) {
            codePrice = platformMarkPrice;
        } else {
            // 如果是其它币，取非小号
            RedisService redisService = (RedisService) ContextUtil.getBean("redisService");
            String result = redisService.get("cn:newFeixiaohaoPrice");
            if (hry.util.StringUtil.isNull(result)) {
                List<FeixiaohaoPriceVo> list = JSON.parseArray(result, FeixiaohaoPriceVo.class);
                for (FeixiaohaoPriceVo feixiaohaoPriceVo : list) {
                    String name = feixiaohaoPriceVo.getName();
                    // 如果是自定义币，取非小号的Price为空会报错，做个判断
                    if (name.equals(coinCode) && feixiaohaoPriceVo.getPrice() != null) {
                        codePrice = new BigDecimal(feixiaohaoPriceVo.getPrice());
                        return codePrice;
                    }
                }
            } else {
                System.out.println("未查询到redis中" + coinCode + "行情信息");
                return BigDecimal.ZERO;
            }

        }
        return codePrice;
    }
}
