package hry.app.otc.remote;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import hry.app.customer.person.service.AppPersonInfoService;
import hry.app.customer.user.service.AppCustomerService;
import hry.app.otc.releaseAdvertisement.service.*;
import hry.bean.FrontPage;
import hry.bean.JsonResult;
import hry.bean.ObjectUtil;
import hry.front.redis.model.UserRedis;
import hry.manage.remote.RemoteManageService;
import hry.manage.remote.model.User;
import hry.otc.manage.remote.api.RemoteNewAdvertisementService;
import hry.otc.manage.remote.api.RemoteNewOtcService;
import hry.otc.manage.remote.model.AppTransaction;
import hry.otc.manage.remote.model.OtcAppTransactionRemote;
import hry.otc.manage.remote.model.OtcChatMessageRemote;
import hry.otc.manage.remote.model.ReleaseAdvertisementRemote;
import hry.otc.manage.remote.model.account.AppBankCard;
import hry.otc.manage.remote.model.customer.person.AppPersonInfo;
import hry.otc.manage.remote.model.customer.user.AppCustomer;
import hry.otc.manage.remote.model.releaseAdvertisement.*;
import hry.otc.remote.model.AppAppealRemote;
import hry.otc.remote.model.AppBankCardRemote;
import hry.otc.remote.model.AppOrderSpeakRemote;
import hry.otc.remote.model.OtcAppLogRemote;
import hry.redis.common.dao.RedisUtil;
import hry.redis.common.utils.RedisService;
import hry.redis.otc.mdel.RedisModel;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.*;
import hry.util.common.BaseConfUtil;
import hry.util.constant.DealConstant;
import hry.util.dto.OtcAccountRedis;
import hry.util.properties.PropertiesUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class RemoteNewAdvertisementServiceImpl implements RemoteNewAdvertisementService {

    @Resource
    private OtcServiceImpl otcService;
    @Resource
    private ReleaseAdvertisementService releaseAdvertisementService;
    @Resource
    private ExCoinExchangeRateService exCoinExchangeRateService;
    @Resource
    private OtcAppTransactionService otcAppTransactionService;
    @Resource
    private RedisService redisService;
    @Resource
    private TrustShieldService trustShieldService;
    @Resource
    private AppAppealService appAppealService;
    @Resource
    private ExCoinFeeService exCoinFeeService;
    @Resource
    private AppOrderSpeakService appOrderSpeakService;
    @Resource
    private OtcAppLogService otcAppLogService;
    @Resource
    private OtcCompletionRateService otcCompletionRateService;
    @Resource
    private AppPersonInfoService appPersonInfoService;
    @Resource
    private AppCustomerService appCustomerService;
    @Resource
    private RemoteNewOtcService remoteNewOtcService;
    @Resource
    private RemoteManageService remoteManageService;


    public static String transactionNum(String bussType) {
        SimpleDateFormat format = new SimpleDateFormat("yyMMddHHmmssSSS");
        String time = format.format(new Date(System.currentTimeMillis()));
        String randomStr = RandomStringUtils.random(4, false, true);
        if (null == bussType || "".equals(bussType)) {
            bussType = "00";
        }
        return bussType + time + randomStr;
    }

    @Override
    public ExDigitalmoneyAccountRedis getCoinAccountRedis(Long userId, String coinCode) {
        // ???????????????????????????
        RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
        UserRedis userRedis = redisUtil.get(userId.toString());
        // ?????????????????????????????????id
        Map<String,Long> map = userRedis.getDmAccountId();
        Set<String> keySet = map.keySet();

        ExDigitalmoneyAccountRedis dmAccount = null;
        for (String key : keySet) {
            if (key.equals(coinCode)) {
                RedisUtil<ExDigitalmoneyAccountRedis> a = new RedisUtil<ExDigitalmoneyAccountRedis>(ExDigitalmoneyAccountRedis.class);
                dmAccount = a.get(userRedis.getDmAccountId(key).toString());
            }
        }
        return dmAccount;
    }

    @Override
    public int keepDecimalForCoin(String coinCode) {
        int keepDecimalForCoin = 0;

        String productinfoListall = redisService.get("otc:coinCodeList");
        JSONArray parseArray = JSON.parseArray(productinfoListall);
        for (int k = 0; k < parseArray.size(); k++) {
            JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
            if (jo.getString("coinCode").equals(coinCode)) {
                if (jo.getInteger("keepDecimalForCoin") != null) {
                    keepDecimalForCoin = jo.getInteger("keepDecimalForCoin");
                }
            }
        }
        return keepDecimalForCoin;
    }


    @Override
    public JsonResult getExchangeRateByCode(String baseCoinCode) {
        QueryFilter filter = new QueryFilter(ExCoinExchangeRate.class);
        filter.addFilter("baseCoinCode=", baseCoinCode);
        filter.setOrderby(" modified desc");
        ExCoinExchangeRate exCoinExchangeRate = exCoinExchangeRateService.get(filter);
        if (exCoinExchangeRate != null && exCoinExchangeRate.getExchangeRate() != null) {
            return new JsonResult().setSuccess(true).setObj(exCoinExchangeRate.getExchangeRate());
        }
        return new JsonResult().setSuccess(false).setObj(null);
    }

    @Override
    public JsonResult getPersonalAsset(Long userId, String type, String isDefault) {
        //??????????????????
        List<AppBankCard> list = otcService.getPersonalAsset(userId, type, isDefault);
        if (list != null && list.size() > 0) {
            List<AppBankCardRemote> beanList = ObjectUtil.beanList(list, AppBankCardRemote.class);
            return new JsonResult().setSuccess(true).setObj(beanList);
        }
        return new JsonResult().setSuccess(false).setObj(null);
    }

    @Override
    public synchronized JsonResult addReleaseAdvertisement(String transactionMode, String coinCode, String coinName, String nationality, String isFixed, String tradeMoney, String premium, String paymentTerm, String tradeMoneyMix, String tradeMoneyMax, String remark, String isAutomatic, String isSecurity, String isBeTrusted, Long userId, String payType, String payTypeRemake, String serviceCharge, BigDecimal coinNumMin, BigDecimal coinNumMax, String hidshichangjiage, String legalCurrency, String legalCurrencySymbol,String cellPhone) {
        try {

            coinCode = coinCode.split(",")[0];

            String coinPercent = "0";// ??????????????????
            int keepDecimalForCoin = 0;//???????????????
            String otcMinPercent = "0";//otc??????????????????????????????
            String otcMaxPercent = "0";//otc??????????????????????????????

            BigDecimal buyAdvertisementMinMoney = new BigDecimal(0);//??????????????????????????????
            BigDecimal sellAdvertisementMinMoney = new BigDecimal(0);//??????????????????????????????
            String isFixedType = "";//otc???????????? 0???????????????1???????????????0,1???????????????

            BigDecimal eatFee = new BigDecimal(0); // ??????????????????
            int eatFeeType = 0;// ?????????????????????  0???????????? 1???????????????

            String productinfoListall = redisService.get("otc:coinCodeList");
            JSONArray parseArray = JSON.parseArray(productinfoListall);
            for (int k = 0; k < parseArray.size(); k++) {
                JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                if (jo.getString("coinCode").equals(coinCode)) {
                    if (jo.getString("coinPercent") != null) {
                        coinPercent = jo.getString("coinPercent");
                    }
                    if (jo.getInteger("keepDecimalForCoin") != null) {
                        keepDecimalForCoin = jo.getInteger("keepDecimalForCoin");
                    }
                    if (jo.getString("otcMinPercent") != null) {
                        otcMinPercent = jo.getString("otcMinPercent");
                    }
                    if (jo.getString("otcMaxPercent") != null) {
                        otcMaxPercent = jo.getString("otcMaxPercent");
                    }

                    if (jo.getString("buyAdvertisementMinMoney") != null) {
                        buyAdvertisementMinMoney = new BigDecimal(jo.getString("buyAdvertisementMinMoney"));
                    }

                    if (jo.getString("sellAdvertisementMinMoney") != null) {
                        sellAdvertisementMinMoney = new BigDecimal(jo.getString("sellAdvertisementMinMoney"));
                    }

                    if (jo.getString("isFixedType") != null) {
                        isFixedType = jo.getString("isFixedType");
                    }

                    if (jo.getInteger("eatFee") != null) {
                        eatFee = jo.getBigDecimal("eatFee");
                    }
                    if (jo.getInteger("eatFeeType") != null) {
                        eatFeeType = jo.getInteger("eatFeeType");
                    }

                }
            }

            //OTC???????????????
            BigDecimal otcFee = new BigDecimal(0);
            String otcFeeType = BaseConfUtil.getConfigSingle("otcFeeType", "configCache:otcConfig");//OTC?????????????????????  0 ???????????????1?????????
            if(otcFeeType.equals("1")){//????????????????????????????????????????????????
                if (0 == eatFeeType) {// ??????
                    otcFee = eatFee;
                } else if (1 == eatFeeType) {// ?????????
                    otcFee = eatFee.multiply(coinNumMax.divide(new BigDecimal(100)).setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN));
                }
            }

            if ("1".equals(transactionMode)) {//?????????????????????????????????
				/*// ??????redis??????
				RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
				UserRedis userRedis = redisUtil.get(userId.toString());
				ExDigitalmoneyAccountRedis ear = UserRedisUtils.getAccount(userRedis.getDmAccountId(coinCode).toString(), coinCode);
				if (ear == null) {
					return new JsonResult().setSuccess(false).setMsg("before_login");
				} else {
					if (ear.getHotMoney().compareTo(coinNumMax) < 0) {
						return new JsonResult().setSuccess(false).setMsg(coinCode+"??????");
					}
				}*/
                BigDecimal money = BigDecimal.ZERO;
                money = remoteNewOtcService.getHotMoneyByCoin(coinCode, userId);
                if (money.compareTo(coinNumMax.add(otcFee)) < 0) {
                    return new JsonResult().setSuccess(false).setMsg(coinCode + "??????");
                }
            }


            //OTC??????????????????????????????
            String otcPhoneMustFill = BaseConfUtil.getConfigSingle("otcPhoneMustFill", "configCache:otcConfig");
            if ("0".equals(otcPhoneMustFill)) {
                if(StringUtils.isEmpty(cellPhone)){
                    return new JsonResult().setSuccess(false).setMsg("lianxishoujibunengweikong");
                }
            }

            //OTC????????????
            String otcPaymentTime = BaseConfUtil.getConfigSingle("otcPaymentTime", "configCache:otcConfig");
            if (!StringUtils.isEmpty(otcPaymentTime)) {
                paymentTerm = otcPaymentTime;
            }

            if(transactionMode.equals("1")){// 1 ????????????
                if(coinNumMax.compareTo(sellAdvertisementMinMoney)<0){//????????????????????????????????????????????????????????????
                    return new JsonResult().setSuccess(false).setMsg("bunengxiaoyuzuixiaofabushuliang");
                }
            }

            if(transactionMode.equals("2")){// 2 ????????????
                if(coinNumMax.compareTo(buyAdvertisementMinMoney)<0){//????????????????????????????????????????????????????????????
                    return new JsonResult().setSuccess(false).setMsg("bunengxiaoyuzuixiaofabushuliang");
                }
            }

            if(!isFixedType.contains(isFixed)){//????????????????????????????????????
                return new JsonResult().setSuccess(false).setMsg("cibizhongbuzhichigaijiaoyileixing");
            }

            //???????????????
            BigDecimal yi1 = new BigDecimal("1");// 1
            BigDecimal yibai1 = new BigDecimal("100");// 100
            BigDecimal money = new BigDecimal(tradeMoney);
            BigDecimal shichangjiage = new BigDecimal(hidshichangjiage);
            BigDecimal pre = new BigDecimal("0");
            if (premium != null && !"".equals(premium)) {
                pre = new BigDecimal(premium);
            }
            //??????coinCode??????????????????????????????--????????????????????????
            //??????????????????????????????
            if ("1".equals(isFixed)) {// ???????????????
                //??????????????????
                if (otcMinPercent != null && !"".equals(otcMinPercent) && !"0".equals(otcMinPercent)) {
                    BigDecimal minPercent = new BigDecimal(otcMinPercent);
                    BigDecimal minPrice = shichangjiage.multiply(yi1.subtract(minPercent.divide(yibai1))).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                    if (money.compareTo(minPrice) < 0) {
                        return new JsonResult().setSuccess(false).setMsg("jiagexiaoyuzuidixiane");
                    }
                }
                //??????????????????
                if (otcMaxPercent != null && !"".equals(otcMaxPercent) && !"0".equals(otcMaxPercent)) {
                    BigDecimal maxPercent = new BigDecimal(otcMaxPercent);
                    BigDecimal maxPrice = shichangjiage.multiply(yi1.add(maxPercent.divide(yibai1))).setScale(2, BigDecimal.ROUND_HALF_DOWN);
                    if (money.compareTo(maxPrice) > 0) {
                        return new JsonResult().setSuccess(false).setMsg("jiagedayuzuigaoxiane");
                    }
                }
            } else {
				/*if("1".equals(transactionMode)) { //??????
					//??????????????????
					if( otcMinPercent != null && !"".equals(otcMinPercent) && !"0".equals(otcMinPercent)){
						BigDecimal minPercent=new BigDecimal(otcMinPercent);
						if(pre.compareTo(minPercent) < 0){
							return new JsonResult().setSuccess(false).setMsg("jiagexiaoyuzuidixiane");
						}
					}
				}else if("2".equals(transactionMode)){ //??????
					//??????????????????
					if( otcMaxPercent != null && !"".equals(otcMaxPercent) && !"0".equals(otcMaxPercent)){
						BigDecimal maxPercent=new BigDecimal(otcMaxPercent);
						if(pre.compareTo(maxPercent) > 0){
							return new JsonResult().setSuccess(false).setMsg("jiagedayuzuigaoxiane");
						}
					}
				}*/

            }
            if ("1".equals(transactionMode)) { //??????
                //???????????????????????? 0??? 1???
                if ("0".equals(isFixed)) {
                    //??????

                } else {
                    //??????

                }
            } else if ("2".equals(transactionMode)) {//??????
                //???????????????????????? 0??? 1???
                if ("0".equals(isFixed)) {
                    //??????

                } else {
                    //??????

                }
            }

            //if(new BigDecimal(tradeMoneyMix).compareTo(new BigDecimal(tradeMoney).multiply(new BigDecimal(coinPercent).divide(new BigDecimal(100))))<0){
            //	return new JsonResult().setSuccess(false).setMsg("baifenbihuilv");
            //}

            //??????????????????????????????????????????????????????
            QueryFilter rafilter = new QueryFilter(ReleaseAdvertisement.class);
            rafilter.addFilter("transactionMode=", transactionMode);
            rafilter.addFilter("coinCode=", coinCode);
            rafilter.addFilter("customerId=", userId);
            rafilter.addFilter("status=", 1);
            rafilter.addFilter("state=", 0);
            List<ReleaseAdvertisement> raList = releaseAdvertisementService.find(rafilter);
            if (raList != null && raList.size() > 0) { //?????????????????????????????????
                String msg = "";
                if ("1".equals(transactionMode)) { //????????????
                    msg = "yijingfabuguochushou";
                } else if ("2".equals(transactionMode)) {//????????????
                    msg = "yijingfabuguogoumai";
                } else {
                    msg = "fabuyichang";
                }
                //return new JsonResult().setSuccess(false).setMsg(msg);
            }

            //?????????????????????????????????????????????????????????????????????????????????
            QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
            qf.addFilter("coinCode=", coinCode);
            qf.addFilter("status!=", 5); //????????????
            qf.addFilter("status!=", 14); //????????????
            qf.addFilter("status!=", 1); //???????????????????????????????????????????????????????????????
            //??????
            if ("1".equals(transactionMode)) { //????????????????????????sellUserId
                qf.addFilter("sellUserId=", userId);
            } else if ("2".equals(transactionMode)) { //????????????????????????buyUserId
                qf.addFilter("buyUserId=", userId);
            } else {
                return new JsonResult().setSuccess(false).setMsg("leixingyichang");
            }
            List<OtcAppTransaction> atList = otcAppTransactionService.find(qf);
            if (atList != null && atList.size() > 0) { //????????????????????????
                return new JsonResult().setSuccess(false).setMsg("bunengfabuguanggaoing");
            }

            // ?????? ????????????
            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(userId, coinCode);
            if (otcAccountRedis == null) {
                return new JsonResult().setSuccess(false).setMsg("xnzh_no_exist");
            }

            ReleaseAdvertisement releaseAdvertisement = new ReleaseAdvertisement();
            //????????????
            String advertisementNum = transactionNum("");
            if (transactionMode.equals("1")) { // ?????? ??????
                advertisementNum = "OS" + advertisementNum;  //Os == otc sell
            } else {
                advertisementNum = "OB" + advertisementNum;   //OB == otc buy
            }
            releaseAdvertisement.setAdvertisementNum(advertisementNum);
            releaseAdvertisement.setTransactionMode(Integer.valueOf(transactionMode));
            releaseAdvertisement.setCoinCode(coinCode);
            releaseAdvertisement.setCoinName(coinName);
            releaseAdvertisement.setNationality(nationality);
            releaseAdvertisement.setIsFixed(Integer.valueOf(isFixed));
            releaseAdvertisement.setTradeMoney(new BigDecimal(tradeMoney).setScale(2, BigDecimal.ROUND_DOWN));
            if (!premium.isEmpty()) {
                releaseAdvertisement.setPremium(new BigDecimal(premium));
            }
            releaseAdvertisement.setPaymentTerm(paymentTerm);
            releaseAdvertisement.setPayType(payType);
            releaseAdvertisement.setTradeMoneyMix(new BigDecimal(tradeMoneyMix).setScale(2, BigDecimal.ROUND_DOWN));
            releaseAdvertisement.setTradeMoneyMax(new BigDecimal(tradeMoneyMax).setScale(2, BigDecimal.ROUND_DOWN));
            releaseAdvertisement.setRemark(remark);
            releaseAdvertisement.setCustomerId(userId);
            releaseAdvertisement.setModified(new Date());
            releaseAdvertisement.setCreated(new Date());
            //????????????
            releaseAdvertisement.setPayTypeRemake(payTypeRemake);

            releaseAdvertisement.setStatus(1);
            releaseAdvertisement.setState(0);
            releaseAdvertisement.setAccountId(otcAccountRedis.getId());

            //???????????????????????????????????????
            //?????????????????????????????????
            if (payType != null && !"".equals(payType)) {
                AppBankCard appBankCard = null;
                if (payType.contains("1")) {//?????????
                    appBankCard = otcService.selectByParameter(userId, 1, 1);
                    if (appBankCard != null) {
                        releaseAdvertisement.setBankId(appBankCard.getId());
                        //??????????????????
                        releaseAdvertisement.setBankNumber(appBankCard.getCardNumber());
                    }
                }
                if (payType.contains("2")) {//?????????
                    appBankCard = otcService.selectByParameter(userId, 2, 1);
                    if (appBankCard != null) {
                        releaseAdvertisement.setAlipayId(appBankCard.getId());
                        //?????????????????????
                        releaseAdvertisement.setAlipayAccount(appBankCard.getCardNumber());
                        //??????????????????
                        releaseAdvertisement.setAlipayThingUrl(appBankCard.getThingUrl());
                    }
                }
                if (payType.contains("3")) {//??????
                    appBankCard = otcService.selectByParameter(userId, 3, 1);
                    if (appBankCard != null) {
                        releaseAdvertisement.setWechatId(appBankCard.getId());
                        //??????????????????
                        releaseAdvertisement.setWechatAccount(appBankCard.getCardNumber());
                        //??????????????????
                        releaseAdvertisement.setWechatThingUrl(appBankCard.getThingUrl());
                    }
                }
            }
            releaseAdvertisement.setCoinNumMin(coinNumMin);
            releaseAdvertisement.setCoinNumMax(coinNumMax);
            releaseAdvertisement.setInitialCoinNumMin(coinNumMin);//??????????????????????????????
            releaseAdvertisement.setInitialCoinNumMax(coinNumMax);// ??????????????????????????????
            releaseAdvertisement.setLegalCurrency(legalCurrency);
            releaseAdvertisement.setLegalCurrencySymbol(legalCurrencySymbol);
            releaseAdvertisement.setCellPhone(cellPhone);//???????????????
            releaseAdvertisement.setOtcFeeType(Integer.valueOf(otcFeeType));//OTC?????????????????????  0 ???????????????1?????????
            releaseAdvertisement.setInitialOtcFee(otcFee);//?????????????????????
            releaseAdvertisement.setOtcFee(otcFee);
            releaseAdvertisementService.save(releaseAdvertisement);
            //?????????????????????,?????????????????????????????????
            if (transactionMode.equals("1")) { // ?????? ??????
                if (isFixed.equals("1")) { // ???????????????
                    remoteNewOtcService.publish(userId, otcAccountRedis.getId(), coinCode, coinNumMax.add(otcFee), 2, 1, advertisementNum, 13);
                    remoteNewOtcService.publish(userId, otcAccountRedis.getId(), coinCode, coinNumMax.add(otcFee).multiply(new BigDecimal(-1)), 1, 1, advertisementNum, 3);
                } else {
                    // ????????? ?????? ??????=?????????
                    remoteNewOtcService.publish(userId, otcAccountRedis.getId(), coinCode, coinNumMax.add(otcFee), 2, 1, advertisementNum, 13);
                    remoteNewOtcService.publish(userId, otcAccountRedis.getId(), coinCode, coinNumMax.add(otcFee).multiply(new BigDecimal(-1)), 1, 1, advertisementNum, 3);
                }
            }

            return new JsonResult().setSuccess(true).setMsg("fabuchenggong");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false).setMsg("fabushibai");
    }

    @Override
    public JsonResult userExCoinParameter(Long userId, String premium, String shichangjiage, String coinCode, String tradeMoneyMax, String transactionMode, String isFixeds, String tradeMoney) {
        BigDecimal tradeMoneyMaxBD = new BigDecimal(tradeMoneyMax);
        BigDecimal yi = new BigDecimal("1");// 1
        BigDecimal yibai = new BigDecimal("100");// 100

        ExDigitalmoneyAccountRedis dmAccount = getCoinAccountRedis(userId, coinCode);

        if (isFixeds.equals("true")) {// ???????????????
            BigDecimal price = new BigDecimal(tradeMoney);
            if (price.compareTo(BigDecimal.ZERO) == 0) {
                return new JsonResult().setSuccess(false).setMsg("jiagebunengxiaoyudengyu0");
            }
            // ????????? ?????? ??????=?????????
            BigDecimal bs = tradeMoneyMaxBD.divide(price, 4, BigDecimal.ROUND_DOWN);
            // ???????????????
            if (transactionMode.equals("1")) {// ??????---?????? ???????????????????????????
                if (dmAccount.getHotMoney().compareTo(bs) < 0) {
                    return new JsonResult().setSuccess(false).setMsg("bishuliangbugou");
                } else {
                    return new JsonResult().setSuccess(true).setObj("0");// serviceCharge.toString()
                }
            } else {// ??????
                return new JsonResult().setSuccess(true).setObj("0");// serviceCharge.toString()
            }

        } else {// ????????????????????????

            BigDecimal premiumBD = new BigDecimal(premium);
            BigDecimal shichangjiageBD = new BigDecimal(shichangjiage);
            System.out.println(premiumBD.toString());
            System.out.println(shichangjiageBD.toString());
            if (transactionMode.equals("1")) {// ?????? ??????
                BigDecimal price = shichangjiageBD.multiply(yi.subtract(premiumBD.divide(yibai))); // ????????????*(1-??????????????????)????????????
                System.out.println("??????" + price.toString());
                // ????????? ?????? ??????=?????????
                BigDecimal bs = tradeMoneyMaxBD.divide(price, 4, BigDecimal.ROUND_DOWN);

                System.out.println("?????????" + bs.toString());
                if (dmAccount.getHotMoney().compareTo(bs) < 0) {
                    return new JsonResult().setSuccess(false).setMsg("bishuliangbugou");
                } else {
                    return new JsonResult().setSuccess(true).setObj("0");// serviceCharge.toString()
                }

            } else {// ?????? ??????
                return new JsonResult().setSuccess(true).setObj("0");
            }

        }
    }

    @Override
    public FrontPage advertisingHallDetail(String payType, String nationality, String coinCode, String transactionMode, String offset, String limit, String legalCurrencySymbol, String legalCurrency) {

        Map<String,String> params = new HashMap<String,String>();
        params.put("payType", payType);
        params.put("nationality", nationality);
        params.put("coinCode", coinCode);
        params.put("transactionMode", transactionMode);
        params.put("offset", offset);
        params.put("limit", limit);
        params.put("legalCurrencySymbol", legalCurrencySymbol);
        params.put("legalCurrency", legalCurrency);
        FrontPage frontPage = releaseAdvertisementService.findPageHall(params);
        List<ReleaseAdvertisement> list = frontPage.getRows();

        String productinfoListall = redisService.get("otc:coinCodeList");
        JSONArray parseArray = JSON.parseArray(productinfoListall);
        int keepDecimalForCoin = 0;
        if (list.size() > 0) {
            List<ReleaseAdvertisementRemote> beanList = ObjectUtil.beanList(list, ReleaseAdvertisementRemote.class);
            for (int i = 0; i < beanList.size(); i++) {
                for (int k = 0; k < parseArray.size(); k++) {
                    JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                    if (jo.getString("coinCode").equals(beanList.get(i).getCoinCode())) {
                        if (jo.getInteger("keepDecimalForCoin") != null) {
                            keepDecimalForCoin = jo.getInteger("keepDecimalForCoin");
                        }
                    }
                }
                beanList.get(i).setKeepDecimalForCoin(keepDecimalForCoin);
                AppCustomer appCustomer = otcService.getAppCustomerById(beanList.get(i).getCustomerId());
                if (!StringUtils.isEmpty(appCustomer)) {
                    beanList.get(i).setUsername(appCustomer.getNickNameOtc());
                }
                Map<String,BigDecimal> result = otcAppTransactionService.getTransactionSumById(beanList.get(i).getId());
                String s = result.get("count1").add(result.get("count2")).add(result.get("count3")).add(result.get("count4")).add(result.get("count5")).toString();
                int sum = Integer.parseInt(s);
                beanList.get(i).setOrderSum(sum);
            }
            frontPage.setRows(beanList);
        }
        return frontPage;
    }

    @Override
    public JsonResult adUserInfor(Long id, String coinCode) {
        try {
            Map<Object,Object> map = new HashMap<Object,Object>();
            // ??????30??????????????????
            BigDecimal adUserBy30 = otcAppTransactionService.adUserBy30(id, coinCode);
            map.put("adUserBy30", adUserBy30 == null ? 0 : adUserBy30);

            Map<String,Object> params = new HashMap<String,Object>();
            params.put("id", id);
            params.put("status", 14);
            Integer tradeNumAll = otcAppTransactionService.allTradeCount(params);
            map.put("tradeNumAll", tradeNumAll == null ? 0 : tradeNumAll);
            // ???????????????
            BigDecimal adUserAll = otcAppTransactionService.adUserAll(id, coinCode);
            map.put("tradeNum", adUserAll == null ? 0 : adUserAll);
            // ????????????
            AppCustomer appCustomer = otcService.getAppCustomerById(id);
            map.put("adUserAll", appCustomer.getTrustNum() == null ? 0 : appCustomer.getTrustNum());

            //???????????????
            map.put("keepDecimalFixPrice", keepDecimalForCoin(coinCode));
            return new JsonResult().setSuccess(true).setObj(map);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult selectTrustShield(Long id1, Long id2) {
        Map<String,Object> map = new HashMap<String,Object>();
        int trust = 0, shield = 0;
        QueryFilter qf = new QueryFilter(TrustShield.class);
        qf.addFilter("trust=", id1);
        qf.addFilter("isTrust=", id2);
        List<TrustShield> list = trustShieldService.find(qf);
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStatus() == 1) {
                    trust = 1;
                } else if (list.get(i).getStatus() == 2) {
                    shield = 1;
                }
            }
            map.put("trust", trust);
            map.put("shield", shield);
            return new JsonResult().setSuccess(true).setObj(map);
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult getById(Long id) {
        QueryFilter qf = new QueryFilter(ReleaseAdvertisement.class);
        qf.addFilter("id=", id);
        ReleaseAdvertisement r = releaseAdvertisementService.get(id);
        if (r != null) {
            ReleaseAdvertisementRemote bean2bean = ObjectUtil.bean2bean(r, ReleaseAdvertisementRemote.class);
            AppCustomer appCustomer = otcService.getAppCustomerById(bean2bean.getCustomerId());
            int by30 = otcAppTransactionService.getOrderSumBy30(bean2bean.getCustomerId(), bean2bean.getCoinCode());
            bean2bean.setOrderSum(by30);
            if (!StringUtils.isEmpty(appCustomer)) {
                bean2bean.setUsername(appCustomer.getNickNameOtc());
            }
            return new JsonResult().setSuccess(true).setObj(bean2bean);
        }
        return null;
    }

    @Override
    public JsonResult isCanTransaction(String customerId, String transactionMode, String coinCode) {
        //????????????????????????????????????????????????????????????  ?????????????????????????????????????????????????????????
        QueryFilter filter = new QueryFilter(ReleaseAdvertisement.class);
        filter.addFilter("customerId=", customerId);
        if ("1".equals(transactionMode)) {
            filter.addFilter("transactionMode=", 2);
        } else if ("2".equals(transactionMode)) {
            filter.addFilter("transactionMode=", 1);
        } else {
            return new JsonResult().setSuccess(false).setMsg("jiaoyileixingyichang");
        }
        filter.addFilter("coinCode=", coinCode);
        filter.addFilter("status=", 1);
        filter.addFilter("state=", 0);
        List<ReleaseAdvertisement> raList = releaseAdvertisementService.find(filter);
		/*if(raList != null && raList.size() > 0){ //????????????
			return new JsonResult().setSuccess(false).setMsg("yijingyoule");
		}*/

        //??????????????????????????????????????????????????????????????????????????????
        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        //qf.addFilter("status_in", "14,5");//?????????????????????
        qf.addFilter("status!=", 14); //??????
        qf.addFilter("status!=", 5); //??????
        qf.addFilter("status!=", 1); //???????????????????????????????????????????????????????????????
        qf.addFilter("status!=", 11); //??????????????????
        qf.addFilter("status!=", 12); //??????????????????
        //qf.addFilter("transactionMode=", transactionMode);
        qf.addFilter("coinCode=", coinCode);
        qf.addFilter("customerId=", customerId);
        List<OtcAppTransaction> atList = otcAppTransactionService.find(qf);
        if (atList != null && atList.size() > 0) {
            return new JsonResult().setSuccess(false).setMsg("yiyouzhengzaijiaoyizhong");
        }

        return new JsonResult().setSuccess(true).setMsg("keyijinxingjiaoyi");
    }

    @Override
    public JsonResult orderAccounting(String tradeNum, Long releaseId) {
        Map<String,Object> map = new HashMap<String,Object>();

        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("transactionNum=", tradeNum);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);
        if (otcAppTransaction != null) {
            otcAppTransaction.setKeepDecimalForCoin(keepDecimalForCoin(otcAppTransaction.getCoinCode()));
            OtcAppTransactionRemote bean2bean = ObjectUtil.bean2bean(otcAppTransaction, OtcAppTransactionRemote.class);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Long advertisementId = bean2bean.getAdvertisementId();
            ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(advertisementId);
            if (releaseAdvertisement != null) {
                String advRemark = releaseAdvertisement.getRemark();
                advRemark = StringUtils.isEmpty(advRemark) ? "" : advRemark;
                bean2bean.setAdvRemark(advRemark);
            }

            bean2bean.setCreatedString(sdf.format(bean2bean.getCreated()));
            switchName(bean2bean);
            map.put("app", bean2bean);
            map.put("appCustomerId", otcAppTransaction.getCustomerId());

            //?????????????????????????????????????????????
            if (2 == otcAppTransaction.getTransactionMode()) {
                //?????????????????????
                if (otcAppTransaction.getBankId() != null) {
                    AppBankCard appBankCard = otcService.getById(otcAppTransaction.getBankId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("bank", paRemote);
                }
                //?????????????????????
                if (otcAppTransaction.getAlipayId() != null) {
                    AppBankCard appBankCard = otcService.getById(otcAppTransaction.getAlipayId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("alipay", paRemote);
                }
                //??????????????????
                if (otcAppTransaction.getWechatId() != null) {
                    AppBankCard appBankCard = otcService.getById(otcAppTransaction.getWechatId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("wechat", paRemote);
                }
            }
        }
        ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(releaseId);
        if (releaseAdvertisement != null) {
            map.put("payTypeRemake", releaseAdvertisement.getPayTypeRemake());

            List<String> list = new ArrayList<String>();
            String[] split = releaseAdvertisement.getPayType().split(",");
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    if (otcAppTransaction.getPayType().contains(split[i]) && "1".equals(split[i])) {
                        list.add("yinhangzhuanzhang");
                    } else if (otcAppTransaction.getPayType().contains(split[i]) && "2".equals(split[i])) {
                        list.add("zhifubao2");
                    } else if (otcAppTransaction.getPayType().contains(split[i]) && "3".equals(split[i])) {
                        list.add("weixinzhifu");
                    }
                }
            }
            map.put("list", list);
            map.put("paymentTerm", releaseAdvertisement.getPaymentTerm());
            //?????????????????????
            map.put("releaseRemark", releaseAdvertisement.getRemark());
            //?????????????????????
            map.put("cellPhone", releaseAdvertisement.getCellPhone());
            // ????????????
            map.put("releasePrice", otcAppTransaction.getAdvertisementTradeMoney());

            //????????????????????????????????????????????????????????????
            if (releaseAdvertisement.getTransactionMode() == 1) {
                //?????????????????????
                if (releaseAdvertisement.getBankId() != null) {
                    AppBankCard appBankCard = otcService.getById(releaseAdvertisement.getBankId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("bank", paRemote);
                }
                //?????????????????????
                if (releaseAdvertisement.getAlipayId() != null) {
                    AppBankCard appBankCard = otcService.getById(releaseAdvertisement.getAlipayId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("alipay", paRemote);
                }
                //??????????????????
                if (releaseAdvertisement.getWechatId() != null) {
                    AppBankCard appBankCard = otcService.getById(releaseAdvertisement.getWechatId());
                    AppBankCardRemote paRemote = ObjectUtil.bean2bean(appBankCard, AppBankCardRemote.class);
                    if (StringUtil.isNotEmpty(paRemote.getThingUrl())) {
                        paRemote.setThingUrl(getCloudStorage(paRemote.getThingUrl()));
                    }
                    map.put("wechat", paRemote);
                }
            }
        }
        return new JsonResult().setObj(map);
    }

    public String getCloudStorage(String url) {
        String img_server_type = PropertiesUtils.APP.getProperty("app.img.server.type");
        String allUrl = "";
        try {
            switch (img_server_type) {
                case "oss": // ?????????oss
                    allUrl = OssUtil.getUrl(url);
                    break;
                case "aws": // ?????????aws
                    allUrl = AWSUtil.getUrlFromS3(url);
                    break;
                case "azure": // ??????azure
                    allUrl = AzureUtil.getUrl(url);
                    break;
                default: // ???????????????oss
                    allUrl = OssUtil.getUrl(url);
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return allUrl;
    }


    public void switchName(OtcAppTransactionRemote bean2bean) {
        QueryFilter filter = new QueryFilter(AppPersonInfo.class);
        filter.addFilter("customerId_EQ", bean2bean.getBuyUserId());
        AppPersonInfo appPersonInfo = appPersonInfoService.get(filter);
        bean2bean.setBuyUserName(appPersonInfo.getSurname() + appPersonInfo.getTrueName());

        QueryFilter filter1 = new QueryFilter(AppPersonInfo.class);
        filter1.addFilter("customerId_EQ", bean2bean.getSellUserId());
        AppPersonInfo appPersonInfo1 = appPersonInfoService.get(filter1);
        bean2bean.setSellUserName(appPersonInfo1.getSurname() + appPersonInfo1.getTrueName());
    }

    @Override
    public JsonResult redisTimeOrder(String tradeNum, String paymentTerm) {
        Double index = redisService.zscore("otc:tradeNum", tradeNum);
        if (index != null) {
            QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
            qf.addFilter("transactionNum=", tradeNum);
            OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);

            if (StringUtil.isEmpty(paymentTerm)) {

                if (otcAppTransaction != null) {
                    ReleaseAdvertisement r = releaseAdvertisementService.get(otcAppTransaction.getAdvertisementId());
                    if (r != null) {
                        paymentTerm = r.getPaymentTerm();
                    }
                }
            }
            if (otcAppTransaction != null) {
                otcAppTransaction.setStatus(2);// 2???????????????
                otcAppTransactionService.update(otcAppTransaction);
            }

            Double zscore = redisService.zscore("otc:tradeNum", tradeNum);

            return new JsonResult().setSuccess(true).setObj((int) (zscore - System.currentTimeMillis() / 1000));
        }
        return null;
    }


    @Override
    public JsonResult getExCoinFee(String coinCodeMoney, String coinCode) {
        BigDecimal eatFee = new BigDecimal(0); // ???????????????
        int eatFeeType = 0;// ???????????????????????????

        String productinfoListall = redisService.get("otc:coinCodeList");
        JSONArray parseArray = JSON.parseArray(productinfoListall);
        for (int k = 0; k < parseArray.size(); k++) {
            JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
            if (jo.getString("coinCode").equals(coinCode)) {
                if (jo.getInteger("eatFee") != null) {
                    eatFee = jo.getBigDecimal("eatFee");
                }
                if (jo.getInteger("eatFeeType") != null) {
                    eatFeeType = jo.getInteger("eatFeeType");
                }
            }
        }
        BigDecimal bs = new BigDecimal(coinCodeMoney);

        if (0 == eatFeeType) {// ??????
            return new JsonResult().setSuccess(true).setCode(eatFee.setScale(2, BigDecimal.ROUND_DOWN).toString());

        } else if (1 == eatFeeType) {// ?????????
            eatFee = eatFee.multiply(new BigDecimal(coinCodeMoney)).divide(new BigDecimal(100)).setScale(2);
            return new JsonResult().setSuccess(true).setCode(eatFee.setScale(2, BigDecimal.ROUND_DOWN).toString());
        }
        return new JsonResult().setSuccess(true).setCode("0");
    }


    public String[] getExCoinFeeType(String coinCode) {
        BigDecimal eatFee = new BigDecimal(0); // ???????????????
        int eatFeeType = 0;// ???????????????????????????
        String[] res = new String[2];
        String productinfoListall = redisService.get("otc:coinCodeList");
        JSONArray parseArray = JSON.parseArray(productinfoListall);
        for (int k = 0; k < parseArray.size(); k++) {
            JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
            if (jo.getString("coinCode").equals(coinCode)) {
                if (jo.getInteger("eatFee") != null) {
                    eatFee = jo.getBigDecimal("eatFee");
                }
                if (jo.getInteger("eatFeeType") != null) {
                    eatFeeType = jo.getInteger("eatFeeType");
                }
            }
        }
        res[0] = String.valueOf(eatFeeType);
        res[1] = eatFee.setScale(2, BigDecimal.ROUND_HALF_DOWN).toPlainString();
        return res;
    }

    public static void main(String[] args) {
        Double d1 = new Double(152);
        Double d2 = new Double(150);
        System.out.println((int) (d1 - d2));

        System.out.println(RandomStringUtils.random(8, true, true));
    }

    @Override
    public synchronized JsonResult buydetail(Long releaseId, String tradeMoney, String coinCodeMoney, String remark, Long buyId, Long sellId, String transactionMode, String coinCode, String payType, Long userid, String editor,String cellPhone) {
        try {

            //OTC??????????????????????????????
            String otcPhoneMustFill = BaseConfUtil.getConfigSingle("otcPhoneMustFill", "configCache:otcConfig");
            if ("0".equals(otcPhoneMustFill)) {
                if(StringUtils.isEmpty(cellPhone)){
                    return new JsonResult().setSuccess(false).setMsg("lianxishoujibunengweikong");
                }
            }

            //??????24???????????????????????????3?????????30???????????????????????????????????????????????????
            // ???24??????????????????????????????
            if ("1".equals(transactionMode)) {
                String ss = redisService.get("otc:canNotBuy:" + buyId);
                if (StringUtil.isNotEmpty(ss)) {
                    return new JsonResult().setSuccess(false).setMsg("24xiaoshibunenggoujiaoyi");
                }
            }


            BigDecimal eatFee = new BigDecimal(0); // ???????????????
            int eatFeeType = 0;// ???????????????????????????
            String coinPercent = "0";// ??????????????????
            int keepDecimalForCoin = keepDecimalForCoin(coinCode);// ???????????????

            String productinfoListall = redisService.get("otc:coinCodeList");
            JSONArray parseArray = JSON.parseArray(productinfoListall);
            for (int k = 0; k < parseArray.size(); k++) {
                JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                if (jo.getString("coinCode").equals(coinCode)) {
                    if (jo.getInteger("eatFee") != null) {
                        eatFee = jo.getBigDecimal("eatFee");
                    }
                    if (jo.getInteger("eatFeeType") != null) {
                        eatFeeType = jo.getInteger("eatFeeType");
                    }
                    if (jo.getString("coinPercent") != null) {
                        coinPercent = jo.getString("coinPercent");
                    }
                }
            }

            ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(releaseId);
            BigDecimal currentTradePrice = new BigDecimal(tradeMoney).divide(new BigDecimal(coinCodeMoney), 2, BigDecimal.ROUND_HALF_UP);
            //????????????
            if (releaseAdvertisement.getIsFixed() == 1) {
                //??????????????????0.5???????????????
                if (currentTradePrice.subtract(releaseAdvertisement.getTradeMoney()).abs().compareTo(new BigDecimal(0.5)) == 1) {
                    return new JsonResult().setSuccess(false).setMsg("guanggaobeixiugaiyishixiao");
                }
            } else {
                //???????????????????????????????????????????????????????????????
                if ("2".equals(transactionMode)) {
                    if (currentTradePrice.subtract(new BigDecimal(0.5)).compareTo(releaseAdvertisement.getTradeMoney()) == 1) {
                        return new JsonResult().setSuccess(false).setMsg("guanggaobeixiugaiyishixiao");
                    }
                } else {//???????????????????????????????????????????????????????????????
                    if (currentTradePrice.add(new BigDecimal(0.5)).compareTo(releaseAdvertisement.getTradeMoney()) == -1) {
                        return new JsonResult().setSuccess(false).setMsg("guanggaobeixiugaiyishixiao");
                    }
                }
            }

            //????????????????????????????????????????????????????????????   ????????????????????????????????????
            if (releaseAdvertisement.getStatus() == 0 || releaseAdvertisement.getCoinNumMax().compareTo(new BigDecimal(coinCodeMoney)) < 0) {
                return new JsonResult().setSuccess(false).setMsg("ciguanggaoyiguoqi");
            }

            if (releaseAdvertisement.getTradeMoneyMix().compareTo(new BigDecimal(tradeMoney)) > 0 || releaseAdvertisement.getTradeMoneyMax().compareTo(new BigDecimal(tradeMoney)) < 0) {
                return new JsonResult().setSuccess(false).setMsg("chaochudanbixiane");
            }

			/*if(StringUtils.isEmpty(coinPercent)){
				return new JsonResult().setSuccess(false).setMsg("qingshezhibaifenbihuilv");
			}else{
				//??????????????????
				*//*if(releaseAdvertisement != null && releaseAdvertisement.getPremium() != null && releaseAdvertisement.getPremium().compareTo(new BigDecimal(0))>0){
					BigDecimal price = releaseAdvertisement.getTradeMoney().multiply(new BigDecimal("1").subtract(releaseAdvertisement.getPremium().divide(new BigDecimal("100")))); // ????????????*(1-??????????????????)????????????
					if(releaseAdvertisement.getTradeMoneyMax().subtract(new BigDecimal(tradeMoney)).compareTo(price.multiply(new BigDecimal(coinPercent).divide(new BigDecimal(100))))<0){
						releaseAdvertisement.setState(1);//????????????
					}
				}else{*//*
					//if(releaseAdvertisement.getTradeMoneyMax().subtract(new BigDecimal(tradeMoney)).compareTo(releaseAdvertisement.getTradeMoney().multiply(new BigDecimal(coinPercent).divide(new BigDecimal(100))))<0){
					//	releaseAdvertisement.setState(1);//????????????
					//}
				//}
			}*/

            //TODO ?????????????????????????????? 2018.09.29,????????????????????????
			/*if(releaseAdvertisement.getIsBeTrusted()==1){
				QueryFilter trusfilter=new QueryFilter(TrustShield.class);
				trusfilter.addFilter("trust=", userid);
				trusfilter.addFilter("isTrust=", releaseAdvertisement.getCustomerId());				trusfilter.addFilter("status=", 1);
				List<TrustShield> list=trustShieldService.find(trusfilter);
				if (list.size()<=0) {
					return new JsonResult().setSuccess(false).setMsg("xinrenbenguanggao");
				}
			}*/
            //??????????????????????????????//TODO ?????????????????????????????? 2018.09.29,????????????????????????
			/*if(releaseAdvertisement.getIsSecurity()==1){
				AppCustomer customer=otcService.getAppCustomerById(userid);
				if (customer != null) {
					if( customer.getPhoneState() != 1 ){
						return new JsonResult().setSuccess(false).setMsg("bangdingshoujihoukejinxingjiaoyi");
					}
					if( customer.getHasEmail() != 1 ){
						return new JsonResult().setSuccess(false).setMsg("bangdingyouxianghoukejinxingjiaoyi");
					}
				}
			}*/

            OtcAppTransaction otcAppTransaction = new OtcAppTransaction();
            otcAppTransaction.setTransactionNum(transactionNum(""));
            //????????????????????????????????????????????????????????????????????????????????????
            otcAppTransaction.setAdvertisementTradeMoney(currentTradePrice);
            String randomStr = RandomStringUtils.randomNumeric(4);

            otcAppTransaction.setReferenceNum(randomStr);// ???????????????
            otcAppTransaction.setCustomerId(userid);
            //????????? redis
            //ExDigitalmoneyAccountRedis exDigitalmoneyAccountRedis = getCoinAccountRedis(userid, coinCode);
            OtcAccountRedis accountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(userid, coinCode);
            if (accountRedis != null) {
                otcAppTransaction.setAccountId(accountRedis.getId());
            }
            // ??????transactionMode???1??????id???????????????2??????????????????????????????
            AppCustomer appCustomerBuy = otcService.getAppCustomerById(buyId);
            if (appCustomerBuy != null) {
                otcAppTransaction.setBuyUserName(appCustomerBuy.getNickNameOtc());
            }
            AppCustomer appCustomerSell = otcService.getAppCustomerById(sellId);
            if (appCustomerBuy != null) {
                otcAppTransaction.setSellUserName(appCustomerSell.getNickNameOtc());
            }
            otcAppTransaction.setBuyUserId(buyId);
            BigDecimal yi = new BigDecimal("1");// 1
            BigDecimal yibai = new BigDecimal("100");// 100
            Integer otcFeeType = releaseAdvertisement.getOtcFeeType();//OTC?????????????????????  0 ???????????????1?????????
            if ("1".equals(transactionMode)) {// ??? - ??????
                otcAppTransaction.setStatus(2);
                BigDecimal otcFee = new BigDecimal(0);
                if (0 == eatFeeType) {// ??????
                    otcFee = eatFee;
                } else if (1 == eatFeeType) {// ?????????
                    otcFee = eatFee.multiply(new BigDecimal(coinCodeMoney)).divide(new BigDecimal(100)).setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN);
                }

                if(otcFeeType == 0){//??????????????????????????????
                    otcAppTransaction.setSellfee(otcFee);
                    otcAppTransaction.setBuyfee(new BigDecimal(0));//??????????????????????????????
                    releaseAdvertisement.setInitialOtcFee(new BigDecimal(0));//?????????????????????
                }else{
                    otcAppTransaction.setSellfee(new BigDecimal(0));
                    otcAppTransaction.setBuyfee(otcFee);//??????????????????????????????
                    releaseAdvertisement.setOtcFee(releaseAdvertisement.getOtcFee().subtract(otcAppTransaction.getBuyfee()));//?????????????????????
                }
                System.out.println("=========coinCodeMoney========="+coinCodeMoney);
                System.out.println("=========otcAppTransaction.getSellfee()========="+otcAppTransaction.getSellfee());
                if(otcFeeType == 0){//??????????????????????????????
                    if (new BigDecimal(coinCodeMoney).subtract(otcAppTransaction.getSellfee()).compareTo(BigDecimal.ZERO) < 1) {
                        return new JsonResult().setSuccess(false).setMsg("goumaishuliangyaodayushouxufei");
                    }
                }else{
                    if (new BigDecimal(coinCodeMoney).subtract(otcAppTransaction.getBuyfee()).compareTo(BigDecimal.ZERO) < 1) {
                        return new JsonResult().setSuccess(false).setMsg("goumaishuliangyaodayushouxufei");
                    }
                }

                //???????????????
                BigDecimal price;
                //??????coinCode??????????????????????????????--????????????????????????
                if (releaseAdvertisement.getIsFixed() == 1) {// ???????????????
                    price = releaseAdvertisement.getTradeMoney();
                } else {
                    //????????????????????????
					/*price = releaseAdvertisement.getTradeMoney().multiply(
							yi.subtract(releaseAdvertisement.getPremium().divide(yibai))).setScale(3, BigDecimal.ROUND_DOWN);*/
                    price = releaseAdvertisement.getTradeMoney();
                }
                releaseAdvertisement.setCoinNumMax(releaseAdvertisement.getCoinNumMax().subtract(new BigDecimal(coinCodeMoney)));

                //????????????????????????????????????????????????????????????????????????
				/*if(releaseAdvertisement.getCoinNumMax().compareTo(BigDecimal.ZERO) == 0){
					releaseAdvertisement.setState(1);//????????????
					releaseAdvertisement.setStatus(2);//?????????
				}*/

                releaseAdvertisement.setTransactionNum(releaseAdvertisement.getTransactionNum() + 1);
                releaseAdvertisementService.update(releaseAdvertisement);

                //??????zset??????
                redisService.zadd("otc:tradeNum", System.currentTimeMillis() / 1000 + Integer.parseInt(releaseAdvertisement.getPaymentTerm()) * 60, otcAppTransaction.getTransactionNum());

                otcAppTransaction.setTradeNum(new BigDecimal(coinCodeMoney).subtract(otcAppTransaction.getSellfee()));
            } else if ("2".equals(transactionMode)) {// ??? - ??????
                BigDecimal fee = new BigDecimal("0");
                if (0 == eatFeeType) {// ??????
                    fee = eatFee;
                } else if (1 == eatFeeType) {
                    fee = eatFee.multiply(new BigDecimal(coinCodeMoney)).divide(new BigDecimal(100)).setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN);
                }
                if (accountRedis.getHotMoney().compareTo(BigDecimal.ZERO) <= 0 || accountRedis.getHotMoney().compareTo(new BigDecimal(coinCodeMoney).add(fee)) < 0) {
                    return new JsonResult().setSuccess(false).setMsg("nindebibuzu");
                }

                // ??????????????? ???????????????
                // ?????????
                BigDecimal bs = new BigDecimal(coinCodeMoney);
                //???????????????

                releaseAdvertisement.setCoinNumMax(releaseAdvertisement.getCoinNumMax().subtract(bs));
                if (0 == eatFeeType) {// ??????

                    otcAppTransaction.setSellfee(eatFee);

                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, bs.add(eatFee), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 15);
                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, (bs.add(eatFee)).multiply(new BigDecimal(-1)), 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 5);

                } else if (1 == eatFeeType) {// ?????????
                    BigDecimal EatFee = eatFee.multiply(new BigDecimal(coinCodeMoney)).divide(new BigDecimal(100)).setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN);

                    otcAppTransaction.setSellfee(EatFee);

                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, bs.add(EatFee), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 15);
                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, (bs.add(EatFee)).multiply(new BigDecimal(-1)), 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 5);
                } else {
                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, bs, 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 15);
                    remoteNewOtcService.publish(userid, accountRedis.getId(), coinCode, bs.multiply(new BigDecimal(-1)), 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 5);
                }
                /*
                 * if (0==exCoinParameter.getPutFeeType()) {
                 * appTransaction.setBuyfee(exCoinParameter.getPutFee()); }
                 * else if (1==exCoinParameter.getEatFeeType()) {
                 * appTransaction.setBuyfee(exCoinParameter.getPutFee().
                 * multiply(new BigDecimal(coinCodeMoney)) .divide(new
                 * BigDecimal(100)).setScale(2)); }
                 */
                // ????????????
                //releaseAdvertisement.setState(1);
                BigDecimal price;
                //??????coinCode??????????????????????????????--????????????????????????
                if (releaseAdvertisement.getIsFixed() == 1) {// ???????????????
                    price = releaseAdvertisement.getTradeMoney();
                } else {
                    //????????????????????????
					/*price = releaseAdvertisement.getTradeMoney().multiply(
							yi.add(releaseAdvertisement.getPremium().divide(yibai))).setScale(2, BigDecimal.ROUND_HALF_DOWN);*/
                    price = releaseAdvertisement.getTradeMoney();
                }
                //releaseAdvertisement.setPayType(payType);
                //releaseAdvertisement.setPayTypeRemake(editor);

                releaseAdvertisement.setTransactionNum(releaseAdvertisement.getTransactionNum() + 1);
                releaseAdvertisementService.update(releaseAdvertisement);
                otcAppTransaction.setStatus(2);
				/*redisService.save("otc:tradeNum:" + otcAppTransaction.getTransactionNum(), otcAppTransaction.getTransactionNum(),
						Integer.valueOf(releaseAdvertisement.getPaymentTerm().replaceAll("min","")) * 60);*/
                redisService.zadd("otc:tradeNum", System.currentTimeMillis() / 1000 + Integer.valueOf(releaseAdvertisement.getPaymentTerm()) * 60, otcAppTransaction.getTransactionNum());

                otcAppTransaction.setTradeNum(new BigDecimal(coinCodeMoney));
            }

            otcAppTransaction.setSellUserId(sellId);
            otcAppTransaction.setCoinCode(coinCode);
            otcAppTransaction.setTransactionMode(Integer.valueOf(transactionMode));
            BigDecimal count = new BigDecimal(coinCodeMoney);
            if (count != null && count.compareTo(BigDecimal.ZERO) != 0 && otcAppTransaction.getAdvertisementTradeMoney() != null && otcAppTransaction.getAdvertisementTradeMoney().compareTo(BigDecimal.ZERO) != 0) {
                //??????????????????????????????????????????????????????????????????????????????????????????
                BigDecimal multiply = count.multiply(otcAppTransaction.getAdvertisementTradeMoney()).setScale(2, BigDecimal.ROUND_DOWN);
                if (multiply.compareTo(BigDecimal.ZERO) > 0) {
                    otcAppTransaction.setTradeMoney(multiply);
                } else {
                    otcAppTransaction.setTradeMoney(new BigDecimal(tradeMoney));
                }
            } else {
                otcAppTransaction.setTradeMoney(new BigDecimal(tradeMoney));
            }

            otcAppTransaction.setCreated(new Date());
            otcAppTransaction.setRemark(remark);
            otcAppTransaction.setAdvertisementId(releaseId);
            otcAppTransaction.setPayType(payType);
            if (releaseAdvertisement.getIsFixed() == 0) {
                otcAppTransaction.setTransactionType(2);
            } else if (releaseAdvertisement.getIsFixed() == 1) {//??????
                otcAppTransaction.setTransactionType(1);
            }

            //????????????????????????  0
            otcAppTransaction.setSellIsDeleted(0);
            otcAppTransaction.setBuyIsDeleted(0);

            //???-???????????????????????????
            //if("2".equals(transactionMode)){
            //???????????????????????????????????????
            //?????????????????????????????????
            if (payType != null && !"".equals(payType)) {
                AppBankCard appBankCard = null;
                Long customerId = releaseAdvertisement.getCustomerId();
                if ("2".equals(transactionMode)) {
                    customerId = sellId;
                }
                if (payType.contains("1")) {//?????????
                    appBankCard = otcService.selectByParameter(customerId, 1, 1);
                    if (appBankCard != null) {
                        //??????????????????
                        otcAppTransaction.setBankId(appBankCard.getId());
                        otcAppTransaction.setBankNumber(appBankCard.getCardNumber());
                    }
                }
                if (payType.contains("2")) {//?????????
                    appBankCard = otcService.selectByParameter(customerId, 2, 1);
                    if (appBankCard != null) {
                        otcAppTransaction.setAlipayId(appBankCard.getId());
                        //?????????????????????
                        otcAppTransaction.setAlipayAccount(appBankCard.getCardNumber());
                        //??????????????????
                        otcAppTransaction.setAlipayThingUrl(appBankCard.getThingUrl());
                    }
                }
                if (payType.contains("3")) {//??????
                    appBankCard = otcService.selectByParameter(customerId, 3, 1);
                    if (appBankCard != null) {
                        otcAppTransaction.setWechatId(appBankCard.getId());
                        //??????????????????
                        otcAppTransaction.setWechatAccount(appBankCard.getCardNumber());
                        //??????????????????
                        otcAppTransaction.setWechatThingUrl(appBankCard.getThingUrl());
                    }
                }
            }
            //}
            String random = RandomStringUtils.random(6, false, true);
            otcAppTransaction.setPaymentCode(random);
            otcAppTransaction.setLegalCurrency(releaseAdvertisement.getLegalCurrency());
            otcAppTransaction.setCellPhone(cellPhone);
            otcAppTransactionService.save(otcAppTransaction);

            Map<String,String> map = new HashMap<String,String>();
            map.put("transactionNum", otcAppTransaction.getTransactionNum());
            map.put("referenceNum", otcAppTransaction.getReferenceNum());
            return new JsonResult().setSuccess(true).setObj(map);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult().setSuccess(false);
        }
    }

    @Override
    public List<AppOrderSpeakRemote> selectOrderSpeak(Long orderId) {
        QueryFilter qf = new QueryFilter(AppOrderSpeak.class).addFilter("orderId=", orderId);
        List<AppOrderSpeak> list = appOrderSpeakService.find(qf);
        List<AppOrderSpeakRemote> beanList = ObjectUtil.beanList(list, AppOrderSpeakRemote.class);
        return beanList;
    }

    @Override
    public void addSellOrderSpeak(String orderId, Long buyId, Long sellId, String sellSpeak) {
        AppOrderSpeak appOrderSpeak = new AppOrderSpeak();
        appOrderSpeak.setBuyId(buyId);
        appOrderSpeak.setSellId(sellId);
        appOrderSpeak.setSellSpeak(sellSpeak);
        appOrderSpeak.setOrderId(orderId);
        appOrderSpeakService.save(appOrderSpeak);
    }

    @Override
    public FrontPage userAppTransaction(Map<String,String> map) {

        Page<AppTransaction> page = PageFactory.getPage(map);

        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        if (StringUtil.isNotEmpty(map.get("buyUserId"))) {
            qf.addFilter("buyUserId=", map.get("buyUserId"));
        }
        if (StringUtil.isNotEmpty(map.get("sellUserId"))) {
            qf.addFilter("sellUserId=", map.get("sellUserId"));
        }
        if (StringUtil.isNotEmpty(map.get("transactionNum"))) {
            qf.addFilter("transactionNum_like", "%" + map.get("transactionNum") + "%");
        }
        if (StringUtil.isNotEmpty(map.get("status"))) {
            if ("3".equals(map.get("status"))) {
                qf.addFilter("status_in", "3,6,7");
            } else if ("4".equals(map.get("status"))) {
                qf.addFilter("status_in", "4,9,10,11,12,15,16");
            } else {
                qf.addFilter("status=", map.get("status"));
            }
        }
        //?????????????????????
        if (StringUtil.isNotEmpty(map.get("buyIsDeleted"))) {
            qf.addFilter("buyIsDeleted=", 0);
        }
        if (StringUtil.isNotEmpty(map.get("sellIsDeleted"))) {
            qf.addFilter("sellIsDeleted=", 0);
        }

        if (StringUtil.isNotEmpty(map.get("coinCode"))) {
            qf.addFilter("coinCode=", map.get("coinCode"));
        }
        if (StringUtil.isNotEmpty(map.get("id"))) {
            qf.addFilter("advertisementId=", map.get("id"));
        }
        qf.setOrderby("created desc");
        List<OtcAppTransaction> list = new ArrayList<>();
        if ("1".equals(map.get("type"))) {
            list = otcAppTransactionService.findOtcAppTransactionListBySql(map);
        } else {
            list = otcAppTransactionService.find(qf);
        }
        FrontPage userAppTransaction = new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
        // FrontPage userAppTransaction =
        // appTransactionService.userAppTransaction(map);
        List<OtcAppTransaction> list1 = userAppTransaction.getRows();
        List<OtcAppTransactionRemote> beanList = ObjectUtil.beanList(list1, OtcAppTransactionRemote.class);
        if (beanList != null && beanList.size() > 0) {
            for (OtcAppTransactionRemote att : beanList) {
                if (att != null && att.getTransactionNum() != null) {
                    ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(att.getAdvertisementId());
                    //???????????????id
                    QueryFilter filter = new QueryFilter(AppAppeal.class);
                    filter.addFilter("transactionNum=", att.getTransactionNum());
                    AppAppeal appeal = appAppealService.get(filter);
                    if (appeal != null && appeal.getUserId() != null) {
                        att.setAppealId(appeal.getUserId()); //????????????????????????id
                    }
                    QueryFilter personFilter1 = new QueryFilter(AppPersonInfo.class);
                    personFilter1.addFilter("customerId=", att.getBuyUserId());
                    AppPersonInfo buy = appPersonInfoService.get(personFilter1);
                    QueryFilter personFilter2 = new QueryFilter(AppPersonInfo.class);
                    personFilter2.addFilter("customerId=", att.getSellUserId());
                    AppPersonInfo sell = appPersonInfoService.get(personFilter2);
                    att.setBuySureName(buy.getSurname());
                    att.setBuyTrueName(buy.getTrueName());
                    att.setSellSureName(sell.getSurname());
                    att.setSellTrueName(sell.getTrueName());
                }
            }
        }
        userAppTransaction.setRows(beanList);
        return userAppTransaction;
    }

    @Override
    public synchronized JsonResult addAppeal(Long userId, String tradeNum, String appeal, String content, String thingUrl, String transactionMode) {
        try {
            OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(new QueryFilter(OtcAppTransaction.class).addFilter("transactionNum=", tradeNum));
            if (otcAppTransaction != null) {
                AppAppeal appAppeal = appAppealService.get(new QueryFilter(AppAppeal.class).addFilter("transactionNum=", tradeNum));
                if (appAppeal != null) {
                    userId = appAppeal.getUserId();
                    if ("1".equals(transactionMode)) {//??????????????????
                        appAppeal.setAppeal(appeal);
                        appAppeal.setContent(content);
                        appAppeal.setUserId(userId);
                        appAppeal.setTransactionNum(tradeNum);
                        appAppeal.setThingUrl(thingUrl);
                        appAppealService.update(appAppeal);

                        otcAppTransaction.setStatus(15);
                        otcAppTransactionService.update(otcAppTransaction);
                        return new JsonResult().setSuccess(true);
                    } else {
                        appAppeal.setAppealSell(appeal);
                        appAppeal.setContentSell(content);
                        appAppeal.setUserId(userId);
                        appAppeal.setTransactionNum(tradeNum);
                        appAppeal.setThingUrlSell(thingUrl);
                        appAppealService.update(appAppeal);

                        otcAppTransaction.setStatus(16);
                        otcAppTransactionService.update(otcAppTransaction);
                        return new JsonResult().setSuccess(true);
                    }
                } else {
                    if ("1".equals(transactionMode)) {//??????????????????
                        AppAppeal appAppealn = new AppAppeal();
                        appAppealn.setAppeal(appeal);
                        appAppealn.setContent(content);
                        appAppealn.setUserId(userId);
                        appAppealn.setTransactionNum(tradeNum);
                        appAppealn.setThingUrl(thingUrl);
                        appAppealService.save(appAppealn);

                        otcAppTransaction.setStatus(15);
                        otcAppTransactionService.update(otcAppTransaction);
                        return new JsonResult().setSuccess(true);
                    } else {
                        AppAppeal appAppealn = new AppAppeal();
                        appAppealn.setAppealSell(appeal);
                        appAppealn.setContentSell(content);
                        appAppealn.setUserId(userId);
                        appAppealn.setTransactionNum(tradeNum);
                        appAppealn.setThingUrlSell(thingUrl);
                        appAppealService.save(appAppealn);

                        otcAppTransaction.setStatus(16);
                        otcAppTransactionService.update(otcAppTransaction);
                        return new JsonResult().setSuccess(true);
                    }
                }
            }
            return new JsonResult().setSuccess(false).setMsg("dingdanbucunzai");
        } catch (Exception e) {
            return new JsonResult().setSuccess(false).setMsg("dingdanyichang");
        }
    }

    @Override
    public JsonResult appealInfor(String tradeNum) {
        Map<String,Object> map = new HashMap<String,Object>();

        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(new QueryFilter(OtcAppTransaction.class).addFilter("transactionNum=", tradeNum));
        if (otcAppTransaction != null) {
            // ????????????
            OtcAppTransactionRemote otcappTransactionRemote = ObjectUtil.bean2bean(otcAppTransaction, OtcAppTransactionRemote.class);
            if (otcAppTransaction.getStatus() == 1) {
                otcappTransactionRemote.setStateZHCN("daiquerendingdan");
            } else if (otcAppTransaction.getStatus() == 2) {
                otcappTransactionRemote.setStateZHCN("yiquerendingdan");
            } else if (otcAppTransaction.getStatus() == 3) {
                otcappTransactionRemote.setStateZHCN("yiwanchengzhifu");
            } else if (otcAppTransaction.getStatus() == 4) {
                otcappTransactionRemote.setStateZHCN("shensuzhongdaihuifu");
            } else if (otcAppTransaction.getStatus() == 5) {
                otcappTransactionRemote.setStateZHCN("yiquxiao");
            } else if (otcAppTransaction.getStatus() == 6) {
                otcappTransactionRemote.setStateZHCN("shenqingtuikuanzhong");
            } else if (otcAppTransaction.getStatus() == 7) {
                otcappTransactionRemote.setStateZHCN("tuikuanyibohui");
            } else if (otcAppTransaction.getStatus() == 8) {
                otcappTransactionRemote.setStateZHCN("shensuwancheng");
            } else if (otcAppTransaction.getStatus() == 9) {
                otcappTransactionRemote.setStateZHCN("shensuchenggongdaiqueren");
            } else if (otcAppTransaction.getStatus() == 10) {
                otcappTransactionRemote.setStateZHCN("shensushibaidaiqueren");
            } else if (otcAppTransaction.getStatus() == 11) {
                otcappTransactionRemote.setStateZHCN("pingtaitongguoshensu");
            } else if (otcAppTransaction.getStatus() == 12) {
                otcappTransactionRemote.setStateZHCN("pingtaibohuishensu");
            } else if (otcAppTransaction.getStatus() == 13) {
                otcappTransactionRemote.setStateZHCN("tuikuanchenggong");
            } else if (otcAppTransaction.getStatus() == 14) {
                otcappTransactionRemote.setStateZHCN("yiwancheng");
            } else if (otcAppTransaction.getStatus() == 15) {
                otcappTransactionRemote.setStateZHCN("maijiashensuzhong");
            } else if (otcAppTransaction.getStatus() == 16) {
                otcappTransactionRemote.setStateZHCN("maijiashensuzhong1");
            }
            String str = "";
            String[] split = otcappTransactionRemote.getPayType().split(",");
            if (split.length > 0) {
                for (int i = 0; i < split.length; i++) {
                    if ("1".equals(split[i])) {
                        str += "yinhangzhuanzhang,";
                        if (StringUtils.isEmpty(otcappTransactionRemote.getBankNumber())) {
                            otcappTransactionRemote.setBankNumber("****");
                        } else {
                            if (otcappTransactionRemote.getBankNumber().length() > 4) {
                                otcappTransactionRemote.setBankNumber(otcappTransactionRemote.getBankNumber().substring(0, 3) + "****");
                            } else {
                                otcappTransactionRemote.setBankNumber(otcappTransactionRemote.getBankNumber());
                            }
                        }
                    } else if ("2".equals(split[i])) {
                        str += "zhifubao2,";
                        if (StringUtils.isEmpty(otcappTransactionRemote.getAlipayAccount())) {
                            otcappTransactionRemote.setAlipayAccount("****");
                        } else {
                            if (otcappTransactionRemote.getAlipayAccount().length() > 4) {
                                otcappTransactionRemote.setAlipayAccount(otcappTransactionRemote.getAlipayAccount().substring(0, 3) + "****");
                            } else {
                                otcappTransactionRemote.setAlipayAccount("****");
                            }
                        }
                    } else if ("3".equals(split[i])) {
                        str += "weixinzhifu,";
                        if (StringUtils.isEmpty(otcappTransactionRemote.getWechatAccount())) {
                            otcappTransactionRemote.setWechatAccount("****");
                        } else {
                            if (otcappTransactionRemote.getWechatAccount().length() > 4) {
                                otcappTransactionRemote.setWechatAccount(otcappTransactionRemote.getWechatAccount().substring(0, 3) + "****");
                            } else {
                                otcappTransactionRemote.setWechatAccount("****");
                            }
                        }
                    }
                }
            }
            if (str.length() > 1) {
                str = str.substring(0, str.length() - 1);
            }
            otcappTransactionRemote.setPayType(str);
            //???????????????????????????????????????
            int keepDecimalForCoin = keepDecimalForCoin(otcappTransactionRemote.getCoinCode());

            otcappTransactionRemote.setTradeMoney(otcappTransactionRemote.getTradeMoney().setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN));
            otcappTransactionRemote.setTradeNum(otcappTransactionRemote.getTradeNum().setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN));


            map.put("app", otcappTransactionRemote);
            // ????????????
            ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(otcappTransactionRemote.getAdvertisementId());
            map.put("releaseId", otcappTransactionRemote.getAdvertisementId());
            BigDecimal tradeMoney = new BigDecimal("0");
            if (releaseAdvertisement != null) {
                map.put("payTypeRemake", releaseAdvertisement.getPayTypeRemake());
                map.put("isFixed", releaseAdvertisement.getIsFixed());
                map.put("paymentTerm", releaseAdvertisement.getPaymentTerm());
                //??????
                map.put("remark", releaseAdvertisement.getRemark());

                if (releaseAdvertisement.getIsFixed() == 0) {// ????????????
                    BigDecimal yi = new BigDecimal("1");// 1
                    BigDecimal yibai = new BigDecimal("100");// 100
                    BigDecimal premiumBD = releaseAdvertisement.getPremium();
                    BigDecimal shichangjiageBD = releaseAdvertisement.getTradeMoney();

                    if (releaseAdvertisement.getTransactionMode() == 1) {// ??????
                        // ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.subtract(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN))); // ????????????*(1-??????????????????)????????????

                        //map.put("tradeMoney", price.toString());
                        tradeMoney = price;

                    } else {// ?????? ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.add(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN)));// ????????????*(1+??????????????????)????????????

                        //map.put("tradeMoney", price.toString());
                        tradeMoney = price;
                    }
                } else {
                    //map.put("tradeMoney", releaseAdvertisement.getTradeMoney().toString());
                    tradeMoney = releaseAdvertisement.getTradeMoney();
                }
                tradeMoney = tradeMoney.setScale(keepDecimalForCoin, BigDecimal.ROUND_DOWN);
                map.put("tradeMoney", tradeMoney.toString());
            }
            // ????????????
            AppAppeal appAppeal = appAppealService.get(new QueryFilter(AppAppeal.class).addFilter("transactionNum=", otcAppTransaction.getTransactionNum()));
            String img_server_type = PropertiesUtils.APP.getProperty("app.img.server.type");
            if (appAppeal != null) {
                AppAppealRemote bean2bean = ObjectUtil.bean2bean(appAppeal, AppAppealRemote.class);
                if (StringUtil.isNotEmpty(bean2bean.getThingUrl())) {
                    String[] spl = bean2bean.getThingUrl().split(",");
                    List<String> list = new LinkedList<String>();
                    switch (img_server_type) {
                        case "oss": // ?????????oss
                            for (int i = 0; i < spl.length; i++) {
                                list.add(OssUtil.getUrl(spl[i]));
                            }
                            break;
                        case "aws": // ?????????aws
                            for (int i = 0; i < spl.length; i++) {
                                try {
                                    list.add(AWSUtil.getUrlFromS3(spl[i]));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "azure": // ??????azure
                            for (int i = 0; i < spl.length; i++) {
                                list.add(AzureUtil.getUrl(spl[i]));
                            }
                            break;
                        default: // ???????????????oss
                            for (int i = 0; i < spl.length; i++) {
                                list.add(OssUtil.getUrl(spl[i]));
                            }
                            break;
                    }
                    bean2bean.setImgUrl(list);
                }
                if (StringUtil.isNotEmpty(bean2bean.getThingUrlSell())) {
                    String[] spl = bean2bean.getThingUrlSell().split(",");
                    List<String> list = new LinkedList<String>();
                    switch (img_server_type) {
                        case "oss": // ?????????oss
                            for (int i = 0; i < spl.length; i++) {
                                list.add(OssUtil.getUrl(spl[i]));
                            }
                            break;
                        case "aws": // ?????????aws
                            for (int i = 0; i < spl.length; i++) {
                                try {
                                    list.add(AWSUtil.getUrlFromS3(spl[i]));
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            break;
                        case "azure": // ??????azure
                            for (int i = 0; i < spl.length; i++) {
                                list.add(AzureUtil.getUrl(spl[i]));
                            }
                            break;
                        default: // ???????????????oss
                            for (int i = 0; i < spl.length; i++) {
                                list.add(OssUtil.getUrl(spl[i]));
                            }
                            break;
                    }
                    bean2bean.setImgUrlSell(list);
                }
                map.put("appAppealRemote", bean2bean);
            }
        }
        return new JsonResult().setSuccess(true).setObj(map);
    }

    @Override
    public AppAppealRemote getAppAppealByNum(String transactionNum) {
        AppAppeal appAppeal = appAppealService.get(new QueryFilter(AppAppeal.class).addFilter("transactionNum=", transactionNum));
        if (appAppeal != null) {
            AppAppealRemote bean2bean = ObjectUtil.bean2bean(appAppeal, AppAppealRemote.class);
            if (StringUtil.isNotEmpty(bean2bean.getThingUrl())) {
                String[] spl = bean2bean.getThingUrl().split(",");
                List<String> list = new LinkedList<String>();
                for (int i = 0; i < spl.length; i++) {
                    list.add(spl[i]);
                }
                bean2bean.setImgUrl(list);
            }
            if (StringUtil.isNotEmpty(bean2bean.getThingUrlSell())) {
                String[] spl = bean2bean.getThingUrlSell().split(",");
                List<String> list = new LinkedList<String>();
                for (int i = 0; i < spl.length; i++) {
                    list.add(spl[i]);
                }
                bean2bean.setImgUrlSell(list);
            }
            return bean2bean;
        }
        return null;
    }

    @Override
    public JsonResult cancelAppeal(String transactionNum) {
        //???????????????????????????????????????????????????
        QueryFilter filter = new QueryFilter(OtcAppTransaction.class);
        filter.addFilter("transactionNum=", transactionNum);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(filter);
        //????????????
        if (otcAppTransaction != null) {
            otcAppTransaction.setStatus(3);//????????????
            otcAppTransactionService.update(otcAppTransaction);
        }

        //????????????????????????????????????????????????
        QueryFilter qf = new QueryFilter(AppAppeal.class);
        qf.addFilter("transactionNum=", transactionNum);
        AppAppeal appAppeal = appAppealService.get(qf);
        boolean success = false;
        if (appAppeal != null) {
            success = appAppealService.delete(appAppeal.getId());
        }
        return new JsonResult().setSuccess(success);
    }

    @Override
    public JsonResult refundAndReject(String tradeNum, String type, String transactionMode, String content, Long userid) {
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(new QueryFilter(OtcAppTransaction.class).addFilter("transactionNum=", tradeNum));
        if (otcAppTransaction != null) {
            if (transactionMode.equals("2")) {// ??????
                if ("6".equals(type)) {// ????????????

                    otcAppTransaction.setStatus(Integer.valueOf(type));
                    otcAppTransactionService.update(otcAppTransaction);
                    // ??????

                    ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(otcAppTransaction.getAdvertisementId());

                    if (null != releaseAdvertisement) {
                        int keepDecimalForCoin = keepDecimalForCoin(releaseAdvertisement.getCoinCode());

                        if (releaseAdvertisement.getCustomerId().equals(userid)) {// ?????????
                            // ???????????????
                            // ????????????
                            // ????????????????????????
                            //ExDigitalmoneyAccountRedis coinAccountRedis = getCoinAccountRedis(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());
                            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());
                            if (releaseAdvertisement.getIsFixed() == 1) {// ???????????????
                                // ????????? ?????? ??????=?????????
                                BigDecimal bs = releaseAdvertisement.getTradeMoneyMax().divide(releaseAdvertisement.getTradeMoney(), keepDecimalForCoin, BigDecimal.ROUND_DOWN);

                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 16);
                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs, 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 6);

                                //exCoinAccount.setColdMoney(exCoinAccount.getColdMoney().subtract(bs));// ??????
                                //exCoinAccount.setHotMoney(exCoinAccount.getHotMoney().add(bs));// ????????????
                            } else {
                                BigDecimal yi = new BigDecimal("1");// 1
                                BigDecimal yibai = new BigDecimal("100");// 100
                                BigDecimal price = releaseAdvertisement.getTradeMoney().multiply(yi.subtract(releaseAdvertisement.getPremium().divide(yibai))); // ????????????*(1-??????????????????)????????????

                                // ????????? ?????? ??????=?????????
                                BigDecimal bs = releaseAdvertisement.getTradeMoneyMax().divide(price, keepDecimalForCoin, BigDecimal.ROUND_DOWN);

                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 16);
                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs, 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 6);

                                //exCoinAccount.setColdMoney(exCoinAccount.getColdMoney().subtract(bs));// ??????
                                //exCoinAccount.setHotMoney(exCoinAccount.getHotMoney().add(bs));// ????????????
                            }

                        } else {// ????????? ????????? ????????? ???????????????
                            // ???????????????????????????
							/*ExCoinParameter exCoinParameter = exCoinParameterService
									.get(new QueryFilter(ExCoinParameter.class).addFilter("coinCode=",
											releaseAdvertisement.getCoinCode()));*/
                            //ExDigitalmoneyAccountRedis coinAccountRedis = getCoinAccountRedis(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());
                            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());

                            BigDecimal eatFee = new BigDecimal(0); // ???????????????
                            int eatFeeType = 0;// ???????????????????????????

                            String productinfoListall = redisService.get("otc:coinCodeList");
                            JSONArray parseArray = JSON.parseArray(productinfoListall);
                            for (int k = 0; k < parseArray.size(); k++) {
                                JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                                if (jo.getString("coinCode").equals(releaseAdvertisement.getCoinCode())) {
                                    if (jo.getInteger("eatFee") != null) {
                                        eatFee = jo.getBigDecimal("eatFee");
                                    }
                                    if (jo.getInteger("eatFeeType") != null) {
                                        eatFeeType = jo.getInteger("eatFeeType");
                                    }
                                }
                            }

                            if (eatFeeType == 0) {// ???????????????

                                // ????????? ?????? ??????=?????????
                                BigDecimal bs = otcAppTransaction.getTradeNum();

                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), (bs.add(eatFee)).multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 16);
                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.add(eatFee), 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 6);

								/*exCoinAccount.setColdMoney(
										exCoinAccount.getColdMoney().subtract(bs.add(exCoinParameter.getEatFee())));// ??????
								exCoinAccount.setHotMoney(
										exCoinAccount.getHotMoney().add(bs.add(exCoinParameter.getEatFee())));// ????????????

								exCoinAccountService.update(exCoinAccount);// ????????????*/

                            } else {// ??????????????????

                                // ????????? ?????? ??????=?????????
                                BigDecimal bs = otcAppTransaction.getTradeNum();

                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), (bs.add(bs.multiply(eatFee))).multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 16);
                                remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.add(bs.multiply(eatFee)), 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 6);

								/*exCoinAccount.setColdMoney(exCoinAccount.getColdMoney()
										.subtract(bs.add(bs.multiply(exCoinParameter.getEatFee()))));// ??????
								exCoinAccount.setHotMoney(exCoinAccount.getHotMoney()
										.add(bs.add(bs.multiply(exCoinParameter.getEatFee()))));// ????????????

								exCoinAccountService.update(exCoinAccount);// ????????????*/
                            }
                        }
                    }
                    return new JsonResult().setSuccess(true).setMsg("tuikuanchenggong");
                } else if ("14".equals(type)) {// ???????????? ??????????????????
                    otcAppTransaction.setStatus(Integer.valueOf(type));
                    otcAppTransactionService.update(otcAppTransaction);

                    // ??????
                    //ExDigitalmoneyAccountRedis coinAccountRedis = getCoinAccountRedis(otcAppTransaction.getSellUserId(), otcAppTransaction.getCoinCode());
                    OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getSellUserId(), otcAppTransaction.getCoinCode());

                    remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), otcAppTransaction.getCoinCode(), (otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee())).multiply(new BigDecimal(-1)), 2, 1, otcAppTransaction.getTransactionNum(), 16);
                    remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), otcAppTransaction.getCoinCode(), otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee()), 1, 1, otcAppTransaction.getTransactionNum(), 6);
                    // ???????????? //????????????
					/*sell.setColdMoney(sell.getColdMoney()
							.subtract(appTransaction.getTradeNum().add(appTransaction.getSellfee())));
					sell.setHotMoney(
							sell.getHotMoney().add(appTransaction.getTradeNum().add(appTransaction.getSellfee())));

					exCoinAccountService.update(sell);*/

                    return new JsonResult().setSuccess(true).setMsg("shensuchenggongdaiqueren");
                } else if ("10".equals(type)) {
                    return new JsonResult().setSuccess(true).setMsg("shensushibaidaiqueren");
                }
            } else if (transactionMode.equals("1")) {// ??????
                if ("6".equals(type)) {// ????????????
                    AppAppeal appAppeal = new AppAppeal();
                    appAppeal.setUserId(userid);
                    appAppeal.setContent(content);
                    appAppeal.setTransactionNum(tradeNum);
                    appAppealService.save(appAppeal);
                    otcAppTransaction.setStatus(Integer.valueOf(type));
                    otcAppTransactionService.update(otcAppTransaction);
                    return new JsonResult().setSuccess(true).setMsg("shenqingchenggongdaiqueren");
                }
            }
        }
        return new JsonResult().setSuccess(false).setMsg("dingdanbucunzai");
    }

    @Override
    public synchronized JsonResult cancleOrder(String tradeNum, int type, Long customerId) {
        JsonResult j = new JsonResult();
        System.out.println("????????????");
        Long setnx = redisService.setnx("orderNo:" + tradeNum, "1");
        if (setnx != null && setnx == 1L) {
            redisService.expire("orderNo:" + tradeNum, 5);

            QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
            qf.addFilter("transactionNum=", tradeNum);

            OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);
            //type:222222  ?????????????????????????????????
            Boolean flag = true;
            if (otcAppTransaction != null && type != 222222) {
                //??????????????????????????????,???????????????????????????
                if (!otcAppTransaction.getBuyUserId().equals(customerId)) {
                    flag = false;
                }
            }
            //??????????????????????????????????????????
            if (otcAppTransaction != null && otcAppTransaction.getStatus() == 2 && flag) {
                otcAppTransaction.setStatus(5);
                otcAppTransactionService.update(otcAppTransaction);

                //?????????????????????????????????????????????
                if (type == 222222) {
                    otcAppTransactionService.saveOtcLog(otcAppTransaction.getId());
                    publicMessage(otcAppTransaction, otcAppTransaction.getBuyUserId(), "11");
                }


                RedisModel rm = new RedisModel();
                rm.setUserId(otcAppTransaction.getCustomerId());
                rm.setCoinCode(otcAppTransaction.getCoinCode());
                rm.setTransactionMode(otcAppTransaction.getTransactionMode());
                rm.setTradeNum(otcAppTransaction.getTransactionNum());

                //????????????
                redisService.rpush("otc:queue", JSON.toJSONString(rm));
                //????????????
                redisService.publish("otcCompletionRate", JSON.toJSONString(rm));

                // ?????????coin???????????????
                String coinPercent = "";// ??????????????????

                String productinfoListall = redisService.get("otc:coinCodeList");
                JSONArray parseArray = JSON.parseArray(productinfoListall);
                for (int k = 0; k < parseArray.size(); k++) {
                    JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                    if (jo.getString("coinCode").equals(otcAppTransaction.getCoinCode())) {
                        if (jo.getString("coinPercent") != null) {
                            coinPercent = jo.getString("coinPercent");
                        }
                    }
                }


                // ????????????
                ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(otcAppTransaction.getAdvertisementId());
                if (releaseAdvertisement != null) {
                    //???????????????
                    BigDecimal yi = new BigDecimal("1");// 1
                    BigDecimal yibai = new BigDecimal("100");// 100
                    BigDecimal price = new BigDecimal("0");
                    //??????coinCode??????????????????????????????--????????????????????????
                    if (releaseAdvertisement.getIsFixed() == 1) {// ???????????????
                        price = releaseAdvertisement.getTradeMoney();
                    } else {
                        price = releaseAdvertisement.getTradeMoney();
                    }

                    //???????????????
                    if (releaseAdvertisement.getStatus() == 1) {
                        //?????????????????????????????????
                        if (otcAppTransaction.getTransactionMode() == 2) { //?????????????????????????????????otcAppTransaction???tradeNum???????????????????????????
                            releaseAdvertisement.setCoinNumMax(releaseAdvertisement.getCoinNumMax().add(otcAppTransaction.getTradeNum()));
                        } else if (otcAppTransaction.getTransactionMode() == 1) { //?????????????????????????????????otcAppTransaction???tradeNum??????????????????????????????????????????
                            if(releaseAdvertisement.getOtcFeeType()== 1){//OTC?????????????????????  0 ???????????????1?????????
                                releaseAdvertisement.setCoinNumMax(releaseAdvertisement.getCoinNumMax().add(otcAppTransaction.getTradeNum()));
                                releaseAdvertisement.setOtcFee(releaseAdvertisement.getOtcFee().add(otcAppTransaction.getBuyfee()));
                            }else{
                                releaseAdvertisement.setCoinNumMax(releaseAdvertisement.getCoinNumMax().add(otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee())));
                            }
                        }
                        //	releaseAdvertisement.setTradeMoneyMax(releaseAdvertisement.getCoinNumMax().multiply(price).setScale(2, BigDecimal.ROUND_HALF_DOWN));
                        //???????????????
                        releaseAdvertisement.setTransactionNum(releaseAdvertisement.getTransactionNum() - 1);
                        releaseAdvertisementService.update(releaseAdvertisement);

                        if (otcAppTransaction.getTransactionMode() == 2) {
                            //??????????????????
                            BigDecimal all = otcAppTransaction.getSellfee().add(otcAppTransaction.getTradeNum());
                            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getSellUserId(), releaseAdvertisement.getCoinCode());

                            remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), all.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 17);
                            remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), all, 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 7);
                        }
                    } else {//???????????????
                        if (otcAppTransaction.getTransactionMode() == 1) { //?????????????????????????????????otcAppTransaction???tradeNum??????????????????????????????????????????
                            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());
                            BigDecimal bs = new BigDecimal(0);
                            if(releaseAdvertisement.getOtcFeeType()== 1){//OTC?????????????????????  0 ???????????????1?????????
                                bs = otcAppTransaction.getTradeNum().add(otcAppTransaction.getBuyfee());
                            }else{
                                bs = otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee());
                            }
                            remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 17);
                            remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs, 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 7);
                        } else {
                            OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getSellUserId(), releaseAdvertisement.getCoinCode());
                            BigDecimal bs = otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee());
                            remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 17);
                            remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), bs, 1, 1, releaseAdvertisement.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 7);
                        }
                    }

                    //????????????????????????????????????????????????????????????????????????????????????????????????
                    if(releaseAdvertisement.getTransactionMode() == 2 && type == 222222){//????????????,?????????????????????????????????
                        this.closeReleaseAdvertisement(releaseAdvertisement.getId());//????????????
                    }
                }
            }
            //redisService.delete("orderNo:" + tradeNum);

            try {
                //??????24???????????????????????????3?????????30??????????????????????????????????????????????????????24??????????????????????????????
                if (otcAppTransaction.getTransactionMode() == 1) {
                    //24?????????????????????5??????
                    // int i=60*60*24;
                    int i = 60 * 5;
                    String ss = redisService.get("otc:cancleOrder:" + otcAppTransaction.getBuyUserId());
                    if (StringUtils.isEmpty(ss)) {
                        //???????????????24??????
                        redisService.save("otc:cancleOrder:" + otcAppTransaction.getBuyUserId(), "1", i);
                    } else {
                        int parseInt = Integer.parseInt(ss) + 1;
                        if (parseInt > 2) {
                            redisService.save("otc:canNotBuy:" + otcAppTransaction.getBuyUserId(), "0", i);
                            redisService.delete("otc:cancleOrder:" + otcAppTransaction.getBuyUserId());
                        }
                        //??????????????????
                        Long time = redisService.getKeyTime("otc:cancleOrder:" + otcAppTransaction.getBuyUserId());
                        if (time == null) {
                            redisService.save("otc:cancleOrder:" + otcAppTransaction.getBuyUserId(), parseInt + "", i);
                        } else {
                            redisService.save("otc:cancleOrder:" + otcAppTransaction.getBuyUserId(), parseInt + "", time.intValue());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            redisService.zrem("otc:tradeNum", tradeNum);
            return j.setSuccess(true);
        }
        return j.setSuccess(false).setMsg("qingbuyaochongfuquxiao");
    }


    @Override
    public synchronized JsonResult orderCompleted(String tradeNum, String paymentTerm, Long customerId) {
        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("transactionNum=", tradeNum);
        qf.addFilter("sellUserId=", customerId);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);

        if (otcAppTransaction != null) {
            //????????????????????????
            if (otcAppTransaction.getStatus() != 3) {
                //????????????????????????????????????????????????
                return new JsonResult().setSuccess(false).setMsg("dingdanyishixiao");
            }

            otcAppTransaction.setStatus(14);// ????????????
            otcAppTransaction.setPayTime(new Date());
            otcAppTransactionService.update(otcAppTransaction);

            RedisModel rm = new RedisModel();
            rm.setUserId(otcAppTransaction.getCustomerId());
            rm.setCoinCode(otcAppTransaction.getCoinCode());
            rm.setTransactionMode(otcAppTransaction.getTransactionMode());
            rm.setTradeNum(otcAppTransaction.getTransactionNum());

            //????????????
            redisService.rpush("otc:queue", JSON.toJSONString(rm));
            //????????????
            redisService.publish("otcCompletionRate", JSON.toJSONString(rm));
            try {
                //???????????????????????????????????????????????????redis?????????
                redisService.delete("otc:ReleaseAdvertisementAfterColse:" + tradeNum);
            } catch (Exception e) {
            }
            // ??????????????????????????????????????????????????????????????????????????????????????????
            // ????????? ????????????????????? ????????????
            // ??????
            // ex_coin_account
            //ExDigitalmoneyAccountRedis coinAccountRedisSell = getCoinAccountRedis(otcAppTransaction.getSellUserId(), otcAppTransaction.getCoinCode());
            OtcAccountRedis otcSellAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getSellUserId(), otcAppTransaction.getCoinCode());
            // ??????????????? ?????????????????????

            ReleaseAdvertisement r = releaseAdvertisementService.get(otcAppTransaction.getAdvertisementId());
            if (r.getCustomerId().equals(otcAppTransaction.getSellUserId())) {// ?????????????????????
                //?????????????????????
                remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcSellAccountRedis.getId(), otcAppTransaction.getCoinCode(), otcAppTransaction.getTradeNum().multiply(new BigDecimal(-1)), 2, 1, r.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 18);
                if (otcAppTransaction.getTransactionMode() == 1) {
                    if(r.getOtcFeeType() == 1){//OTC?????????????????????  0 ???????????????1?????????
                        // ??????????????????
                        remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcSellAccountRedis.getId(), otcAppTransaction.getCoinCode(), otcAppTransaction.getBuyfee().multiply(new BigDecimal(-1)), 2, 1, r.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 8);
                    }else{
                        // ????????????????????????
                        remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcSellAccountRedis.getId(), otcAppTransaction.getCoinCode(), otcAppTransaction.getSellfee().multiply(new BigDecimal(-1)), 2, 1, r.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 8);
                    }
                }
            } else {
                //??????????????????????????????????????????
                //???????????????????????????????????????
                remoteNewOtcService.publish(otcAppTransaction.getSellUserId(), otcSellAccountRedis.getId(), otcAppTransaction.getCoinCode(), (otcAppTransaction.getTradeNum().add(otcAppTransaction.getSellfee())).multiply(new BigDecimal(-1)), 2, 1, r.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 18);
                //TODO 1.?????????????????????????????????
                //????????????????????????????????????????????????
                //remoteNewOtcService.publish(otcAppTransaction.getSellUserId(),otcSellAccountRedis.getId(),otcAppTransaction.getCoinCode(),  otcAppTransaction.getSellfee(), 1, 1, r.getAdvertisementNum() +","+otcAppTransaction.getTransactionNum(), 8);
            }

            // ????????? ?????????????????????
            // ??????
            //ExDigitalmoneyAccountRedis coinAccountRedisBuy = getCoinAccountRedis(otcAppTransaction.getBuyUserId(), otcAppTransaction.getCoinCode());
            OtcAccountRedis otcBuyAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getBuyUserId(), otcAppTransaction.getCoinCode());

            // ???????????? //????????????
            remoteNewOtcService.publish(otcAppTransaction.getBuyUserId(), otcBuyAccountRedis.getId(), otcAppTransaction.getCoinCode(), otcAppTransaction.getTradeNum(), 1, 1, r.getAdvertisementNum() + "," + otcAppTransaction.getTransactionNum(), 8);

            //TODO 2.?????????????????????????????????????????????
			/*if(otcAppTransaction.getTransactionMode() == 2){
				OtcAccountRedis otcAccountRedisCustomer = remoteNewOtcService.getAccoutByIdAndCoinCode(otcAppTransaction.getCustomerId(), otcAppTransaction.getCoinCode());

				// ?????????????????????
				remoteNewOtcService.publish(otcAppTransaction.getCustomerId(),otcAccountRedisCustomer.getId(),otcAppTransaction.getCoinCode(), otcAppTransaction.getSellfee().multiply(new BigDecimal(-1)), 1, 1,
                        r.getAdvertisementNum() +","+otcAppTransaction.getTransactionNum(), 8);
			}*/

            // ???????????????
            // ex_coin_fee
            ExCoinFee exCoinFee = new ExCoinFee();
            exCoinFee.setCoinCode(otcAppTransaction.getCoinCode());
            exCoinFee.setCoinName(otcAppTransaction.getCoinCode());
            exCoinFee.setCustomerId(otcAppTransaction.getCustomerId());
            AppCustomer appCustomer = otcService.getAppCustomerById(otcAppTransaction.getCustomerId());
            if (appCustomer != null) {
                exCoinFee.setUserName(appCustomer.getUserName());
            }

            if(r.getTransactionMode() == 1){//????????????
                if(r.getOtcFeeType() == 1) {//OTC?????????????????????  0 ???????????????1?????????
                    exCoinFee.setFee(otcAppTransaction.getBuyfee());
                }else{
                    exCoinFee.setFee(otcAppTransaction.getSellfee());
                }
            }else{
                exCoinFee.setFee(otcAppTransaction.getSellfee());
            }


            int eatFeeType = 0;// ?????????????????????

            String productinfoListall = redisService.get("otc:coinCodeList");
            JSONArray parseArray = JSON.parseArray(productinfoListall);
            for (int k = 0; k < parseArray.size(); k++) {
                JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                if (jo.getString("name").equals(otcAppTransaction.getCoinCode())) {
                    if (jo.getInteger("eatFeeType") != null) {
                        eatFeeType = jo.getInteger("eatFeeType");
                    }
                }
            }

            exCoinFee.setFeeType(eatFeeType);
            exCoinFee.setVolume(otcAppTransaction.getTradeNum());
            exCoinFee.setStatus(2);
            exCoinFeeService.save(exCoinFee);

            Boolean flag = false;

            String[] exCoinFeeType = getExCoinFeeType(r.getCoinCode());
            if (r.getTradeMoneyMix().compareTo(r.getCoinNumMax().multiply(r.getTradeMoney())) == 1) {
                flag = true;
            } else if ("0".equals(exCoinFeeType[0]) && r.getCoinNumMax().compareTo(new BigDecimal(exCoinFeeType[1])) <= 0) {
                flag = true;
            }

            if (flag) {
                QueryFilter f1 = new QueryFilter(OtcAppTransaction.class);
                f1.addFilter("advertisementId=", r.getId());
                f1.addFilter("status_in", "1,2,3,4,6,7,8,9,10,11,12,13,15,16");
                Long count = otcAppTransactionService.count(f1);
                if (count == 0L) {
                    // ?????? ????????????
                    OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(r.getCustomerId(), r.getCoinCode());
                    r.setState(1);//????????????
                    r.setStatus(2);//?????????
                    if (r.getTransactionMode() == 1 && r.getCoinNumMax().compareTo(BigDecimal.ZERO) > 0) { //????????????
                        remoteNewOtcService.publish(r.getCustomerId(), otcAccountRedis.getId(), r.getCoinCode(), r.getCoinNumMax().add(r.getOtcFee()).multiply(new BigDecimal(-1)), 2, 1, r.getAdvertisementNum(), 19);
                        remoteNewOtcService.publish(r.getCustomerId(), otcAccountRedis.getId(), r.getCoinCode(), r.getCoinNumMax().add(r.getOtcFee()), 1, 1, r.getAdvertisementNum(), 9);
                    }
                }
            }


            ////????????????????????????????????????????????????????????????????????????
            //if(r.getCoinNumMax().compareTo(BigDecimal.ZERO) == 0){
            //	r.setState(1);//????????????
            //	r.setStatus(2);//?????????
            //}
            releaseAdvertisementService.update(r);

            return new JsonResult().setSuccess(true).setObj(otcAppTransaction.getBuyUserId());
        }
        return new JsonResult().setSuccess(false).setMsg("dingdanbucunzai");
    }

    @Override
    public synchronized JsonResult orderPayment(String tradeNum, String paymentTerm, String selPay, Long customerId) {
        //????????????????????????
        Double index = redisService.zscore("otc:tradeNum", tradeNum);
        if (index == null) {//???????????????
            return new JsonResult().setSuccess(false).setMsg("dingdanyishixiao");
        }

        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("transactionNum=", tradeNum);
        qf.addFilter("buyUserId=", customerId);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);
        if (otcAppTransaction != null) {
            if (otcAppTransaction.getStatus() != 2) {
                //????????????????????????????????????,????????????????????????
                return new JsonResult().setSuccess(false).setMsg("dingdanbucunzai").setObj("1");
            }

            JsonResult result = getPersonalAsset(otcAppTransaction.getSellUserId(), selPay, "1");
            //????????????
            if (!result.getSuccess()) {
                if ("1".equals(selPay)) {
                    return new JsonResult().setSuccess(false).setMsg(hry.util.common.SpringUtil.diff("qingxianshezhiyinghangka"));
                } else if ("2".equals(selPay)) {
                    return new JsonResult().setSuccess(false).setMsg(hry.util.common.SpringUtil.diff("qingxianshezhizhifubao"));
                } else if ("3".equals(selPay)) {
                    return new JsonResult().setSuccess(false).setMsg(hry.util.common.SpringUtil.diff("qingxianshezhiweixin"));
                }
            }

            otcAppTransaction.setStatus(3);// ????????????
            otcAppTransaction.setPayTime(new Date());
            otcAppTransaction.setSelPay(selPay);
            otcAppTransactionService.update(otcAppTransaction);
            redisService.zrem("otc:tradeNum", tradeNum);
            return new JsonResult().setSuccess(true).setMsg("caozuochenggong").setObj(otcAppTransaction.getSellUserId());
        }
        return new JsonResult().setSuccess(false).setMsg("dingdanbucunzai");
    }

    @Override
    public JsonResult updateIsDeleted(String transactionNum, String type) {
        QueryFilter filter = new QueryFilter(OtcAppTransaction.class);
        filter.addFilter("transactionNum=", transactionNum);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(filter);
        if (otcAppTransaction != null) {
            if ("1".equals(type)) {
                otcAppTransaction.setBuyIsDeleted(1);
            } else if ("2".equals(type)) {
                otcAppTransaction.setSellIsDeleted(1);
            }
            otcAppTransactionService.update(otcAppTransaction);
            return new JsonResult().setSuccess(true);
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public synchronized JsonResult closeReleaseAdvertisement(Long releaseId) {
        Long setnx = redisService.setnx("Advertisement:" + releaseId, "1");
        if (setnx != null && setnx == 1L) {
            redisService.expire("Advertisement:" + releaseId, 5);

            ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(releaseId);

            if (releaseAdvertisement.getStatus() == 0) {
                return new JsonResult().setSuccess(false).setMsg("ciguanggaoyiguanbi");
            }
            if (null != releaseAdvertisement) {

                //ExDigitalmoneyAccountRedis coinAccountRedis = getCoinAccountRedis(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());
                // ?????? ????????????
                OtcAccountRedis otcAccountRedis = remoteNewOtcService.getAccoutByIdAndCoinCode(releaseAdvertisement.getCustomerId(), releaseAdvertisement.getCoinCode());

                if (releaseAdvertisement.getTransactionMode() == 1) {// ?????? ??????

                    Map<String,BigDecimal> result = otcAppTransactionService.getTransactionSumById(releaseId);
                    BigDecimal coldNum = BigDecimal.ZERO;
                    BigDecimal yiwancheng = new BigDecimal(0);//?????????
                    BigDecimal daifukuan = new BigDecimal(0);//?????????
                    BigDecimal yifukuan = new BigDecimal(0);//?????????
                    BigDecimal shensuzhong = new BigDecimal(0);//?????????
                    if(releaseAdvertisement.getOtcFeeType() == 0){//OTC?????????????????????  0 ???????????????1?????????
                        yiwancheng = result.get("tradeNum3").add(result.get("sellfee3"));
                        daifukuan = result.get("tradeNum1").add(result.get("sellfee1"));
                        yifukuan = result.get("tradeNum2").add(result.get("sellfee2"));
                        shensuzhong = result.get("tradeNum5").add(result.get("sellfee5"));
                        //????????????????????????????????? = ??????????????????-???????????????= ???????????? - ????????? - ????????? - ????????? - ????????? - ?????????
                        coldNum = releaseAdvertisement.getInitialCoinNumMax().subtract(shensuzhong).subtract(yifukuan).subtract(daifukuan).subtract(yiwancheng);
                    }else{
                        yiwancheng = result.get("tradeNum3").add(result.get("buyfee3"));
                        daifukuan = result.get("tradeNum1").add(result.get("buyfee1"));
                        yifukuan = result.get("tradeNum2").add(result.get("buyfee2"));
                        shensuzhong = result.get("tradeNum5").add(result.get("buyfee5"));
                        //????????????????????????????????? = ???????????? + ??????????????? - ????????? - ????????? - ????????? - ?????????
                        coldNum = releaseAdvertisement.getInitialCoinNumMax().add(releaseAdvertisement.getInitialOtcFee())
                                .subtract(shensuzhong).subtract(yifukuan).subtract(daifukuan).subtract(yiwancheng);
                    }




                    if (releaseAdvertisement.getIsFixed() == 1) {// ???????????????
                        remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), coldNum.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum(), 19);
                        remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), coldNum, 1, 1, releaseAdvertisement.getAdvertisementNum(), 9);
                    } else {
                        remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), coldNum.multiply(new BigDecimal(-1)), 2, 1, releaseAdvertisement.getAdvertisementNum(), 19);
                        remoteNewOtcService.publish(releaseAdvertisement.getCustomerId(), otcAccountRedis.getId(), releaseAdvertisement.getCoinCode(), coldNum, 1, 1, releaseAdvertisement.getAdvertisementNum(), 9);
                    }
                }
                releaseAdvertisement.setState(1);
                releaseAdvertisement.setStatus(0);
                releaseAdvertisementService.update(releaseAdvertisement);// ????????????

                redisService.delete("Advertisement:" + releaseId);

                return new JsonResult().setSuccess(true).setMsg("chuliwancheng");
            } else {
                return new JsonResult().setSuccess(false).setMsg("ciguanggaoyiguoqi");
            }
        }
        return new JsonResult().setSuccess(false).setMsg("qingbuyaochongfuquxiao");
    }

    @Override
    public FrontPage queryReleaseAdvertisement(Map<String,String> params) {
        FrontPage userAppTransaction = releaseAdvertisementService.findPageBySql(params);
        List<ReleaseAdvertisement> list = userAppTransaction.getRows();
        int keepDecimalForCoin = 0;
        String productinfoListall = redisService.get("otc:coinCodeList");
        JSONArray parseArray = JSON.parseArray(productinfoListall);

        if (list != null && list.size() > 0) {
            for (ReleaseAdvertisement ad : list) {
                for (int k = 0; k < parseArray.size(); k++) {
                    JSONObject jo = JSON.parseObject(parseArray.get(k).toString());
                    if (jo.getString("coinCode").equals(ad.getCoinCode())) {
                        if (jo.getInteger("keepDecimalForCoin") != null) {
                            keepDecimalForCoin = jo.getInteger("keepDecimalForCoin");
                        }
                    }
                }
                ad.setKeepDecimalForCoin(keepDecimalForCoin);
                if (ad != null && ad.getIsFixed() != null && ad.getIsFixed() == 0) { //???????????? ????????????
                    BigDecimal yi = new BigDecimal("1");// 1
                    BigDecimal yibai = new BigDecimal("100");// 100
                    BigDecimal premiumBD = ad.getPremium();
                    BigDecimal shichangjiageBD = ad.getTradeMoney();
                    if (ad.getTransactionMode() == 1) {// ?????? ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.subtract(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN))).setScale(2, BigDecimal.ROUND_HALF_DOWN); // ????????????*(1-??????????????????)????????????
                        ad.setTradeMoney(price);
                    } else {// ?????? ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.add(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN))).setScale(2, BigDecimal.ROUND_HALF_DOWN);// ????????????*(1+??????????????????)????????????
                        ad.setTradeMoney(price);
                    }
                }
                //???????????????????????????????????????????????????
                BigDecimal zero = new BigDecimal("0");
                if (ad.getInitialCoinNumMax() != null) {
                    if (ad.getStatus() == 0) {
                        if (ad.getInitialCoinNumMax().compareTo(ad.getCoinNumMax()) == 0) {
                            //?????????
                            ad.setAdvStatus(3);
                        } else if (ad.getCoinNumMax().compareTo(zero) == 0) {
                            //????????????
                            ad.setAdvStatus(1);
                        } else {
                            //????????????
                            ad.setAdvStatus(2);
                        }
                    }
                } else {
                    //???????????????????????????
                    ad.setAdvStatus(3);
                }

                Map<String,BigDecimal> result = otcAppTransactionService.getTransactionSumById(ad.getId());
                ad.setOrderMoney(new BigDecimal(result.get("tradeMoney3").toString()));
                BigDecimal orderNum = BigDecimal.ZERO;//????????????
                BigDecimal currentColdNum = BigDecimal.ZERO;//??????????????????
                BigDecimal settleNum = BigDecimal.ZERO;//???????????????

                BigDecimal orderFeeNum = BigDecimal.ZERO;//?????????????????????
                BigDecimal currentColdFeeNum = BigDecimal.ZERO;//???????????????????????????
                BigDecimal settleFeeNum = BigDecimal.ZERO;//????????????????????????

                if (ad.getTransactionMode() == 1) {
                    orderNum = result.get("tradeNum3").add(result.get("sellfee3"));
                    settleNum = result.get("tradeNum1").add(result.get("sellfee1")).add(result.get("tradeNum2")).
                            add(result.get("sellfee2")).add(result.get("tradeNum5")).add(result.get("sellfee5"));
                    if (ad.getStatus() == 1) {
                        currentColdNum = ad.getInitialCoinNumMax().subtract(result.get("tradeNum3").add(result.get("sellfee3")));
                    } else {
                        currentColdNum = result.get("tradeNum1").add(result.get("sellfee1")).add(result.get("tradeNum2")).
                                add(result.get("sellfee2")).add(result.get("tradeNum5")).add(result.get("sellfee5"));
                    }

                    orderFeeNum = result.get("buyfee3");
                    settleFeeNum = result.get("buyfee1").add(result.get("buyfee2")).add(result.get("buyfee5"));
                    if (ad.getStatus() == 1) {
                        currentColdFeeNum = ad.getInitialOtcFee().subtract(result.get("sellfee3"));
                    }else{
                        currentColdFeeNum = result.get("buyfee1").add(result.get("buyfee2")).add(result.get("buyfee5"));
                    }

                } else {
                    orderNum = result.get("tradeNum3");
                    settleNum = result.get("tradeNum1").add(result.get("tradeNum2")).add(result.get("tradeNum5"));
                    if (ad.getStatus() == 1) {
                        currentColdNum = ad.getInitialCoinNumMax().subtract(result.get("tradeNum3"));
                    } else {
                        currentColdNum = result.get("tradeNum1").add(result.get("tradeNum2")).add(result.get("tradeNum5"));
                    }
                }
                ad.setOrderNum(orderNum);
                ad.setCurrentColdNum(currentColdNum);
                ad.setSettleNum(settleNum);

                ad.setOrderFeeNum(orderFeeNum);
                ad.setCurrentColdFeeNum(currentColdFeeNum);
                ad.setSettleFeeNum(settleFeeNum);

                String s = result.get("count1").add(result.get("count2")).add(result.get("count3")).add(result.get("count4")).add(result.get("count5")).toString();
                int sum = Integer.parseInt(s);
                ad.setOrderSum(sum);
            }
        }
        List<ReleaseAdvertisementRemote> beanList = ObjectUtil.beanList(list, ReleaseAdvertisementRemote.class);

        //??????created???????????????
        if (beanList != null && beanList.size() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < beanList.size(); i++) {
                beanList.get(i).setCreated(sdf.format(new Date(Long.valueOf(beanList.get(i).getCreated()))));
            }
        }
        userAppTransaction.setRows(beanList);
        return userAppTransaction;
    }

    /**
     * ???????????????id???????????????????????????
     */
    @Override
    public JsonResult batchCloseAd(Long customerId, String transactionMode) {
        QueryFilter filter = new QueryFilter(ReleaseAdvertisement.class);
        filter.addFilter("customerId=", customerId);
        filter.addFilter("transactionMode=", transactionMode);
        List<ReleaseAdvertisement> list = releaseAdvertisementService.find(filter);
        boolean flag = true;
        if (list != null) {
            for (ReleaseAdvertisement ad : list) {
                if (ad != null) {
                    //????????????????????????????????????
                    if (ad.getStatus() == 1) {//??????????????????
                        JsonResult jsonResult = this.closeReleaseAdvertisement(ad.getId());
                        flag = flag || jsonResult.getSuccess();
                    }
                }
            }
        }
        if (flag) {
            return new JsonResult().setSuccess(true);
        } else {
            return new JsonResult().setSuccess(false);
        }
    }

    @Override
    public void addPersonalAsset(String type, String bankName, String account, String surname, String truename, String subBranch, String thingUrl, String customerId, String bankAddress, String bankProvince, String cardBank) {

        //??????????????????
        Map<String,String> params = new HashMap<String,String>();
        params.put("customerId", customerId);
        params.put("type", type);
        otcService.updateIsDefault(params);

        //????????????????????????
        AppBankCard appBankCard = new AppBankCard();
        appBankCard.setType(Integer.valueOf(type));
        appBankCard.setCardNumber(account);
        appBankCard.setCardBank(cardBank);
        appBankCard.setBankProvince(bankProvince);
        appBankCard.setBankAddress(bankAddress);
        appBankCard.setCustomerId(Long.valueOf(customerId));
        appBankCard.setSurname(surname);
        appBankCard.setTrueName(truename);
        appBankCard.setSubBank(subBranch);
        appBankCard.setThingUrl(thingUrl);
        appBankCard.setIsDefault(1); //????????????
        otcService.save(appBankCard);
    }

    @Override
    public JsonResult deletePersonalAsset(Long id, Long userId) {
        List<ReleaseAdvertisement> list = releaseAdvertisementService.find(new QueryFilter(ReleaseAdvertisement.class).addFilter("customerId=", userId));
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStatus() == 1 || list.get(i).getState() == 0) {
                    return new JsonResult().setSuccess(false).setMsg("yinhangkbunengshanchu");
                }
            }
        }
        otcService.updateIsDeleteById(id);
        return new JsonResult().setSuccess(true).setMsg("shanchuchenggong");
    }

    /**
     * ???????????????????????????
     */
    @Override
    public JsonResult setDefault(Long id, String type, Long userId) {
        //??????????????????
        Map<String,String> params = new HashMap<String,String>();
        params.put("customerId", userId.toString());
        params.put("type", type);
        otcService.updateIsDefault(params);

        //??????id????????????
        AppBankCard appBankCard = otcService.getById(id);
        if (appBankCard != null) {
            //????????????????????????????????????????????????
            appBankCard.setIsDefault(1);
            otcService.update(appBankCard);
            return new JsonResult().setSuccess(true);
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult updateNickName(String nName, Long id) {
        JsonResult jsonResult = new JsonResult();
        QueryFilter filter = new QueryFilter(AppCustomer.class);
        filter.addFilter("nickNameOtc=", nName);
        AppCustomer appCustomer = appCustomerService.get(filter);
        if (appCustomer != null) {
            jsonResult.setMsg("nichengyicunzai");
            jsonResult.setSuccess(false);
            return jsonResult;
        }
        otcService.updateNickName(nName, id);
        jsonResult.setSuccess(true);
        return jsonResult;
    }

    @Override
    public FrontPage gettransaction(Map<String,String> params) {
        FrontPage wFrontPage = new FrontPage(null, 0, 0, 0);
        List<OtcAppTransaction> list = otcAppTransactionService.find(new QueryFilter(AppTransaction.class).addFilter("status_in", "1,2"));
        List<OtcAppTransactionRemote> beanList = ObjectUtil.beanList(list, OtcAppTransactionRemote.class);
        wFrontPage.setRows(beanList);
        return wFrontPage;
    }

    @Override
    public JsonResult recoveryReleaseAdvertisement(String tradeNum, Long releaseId, int type) {
        try {
            cancleOrder(tradeNum, type, 0L);
            return new JsonResult().setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
            return new JsonResult().setSuccess(false);
        }
    }

    @Override
    public void addOrderSpeak(String orderId, Long buyId, Long sellId, String buySpeak) {
        AppOrderSpeak appOrderSpeak = new AppOrderSpeak();
        appOrderSpeak.setOrderId(orderId);
        appOrderSpeak.setBuyId(buyId);
        appOrderSpeak.setSellId(sellId);
        appOrderSpeak.setBuySpeak(buySpeak);
        appOrderSpeakService.save(appOrderSpeak);
    }

    @Override
    public JsonResult isBankCard(String type, Long id) {
        AppBankCard bankCard = otcService.isBankCard(type, id);
        if (bankCard != null) {
            return new JsonResult().setSuccess(true);
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult trustShield(Long trustId, Long ShieldId, String type) {
        Map<String,Object> map = new HashMap<String,Object>();

        QueryFilter qf = new QueryFilter(TrustShield.class);
        qf.addFilter("trust=", trustId);
        qf.addFilter("isTrust=", ShieldId);
        List<TrustShield> list = trustShieldService.find(qf);
        if (list.size() == 0) {
            AppCustomer appCustomer = otcService.getAppCustomerById(ShieldId);
            if (appCustomer != null) {
                TrustShield trustShield1 = new TrustShield();
                trustShield1.setTrust(trustId);
                trustShield1.setIsTrust(ShieldId);
                if ("1".equals(type)) {// ??????
                    appCustomer.setTrustNum(appCustomer.getTrustNum() + 1);
                    trustShield1.setStatus(1);
                } else if ("2".equals(type)) {// ??????
                    appCustomer.setShieldNum(appCustomer.getShieldNum() + 1);
                    trustShield1.setStatus(2);
                }
                trustShieldService.save(trustShield1);
                otcService.updateAppCustomer(appCustomer);
                map.put("trustNum", appCustomer.getTrustNum());
                map.put("isflag", "1");
                map.put("type", type);
            }
        } else {
            AppCustomer appCustomer = otcService.getAppCustomerById(ShieldId);
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).getStatus().toString().equals(type)) {
                    if ("1".equals(type)) {// ??????
                        appCustomer.setTrustNum(appCustomer.getTrustNum() - 1 < 0 ? 0 : appCustomer.getTrustNum() - 1);
                    } else if ("2".equals(type)) {// ??????
                        appCustomer.setShieldNum(appCustomer.getShieldNum() - 1 < 0 ? 0 : appCustomer.getShieldNum() - 1);
                    }
                    trustShieldService.delete(list.get(i).getId());
                    otcService.updateAppCustomer(appCustomer);
                    map.put("trustNum", appCustomer.getTrustNum());
                    map.put("isflag", "2");
                    map.put("type", type);
                } else {
                    TrustShield trustShield1 = new TrustShield();
                    trustShield1.setTrust(trustId);
                    trustShield1.setIsTrust(ShieldId);
                    if ("1".equals(type)) {// ??????
                        appCustomer.setTrustNum(appCustomer.getTrustNum() + 1);
                        trustShield1.setStatus(1);
                    } else if ("2".equals(type)) {// ??????
                        appCustomer.setShieldNum(appCustomer.getShieldNum() + 1);
                        trustShield1.setStatus(2);
                    }
                    trustShieldService.save(trustShield1);
                    otcService.updateAppCustomer(appCustomer);
                    map.put("trustNum", appCustomer.getTrustNum());
                    map.put("isflag", "1");
                    map.put("type", type);
                }
            }
        }
        return new JsonResult().setSuccess(true).setObj(map);
    }

    @Override
    public JsonResult getUserById(Long id) {
        Map<String,Object> map = new HashMap<String,Object>();
        AppCustomer appCustomer = otcService.getAppCustomerById(id);
        if (appCustomer != null) {
            User user = ObjectUtil.bean2bean(appCustomer, User.class);
            user.setNickNameOtc(appCustomer.getNickNameOtc());
            user.setCustomerId(appCustomer.getId());

            AppPersonInfo appPersonInfo = otcService.getAppPersonInfoByUserId(id);
            if (appPersonInfo != null) {
                user.setMobile(appPersonInfo.getMobilePhone());
                user.setEmail(appPersonInfo.getEmail());
            }
            map.put("user", user);

            QueryFilter qf = new QueryFilter(OtcAppLog.class);
            qf.addFilter("userId=", id);
            OtcAppLog otcAppLog = otcAppLogService.get(qf);
            if (otcAppLog != null) {
                OtcAppLogRemote bean2bean = ObjectUtil.bean2bean(otcAppLog, OtcAppLogRemote.class);
                map.put("logoutTime", bean2bean.getLogoutTime());
                map.put("tradeTime", bean2bean.getTradeTime());
            }
        }
        return new JsonResult().setSuccess(true).setObj(map);
    }

    @Override
    public String avgTime(Long customerId) {
        String avgTime = releaseAdvertisementService.avgTime(customerId);
        if (avgTime == null) {
            avgTime = "0";
        }
        return avgTime;
    }

    @Override
    public JsonResult isLogLogin(Long userId) {
        QueryFilter qf = new QueryFilter(OtcAppLog.class);
        qf.addFilter("userId=", userId);
        OtcAppLog appLog = otcAppLogService.get(qf);
        if (appLog != null) {
            OtcAppLogRemote appLogRemote = ObjectUtil.bean2bean(appLog, OtcAppLogRemote.class);
            return new JsonResult().setObj(appLogRemote);
        }
        return new JsonResult().setObj(null);
    }

    @Override
    public void updateByPrimaryKeySelective(OtcAppLogRemote appLogRemote) {
        OtcAppLog appLog = ObjectUtil.bean2bean(appLogRemote, OtcAppLog.class);
        otcAppLogService.updateNull(appLog);
    }

    @Override
    public void insert(OtcAppLogRemote appLogRemote) {
        OtcAppLog appLog = ObjectUtil.bean2bean(appLogRemote, OtcAppLog.class);
        otcAppLogService.save(appLog);
    }

    @Override
    public JsonResult aduserTable(Long id, String state) {
        QueryFilter qf = new QueryFilter(ReleaseAdvertisement.class);
        qf.addFilter("customerId=", id);
        qf.addFilter("transactionMode=", state);
        qf.addFilter("state=", 0);
        qf.addFilter("status=", 1);
        List<ReleaseAdvertisement> list = releaseAdvertisementService.find(qf);
        if (list != null && list.size() > 0) {
            //??????
            for (ReleaseAdvertisement ad : list) {
                if (ad != null && ad.getIsFixed() != null && ad.getIsFixed() == 0) { //???????????? ????????????
                    BigDecimal yi = new BigDecimal("1");// 1
                    BigDecimal yibai = new BigDecimal("100");// 100
                    BigDecimal premiumBD = ad.getPremium();
                    BigDecimal shichangjiageBD = ad.getTradeMoney();
                    if (ad.getTransactionMode() == 1) {// ?????? ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.subtract(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN))); // ????????????*(1-??????????????????)????????????
                        ad.setTradeMoney(price);
                    } else {// ?????? ??????
                        BigDecimal price = shichangjiageBD.multiply(yi.add(premiumBD.divide(yibai, 4, BigDecimal.ROUND_DOWN)));// ????????????*(1+??????????????????)????????????
                        ad.setTradeMoney(price);
                    }
                }
            }

            List<ReleaseAdvertisementRemote> beanList = ObjectUtil.beanList(list, ReleaseAdvertisementRemote.class);
            return new JsonResult().setSuccess(true).setObj(beanList);
        }
        return new JsonResult().setSuccess(false);
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param releaseId
     * @return
     */
    @Override
    public Boolean findOrderByStatus(Long releaseId) {
        ReleaseAdvertisement releaseAdvertisement = releaseAdvertisementService.get(releaseId);
        if (releaseAdvertisement != null) {
            QueryFilter filter = new QueryFilter(OtcAppTransaction.class);
            //????????????(1????????????,2????????????,3????????????)
            if (releaseAdvertisement.getTransactionMode() == 2) {
                filter.addFilter("buyUserId=", releaseAdvertisement.getCustomerId());
            } else if (releaseAdvertisement.getTransactionMode() == 1) {
                filter.addFilter("sellUserId=", releaseAdvertisement.getCustomerId());
            }
            filter.addFilter("status _in ", "(1,2,3,4,6,7,8,9,10,11,12,13,15,16)");
            filter.addFilter("advertisementId =", releaseAdvertisement.getId());
            Long count = otcAppTransactionService.count(filter);
            if (count > 0L) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void closeAllReleaseAdvertisement() {
        //???????????????????????????????????????
        QueryFilter filter = new QueryFilter(ReleaseAdvertisement.class);
        filter.addFilter("isFixed=", 1);
        filter.addFilter("status=", 1);
        filter.addFilter("tradeMoney_LET", "5.5");
        List<ReleaseAdvertisement> releaseAdvertisements = releaseAdvertisementService.find(filter);
        if (releaseAdvertisements != null && releaseAdvertisements.size() > 0) {
            for (ReleaseAdvertisement releaseAdvertisement : releaseAdvertisements) {
                QueryFilter f1 = new QueryFilter(OtcAppTransaction.class);
                f1.addFilter("advertisementId=", releaseAdvertisement.getId());
                f1.addFilter("status_in", "1,2,3,4,6,7,8,9,10,11,12,13,15,16");
                Long count = otcAppTransactionService.count(f1);
                if (count == 0L) {
                    //????????????5.5?????????????????????????????????????????????????????????????????????
                    closeReleaseAdvertisement(releaseAdvertisement.getId());
                    System.out.println("????????????,??????id??????" + releaseAdvertisement.getId() + ",????????????id??????" + releaseAdvertisement.getCustomerId());
                }
            }
        }

        //??????????????????
        QueryFilter filter1 = new QueryFilter(ReleaseAdvertisement.class);
        filter1.addFilter("isFixed=", 0);
        filter1.addFilter("status=", 1);
        BigDecimal money = new BigDecimal("5.5");
        List<ReleaseAdvertisement> releaseAdvertisementList = releaseAdvertisementService.find(filter1);
        if (releaseAdvertisementList != null && releaseAdvertisementList.size() > 0) {
            for (ReleaseAdvertisement releaseAdvertisement : releaseAdvertisementList) {
                QueryFilter f1 = new QueryFilter(OtcAppTransaction.class);
                f1.addFilter("advertisementId=", releaseAdvertisement.getId());
                f1.addFilter("status_in", "1,2,3,4,6,7,8,9,10,11,12,13,15,16");
                Long count = otcAppTransactionService.count(f1);
                if (count == 0L) {
                    //????????????????????????????????????5.5
                    BigDecimal price = new BigDecimal("0");
                    if (releaseAdvertisement.getTransactionMode() == 1) {
                        price = releaseAdvertisement.getTradeMoney().multiply(new BigDecimal("1").subtract(releaseAdvertisement.getPremium().divide(new BigDecimal("100")))).setScale(2, BigDecimal.ROUND_HALF_DOWN); // ????????????*(1-??????????????????)????????????
                    } else {
                        price = releaseAdvertisement.getTradeMoney().multiply(new BigDecimal("1").subtract(releaseAdvertisement.getPremium().divide(new BigDecimal("100")))).setScale(2, BigDecimal.ROUND_HALF_DOWN); // ????????????*(1-??????????????????)????????????
                    }
                    if (money.compareTo(price) >= 0) {
                        //????????????5.5?????????????????????????????????????????????????????????????????????
                        closeReleaseAdvertisement(releaseAdvertisement.getId());
                        System.out.println("????????????,??????id??????" + releaseAdvertisement.getId() + ",????????????id??????" + releaseAdvertisement.getCustomerId());
                    }
                }
            }
        }
    }

    @Override
    public OtcAppTransactionRemote getOtcTransactionByNum(String to) {
        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("transactionNum =", to);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);
        OtcAppTransactionRemote otcappTransactionRemote = ObjectUtil.bean2bean(otcAppTransaction, OtcAppTransactionRemote.class);
        return otcappTransactionRemote;
    }

    @Override
    public OtcAppTransactionRemote getOtcTransactionById(String to) {
        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("id =", to);
        OtcAppTransaction otcAppTransaction = otcAppTransactionService.get(qf);
        OtcAppTransactionRemote otcappTransactionRemote = ObjectUtil.bean2bean(otcAppTransaction, OtcAppTransactionRemote.class);
        return otcappTransactionRemote;
    }

    @Override
    public JsonResult findOtcOrderById(Long customerId, String tradeNum) {
        QueryFilter qf = new QueryFilter(OtcAppTransaction.class);
        qf.addFilter("transactionNum =", tradeNum);
        qf.addFilter("customerId =", customerId);
        OtcAppTransaction app = otcAppTransactionService.get(qf);
        if (app != null) {
            return new JsonResult().setSuccess(true).setObj(app.getStatus());
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public BigDecimal getCompletionRate(Long userId, String coinCode) {
        QueryFilter qf = new QueryFilter(OtcCompletionRate.class);
        qf.addFilter("customerId=", userId);
        qf.addFilter("coinCode=", coinCode);
        OtcCompletionRate otcCompletionRate = otcCompletionRateService.get(qf);
        if (otcCompletionRate == null) {
            return new BigDecimal(100);
        }
        return otcCompletionRate.getCompletionRate();
    }

    //????????????????????????
    public void publicMessage(OtcAppTransaction otc, Long userId, String type) {
        Long buyUserId = otc.getBuyUserId();
        Long sellUserId = otc.getSellUserId();
        OtcChatMessageRemote chat = new OtcChatMessageRemote();
        chat.setOrderId(otc.getId());
        chat.setFromID(userId);
        chat.setToID(userId.equals(buyUserId) ? sellUserId : buyUserId);
        chat.setFromName(userId.equals(buyUserId) ? otc.getBuyUserName() : otc.getSellUserName());
        chat.setToName(userId.equals(buyUserId) ? otc.getSellUserName() : otc.getBuyUserName());
        chat.setContent(type);
        chat.setType(3);
        try {
            redisService.publish(DealConstant.JedisPubSub_MESSAGE, JSON.toJSONString(chat));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
