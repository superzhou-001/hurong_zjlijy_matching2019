package hry.cm.remote;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import hry.util.decimal.DecimalUtil;
import hry.cmson.account.model.CmSonAccount;
import hry.cmson.account.model.CmSonAccountRecord;
import hry.cmson.account.service.CmSonAccountRecordService;
import hry.cmson.account.service.CmSonAccountService;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;

import hry.bean.ApiJsonResult;
import hry.bean.FrontPage;
import hry.bean.JsonResult;
import hry.bean.ObjectUtil;
import hry.cm.account.model.CmAccount;
import hry.cm.account.model.CmAccountRecord;
import hry.cm.account.service.CmAccountRecordService;
import hry.cm.account.service.CmAccountService;
import hry.cm.account.service.ContractMainAccountRecordService;
import hry.cm.constant.ExchangeDataCacheRedis;
import hry.cm.deal.CmCustomerUtil;
import hry.cm.deal.CmDealUtil;
import hry.cm.dto.CmAccountRedis;
import hry.cm.remote.model.CmAccountRecordRemote;
import hry.cm.util.AccountDeEnUtil;
import hry.cm.util.CmRedis;
import hry.core.constant.CodeConstant;
import hry.front.redis.model.UserRedis;
import hry.manage.remote.model.FeixiaohaoPriceVo;
import hry.redis.common.dao.RedisUtil;
import hry.redis.common.utils.RedisService;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.PageFactory;
import hry.util.QueryFilter;
import hry.util.SpringUtil;
import hry.utils.BaseConfUtil;
import hry.utils.CmConfigUtil;
import hry.utils.UserRedisUtils;
import redis.clients.jedis.Jedis;

public class RemoteCmAccountServiceImpl implements RemoteCmAccountService {
	
	@Resource
    public CmAccountService cmAccountService;

	@Resource
    public CmSonAccountService cmSonAccountService;

    @Resource
    public CmAccountRecordService cmAccountRecordService;
    @Resource
    public CmSonAccountRecordService cmSonAccountRecordService;

    @Resource
    public ContractMainAccountRecordService contractMainAccountRecordService;


    @Override
    public synchronized String[] accountSubTomain(Long customerId, Long mainAccountId, String coinCode, BigDecimal count) {
        RedisService redisService = SpringUtil.getBean("redisService");
        //??????????????????
        Integer retentionNumber = Integer.valueOf(redisService.get("retentionNumber:"+ coinCode));
        String[] rt = new String[2];
        CmAccountRedis tcbyAccountRedis = CmDealUtil.getCmAccount( customerId,  coinCode);
        Long binaryAccountId = tcbyAccountRedis.getId();
        //???????????????
        BigDecimal maxWithdrawal = CmCustomerUtil.getMaxWithdrawal(customerId,coinCode);
        if (count.compareTo(new BigDecimal("0")) > 0 && maxWithdrawal.compareTo(count) >= 0) {
            contractMainAccountRecordService.contractTomain(customerId, mainAccountId, binaryAccountId, coinCode, count);
            rt[0] = CodeConstant.CODE_SUCCESS;
            rt[1] = "????????????";
            return rt;
        } else {
            rt[0] = CodeConstant.CODE_FAILED;
            rt[1] = "????????????,????????????"+ DecimalUtil.getPrettyNumber(maxWithdrawal.setScale(retentionNumber,BigDecimal.ROUND_DOWN).toString());
            return rt;
        }
    }

    //???????????????????????????
    public BigDecimal getPrettyNumber(String dec){
        BigDecimal decimal = new BigDecimal(BigDecimal.valueOf(Double.parseDouble(dec)).stripTrailingZeros().toPlainString());
        return decimal;
    }

    @Override
    public String[] accountMainToSub(Long customerId, Long mainAccountId, String coinCode, BigDecimal count) {
        //???????????????front?????????????????????????????????????????????
        String[] rt = new String[2];
        CmAccountRedis contractAccountRedis = CmDealUtil.getCmAccount( customerId,  coinCode);
        Long contractAccountId = contractAccountRedis.getId();
        contractMainAccountRecordService.mainTocontract(customerId, mainAccountId, contractAccountId, coinCode, count);
        rt[0] = CodeConstant.CODE_SUCCESS;
        rt[1] = "????????????";
        return rt;
    }
    
    @Override
    public synchronized String[] beforaccount(Long customerId, String coinCode) {
        //???????????????front?????????????????????????????????????????????
        String[] rt = new String[2];
        CmAccountRedis contractAccountRedis = CmDealUtil.getCmAccount( customerId,  coinCode);

        rt[0] = CodeConstant.CODE_SUCCESS;
        return rt;
    }

    @Override
    public Boolean initAccount(Long customerId) {
        //?????????????????????????????????redis??????redis?????????????????????????????????????????????????????????????????????
        // ??????????????????????????????????????????
        QueryFilter accountQf = new QueryFilter(CmAccount.class);
        accountQf.addFilter("customerId=", customerId);
        List<CmAccount> accountList = cmAccountService.find(accountQf);
        if (accountList != null && accountList.size() > 0) {
            // ????????????????????????redis????????????????????????????????????????????????
            try (Jedis jedis = CmRedis.JEDIS_POOL.getResource()){
                for (CmAccount account : accountList) {
                    String hget1 = jedis.hget(ExchangeDataCacheRedis.getAccountKey(account.getCoinCode()), account.getCustomerId().toString());
                    if (StringUtils.isEmpty(hget1)) {
                        CmAccountRedis coinaccount = new CmAccountRedis();
                        coinaccount.setCoinCode(account.getCoinCode());
                        coinaccount.setColdMoney(account.getColdMoney());
                        coinaccount.setCustomerId(account.getCustomerId());
                        coinaccount.setHotMoney(account.getHotMoney());
                        coinaccount.setId(account.getId());
                        jedis.hset(ExchangeDataCacheRedis.getAccountKey(account.getCoinCode()), account.getCustomerId().toString(), AccountDeEnUtil.encode(JSON.toJSONString(coinaccount)));
                    }
                }
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }
    
	@Override
	public JsonResult myAccount(Map<String, String> params) {
		// TODO Auto-generated method stub
		JsonResult jsonResult = new JsonResult();

		Long customerId = Long.valueOf(params.get("customerId"));
		/** ???????????????Code */
		String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");

		/** ????????????????????????????????? */
		CmAccountRedis CmAccountRedis = CmDealUtil.getCmAccount(customerId, platCoin);
		
		return jsonResult.setSuccess(true).setObj(CmAccountRedis);
	}

	@Override
	public FrontPage findMyAccountRecordList(Map<String, String> params) {
		// TODO Auto-generated method stub
		Long customerId = Long.valueOf(params.get("customerId"));
		String remarkkey = params.get("remarkkey");//????????????
		String dateStart = params.get("dateStart");
		String dateEnd = params.get("dateEnd");
		String coinCode = params.get("coinCode");
		
		Page page = PageFactory.getPage(params);
		QueryFilter filterCmAccountRecord = new QueryFilter(CmAccountRecord.class);
		filterCmAccountRecord.addFilter("customerId=", customerId);
		if (!StringUtils.isEmpty(remarkkey)) {
			filterCmAccountRecord.addFilter("remarkkey=", Integer.valueOf(remarkkey));
		}
		if (!StringUtils.isEmpty(coinCode)) {
			filterCmAccountRecord.addFilter("coinCode=", coinCode);
		}
		if (!StringUtils.isEmpty(dateStart)) {
			filterCmAccountRecord.addFilter("created>=", dateStart);
		}
		if (!StringUtils.isEmpty(dateEnd)) {
			filterCmAccountRecord.addFilter("created<=", dateEnd);
		}
		filterCmAccountRecord.setOrderby("created desc");
		List<CmAccountRecord> list = cmAccountRecordService.find(filterCmAccountRecord);
		List<CmAccountRecordRemote> beanList = ObjectUtil.beanList(list, CmAccountRecordRemote.class);
		return new FrontPage(beanList, page.getTotal(), page.getPages(), page.getPageSize());
	}

	@Override
	public JsonResult mainAccount(Map<String, String> params) {
		// TODO Auto-generated method stub
		JsonResult jsonResult = new JsonResult();

		Long customerId = Long.valueOf(params.get("customerId"));
		/** ???????????????Code */
		String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");

		/** ???????????????????????????????????? */
		RedisUtil<UserRedis> redisUtil = new RedisUtil<UserRedis>(UserRedis.class);
		UserRedis userRedis = redisUtil.get(customerId.toString());
		// ???????????????
		ExDigitalmoneyAccountRedis exaccount = UserRedisUtils.getAccount(userRedis.getDmAccountId(platCoin).toString(),
				platCoin);
		
		return jsonResult.setSuccess(true).setObj(exaccount);
	}

	@Override
	public List<CmAccountRedis> getAccoutList(Long customerId) {
		// TODO Auto-generated method stub
		try (Jedis jedis = CmRedis.JEDIS_POOL.getResource()) {
            Set<String> coinKeys = CmConfigUtil.getAllConskeysList();
            QueryFilter qf = new QueryFilter(CmAccount.class);
            qf.addFilter("customerId=", customerId);
            List<CmAccount> list = cmAccountService.find(qf);
            List<CmAccountRedis> listaccount = new ArrayList<CmAccountRedis>();
            for (CmAccount c : list) {
                coinKeys.remove(c.getCoinCode());
                CmAccountRedis account = new CmAccountRedis();
                account.setId(c.getId());
                account.setCoinCode(c.getCoinCode());
                account.setCustomerId(customerId);
                account.setHotMoney(c.getHotMoney());
                account.setColdMoney(c.getColdMoney());

                //??????????????????BTC_CNY,???????????????
                String price = jedis.hget("hry:coinPrice", c.getCoinCode());
                if (StringUtil.isNotEmpty(price)) {
                    account.setValuation((account.getHotMoney().add(account.getColdMoney())).multiply(new BigDecimal(price)));
                }
                listaccount.add(account);
            }
            //???????????????????????????????????????????????????
            Iterator<String> coinKeysIter = coinKeys.iterator();
            while (coinKeysIter.hasNext()) {
                String coinCode = coinKeysIter.next();
                CmAccountRedis account = new CmAccountRedis();
                account.setCoinCode(coinCode);
                account.setCustomerId(customerId);
                account.setHotMoney(BigDecimal.ZERO);
                account.setColdMoney(BigDecimal.ZERO);
                listaccount.add(account);
            }
            return listaccount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //???????????????????????????????????????
        return null;
	}
	
    @Override
    public BigDecimal getHotMoneyByCoin (String coinCode, Long customerId) {
        CmAccountRedis cmAccountRedis = CmDealUtil.getCmAccount( customerId,  coinCode);
        if(cmAccountRedis !=null){
            return cmAccountRedis.getHotMoney();
        }
        return BigDecimal.ZERO;
    }

	@Override
	public FrontPage getAccountRecord(Map<String, String> params) {
		// TODO Auto-generated method stub
		return cmAccountRecordService.getAccountRecord(params);
	}

	@Override
	public ApiJsonResult getCoinRate(String coinCode) {
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
            RedisService redisService = SpringUtil.getBean("redisService");
            String result = redisService.get("cn:newFeixiaohaoPrice");
            if (hry.util.StringUtil.isNull(result)) {
                List<FeixiaohaoPriceVo> list = JSON.parseArray(result, FeixiaohaoPriceVo.class);
                for (FeixiaohaoPriceVo feixiaohaoPriceVo : list) {
                    String name = feixiaohaoPriceVo.getName();
                    // ???????????????????????????????????????Price??????????????????????????????
                    if (name.equals(coinCode) && feixiaohaoPriceVo.getPrice() != null) {
                        codePrice = new BigDecimal(feixiaohaoPriceVo.getPrice());
                        return new ApiJsonResult().setSuccess(true).setObj(codePrice);
                    }
                }
            } else {
                System.out.println("????????????redis???" + coinCode + "????????????");
                return new ApiJsonResult().setSuccess(true).setObj(codePrice);
            }

        }
        return new ApiJsonResult().setSuccess(true).setObj(codePrice);
	}

    @Override
    public JsonResult myCmSonAccount(Map<String, String> params) {
        // TODO Auto-generated method stub
        JsonResult jsonResult = new JsonResult();

        Long customerId = Long.valueOf(params.get("customerId"));
        /** ???????????????Code */
        String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");

        /** ????????????????????????????????? */
        hry.cmson.dto.CmAccountRedis CmAccountRedis = hry.cmson.deal.CmDealUtil.getCmAccount(customerId, platCoin);

        return jsonResult.setSuccess(true).setObj(CmAccountRedis);
    }

    @Override
    public FrontPage findMyCmSonAccountRecordList(Map<String, String> params) {
        // TODO Auto-generated method stub
        Long customerId = Long.valueOf(params.get("customerId"));
        String remarkkey = params.get("remarkkey");//????????????
        String dateStart = params.get("dateStart");
        String dateEnd = params.get("dateEnd");
        String coinCode = params.get("coinCode");

        Page page = PageFactory.getPage(params);
        QueryFilter filterCmAccountRecord = new QueryFilter(CmSonAccountRecord.class);
        filterCmAccountRecord.addFilter("customerId=", customerId);
        if (!StringUtils.isEmpty(remarkkey)) {
            filterCmAccountRecord.addFilter("remarkkey=", Integer.valueOf(remarkkey));
        }
        if (!StringUtils.isEmpty(coinCode)) {
            filterCmAccountRecord.addFilter("coinCode=", coinCode);
        }
        if (!StringUtils.isEmpty(dateStart)) {
            filterCmAccountRecord.addFilter("created>=", dateStart);
        }
        if (!StringUtils.isEmpty(dateEnd)) {
            filterCmAccountRecord.addFilter("created<=", dateEnd);
        }
        filterCmAccountRecord.setOrderby("created desc");
        List<CmSonAccountRecord> list = cmSonAccountRecordService.find(filterCmAccountRecord);
        List<CmAccountRecordRemote> beanList = ObjectUtil.beanList(list, CmAccountRecordRemote.class);
        return new FrontPage(beanList, page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
    public List<hry.cmson.dto.CmAccountRedis> getCmSonAccoutList(Long customerId) {
        // TODO Auto-generated method stub
        try (Jedis jedis = hry.cmson.util.CmRedis.JEDIS_POOL.getResource()) {
            Set<String> coinKeys = CmConfigUtil.getAllConskeysList();
            QueryFilter qf = new QueryFilter(CmSonAccount.class);
            qf.addFilter("customerId=", customerId);
            List<CmSonAccount> list = cmSonAccountService.find(qf);
            List<hry.cmson.dto.CmAccountRedis> listaccount = new ArrayList<hry.cmson.dto.CmAccountRedis>();
            for (CmSonAccount c : list) {
                coinKeys.remove(c.getCoinCode());
                hry.cmson.dto.CmAccountRedis account = new hry.cmson.dto.CmAccountRedis();
                account.setId(c.getId());
                account.setCoinCode(c.getCoinCode());
                account.setCustomerId(customerId);
                account.setHotMoney(c.getHotMoney());
                account.setColdMoney(c.getColdMoney());

                //??????????????????BTC_CNY,???????????????
                String price = jedis.hget("hry:coinPrice", c.getCoinCode());
                if (StringUtil.isNotEmpty(price)) {
                    account.setValuation((account.getHotMoney().add(account.getColdMoney())).multiply(new BigDecimal(price)));
                }
                listaccount.add(account);
            }
            //???????????????????????????????????????????????????
            Iterator<String> coinKeysIter = coinKeys.iterator();
            while (coinKeysIter.hasNext()) {
                String coinCode = coinKeysIter.next();
                hry.cmson.dto.CmAccountRedis account = new hry.cmson.dto.CmAccountRedis();
                account.setCoinCode(coinCode);
                account.setCustomerId(customerId);
                account.setHotMoney(BigDecimal.ZERO);
                account.setColdMoney(BigDecimal.ZERO);
                listaccount.add(account);
            }
            return listaccount;
        } catch (Exception e) {
            e.printStackTrace();
        }
        //???????????????????????????????????????
        return null;
    }

    @Override
    public BigDecimal getCmSonHotMoneyByCoin (String coinCode, Long customerId) {
        hry.cmson.dto.CmAccountRedis cmAccountRedis = hry.cmson.deal.CmDealUtil.getCmAccount( customerId,  coinCode);
        if(cmAccountRedis !=null){
            return cmAccountRedis.getHotMoney();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public hry.cmson.dto.CmAccountRedis cmSonAccount(Long customerId) {
        /** ???????????????Code */
        String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");
        /** ????????????????????????????????? */
        hry.cmson.dto.CmAccountRedis cmAccountRedis = hry.cmson.deal.CmDealUtil.getCmAccount(customerId, platCoin);
        return cmAccountRedis;
    }


}
