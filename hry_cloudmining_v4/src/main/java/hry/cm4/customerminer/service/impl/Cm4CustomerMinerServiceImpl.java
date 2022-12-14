/**
 * Copyright:
 *
 * @author: yaozh
 * @version: V1.0
 * @Date: 2019-11-21 10:00:27
 */
package hry.cm4.customerminer.service.impl;

import com.alibaba.fastjson.JSON;
import hry.bean.ApiJsonResult;
import hry.cm4.customer.model.Cm4Customer;
import hry.cm4.customerminer.dao.Cm4CustomerMinerDao;
import hry.cm4.customerminer.model.Cm4CustomerMiner;
import hry.cm4.customerminer.service.Cm4CustomerMinerProfitService;
import hry.cm4.customerminer.service.Cm4CustomerMinerService;
import hry.cm4.deal.CmDealUtil;
import hry.cm4.dto.Accountadd;
import hry.cm4.dto.CmAccountRedis;
import hry.cm4.enums.CmAccountRemarkEnum;
import hry.cm4.log.service.Cm4ExceptionLogService;
import hry.cm4.mq.model.CoinRewardMessage;
import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;

import javax.annotation.Resource;

import hry.manage.remote.model.FeixiaohaoPriceVo;
import hry.mq.producer.DealMsgUtil;
import hry.mq.producer.service.MessageProducer;
import hry.redis.common.utils.RedisService;
import hry.util.SpringUtil;
import hry.utils.BaseConfUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> Cm4CustomerMinerService </p>
 * @author: yaozh
 * @Date :          2019-11-21 10:00:27  
 */
@Service("cm4CustomerMinerService")
public class Cm4CustomerMinerServiceImpl extends BaseServiceImpl<Cm4CustomerMiner, Long> implements Cm4CustomerMinerService {

    @Resource(name = "cm4CustomerMinerDao")
    @Override
    public void setDao(BaseDao<Cm4CustomerMiner, Long> dao) {
        super.dao = dao;
    }

    @Resource
    private Cm4ExceptionLogService cmExceptionLogService;
    @Resource
    private Cm4CustomerMinerProfitService cmCustomerMinerProfitService;

    @Resource
    private MessageProducer messageProducer;
    @Resource
    private RedisService redisService;


    @Override
    public void minerCoinageByDay(String message) {
        // TODO Auto-generated method stub
        try {
            Cm4CustomerMiner cmCustomerMiner = JSON.parseObject(message, Cm4CustomerMiner.class);
            /**????????????????????????minerProfitType1???????????? 2????????????
             * 1.??????????????????????????????????????????
             * 		??????profit1 profit3??????
             * 		???????????????
             * 2.????????????????????????????????????????????????mq???????????????
             * 		??????profit2 profit3??????
             * */

            //????????????????????? ???USDT???
            BigDecimal dayProfit = cmCustomerMiner.getDayProfit();
            //??????USDT?????????????????????
            BigDecimal usdtPrice = this.getCoinRate("USDT");

            if (usdtPrice.equals(BigDecimal.ZERO)) {
                throw new Exception("USDT????????????");
            }
            //???????????????????????????
            //??????????????????
            Integer retentionNumber = Integer.valueOf(redisService.get("retentionNumber:" + "USDT"));
            BigDecimal profitPrice = dayProfit.multiply(usdtPrice).setScale(retentionNumber, BigDecimal.ROUND_FLOOR);

            //????????????????????????
            String coinCodeAll = cmCustomerMiner.getProfitCoinCode();
            String[] coinCodes = coinCodeAll.split(",");
            //??????????????????????????????
            String profitProportionAll = cmCustomerMiner.getProfitProportionAll();
            String[] profitProportions = profitProportionAll.split(",");
            if (coinCodes.length != profitProportions.length) {
                throw new Exception("??????????????????????????????");
            }

            //????????????????????????
            Map<String, Object> map = new HashMap<>();//??????????????????
            String coinCode = "";//??????
            BigDecimal profitProportion = BigDecimal.ZERO;//???????????????
            List<Accountadd> accountaddList = new ArrayList<>();
            for (int i = 0; i < coinCodes.length; i++) {
                coinCode = coinCodes[i];
                profitProportion = new BigDecimal(profitProportions[i]).multiply(new BigDecimal(0.01));
                //??????????????????
                Integer retention = Integer.valueOf(redisService.get("retentionNumber:" + "USDT"));
                //???????????????????????????????????????
                BigDecimal rmbPrice = profitPrice.multiply(profitProportion).setScale(retention, BigDecimal.ROUND_FLOOR);
                //??????????????????
                BigDecimal coinPrice = this.getCoinRate(coinCode);
                //???????????????????????????????????????????????????
                BigDecimal coinNum = rmbPrice.divide(coinPrice, retention, BigDecimal.ROUND_FLOOR);
                map.clear();
                map.put(coinCode, coinPrice);
                map.put("USDT", usdtPrice);
                if (cmCustomerMiner.getMinerProfitType() == 1) {
                    //?????????????????? //????????????
                    cmCustomerMinerProfitService.insertProit(cmCustomerMiner, 2, coinCode, profitProportion, map.toString(), coinNum);
                } else if (cmCustomerMiner.getMinerProfitType() == 2) {
                    //?????????????????? //????????????
                    cmCustomerMinerProfitService.insertProit(cmCustomerMiner, 3, coinCode, profitProportion, map.toString(), coinNum);
                    //3.?????????????????????
                    CoinRewardMessage coinRewardMessage = new CoinRewardMessage();
                    coinRewardMessage.setBusinessNum("");
                    coinRewardMessage.setBusinessType("");
                    coinRewardMessage.setCoinCode(coinCode);
                    coinRewardMessage.setCustomerId(cmCustomerMiner.getCustomerId());
                    coinRewardMessage.setRewardNum(coinNum);
                    coinRewardMessage.setRewardType(10);
                    messageProducer.toSocialRewardKey(JSON.toJSONString(coinRewardMessage));
                }
                //3.???????????????
                /** ????????????????????????????????? */
                CmAccountRedis cmAccountRedis = CmDealUtil.getCmAccount(cmCustomerMiner.getCustomerId(), coinCode);
                Accountadd accountadd = new Accountadd(cmCustomerMiner.getCustomerId(), coinCode, cmAccountRedis.getId(),
                        coinNum, 1, CmAccountRemarkEnum.TYPE7.getIndex(),
                        cmCustomerMiner.getTransactionNum());
                accountaddList.add(accountadd);
            }
            //1.??????????????????
            this.updateMinerProfit(cmCustomerMiner.getId(), dayProfit, null, dayProfit);

            if (cmCustomerMiner.getMinerProfitType() == 1) {
                //?????????????????????
                DealMsgUtil.sendAccountaddList(accountaddList);
            }
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
            //??????????????????
            cmExceptionLogService.insertlog(e, "CmCustomerMinerServiceImpl.minerCoinageByDay", message);
        }


    }

    @Override
    public void updateMinerProfit(Long id, BigDecimal profit1, BigDecimal profit2, BigDecimal profit3) {
        // TODO Auto-generated method stub
        if (profit1 == null) {
            profit1 = new BigDecimal(0);
        }
        if (profit2 == null) {
            profit2 = new BigDecimal(0);
        }
        if (profit3 == null) {
            profit3 = new BigDecimal(0);
        }
        ((Cm4CustomerMinerDao) dao).updateMinerProfit(id, profit1, profit2, profit3);
    }

    @Override
    public BigDecimal getMinerPriceSumByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getMinerPriceSumByCustomerId(customerId);
    }

    @Override
    public BigDecimal getTeamMinerPriceSumByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getTeamMinerPriceSumByCustomerId(customerId);
    }

    @Override
    public Cm4Customer getTeamMaxAndMinByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getTeamMaxAndMinByCustomerId(customerId);
    }

    @Override
    public Integer getTeamNumByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getTeamNumByCustomerId(customerId);
    }

    @Override
    public Integer getYouxiaoZtNumByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getYouxiaoZtNumByCustomerId(customerId);
    }

    @Override
    public void updateCloseMiner() {
        // TODO Auto-generated method stub
        ((Cm4CustomerMinerDao) dao).updateCloseMiner();
    }

    @Override
    public void updateWaitMiner() {
        // TODO Auto-generated method stub
        ((Cm4CustomerMinerDao) dao).updateWaitMiner();
    }

    @Override
    public List<Cm4CustomerMiner> getMyMinerCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getMyMinerCustomerId(customerId);
    }

    @Override
    public Integer getMinigNumByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getMinigNumByCustomerId(customerId);
    }

    @Override
    public BigDecimal getMinigProFitByCustomerId(Long customerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getMinigProFitByCustomerId(customerId);
    }

    @Override
    public Integer getMinigNumByCustomerIdAndMinerId(Long customerId, Long minerId) {
        // TODO Auto-generated method stub
        return ((Cm4CustomerMinerDao) dao).getMinigNumByCustomerIdAndMinerId(customerId, minerId);
    }

    @Override
    public BigDecimal getProfitSumByCustomerIdAndHierarchy(Long customerId, Integer hierarchy) {
        return ((Cm4CustomerMinerDao) dao).getProfitSumByCustomerIdAndHierarchy(customerId, hierarchy);
    }

    /**
     * ?????????????????????????????????
     * @param coinCode
     * @return
     */
    private BigDecimal getCoinRate(String coinCode) {
        // TODO Auto-generated method stub
        // ?????? ?????????
        String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");
        // ?????? ???????????????(RMB)
        String platformMarkPriceStr = BaseConfUtil.getConfigSingle("platformMarkPrice", "configCache:basefinanceConfig");
        BigDecimal platformMarkPrice = new BigDecimal(platformMarkPriceStr);
        BigDecimal codePrice = BigDecimal.ZERO;
        // ???????????????????????????????????????(RMB)
        if (coinCode.equals(platCoin)) {
            codePrice = platformMarkPrice;
        } else {
            // ?????????????????????????????????
            //RedisService redisService = SpringUtil.getBean("redisService");
            String result = redisService.get("cn:newFeixiaohaoPrice");
            if (hry.util.StringUtil.isNull(result)) {
                List<FeixiaohaoPriceVo> list = JSON.parseArray(result, FeixiaohaoPriceVo.class);
                for (FeixiaohaoPriceVo feixiaohaoPriceVo : list) {
                    String name = feixiaohaoPriceVo.getName();
                    // ???????????????????????????????????????Price??????????????????????????????
                    if (name.equals(coinCode) && feixiaohaoPriceVo.getPrice() != null) {
                        codePrice = new BigDecimal(feixiaohaoPriceVo.getPrice());
                        return codePrice;
                    }
                }
            } else {
                System.out.println("????????????redis???" + coinCode + "????????????");
                return BigDecimal.ZERO;
            }

        }
        return codePrice;
    }


}
