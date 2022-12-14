/**
 * Copyright:
 *
 * @author: javalx
 * @version: V1.0
 * @Date: 2019-06-11 18:29:25
 */
package hry.social.manage.remote.mill;

import com.alibaba.fastjson.JSON;
import hry.core.shiro.PasswordHelper;
import hry.front.redis.model.UserRedis;
import hry.manage.remote.RemoteManageService;
import hry.manage.remote.model.RemoteResult;
import hry.manage.remote.model.User;
import hry.redis.common.dao.RedisUtil;
import hry.redis.common.utils.RedisService;
import hry.social.SocialPayConf.service.SocialPayConfService;
import hry.social.force.dao.SocialTerminableForceDao;
import hry.social.manage.remote.api.mill.RemoteMillService;
import hry.social.manage.remote.api.task.RemoteTaskService;
import hry.social.manage.remote.model.SocialPayConf.SocialPayConf;
import hry.social.manage.remote.model.force.SocialTerminableForce;
import hry.social.manage.remote.model.mill.SocialMillInfo;
import hry.social.manage.remote.model.mill.SocialMillTradeRecord;
import hry.social.manage.remote.model.traderecord.SocialCoinTradeRecord;
import hry.social.mill.dao.SocialMillTradeRecordDao;
import hry.social.mill.service.SocialMillInfoService;
import hry.social.mq.producer.service.MessageProducer;
import hry.social.mq.producer.service.NoticeMessage;
import hry.social.traderecord.service.SocialCoinTradeRecordService;
import hry.social.transaction.model.ExDmTransaction;
import hry.social.transaction.service.ExDmTransactionService;
import hry.trade.redis.model.Accountadd;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.NoticeTemplateUtil;
import hry.util.QueryFilter;
import hry.util.UserRedisUtils;
import hry.util.date.DateUtil;
import hry.trade.model.AccountRemarkEnum;
import hry.util.idgenerate.IdGenerate;
import hry.util.idgenerate.NumConstant;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;

import javax.annotation.Resource;
import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p> RemoteMillServiceImpl </p>
 *
 * @author: javalx
 * @Date :          2019-06-11 18:29:25
 */
public class RemoteMillServiceImpl implements RemoteMillService {

    @Resource
    SocialMillInfoService socialMillInfoService;
    @Resource
    RemoteManageService remoteManageService;
    @Resource
    RemoteTaskService remoteTaskService;
    @Resource
    private MessageProducer messageProducer;
    @Resource
    SocialCoinTradeRecordService socialCoinTradeRecordService;
    @Resource
    SocialPayConfService socialPayConfService;
    @Resource
    SocialMillTradeRecordDao socialMillTradeRecordDao;
    @Resource
    SocialTerminableForceDao socialTerminableForceDao;
    @Resource
    private ExDmTransactionService exDmTransactionService;
    @Resource
    private RedisService redisService;

    /**
     * ??????????????????????????????
     *
     * @param customerId
     * @return
     */
    @Override
    public RemoteResult getMillPay(Long customerId) {
        RemoteResult remoteResult = new RemoteResult();
        QueryFilter qf = new QueryFilter(SocialPayConf.class);
        qf.addFilter("type=", 1);
        List<SocialPayConf> socialPayConfs = socialPayConfService.find(qf);
        for (SocialPayConf spc : socialPayConfs) {
            String coinCode = spc.getCoinCode();
            // ???????????????????????????????????????
            RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
            UserRedis userRedis = redisUtil.get(customerId.toString());
            ExDigitalmoneyAccountRedis digAccount = UserRedisUtils.getAccount(userRedis.getDmAccountId(coinCode).toString(), coinCode);
            spc.setHotMoney(digAccount.getHotMoney());
        }
        remoteResult.setSuccess(true);
        remoteResult.setObj(socialPayConfs);
        return remoteResult;
    }

    /**
     * ??????????????????????????????
     *
     * @param customerId
     * @return
     */
    @Override
    public RemoteResult getUserMillInfo(Long customerId) {
        return socialMillInfoService.getMillInfo(customerId);
    }

    /**
     * ??????????????????????????????
     *
     * @param customerId
     * @return
     */
    @Override
    public RemoteResult getUerMillList(Long customerId) {
        RemoteResult remoteResult = new RemoteResult();
        //???????????? 1??????????????????2???????????????
        List<SocialMillTradeRecord> forceMillList = socialMillTradeRecordDao.getUerMillList(customerId, 1);
        List<SocialMillTradeRecord> coinMillList = socialMillTradeRecordDao.getUerMillList(customerId, 2);
        Map<String,List<SocialMillTradeRecord>> map = new HashMap<>();
        forceMillList = forceMillList == null ? new ArrayList<>() : forceMillList;
        coinMillList = coinMillList == null ? new ArrayList<>() : coinMillList;
        map.put("forceMillList", forceMillList);
        map.put("coinMillList", coinMillList);
        remoteResult.setSuccess(true);
        remoteResult.setObj(map);
        return remoteResult;
    }

    /**
     * ??????????????????????????????
     *
     * @param rewardType
     * @return
     */
    @Override
    public RemoteResult getMillInfo(Integer rewardType) {
        RemoteResult remoteResult = new RemoteResult();
        QueryFilter qf = new QueryFilter(SocialMillInfo.class);
        qf.addFilter("rewardType=", rewardType);
        qf.addFilter("states=", 1);
        List<SocialMillInfo> socialMillInfos = socialMillInfoService.find(qf);
        remoteResult.setSuccess(true);
        remoteResult.setObj(socialMillInfos);
        return remoteResult;
    }

    /**
     * ??????????????????????????????????????????
     *
     * @param customerId
     * @return
     */
    @Override
    public BigDecimal findMillCoin(Long customerId) {
        return socialMillInfoService.findMillCoin(customerId);
    }

    /**
     * ????????????
     *
     * @param millId     ??????ID
     * @param num        ????????????
     * @param cnyAmount  ???????????????CNY???
     * @param coinCode   ????????????
     * @param payNum     ?????????
     * @param coinMarket ???????????????
     * @param payAmount  ?????????????????????CNY???
     * @param accountPwd ????????????
     * @param customerId ??????ID
     * @return
     */
    @Override
    public RemoteResult millSave(Long millId, BigDecimal num, BigDecimal cnyAmount, String coinCode, BigDecimal payNum, BigDecimal coinMarket, BigDecimal payAmount, String accountPwd, Long customerId) {
        RemoteResult remoteResult = new RemoteResult();
        SocialMillInfo socialMillInfo = socialMillInfoService.get(millId);
        BigDecimal price = socialMillInfo.getPrice();

        if (num.compareTo(BigDecimal.ZERO) <= 0) {
            remoteResult.setMsg("trade_num_lt0_error");
            remoteResult.setSuccess(false);
            return remoteResult;
        }
        if (payNum.compareTo(BigDecimal.ZERO) <= 0) {
            remoteResult.setMsg("pay_num_lt0_error");
            remoteResult.setSuccess(false);
            return remoteResult;
        }
        if (coinMarket.compareTo(BigDecimal.ZERO) <= 0) {
            remoteResult.setMsg("req_error");
            remoteResult.setSuccess(false);
            return remoteResult;
        }
        User user = remoteManageService.getUserByCustomerId(customerId);
        PasswordHelper passwordHelper = new PasswordHelper();
        String apw = passwordHelper.encryString(accountPwd, user.getSalt());
        if (!apw.equals(user.getAccountPassWord())) {
            return new RemoteResult().setMsg("acpwd_error");
        }

        // ???????????????????????????????????????
        RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
        UserRedis userRedis = redisUtil.get(customerId.toString());
        if (userRedis == null) {
            // ?????????????????????
            remoteResult.setMsg("xnzh_no_exist");
            return remoteResult;
        }
        String personId = customerId.toString();
        String lockKey = "LOCK_ACCOUNT:" + personId + ":" + coinCode;
        boolean lock = redisService.lock(lockKey);
        if (lock) {
            ExDigitalmoneyAccountRedis digAccount = UserRedisUtils.getAccount(userRedis.getDmAccountId(coinCode).toString(), coinCode);
            if (digAccount == null) {
                // ?????????????????????
                //?????????
                redisService.unLock(lockKey);
                remoteResult.setMsg("xnzh_no_exist");
                return remoteResult;
            }
            if (digAccount.getHotMoney().compareTo(BigDecimal.ZERO) <= 0) {
                //?????????
                redisService.unLock(lockKey);
                remoteResult.setMsg("xnzh_insufficient");
                return remoteResult;
            }
            if (digAccount.getColdMoney().compareTo(BigDecimal.ZERO) < 0) {
                //?????????
                redisService.unLock(lockKey);
                remoteResult.setMsg("xnzh_abnormal_error");
                return remoteResult;
            }
            if (digAccount.getHotMoney().compareTo(payNum) < 0) {
                //?????????
                redisService.unLock(lockKey);
                remoteResult.setMsg("xnzh_insufficient");
                return remoteResult;
            }
            //????????????????????????
            SocialMillTradeRecord smtr = new SocialMillTradeRecord();
            Integer timeNum = socialMillInfo.getValidNum();
            Integer timeUnit = socialMillInfo.getTimeUnit();
            Integer rewardType = socialMillInfo.getRewardType();
            BigDecimal rewardNum = socialMillInfo.getRewardNum();
            String serialNum = socialMillInfo.getSerialNum();
            Date date = timeDeal(timeNum, timeUnit);
            smtr.setMillId(millId);
            smtr.setCustomerId(customerId);
            smtr.setCnyPrice(price);
            smtr.setNum(num);
            smtr.setCnyAmount(cnyAmount);
            smtr.setCoinCode(socialMillInfo.getCoinCode());
            smtr.setPayCoin(coinCode);
            smtr.setPayNum(payNum);
            smtr.setCoinMarket(coinMarket.toPlainString());
            smtr.setPayAmount(payAmount);
            smtr.setRewardNum(rewardNum);
            smtr.setRewardType(rewardType);
            smtr.setSerialNum(serialNum);
            smtr.setEndTime(date);
            smtr.setOrderNum(IdGenerate.transactionNum("AR"));
            socialMillTradeRecordDao.insertSelective(smtr);
            // ??????????????????????????????????????????????????????
            if (rewardType == 1) { //???????????? 1??????????????????2???????????????
                SocialTerminableForce stf = new SocialTerminableForce();
                stf.setEndTime(date);
                stf.setCustomerId(customerId);
                stf.setType(2);
                stf.setTerminable(num.multiply(rewardNum));
                stf.setRemark(serialNum + "??????" + num + "???");
                socialTerminableForceDao.insertSelective(stf);
                //????????????
                remoteTaskService.calculateForce(customerId);
            }
            //??????????????????????????????
            SocialCoinTradeRecord fsctr = new SocialCoinTradeRecord();
            //???????????????0????????? ??? 1????????????
            fsctr.setCategory(1);
            //?????????0????????????1????????????2????????????3????????????
            //?????????0?????????(???)???1?????????(???)???2?????????(???)???3??????????????????(?????????)???4??????(?????????)??? 5??????(?????????)???6??????(???)???7??????(???)???8????????????(???)???9????????????(???)???
            fsctr.setType(8);
            //??????
            fsctr.setCoinCode(coinCode);
            fsctr.setStates(2);
            fsctr.setSource("mill");
            fsctr.setNumber(payNum);
            fsctr.setCustomerId(customerId);
            //????????????(??????type???sourceNum?????????????????????????????????)
            fsctr.setSourceNum(smtr.getId().toString());
            socialCoinTradeRecordService.save(fsctr);
            // ???????????????
            ExDmTransaction exDmTransaction = new ExDmTransaction();
            exDmTransaction.setCustomerId(customerId);
            String transactionNum = NumConstant.Ex_Dm_Transaction;
            exDmTransaction.setTransactionNum(IdGenerate.transactionNum(transactionNum));
            exDmTransaction.setAccountId(digAccount.getId());
            exDmTransaction.setTransactionType(2);  // ????????????(1????????? ???2?????????)
            exDmTransaction.setTransactionMoney(payNum);
            exDmTransaction.setCustomerName(user.getNickName());
            exDmTransaction.setStatus(2);   // ?????? 1????????? --2????????? -- 3?????????
            exDmTransaction.setCoinCode(coinCode);
            exDmTransaction.setCurrencyType(user.getCommonLanguage());
            exDmTransaction.setFee(BigDecimal.ZERO);
            exDmTransaction.setOrderNo(fsctr.getId().toString());
            exDmTransaction.setOptType(AccountRemarkEnum.TYPE508.getIndex());//TYPE500("????????????", 500), TYPE503("???????????????", 503), TYPE505("???????????????", 505), TYPE506("????????????", 506), TYPE507("?????????????????????", 507), TYPE508("????????????", 508), TYPE509("????????????", 509);
            // ????????????
            exDmTransactionService.save(exDmTransaction);
            // ????????????mq????????????
            Accountadd accountAdd = new Accountadd();
            accountAdd.setAccountId(digAccount.getId());
            accountAdd.setMoney(payNum.multiply(new BigDecimal(-1)));
            accountAdd.setMonteyType(1);
            accountAdd.setAcccountType(1);
            accountAdd.setRemarks(26);
            accountAdd.setTransactionNum(exDmTransaction.getTransactionNum());
            List<Accountadd> list = new ArrayList<Accountadd>();
            list.add(accountAdd);
            messageProducer.toAccount(JSON.toJSONString(list));
            //???????????????
            Map<String,String> fromParamers = new HashMap<>();
            fromParamers.put(NoticeTemplateUtil.HRY_TIME, DateUtil.dateToString(new Date(), "yyyy-MM-dd HH:mm:ss"));
            fromParamers.put(NoticeTemplateUtil.HRY_USER, user.getNickName());
            fromParamers.put(NoticeTemplateUtil.HRY_COIN_OUT, payNum + coinCode);
            NoticeMessage noticeMessage = new NoticeMessage(user,NoticeTemplateUtil.MILL_TRADE_SUCCESS, fromParamers);
            messageProducer.toSendMsg(JSON.toJSONString(noticeMessage));
            //?????????
            redisService.unLock(lockKey);
            remoteResult.setSuccess(true);
            return remoteResult;
        }else {
            remoteResult.setMsg("sys_busy_error");
            return remoteResult;
        }

    }

    /**
     * ??????????????????
     *
     * @param aging ?????????
     * @param unit  ???????????????0?????????1?????????2?????????3????????? ???????????? 1?????????2?????????3???
     * @return
     */
    Date timeDeal(int aging, int unit) {
        int calendarUN = 0;
        switch (unit) {
            case 3:
                calendarUN = Calendar.YEAR;
                break;
            case 2:
                calendarUN = Calendar.MONTH;
                break;
            case 4:
                calendarUN = Calendar.DATE;
                aging = aging * 7;
                break;
            case 1:
                calendarUN = Calendar.DATE;
                break;
            default:
                break;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());    //???????????????
        cal.add(calendarUN, aging);
        Date time = cal.getTime();
        System.out.println("??????::" + time);
        return time;
    }

}
