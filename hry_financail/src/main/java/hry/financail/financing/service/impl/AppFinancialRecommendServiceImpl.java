package hry.financail.financing.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import hry.bean.FrontPage;
import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.financail.account.dao.ExDmTransactionDao;
import hry.financail.account.model.ExDmTransaction;
import hry.financail.customer.dao.AppCommendUserDao;
import hry.financail.customer.dao.AppCustomerDao;
import hry.financail.customer.model.AppCommendUser;
import hry.financail.customer.model.AppCustomer;
import hry.financail.financing.FinancialUtil;
import hry.financail.financing.dao.*;
import hry.financail.financing.model.AppFinancialRecommend;
import hry.financail.financing.model.AppFinancialRecommendTransactionConfig;
import hry.financail.financing.model.AppFinancialUser;
import hry.financail.financing.service.AppFinancialRecommendService;
import hry.front.redis.model.UserRedis;
import hry.redis.common.dao.RedisUtil;
import hry.financail.financing.model.AppFinancialRecommendTransaction;
import hry.redis.common.utils.RedisService;
import hry.remote.model.AppFinancialRecommendVo;
import hry.remote.model.RemoteResult;
import hry.trade.redis.model.Accountadd;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.FinancialRedis;
import hry.util.PageFactory;
import hry.util.QueryFilter;
import hry.util.UserRedisUtils;
import hry.util.idgenerate.IdGenerate;
import hry.util.idgenerate.NumConstant;
import hry.util.sys.ContextUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p> AppFinancialRecommendService </p>
 * @author:         jidn
 * @Date :          2019-04-16 14:03:55  
 */
@Service("appFinancialRecommendService")
public class AppFinancialRecommendServiceImpl extends BaseServiceImpl<AppFinancialRecommend, Long> implements AppFinancialRecommendService {

    @Override
    public void setDao(BaseDao<AppFinancialRecommend, Long> baseDao) {

    }

    @Resource
    private AppFinancialRecommendDao appFinancialRecommendDao;

    @Resource
    private ExDmTransactionDao exDmTransactionDao;

    @Resource
    private AppFinancialRecommendTransactionDao appFinancialRecommendTransactionDao;
    @Resource
    private AppFinancialRecommendTransactionConfigDao appFinancialRecommendTransactionConfigDao;
    @Resource
    private AppFinancialUserDao appFinancialUserDao;
    @Resource
    private AppFinancialProductsDao appFinancialProductsDao;
    @Resource
    private AppCommendUserDao appCommendUserDao;
    @Resource
    private AppCustomerDao appCustomerDao;

    @Override
    public FrontPage findFinancialRecommendList(Map<String, String> paramMap) {

        String customerId = paramMap.get("customerId");
        AppFinancialRecommend byUid = appFinancialRecommendDao.findOne2(Long.valueOf(customerId));
        Page page = PageFactory.getPage(paramMap);
        Map<String,Object> map = new HashMap<String,Object>(2);


        if(!StringUtils.isEmpty(customerId)){
            map.put("customerId", customerId);
        }

        List<hry.remote.model.AppFinancialRecommend> list = appFinancialRecommendDao.findFinancialRecommendList(map);
        for(hry.remote.model.AppFinancialRecommend afr : list){
            afr.setLevel(afr.getLevel() - byUid.getLevel());
        }
        return new FrontPage(list, list.size(), page.getPages(), page.getPageSize());
    }

    @Override
    public List<hry.remote.model.AppFinancialRecommendTransaction> findFinancialRecommendTransactionList(Map<String, String> paramMap) {
        try {
            Map<String,Object> map = new HashMap<String,Object>(2);
            String customerId = paramMap.get("customerId");

            if(!StringUtils.isEmpty(customerId)){
                map.put("customerId", customerId);
            }

            List<hry.remote.model.AppFinancialRecommendTransaction> list = appFinancialRecommendDao.findFinancialRecommendTransactionList(map);
            return list;
        } catch (Exception e){
            e.printStackTrace();
        }
       return null;
    }

    @Override
    public hry.remote.model.AppFinancialRecommend findOne(Long customerId){
        return appFinancialRecommendDao.findOne(customerId);
    }

    @Override
    public List<AppFinancialRecommendVo> findTeamFinancialList(Long customerId) {
        Map<String,Object> paramMap = new HashMap<String,Object>();
        paramMap.put("customerId",customerId);
        List<AppFinancialRecommendVo> topOneList = appFinancialRecommendDao.findTopOneList(paramMap);
        if(topOneList != null && topOneList.size() > 0){
            for(AppFinancialRecommendVo afr : topOneList){
                if(afr.getPendingMoney().compareTo(BigDecimal.ZERO) == 1){
                    String allChilds_str = appFinancialRecommendDao.findAllChilds_Str(afr.getUid());
                    List<AppFinancialRecommendVo> headIncomeList = appFinancialRecommendDao.findTeamFinancialList(allChilds_str);
                    for(AppFinancialRecommendVo afrv : headIncomeList){
                        if(!StringUtils.isEmpty(afrv.getCoinCode()) && afrv.getCoinCode().equals(afr.getCoinCode()) && !afrv.getUid().equals(afr.getUid())){
                            afr.setPendingMoney(afr.getPendingMoney().add(afrv.getCoinMoney()));
                        }
                    }
                }
            }
        }
        return topOneList;
    }


    @Override
    public void statisVipLevel() {
        System.out.println("====================????????????????????????================================");
        try {
            //????????????????????????
            int productCount = appFinancialProductsDao.findBetweenProduct();
            if(productCount > 0){
                Map<String,Object> paramMap = new HashMap<>(3);
                int list_count = appFinancialRecommendDao.findVipLevelUserCount(0);
                int page_size = FinancialUtil.PAGE_SIZE;
                if (list_count >= FinancialUtil.MAX_SIZE) {
                    int export_times = FinancialUtil.calcPage(list_count, page_size);
                    for (int m = 0; m < export_times; m++) {
                        paramMap.put("currentPage", (m*page_size));
                        paramMap.put("pageSize",  page_size);
                        List<AppFinancialRecommend> vipLevelUser = appFinancialRecommendDao.findVipLevelUser(paramMap);
                        dealWithVipLevel(vipLevelUser);
                    }
                } else {
                    List<AppFinancialRecommend> vipLevelUser = appFinancialRecommendDao.findVipLevelUser(paramMap);
                    dealWithVipLevel(vipLevelUser);
                }
                System.out.println("====================?????????????????????====================="+list_count);
            } else {
                System.out.println("========statisVipLevel=====????????????????????????");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void dealWithVipLevel(List<AppFinancialRecommend> vipLevelUser){
        try {
            if(vipLevelUser != null && vipLevelUser.size() > 0){
                for(AppFinancialRecommend r : vipLevelUser){
                    //?????? ??????vip ????????????0  ???????????????????????????????????? ?????? ????????????????????????3??? ???????????????1???
                    //?????? ????????????0  ??????????????????????????????????????????????????????  ????????????3 ???????????????
                    if(r.getVipLevel() == 0){
                        if(r.getFinancialOderNumber() >= FinancialUtil.ORDER_SIZE){
                            r.setVipLevel(r.getVipLevel() + 1);
                        }
                    } else {
                        // ????????????????????????????????? ??????????????????????????????3??? ??? ????????????
                        if(r.getDirectReferrerLevelNumber() >= FinancialUtil.ORDER_SIZE){
                            r.setVipLevel(r.getVipLevel() + 1);
                        }
                        // ????????????????????????????????? ????????????????????????????????????3???
                        // && ??????????????????????????????????????????????????????3??? ????????????vipLevel - 1
                        if(r.getFinancialOderNumber() < FinancialUtil.ORDER_SIZE && r.getDirectReferrerLevelNumber() < FinancialUtil.ORDER_SIZE){
                            if(r.getVipLevel() > 0){
                                r.setVipLevel(r.getVipLevel() - 1);
                            }
                        }
                    }
                    r.setModified(new Date());
                    appFinancialRecommendDao.updateByPrimaryKeySelective(r);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void issueFinancial() {
        System.out.println("====================????????????????????????==========================");
        Map<String,Object> paramMap = new HashMap<String,Object>();
        try {
            //????????????????????????
            int productCount = appFinancialRecommendDao.findBetweenProduct();
            if(productCount > 0){
                int list_count = appFinancialRecommendDao.findPidIsNullListCount();
                int page_size = FinancialUtil.PAGE_SIZE;
                if (list_count >= FinancialUtil.MAX_SIZE) {
                    int export_times = FinancialUtil.calcPage(list_count, page_size);
                    for (int m = 0; m < export_times; m++) {
                        paramMap.put("currentPage", (m * page_size));
                        paramMap.put("pageSize", page_size);
                        //??????????????????
                        List<AppFinancialRecommend> pidIsNullList = appFinancialRecommendDao.findPidIsNullList(paramMap);
                        seekTopOneUser(pidIsNullList,paramMap);
                    }
                } else {
                    //??????????????????
                    List<AppFinancialRecommend> pidIsNullList = appFinancialRecommendDao.findPidIsNullList(paramMap);
                    seekTopOneUser(pidIsNullList,paramMap);
                }
            } else {
                System.out.println("===========issueFinancial==========????????????????????????");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    public void seekTopOneUser(List<AppFinancialRecommend> pidIsNullList, Map<String,Object> paramMap){
        //????????????????????????id
        Set<Long> afuIdSet = new HashSet<Long>();
        try {
            if(pidIsNullList != null && pidIsNullList.size() > 0){
                System.out.println("============???????????????????????????================"+pidIsNullList.size());
                for(AppFinancialRecommend afr : pidIsNullList){
                    paramMap.put("pid",afr.getUid());
                    //paramMap.put("limitSize",getLevel(afr.getVipLevel()));
                    paramMap.put("limitSize",15);
                    //??????vip?????????????????? ??? vip1 ?????????????????????1??? ??????1???  vip2 ??????????????? ???????????????????????? ????????????
                    List<AppFinancialRecommend> oneLevelList = appFinancialRecommendDao.findOneLevelList(paramMap);
                    if(oneLevelList != null && oneLevelList.size()> 0){
                        dealWithRecommendReward(oneLevelList,afr,afuIdSet);
                    }
                }
            }

            if(null != afuIdSet && afuIdSet.size() > 0){
               for(Long afuid : afuIdSet){
                   AppFinancialUser afu = new AppFinancialUser();
                   afu.setId(afuid);
                   afu.setIfRecommend(1);
                   appFinancialUserDao.updateByPrimaryKeySelective(afu);
               }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ??????vip?????? ???????????????  ??????????????????
     * @param oneLevelList
     * @param afr
     */
    public void dealWithRecommendReward(List<AppFinancialRecommend> oneLevelList,AppFinancialRecommend afr,Set<Long> afuIdSet){
        System.out.println("===dealWithRecommendReward=="+oneLevelList.size()+"==uid"+afr.getUid()+"== vipLevel"+afr.getVipLevel() +"== rewardRatio"+afr.getRewardRatio());
        Map<String,Object> paramMap = new HashMap<String,Object>();
        boolean flag = false;
        try {
            for(AppFinancialRecommend r : oneLevelList){

                String allChildsLevel_str = "";

                if(afr.getVipLevel() == 0){
                    paramMap.put("pid",r.getUid());
                    paramMap.put("pageSize","1");
                    //?????????????????? ??? ????????????  ????????????????????????
                    allChildsLevel_str = appFinancialRecommendDao.findAllChildsLevel_Str(paramMap);
                } else {

                    paramMap.put("pid",r.getUid());
                    paramMap.put("pageSize",getLevel(r.getVipLevel()));
                    //?????????????????? ??? ????????????  ????????????????????????
                    allChildsLevel_str = appFinancialRecommendDao.findAllChildsLevel_Str(paramMap);

                    if(StringUtils.isEmpty(allChildsLevel_str)){
                        paramMap.put("pid",r.getUid());
                        paramMap.put("pageSize","1");
                        //?????????????????? ??? ????????????  ????????????????????????
                        allChildsLevel_str = appFinancialRecommendDao.findAllChildsLevel_Str(paramMap);
                    }

                }

                List<AppFinancialRecommendVo> userFinancialOrderList = appFinancialRecommendDao.findUserFinancialOrderList(allChildsLevel_str);
                int n = appFinancialUserDao.findByUid(afr.getUid());

                if(n > 0){
                    flag = true;
                }

                //?????????????????????  ??????????????????
                if(flag){

                    //???????????????????????????  ?????????????????? ?????????????????? ?????????vipLevel ??? ??????????????????????????????????????? ?????????
                    filterRecommendUser(userFinancialOrderList, afr);
                    if(userFinancialOrderList != null && userFinancialOrderList.size() > 0){
                        for(AppFinancialRecommendVo rv : userFinancialOrderList){
                            if(rv.getRealIncome() != null && rv.getRealIncome().compareTo(BigDecimal.ZERO) == 1){
                                BigDecimal money = (rv.getRealIncome().subtract(rv.getCoinMoney())).multiply(getRewardRatio(afr,rv.getUid(),rv.getLevel(),rv.getCoinCode())).divide(FinancialUtil.HUNDRED,6,BigDecimal.ROUND_HALF_DOWN);
                                updateUserMoney(afr.getUid(),rv.getCoinCode(),rv.getAfuid(),paramMap,money);
                                afuIdSet.add(rv.getAfuid());
                            }
                        }
                    }
                    System.out.println("====????????????????????????"+userFinancialOrderList.size());

                }


            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void updateUserMoney(Long pid,String coinCode,Long afuid,Map<String,Object> paramMap,BigDecimal money){
        try {
            paramMap.put("customerId",pid);
            paramMap.put("coinCode",coinCode);
            AppFinancialRecommendTransaction afrt = appFinancialRecommendTransactionDao.findAppFinancialRecommendTransactionByCustomerId(paramMap);

            //????????????????????????????????????   ???????????????????????????  ????????? ?????????
            if(afrt == null){
                afrt = new AppFinancialRecommendTransaction();
                afrt.setCustomerId(pid);
                afrt.setCoinCode(coinCode);
                afrt.setMoney(money);
                afrt.setIssuedMoney(BigDecimal.ZERO);
                afrt.setCreated(new Date());
                afrt.setModified(new Date());
                appFinancialRecommendTransactionDao.insertSelective(afrt);
            } else {
                afrt.setMoney(afrt.getMoney().add(money));
                afrt.setModified(new Date());
                appFinancialRecommendTransactionDao.updateByPrimaryKeySelective(afrt);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????  ?????????????????? vipLevel ????????????????????? ????????????
     * @return
     */
    public void filterRecommendUser(List<AppFinancialRecommendVo> userFinancialOrderList,AppFinancialRecommend afr){
        try {
            List<AppFinancialRecommendVo> list = new ArrayList<AppFinancialRecommendVo>();
            Map<String,Object> paramMap = new HashMap<String,Object>();
            //???????????????????????????  ??????????????????  ????????????????????????
            if(userFinancialOrderList != null && userFinancialOrderList.size() == 1){

            } else {
                if(userFinancialOrderList != null && userFinancialOrderList.size() > 0){
                    for(AppFinancialRecommendVo afrv : userFinancialOrderList){
                        if(afr.getVipLevel() <= afrv.getVipLevel() && !afr.getUid().equals(afrv.getUid())){
                            paramMap.put("pid",afrv.getUid());
                            paramMap.put("pageSize",getLevel(afrv.getVipLevel()));
                            //?????????????????? ??? ????????????  ????????????????????????
                            String allChildsLevel_str = appFinancialRecommendDao.findAllChildsLevel_Str(paramMap);
                            List<AppFinancialRecommendVo> subsetRecommendList = appFinancialRecommendDao.findUserFinancialOrderList(allChildsLevel_str);
                            if(afrv.getLevel() == 1){
                                subsetRecommendList.remove(0);
                            }
                            list.addAll(subsetRecommendList);
                        }
                    }
                }
                userFinancialOrderList.removeAll(list);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * ?????????????????????????????????????????????
     * @param vipLevel
     * @return
     */
    public Integer getLevel(Integer vipLevel){
        Integer level ;
        switch (vipLevel){
            case 0:
                level = 2;
                break;
            case 1:
                level = 2;
                break;
            case 2:
                level = 2;
                break;
            case 3:
                level = 3;
                break;
            case 4:
                level = 8;
                break;
            case 5:
                level = 15;
                break;
            default:
                level = 1;
                break;
        }
        return level;
    }

    /**
     * ??????????????????????????????
     * @param level
     * @return
     */

    public BigDecimal getRewardRatio(AppFinancialRecommend afr,Long oneUid, Integer level,String coinCode){
        BigDecimal ratio = BigDecimal.ZERO;
        Integer level_s = 0;
        AppFinancialRecommendTransactionConfig oneByCoinCode = appFinancialRecommendTransactionConfigDao.findOneByCoinCode(coinCode);
        if(oneByCoinCode == null){
            System.out.println("=======??????????????????????????????");
            return ratio;
        }

        if(afr.getVipLevel() > 0 && afr.getPid() != null ){
            level_s = level - afr.getLevel();
        } else if(afr.getVipLevel() == 0){
            level_s = 0;
        } else {
            level_s = level;
        }

        switch (level_s){
            case 0:
                ratio = oneByCoinCode.getVip_zero();
                break;
            case 1:
                ratio = oneByCoinCode.getVip_one();
                break;
            case 2:
                ratio = oneByCoinCode.getVip_two();
                break;
            case 3:
                ratio = oneByCoinCode.getVip_three();
                break;
            case 4:
                ratio = oneByCoinCode.getVip_four();
                break;
            case 5:
            case 6:
            case 7:
            case 8:
                ratio = oneByCoinCode.getVip_four();
                break;
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
                ratio = oneByCoinCode.getVip_five();
                break;
            default:
                ratio = oneByCoinCode.getVip_one();
                break;
        }
        System.out.println("=======?????????????????????????????????"+coinCode+"??????"+ratio+"???");
        return ratio;
    }

    /**
     * ??????????????????
     * @param customerId
     * @param coinCode
     * @param Money
     * @return
     */
    public Accountadd createAccountAdd(Long customerId, String coinCode, BigDecimal Money){
        Accountadd accountadd = new Accountadd();
        try {
            String transactionNum = IdGenerate.transactionNum(NumConstant.Ex_Dm_Transaction);

            ExDigitalmoneyAccountRedis exDigitalmoneyAccountRedis = getExDigitalmoneyAccountRedis(customerId, coinCode);

            if(exDigitalmoneyAccountRedis != null) {
                ExDmTransaction exDmTransaction = new ExDmTransaction();
                exDmTransaction.setAccountId(exDigitalmoneyAccountRedis.getId());
                exDmTransaction.setCoinCode(coinCode);
                exDmTransaction.setCreated(new Date());
                exDmTransaction.setCurrencyType(null);
                exDmTransaction.setWebsite(null);
                exDmTransaction.setCustomerId(customerId);
                exDmTransaction.setCustomerName(exDigitalmoneyAccountRedis.getUserName());
                exDmTransaction.setFee(new BigDecimal(0));
                exDmTransaction.setTransactionMoney(Money);
                exDmTransaction.setStatus(2);
                exDmTransaction.setTransactionNum(transactionNum);
                exDmTransaction.setTransactionType(1);
                exDmTransaction.setRemark("?????????????????????" + exDmTransaction.getTransactionMoney() + "??????");
                exDmTransaction.setOptType(12);
                exDmTransaction.setUserId(ContextUtil.getCurrentUserId());
                exDmTransaction.setSaasId(transactionNum);
                exDmTransactionDao.insert(exDmTransaction);

                accountadd.setMoney(Money);
                accountadd.setAccountId(exDigitalmoneyAccountRedis.getId());
                accountadd.setMonteyType(1);
                accountadd.setAcccountType(1);
                accountadd.setRemarks(31);
                accountadd.setTransactionNum(transactionNum);
                return accountadd;
            } else {
                System.out.println("====?????????????????????");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * ?????????????????????
     * @param customerId
     * @param coinCode
     * @return
     */
    public ExDigitalmoneyAccountRedis getExDigitalmoneyAccountRedis(Long customerId,String coinCode){
        RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
        UserRedis userRedis = redisUtil.get(customerId.toString());
        if (userRedis != null) {
            ExDigitalmoneyAccountRedis account = UserRedisUtils.getAccount(userRedis.getDmAccountId(coinCode).toString(), coinCode);
            if(account != null){
                return account;
            }
        }
        return null;
    }

    @Override
    public void generateFinancialRecommendationRelationship(){
       try {
           Map<String,Object> paramMap = new HashMap<>();
           int list_count = appCommendUserDao.generateFinancialRecommendationRelationshipCount();
           int page_size = FinancialUtil.PAGE_SIZE;
           if (list_count >= FinancialUtil.MAX_SIZE) {
               int export_times = FinancialUtil.calcPage(list_count, page_size);
               for (int m = 0; m < export_times; m++) {
                   paramMap.put("currentPage", (m * page_size));
                   paramMap.put("pageSize", page_size);
                   List<AppCommendUser> pidIsNullList = appCommendUserDao.generateFinancialRecommendationRelationship(paramMap);
                   dealWithAppCommendToFinancial(pidIsNullList);
               }
           } else {
               List<AppCommendUser> pidIsNullList = appCommendUserDao.generateFinancialRecommendationRelationship(paramMap);
               dealWithAppCommendToFinancial(pidIsNullList);
           }
       } catch (Exception e){
           e.printStackTrace();
       }
    }

    public void dealWithAppCommendToFinancial(List<AppCommendUser> list){
        try {
            if(list != null && list.size() > 0){
                for(AppCommendUser acu : list){
                    AppFinancialRecommend afr = new AppFinancialRecommend();
                    afr.setCreated(new Date());
                    afr.setModified(new Date());
                    afr.setVipLevel(0);
                    if(!StringUtils.isEmpty(acu.getPid()) ){
                        if(!acu.getSid().contains(",")){
                            afr.setUid(acu.getUid());
                            afr.setPid(acu.getPid());
                            afr.setLevel(1);
                            appFinancialRecommendDao.insert(afr);
                        } else {
                            String[] split = acu.getSid().split(",");
                            String sid = split[split.length - 1].split("-")[0];
                            afr.setPid(acu.getPid());
                            afr.setUid(acu.getUid());
                            afr.setLevel(Integer.valueOf(sid));
                            appFinancialRecommendDao.insert(afr);
                        }
                    } else {
                        afr.setPid(null);
                        afr.setUid(acu.getUid());
                        afr.setLevel(0);
                        appFinancialRecommendDao.insert(afr);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public RemoteResult addFinancialRecommendShip(Long customerId, String code){
        RemoteResult remoteResult = new RemoteResult();
        try {
            AppCustomer customer = appCustomerDao.findAppCustomerByReferralCode(code);
            if(customer == null){
                remoteResult.setMsg("user_isNotExist");
                return remoteResult;
            }
            if(customer.getId().equals(customerId)){
                remoteResult.setMsg("not_use_own_code");
                return remoteResult;
            }
            int number = appFinancialRecommendDao.findUserSuperiorNumber(customer.getId());

            String childs_str = appFinancialRecommendDao.findAllChilds_Str(customerId);
            List<AppFinancialRecommend> childs = appFinancialRecommendDao.findAllChilds(childs_str);

            AppFinancialRecommend one2 = appFinancialRecommendDao.findOne2(customerId);

            if(childs != null && childs.size() > 0){

                //??????????????????????????????????????????????????????
                boolean flag = false;
                for(AppFinancialRecommend af : childs){
                    if(af.getUid().equals(customer.getId())){
                        flag = true;
                        break;
                    }
                }

                if(flag){
                    remoteResult.setMsg("not_use_subordinate_code");
                    return remoteResult;
                }

                for(AppFinancialRecommend af : childs){

                    switch (af.getLevel()){
                        case 0:
                            af.setLevel(number+1);
                            break;
                        default:
                            af.setLevel(af.getLevel() + number + 1 );
                            break;
                    }

                    appFinancialRecommendDao.updateByPrimaryKeySelective(af);
                }
            }
            one2.setLevel(number+1);
            one2.setPid(customer.getId());
            appFinancialRecommendDao.updateByPrimaryKeySelective(one2);

            updateAppCommendUser(customerId,code);

            remoteResult.setMsg("success");
            remoteResult.setSuccess(true);
        } catch (Exception e){
            e.printStackTrace();
            remoteResult.setMsg("error");
        }
        return remoteResult;
    }


    StringBuffer s = new StringBuffer(); //?????????
    int level; //??????
    /**
     * ???????????????????????????
     * @param customerId
     * @param commendCode
     */
    public void updateAppCommendUser(Long customerId,String commendCode){
        try {

            AppCommendUser appCommendUser = appCommendUserDao.findByUid(customerId);

            AppCustomer AppCustomer =null;
            if(!StringUtils.isEmpty(commendCode)){//??????????????????
                AppCustomer = appCustomerDao.findAppCustomerByReferralCode(commendCode);
                if(AppCustomer!=null){
                    AppCustomer.setOneNumber(AppCustomer.getOneNumber()+1);
                }
                addSid(AppCustomer);
            }

            if(appCommendUser!=null){
                appCommendUser.setSid(s.toString().substring(0, s.length()-1));
                String str = s.toString().substring(0, s.length()-1);
                appCommendUser.setSid(","+str+",");
                int one = str.lastIndexOf(",");
                String maxid = str.substring((one+1),str.length());
                appCommendUser.setMaxId(Long.valueOf(maxid));
                appCommendUser.setModified(new Date());
                appCommendUserDao.updateByPrimaryKeySelective(appCommendUser);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public String addSid(AppCustomer customer){
        AppCustomer appCustomer = null;
        if(customer!=null){
            s.append( level+"-"+customer.getId()+",");
        }else{
            return null;
        }
        if(!StringUtils.isEmpty(customer.getReferralCode())){
            appCustomer = appCustomerDao.findAppCustomerByReferralCode(customer.getReferralCode());
            level++;
        }else{
            return null;
        }
        return  addSid(appCustomer);
    }

    @Override
    public void renewFinancialRecommend(){
        try (Jedis jedis = FinancialRedis.JEDIS_POOL.getResource()){
            Map<String,Object> paramMap = new HashMap<>(2);
            int list_count = appFinancialRecommendDao.findPidIsNullListCountRenew();
            int page_size = FinancialUtil.PAGE_SIZE;
            jedis.set("jidening","0");
            if (list_count >= FinancialUtil.MAX_SIZE) {
                int export_times = FinancialUtil.calcPage(list_count, page_size);
                for (int m = 0; m < export_times; m++) {
                    paramMap.put("currentPage", (m * page_size));
                    paramMap.put("pageSize", page_size);
                    List<AppFinancialRecommend> pidIsNullList = appFinancialRecommendDao.findPidIsNullListRenew(paramMap);
                    calcLevel(pidIsNullList,jedis);
                }
            } else {
                List<AppFinancialRecommend> pidIsNullList = appFinancialRecommendDao.findPidIsNullListRenew(paramMap);

                calcLevel(pidIsNullList,jedis);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void calcLevel(List<AppFinancialRecommend> pidIsNullList,Jedis jedis){
        Map<String,Object> paramMap = new HashMap<String,Object>();
        List<Long> list = new ArrayList<Long>();
        if( null != pidIsNullList && pidIsNullList.size() > 0){
            for(AppFinancialRecommend afr : pidIsNullList){
                list.add(afr.getUid());
            }
            paramMap.put("list",list);
            List<AppFinancialRecommend> oneLevelList = appFinancialRecommendDao.findOneLevelList(paramMap);
            if(null != oneLevelList && oneLevelList.size() > 0){
                String n = jedis.get("jidening");
                for(AppFinancialRecommend a : oneLevelList){
                    System.out.println("==a=="+a.getUid());
                    a.setLevel(Integer.valueOf(n) + 1);
                    appFinancialRecommendDao.updateByPrimaryKeySelective(a);
                }
                jedis.set("jidening",String.valueOf(Integer.valueOf(n)+1));
                calcLevel(oneLevelList,jedis);
            }
        }
    }


    @Override
    public int findShipCount(Long customerId){
        String childAll = appFinancialRecommendDao.findAllChilds_Str(customerId);
        return appFinancialRecommendDao.findShipCount(childAll);
    }



}
