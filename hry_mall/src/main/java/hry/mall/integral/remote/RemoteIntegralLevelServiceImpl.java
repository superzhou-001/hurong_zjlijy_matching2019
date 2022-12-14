package hry.mall.integral.remote;

import com.alibaba.fastjson.JSON;
import hry.bean.FrontPage;
import hry.bean.JsonResult;
import hry.core.shiro.PasswordHelper;
import hry.mall.integral.model.*;
import hry.mall.integral.service.*;
import hry.mall.lend.person.model.AppPersonInfo;
import hry.mall.lend.person.service.AppPersonInfoService;
import hry.mall.member.commend.model.AppCommendUser;
import hry.mall.member.commend.service.AppCommendUserService;
import hry.mall.member.user.model.AppCustomer;
import hry.mall.member.user.service.AppCustomerService;
import hry.mq.producer.service.MessageProducer;
import hry.redis.common.utils.RedisService;
import hry.util.BaseConfUtil;
import hry.util.QueryFilter;
import hry.util.SNUtil;
import hry.util.SpringUtil;
import hry.util.date.DateUtil;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class RemoteIntegralLevelServiceImpl implements RemoteIntegralLevelService {
	@Resource
	public IntegralLevelService integralLevelService;
	@Resource
	public AppCommendUserService appCommendUserService;
	@Resource
	private IntegralConfigService integralConfigService;
	@Resource
	private IntegralCommissionService integralCommissionService;
	@Resource
	private CustomerIntegralService customerIntegralService;
	@Resource
	private AppCustomerService appCustomerService;
	@Resource
	private AppPersonInfoService appPersonInfoService;
	@Resource(name="transactionManager")
	private DataSourceTransactionManager transactionManager;
	@Resource
	private LevelRecordService levelRecordService;
	@Resource
	private ShopTeamLevelService shopTeamLevelService;
	@Resource
	private ShopMemberPerformanceService shopMemberPerformanceService;


	@Override
	public JsonResult levelList() {
		// TODO Auto-generated method stub
		List<IntegralLevel> list=integralLevelService.findAll();
		return new JsonResult().setSuccess(true).setObj(list);
	}

	@Override
	public JsonResult findByCustomerId(long memberId) {
		JsonResult jsonResult = new JsonResult();
		try {

			AppCommendUser appCommendUser = appCommendUserService.findPageBySqlIntegral(memberId);
			Map<String, String> params = new HashMap<>();
			params.put("id",memberId+"");
			params.put("number","1");
			params.put("limit","1");
			params.put("offset","0");
			FrontPage page = appCommendUserService.findConmmendForntPageBySql(params);
			appCommendUser.setOneNumber(page.getTotal().intValue());
			params.put("number","2");
			FrontPage page1 = appCommendUserService.findConmmendForntPageBySql(params);
			appCommendUser.setTwoNumber(page1.getTotal().intValue());
			params.put("number","3");
			FrontPage page2 = appCommendUserService.findConmmendForntPageBySql(params);
			appCommendUser.setThreeNumber(page2.getTotal().intValue());
			// ??????????????????
			//ApiJsonResult result = integralConfigService.getUserRecommendedNumber(memberId,5);
			//??????????????????
			QueryFilter queryFilter = new QueryFilter(ShopMemberPerformance.class);
			queryFilter.addFilter("memberId=",memberId);
			ShopMemberPerformance shopMemberPerformance = shopMemberPerformanceService.get(queryFilter);
			// ????????????
			appCommendUser.setTeamAllPerformance(shopMemberPerformance.getTeamAllPerformance());

			appCommendUser.setTeamAllPerformance(shopMemberPerformance.getTeamAllPerformance());
			QueryFilter filter = new QueryFilter(IntegralCommission.class);
			filter.addFilter("memberId=", memberId);
			//filter.addFilter("type_in", "3,4");
			List<IntegralCommission> integralCommissionList = integralCommissionService.find(filter);
			// ???????????? //?????????????????????+??????+??????+???????????????
			BigDecimal allTeamAward = new BigDecimal("0");
			// ????????????
			String teamLevelName = "0";
			if (integralCommissionList != null && integralCommissionList.size() > 0) {
				for (IntegralCommission comm : integralCommissionList) {
					allTeamAward = allTeamAward.add(comm.getCount());
				}
			}
			appCommendUser.setAllTeamAward(allTeamAward);
			QueryFilter filter1 = new QueryFilter(CustomerIntegral.class);
			filter1.addFilter("memberId=",memberId);
			CustomerIntegral customerIntegral = customerIntegralService.get(filter1);
			if (customerIntegral.getTeamId() != null) {
				teamLevelName = customerIntegral.getTeamNumber().toString();
			}
			appCommendUser.setTeamLevelName(teamLevelName);
			jsonResult.setSuccess(true).setObj(appCommendUser);
			return jsonResult;
		}catch (Exception e){
			e.printStackTrace();
		}
		return jsonResult.setSuccess(false);

	}



	@Override
	public JsonResult buyLevel(Long memberId,BigDecimal account,String accountPwd) {
		/*
		0.?????????????????????
		1.???????????????
		2.??????????????????????????????????????????????????????
		4.???????????????
		5.????????????????????????
		6.????????????????????????
		8.?????????????????????
		*/
		JsonResult result = new JsonResult();
		IntegralLevel levelByAccount = integralLevelService.getLevelByAccount(account);
		if (null ==levelByAccount){
			result.setSuccess(false).setMsg("meiyouduiyingdengji"); //?????????????????????
			return result;
		}

		//1????????????
		QueryFilter filter = new QueryFilter(AppCustomer.class);
		filter.addFilter("id=",memberId);
		AppCustomer appCustomer = appCustomerService.get(filter);
		//?????????????????????????????????????????????????????????????????????
		if(null==appCustomer.getAccountPassWord() || "".equals(appCustomer.getAccountPassWord())){
			result.setSuccess(false).setMsg("zhifumimakong"); //??????????????????,???????????????????????????
			return result;
		}
		//??????????????????????????????
		PasswordHelper passwordHelper = new PasswordHelper();
		String encryString = passwordHelper.encryString(accountPwd, appCustomer.getSalt());
		if(!encryString.equals(appCustomer.getAccountPassWord())){
			result.setSuccess(false).setMsg("zhifumimacuowu"); //??????????????????
			return result;
		}
		//????????????????????????10W
		BigDecimal userPerformance = levelRecordService.getUserPerformance(memberId);
		if(null==userPerformance){
			userPerformance=new BigDecimal("0");
		}
		BigDecimal count = userPerformance.add(account);
		if(count.compareTo(new BigDecimal("100000"))>0){
			result.setSuccess(false).setMsg("goumaijineguoda"); //??????????????????
			return result;
		}

		// ??????????????????
		//??????????????????
		QueryFilter queryFilter = new QueryFilter(CustomerIntegral.class);
		queryFilter.addFilter("memberId=", memberId);
		//??????????????????
		CustomerIntegral customerIntegral = customerIntegralService.find(queryFilter).get(0);
		IntegralLevel integralLevelOld=null;
		if(null != customerIntegral.getLevelId()){
			integralLevelOld = integralLevelService.get(customerIntegral.getLevelId());
			// ??????????????????
			if(integralLevelOld.getNumber() > levelByAccount.getNumber()){
				result.setSuccess(false).setMsg("goumaigenggaodengji"); //???????????????????????????
				return result;
			}
		}else{
			result.setSuccess(false).setMsg("req_error"); //??????????????????
			return result;
		}

	/*	BigDecimal availableIntegral = customerIntegral.getAvailableIntegral();
		int i = availableIntegral.compareTo(account);
		if(i<0){
			result.setSuccess(false).setMsg("dianziquanyuebuzu");//????????????
			return result;
		}*/
		//?????????????????????????????????
        String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");//???????????????
	       Map<String, String> vmap=new HashMap<String,String>();
	       vmap.put("memberId", memberId.toString());
	       vmap.put("coinCode", platCoin);
	       BigDecimal coincount=customerIntegralService.platCoinCount(account);
	       vmap.put("coinCount", coincount.toString());
	       result=customerIntegralService.validateExaccount(vmap);
	       if(!result.getSuccess()){
	    	   return result;
	       }

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW); // ????????????????????????????????????
		TransactionStatus status = transactionManager.getTransaction(def); // ??????????????????
		try {
			//5.????????????????????????
			LevelRecord levelRecord = new LevelRecord();
			//??????????????????????????????
			if (null != customerIntegral.getLevelId()){
				levelRecord.setOldLevelId(integralLevelOld.getId());
				levelRecord.setOldLevelNumber(integralLevelOld.getNumber());
			}
			String requestNo = SNUtil.create15();
			levelRecord.setNumber(requestNo);
			levelRecord.setMemberId(memberId);
			levelRecord.setLevelId(levelByAccount.getId());
			levelRecord.setLevelNumber(levelByAccount.getNumber());
			levelRecord.setMoney(account);
			levelRecord.setStatus(1);
			levelRecord.setRemark("????????????");
			levelRecord.setStartDate(new Date());
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.YEAR, levelByAccount.getValidityPeriod());
			Date time = calendar.getTime();
			levelRecord.setEndDate(time);
			//??????????????????  ???????????????????????????1???
			Date date = DateUtil.addDay(new Date(), 1);
			levelRecord.setDistributeDate(date);
			levelRecordService.save(levelRecord);

            RedisService redisService = SpringUtil.getBean("redisService");
            String lock = "submitOrder:" + customerIntegral.getMemberId();
            if (redisService.lock(lock)) {//??????
                try{
                    //4.???????????????
                    Map<String, String> map1 = new HashMap<>();
                    map1.put("id", customerIntegral.getId().toString());
                    map1.put("type", CustomerIntegral.TYPE_SUB);
                    map1.put("integralCount", account.toString());
                    map1.put("tradingDetail", "????????????");
                    map1.put("businessType", "7");//??????
                    map1.put("requestNo", requestNo);//?????????
                    boolean flag1 = customerIntegralService.updateInteger(map1);
                    if (!flag1) {
                        transactionManager.rollback(status);
                        result.setSuccess(false).setMsg("kaitongshibai"); //????????????
                        return result;
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    transactionManager.rollback(status);
                    result.setSuccess(false).setMsg("kaitongshibai"); //????????????
                    return result;
                }finally {
                    redisService.unLock(lock);//??????
                }
            }else{
				transactionManager.rollback(status);
				result.setSuccess(false).setMsg("kaitongshibai"); //????????????
			}

            redisService.unLock(lock);//??????

			//???????????????????????????
			QueryFilter queryFilter2 = new QueryFilter(ShopMemberPerformance.class);
			queryFilter2.addFilter("memberId=",memberId);
			ShopMemberPerformance shopMemberPerformance = shopMemberPerformanceService.get(queryFilter2);
			shopMemberPerformance.setAllPerformance(shopMemberPerformance.getAllPerformance().add(account));
			shopMemberPerformance.setTeamAllPerformance(shopMemberPerformance.getTeamAllPerformance().add(account));
			shopMemberPerformanceService.update(shopMemberPerformance);
			//???????????????????????????
			shopTeamLevelService.updateUserTeamLevel(memberId);

			//6??????????????????????????????????????????
			//???????????????????????????????????????
			CustomerIntegral newCustomerIntegral1 = customerIntegralService.get(customerIntegral.getId());
			newCustomerIntegral1.setLevelId(levelByAccount.getId());
			newCustomerIntegral1.setLevelNumber(levelByAccount.getNumber());
			newCustomerIntegral1.setStartDate(new Date());
			newCustomerIntegral1.setEndDate(time);
			customerIntegralService.update(newCustomerIntegral1);


			//???????????? 8.????????????????????? 9.??????5????????????????????????  ??????3????????????10?????????????????????????????????????????????
//			UserByLevelRunable userByLevelRunable = new UserByLevelRunable(memberId, levelByAccount.getId(), account, 3, 10);
//			ThreadPool.exe(userByLevelRunable);

			//??????????????????
			MessageProducer messageProducer = SpringUtil.getBean("messageProducer");
			messageProducer.distributeRecommendRewards(JSON.toJSONString(levelRecord));

			transactionManager.commit(status);
		}catch (Exception e){
			e.printStackTrace();
			transactionManager.rollback(status);

			return new JsonResult().setSuccess(false).setMsg("kaitongshibai");//????????????

		}

		return new JsonResult().setSuccess(true).setMsg("kaitongchenggong");//????????????

	}

	@Override
	public JsonResult seeUserLevel(long memberId){
		AppCustomer appCustomer = appCustomerService.get(memberId);
		HashMap<String, Object> map = new HashMap<>();
		map.put("nickNameOtc",appCustomer.getNickNameOtc());//??????
		map.put("userFace",appCustomer.getUserFace());//??????
		//??????????????????
		QueryFilter queryFilter = new QueryFilter(CustomerIntegral.class);
		queryFilter.addFilter("memberId=", memberId);
		List<CustomerIntegral> customerIntegrals = customerIntegralService.find(queryFilter);
		//??????????????????
		CustomerIntegral customerIntegral = customerIntegrals.get(0);
		map.put("isLevel","0");//?????????

		QueryFilter queryFilter1 = new QueryFilter(IntegralLevel.class);
		queryFilter1.setOrderby("number asc");
		List<IntegralLevel> list=integralLevelService.find(queryFilter1);
		if(null != list && list.size()>0){
			IntegralLevel integralLevel1 = list.get(0);
			//???????????????????????????????????????????????????
			if (null != customerIntegral.getLevelId()&& customerIntegral.getLevelId()!=integralLevel1.getId() && null != customerIntegral.getEndDate() && !customerIntegral.getEndDate().before(new Date())){
				IntegralLevel integralLevel = integralLevelService.get(customerIntegral.getLevelId());
				map.put("levelName",integralLevel.getName());
				map.put("levelNum",integralLevel.getNumber());
				map.put("lowestPrice",integralLevel.getLowestPrice());
				map.put("highestPrice",integralLevel.getHighestPrice());
				map.put("endDate",customerIntegral.getEndDate());
				map.put("isLevel","1");//??????????????????
			}
			//?????????????????????
			if(null != list && list.size()>1){
                IntegralLevel integralLevel2 = list.get(1);
                if(customerIntegral.getLevelNumber()!=null){
                    if(customerIntegral.getLevelNumber()>integralLevel2.getNumber()){
                        map.put("isLevel","2");//????????????????????????
                    }
                }
				//?????????????????????????????????
				list.remove(0);
			}
			//?????????????????????????????????
			list.remove(0);

		}
		map.put("integralLevels",list);//????????????????????????

		return new JsonResult().setSuccess(true).setObj(map);
	}


	@Override
	public JsonResult getLevelByAccount(BigDecimal account){
		if (null ==account){
			return new JsonResult().setSuccess(false).setMsg("buyAccount_not_null");
		}
		IntegralLevel integralLevel = integralLevelService.getLevelByAccount(account);
		if (null ==integralLevel){
			return new JsonResult().setSuccess(false).setMsg("meiyouduiyingdengji"); //?????????????????????
		}
		return new JsonResult().setSuccess(true).setObj(integralLevel);
	}


	@Override
	public JsonResult addLevelRecord(Map<String, Object> map){
		String mobilePhone = map.get("mobilePhone")==null? null :map.get("mobilePhone").toString();
		String number = map.get("number")==null?null:map.get("number").toString();
		String oldLevelNumber = map.get("oldLevelNumber")==null?null:map.get("oldLevelNumber").toString();
		String levelNumber = map.get("levelNumber")==null?null:map.get("levelNumber").toString();
		String money = map.get("money")==null?null:map.get("money").toString();
		String remark = map.get("remark")==null?null:map.get("remark").toString();
		String startDate = map.get("startDate")==null?null:map.get("startDate").toString();
		String endDate = map.get("endDate")==null?null:map.get("endDate").toString();
		String distributeDate = map.get("distributeDate")==null?null:map.get("distributeDate").toString();

		if(!StringUtils.isEmpty(mobilePhone)) {
			// ??????????????????????????????
			QueryFilter filter = new QueryFilter(AppPersonInfo.class);
			filter.addFilter("mobilePhone=", mobilePhone);
			AppPersonInfo appPersonInfo = appPersonInfoService.get(filter);
			if (appPersonInfo == null) {
				System.out.println("???????????????"+mobilePhone+"??????????????????");
				return new JsonResult().setSuccess(false).setMsg(mobilePhone);
			}
			LevelRecord levelRecord = new LevelRecord();
			levelRecord.setMemberId(appPersonInfo.getCustomerId());
			//??????????????????????????????
			if (!StringUtils.isEmpty(oldLevelNumber)){
				QueryFilter queryFilter = new QueryFilter(IntegralLevel.class);
				queryFilter.addFilter("number=",oldLevelNumber);
				IntegralLevel integralLevelOld = integralLevelService.get(queryFilter);
				if(integralLevelOld !=null){
					levelRecord.setOldLevelId(integralLevelOld.getId());
				}
				levelRecord.setOldLevelNumber(Integer.valueOf(oldLevelNumber));
			}
			if (!StringUtils.isEmpty(levelNumber)){
				QueryFilter queryFilter = new QueryFilter(IntegralLevel.class);
				queryFilter.addFilter("number=",levelNumber);
				IntegralLevel integralLevel = integralLevelService.get(queryFilter);
				//??????????????????
				QueryFilter queryFilter1 = new QueryFilter(CustomerIntegral.class);
				queryFilter1.addFilter("memberId=",appPersonInfo.getCustomerId());
				CustomerIntegral customerIntegral = customerIntegralService.get(queryFilter1);
				customerIntegral.setLevelId(integralLevel.getId());
				customerIntegral.setLevelNumber(integralLevel.getNumber());
				customerIntegralService.update(customerIntegral);

				if(integralLevel !=null){
					levelRecord.setLevelId(integralLevel.getId());
				}
				levelRecord.setLevelNumber(Integer.valueOf(levelNumber));
			}

			try{
				levelRecord.setNumber(number);
				levelRecord.setMoney(new BigDecimal(money));
				levelRecord.setStatus(1);
				levelRecord.setRemark(remark);
				SimpleDateFormat sDateFormat=new SimpleDateFormat("MM/dd/yyyy HH:mm:ss"); //????????????
				//SimpleDateFormat sDateFormat2=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); //????????????
				levelRecord.setStartDate(sDateFormat.parse(startDate));
				levelRecord.setEndDate(sDateFormat.parse(endDate));
				//??????????????????  ???????????????????????????1???
				levelRecord.setDistributeDate(sDateFormat.parse(distributeDate));
				levelRecordService.save(levelRecord);
				return new JsonResult().setSuccess(true);
			}catch (Exception e){
				e.printStackTrace();
				return new JsonResult().setSuccess(false).setMsg("????????????:"+mobilePhone+"?????????????????????");
			}

		}

		return new JsonResult().setSuccess(false).setMsg("???????????????");

	}

}
