package hry.utils;


import com.alibaba.fastjson.JSON;
import hry.cm4.constant.ExchangeDataCacheRedis;
import hry.cm4.util.CmRedis;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CmConfigUtil {


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
}
