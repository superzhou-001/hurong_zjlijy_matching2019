package hry.cm2.deal;

import com.alibaba.fastjson.JSON;
import hry.cm2.account.model.Cm2Account;
import hry.cm2.account.service.Cm2AccountService;
import hry.cm2.constant.ExchangeDataCacheRedis;
import hry.cm2.dto.CmAccountRedis;
import hry.cm2.util.AccountDeEnUtil;
import hry.cm2.util.CmRedis;
import hry.util.QueryFilter;
import hry.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;

public class CmDealUtil {
    /**
     * 更新币种账户
     */
    public static void setCmAccount(CmAccountRedis CmAccount, redis.clients.jedis.Transaction transaction ){
        String accountKey = ExchangeDataCacheRedis.getAccountKey(CmAccount.getCoinCode());
        String  accountStr=AccountDeEnUtil.encode(JSON.toJSONString(CmAccount));
        transaction.hset(accountKey,CmAccount.getCustomerId()+"",accountStr);
    }


    /***
     *获取账户
     * @param
     * @param coinCode
     * @return
     */
    public static CmAccountRedis getCmAccount(Long customerId, String coinCode) {
        try (Jedis jedis = CmRedis.JEDIS_POOL.getResource()) {
            String accountKey = ExchangeDataCacheRedis.getAccountKey(coinCode);
            String accountStr = jedis.hget(accountKey, customerId + "");
            if (StringUtils.isNotEmpty(accountStr)) {
                accountStr= AccountDeEnUtil.decode(accountStr);
                return JSON.parseObject(accountStr,  CmAccountRedis.class);
            }

            Cm2AccountService cmAccountService = (Cm2AccountService) SpringUtil.getBean("cm2AccountService");
            synchronized (customerId) {
                QueryFilter filter = new QueryFilter(Cm2Account.class);
                filter.addFilter("customerId=", customerId);
                filter.addFilter("coinCode=", coinCode);
                List<Cm2Account> cmAccountlist = cmAccountService.find(filter);

                if (null != cmAccountlist &&cmAccountlist.size()>0) {
                    Cm2Account binaryAccount=cmAccountlist.get(0);
                    CmAccountRedis cmAccountRedis = JSON.parseObject(JSON.toJSONString(binaryAccount), CmAccountRedis.class);
                    jedis.hset(accountKey, customerId + "", AccountDeEnUtil.encode(JSON.toJSONString(cmAccountRedis)));
                    return cmAccountRedis;
                } else {
                    /**
                     * 创建币账户
                     */
                    Cm2Account account = new  Cm2Account();
                    account.setCoinCode(coinCode);
                    account.setCustomerId(customerId);
                    account.setHotMoney(BigDecimal.ZERO);
                    account.setColdMoney(BigDecimal.ZERO);
                    cmAccountService.save(account);

                    CmAccountRedis accountRedis = new CmAccountRedis();
                    BeanUtils.copyProperties(account, accountRedis);
                    jedis.hset(accountKey, customerId + "", AccountDeEnUtil.encode(JSON.toJSONString(accountRedis)));
                    return accountRedis;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
