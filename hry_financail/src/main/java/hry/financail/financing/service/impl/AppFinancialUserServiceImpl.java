/**
 * Copyright:
 * @author:      jidn
 * @version:     V1.0
 * @Date:        2019-04-03 11:07:50
 */
package hry.financail.financing.service.impl;

import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.core.thread.ThreadPool;
import hry.financail.financing.dao.AppFinancialRedAcountDao;
import hry.financail.financing.dao.AppFinancialUserDao;
import hry.financail.financing.model.AppFinancialProducts;
import hry.financail.financing.model.AppFinancialRedAcount;
import hry.financail.financing.model.AppFinancialUser;
import hry.financail.financing.runnable.FinancialRunnable;
import hry.financail.financing.service.AppFinancialRedAcountService;
import hry.financail.financing.service.AppFinancialUserService;
import hry.remote.model.FinancialUser;
import hry.remote.model.RemoteResult;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.UserRedisUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p> AppFinancialUserService </p>
 * @author:         jidn
 * @Date :          2019-04-03 11:07:50
 */
@Service("appFinancialUserService")
public class AppFinancialUserServiceImpl extends BaseServiceImpl<AppFinancialUser, String> implements AppFinancialUserService{

	private static Logger logger = Logger.getLogger(AppFinancialUserServiceImpl.class);

	@Resource(name="appFinancialUserDao")
	@Override
	public void setDao(BaseDao<AppFinancialUser, String> dao) {
		super.dao = dao;
	}

	@Resource
	private AppFinancialUserDao appFinancialUserDao;
	@Resource
	private AppFinancialRedAcountDao appFinancialRedAcountDao;
	@Resource
	private AppFinancialRedAcountService appFinancialRedAcountService;

	@Override
	public synchronized RemoteResult createFinancialOrder(AppFinancialProducts afp,String customerId, BigDecimal buyNumber,BigDecimal redPacketsMoney) {
		RemoteResult remoteResult = new RemoteResult();
		try {

			BigDecimal actual = buyNumber.add(redPacketsMoney);
			//????????????
			ExDigitalmoneyAccountRedis accountRedis = UserRedisUtils.getAccountRedis(customerId, afp.getCoinCode());
			//?????????
			BigDecimal poundage=buyNumber.multiply(new BigDecimal(afp.getBuyRate())).divide(new BigDecimal(100));
			//????????????????????????????????????????????????????????????????????????
			//??????  ????????????10???BTC ?????????????????????100???BTC ????????????1???BTC  ????????????????????? 100+1-10=91???
			//?????? ???????????????100??? ??????????????????91??? ??????????????????10???
			//????????????

			BigDecimal  coinMoney=(buyNumber.add(poundage)).subtract(redPacketsMoney);
			//?????????????????????????????? ???????????????????????????
			if(buyNumber.add(poundage).compareTo(accountRedis.getHotMoney()) == 1 ){
				return new RemoteResult().setSuccess(false).setMsg("financial_balanceInsufficient").setObj(poundage.toPlainString());
			}
			if(coinMoney.compareTo(BigDecimal.ZERO)!=1){
				return new RemoteResult().setSuccess(false).setMsg("financial_notAllowUseRedAccount");
			}


            //??????????????????
			Map<String,String> paramMap1 = new HashMap<>(4);
			paramMap1.put("coinCode",afp.getCoinCode());
			paramMap1.put("isRedeem","0");
			paramMap1.put("productId",afp.getId().toString());
			List<FinancialUser> financialUserList = appFinancialUserDao.findFinancialUserList(paramMap1);

			//?????????????????????????????????
			BigDecimal allUserBuyNum =new BigDecimal(0);
			//??????????????????????????????
			BigDecimal thisUserBuyNum = new BigDecimal(0);
			for(FinancialUser fu : financialUserList){
                allUserBuyNum = allUserBuyNum.add(fu.getCoinMoney().add(fu.getRedPacketsMoney()));
                if(fu.getCustomerId().equals(Long.valueOf(customerId))){
                    thisUserBuyNum = thisUserBuyNum.add(fu.getCoinMoney()).add(fu.getRedPacketsMoney());
                }
            }

            //????????????????????????
			if((afp.getTotalMoney().compareTo(thisUserBuyNum)) == -1){
				remoteResult.setMsg("financial_isAllowPriceAllPrice");
				return remoteResult;
			}
			//???????????????????????????
			if((afp.getTotalMoney().compareTo(allUserBuyNum.add(buyNumber.add(redPacketsMoney)))) == -1){
				remoteResult.setMsg("financial_totalInvestmentLimitCredit");
				return remoteResult;
			}
			//??????????????????????????????
			if((afp.getInvestmentCeilingMoney().compareTo(thisUserBuyNum.add(buyNumber.add(redPacketsMoney)))) == -1){
				remoteResult.setMsg("financial_personLimitCredit");
				return remoteResult;
			}

			BigDecimal expectedIncome = BigDecimal.ZERO;

			if (afp.getCanUseRedPackets() == 1) {
				//??????????????????
				expectedIncome = buyNumber.add(redPacketsMoney);
				//??????????????????????????????
				expectedIncome = actual.multiply(new BigDecimal(afp.getIncomeYearFloor())).divide(new BigDecimal(100));
				//????????? ??????????????????
				expectedIncome = expectedIncome.multiply(new BigDecimal(afp.getInvestmentPeriod()));
				//????????????????????????????????????????????????????????????
				if(redPacketsMoney.compareTo(BigDecimal.ZERO)==1){

					AppFinancialRedAcount appFinancialRedAcount = appFinancialRedAcountService.getFinancialRedAccount(customerId,afp.getCoinCode());

					if(appFinancialRedAcount!=null){
						if(appFinancialRedAcount.getMoney().compareTo(redPacketsMoney)!=-1){
							//????????????????????????
							appFinancialRedAcount.setMoney(appFinancialRedAcount.getMoney().subtract(redPacketsMoney));
							//???????????????????????????
							appFinancialRedAcount.setUsedMoney(appFinancialRedAcount.getUsedMoney().add(redPacketsMoney));
							appFinancialRedAcountDao.updateByPrimaryKeySelective(appFinancialRedAcount);
						} else {
							remoteResult.setMsg("financial_redAccountInsufficient");
							return remoteResult;
						}
					}else{
						remoteResult.setMsg("financial_redAccountNotExit");
						return remoteResult;
					}
				}
			} else {
				if(redPacketsMoney.compareTo(BigDecimal.ZERO) == 1){
					return new RemoteResult().setSuccess(false).setMsg("financial_notAllowUseRedAccountPrice");
				}
				expectedIncome = actual.multiply(new BigDecimal(afp.getIncomeYearFloor())).divide(new BigDecimal(100));
				expectedIncome = expectedIncome.multiply(new BigDecimal(afp.getInvestmentPeriod()));
			}

			//????????????
            ThreadPool.exe(new FinancialRunnable(afp,customerId,buyNumber,redPacketsMoney,expectedIncome));

			remoteResult.setSuccess(true);
		} catch (Exception e){
			e.printStackTrace();
			remoteResult.setMsg("neibucuowu");
		}
		return remoteResult;
	}

	@Override
	public List<FinancialUser> findFinancialUserList(Map<String, String> paramMap) {
		return appFinancialUserDao.findFinancialUserList(paramMap);
	}

	/**
	 * ?????????????????????
	 * @param pMap
	 * @return
	 */
	@Override
	public Integer findInvestmentedPersonCount(Map<String, Object> pMap) {
		return appFinancialUserDao.findInvestmentedPersonCount(pMap);
	}

}
