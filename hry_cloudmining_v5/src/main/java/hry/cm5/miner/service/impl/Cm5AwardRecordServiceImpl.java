/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2020-01-08 14:06:05 
 */
package hry.cm5.miner.service.impl;

import hry.bean.ObjectUtil;
import hry.cm5.customer.model.Cm5CustomerLevel;
import hry.cm5.customer.service.Cm5CustomerLevelService;
import hry.cm5.deal.CmDealUtil;
import hry.cm5.dto.Accountadd;
import hry.cm5.dto.CmAccountRedis;
import hry.cm5.enums.CmAccountRemarkEnum;
import hry.cm5.miner.dao.Cm5AwardRecordDao;
import hry.cm5.miner.model.Cm5AwardRecord;
import hry.cm5.miner.model.Cm5MinerGoods;
import hry.cm5.miner.model.Cm5MinerOrder;
import hry.cm5.miner.service.Cm5AwardRecordService;
import hry.cm5.miner.service.Cm5MinerGoodsService;
import hry.cm5.miner.service.Cm5MinerOrderService;
import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.mq.producer.DealMsgUtil;
import hry.util.QueryFilter;
import hry.util.idgenerate.IdGenerate;
import hry.utils.BaseConfUtil;
import hry.utils.CmConfigUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * <p> Cm5AwardRecordService </p>
 * @author:         zhouming
 * @Date :          2020-01-08 14:06:05  
 */
@Service("cm5AwardRecordService")
public class Cm5AwardRecordServiceImpl extends BaseServiceImpl<Cm5AwardRecord, Long> implements Cm5AwardRecordService{
	@Resource
	private Cm5MinerOrderService cm5MinerOrderService;

	@Resource
	private Cm5MinerGoodsService cm5MinerGoodsService;

	@Resource
	private Cm5CustomerLevelService cm5CustomerLevelService;

	@Resource(name="cm5AwardRecordDao")
	@Override
	public void setDao(BaseDao<Cm5AwardRecord, Long> dao) {
		super.dao = dao;
	}

	@Override
	public BigDecimal getAwardTotal(String awardCode, String inheritNum, String awardType) {
		Map<String, Object> params = new HashMap<>();
		params.put("inheritNum", inheritNum);
		params.put("awardType", awardType);
		params.put("awardCode", awardCode);
		return ((Cm5AwardRecordDao)dao).getAwardTotal(params);
	}

	@Override
	public void giveOutAward(Cm5MinerOrder minerOrder) {
		// 矿机信息
		Cm5MinerGoods minerGoods = cm5MinerGoodsService.get(minerOrder.getMinerId());
		// us日产币比例
		BigDecimal usRatio = new BigDecimal(minerGoods.getUsRatio());
		// us应产币数量
		BigDecimal usAwardOne = minerGoods.getMinerPrice().multiply(usRatio).setScale(10, BigDecimal.ROUND_DOWN);
		// 已产US数
		BigDecimal usAwardTotal = this.getAwardTotal(CmConfigUtil.getConfigCoinCode(CmConfigUtil.usCoinCode), minerOrder.getInheritNum(), "1");
		// uskc 日产币比例
		BigDecimal uskcRatio = new BigDecimal(minerGoods.getUskcRatio());
		// uskc应产币数量---忽略单位（注：us == uskc）
		BigDecimal uskcAwardOne = minerGoods.getMinerPrice().multiply(uskcRatio).setScale(10, BigDecimal.ROUND_DOWN);
		// 已产uskc数
		BigDecimal uskcAwardTotal = this.getAwardTotal(CmConfigUtil.getConfigCoinCode(CmConfigUtil.uskcCoinCode), minerOrder.getInheritNum(), "2");
		// us实际产币数
		BigDecimal usAwardTwo = new BigDecimal("0");
		// us产币逻辑 注： us收益数最大数为矿机价值
		if (usAwardTotal.compareTo(minerGoods.getMinerPrice()) == -1) {
			if (usAwardOne.add(usAwardTotal).compareTo(minerGoods.getMinerPrice()) < 1) {
				usAwardTwo = usAwardOne;
			} else {
				usAwardTwo = minerGoods.getMinerPrice().subtract(usAwardTotal);
			}
		}
		// 管道 日产币比例
		BigDecimal pipeRatio = new BigDecimal(minerGoods.getPipeRatio());
		// 管道算力应产币币种（注：（直推用户USKC算力 + 直推用户管道算力）* 取小的比例）
		BigDecimal pipeAwardOne = this.getPipeAwardOneTotal(minerOrder.getCustomerId(), pipeRatio);

		// 已产管道算力
		BigDecimal pipeAwardTotal = this.getAwardTotal(CmConfigUtil.getConfigCoinCode(CmConfigUtil.pipeCoinCode), minerOrder.getInheritNum(), "3");
		// 已产出总收益
		BigDecimal awardTotal = usAwardTotal.add(uskcAwardTotal).add(pipeAwardTotal);
		// 应产收益 us + uskc + 管道 (注：usAwardTwo, us收益达到矿机价值后不再发放，则这块直接去实际收益)
		BigDecimal awardOne = usAwardTwo.add(uskcAwardOne).add(pipeAwardOne);
		// 预计应产总收益
		BigDecimal awardOneTotal = awardOne.add(awardTotal);
		// uskc实际产币
		BigDecimal uskcAwardTwo = new BigDecimal("0");
		// 管道实际产币
		BigDecimal pipeAwardTwo = new BigDecimal("0");
		// 需要冻结的收益---单位 uskc
		BigDecimal awardFreeze = new BigDecimal("0");
		// 出局标识
		Boolean outFlag = false;
		// 校验是否出局
		if (awardOneTotal.compareTo(minerOrder.getTotalProfit()) != -1) {
			outFlag = true;
			awardFreeze = awardOneTotal.subtract(minerOrder.getTotalProfit());
			// 细节判断---取uskc和管道收益实际发放数
			if (usAwardTwo.add(awardTotal).compareTo(minerOrder.getTotalProfit()) == -1) {
				if (usAwardTwo.add(uskcAwardOne).add(awardTotal).compareTo(minerOrder.getTotalProfit()) == -1) {
					pipeAwardTwo = awardFreeze.subtract(usAwardTwo).subtract(uskcAwardOne);
				} else {
					uskcAwardTwo = awardFreeze.subtract(usAwardTwo).subtract(pipeAwardOne);
				}
			}
		} else {
			uskcAwardTwo = uskcAwardOne;
			pipeAwardTwo = pipeAwardOne;
		}
		// 添加奖励记录
		Cm5AwardRecord awardRecord = new Cm5AwardRecord();
		awardRecord.setInheritNum(minerOrder.getInheritNum());
		awardRecord.setMinerOrderId(minerOrder.getId());
		awardRecord.setCustomerId(minerOrder.getCustomerId());
		awardRecord.setOrderNum(minerOrder.getOrderNum());
		awardRecord.setTransactionNum(IdGenerate.transactionNum("AW"));
		/*~~~~~ us收益记录---start ~~~~~*/
		if (usAwardTwo.compareTo(BigDecimal.ZERO) == 1) {
			Cm5AwardRecord usRecord = ObjectUtil.bean2bean(awardRecord, Cm5AwardRecord.class);
			usRecord.setAwardCode(CmConfigUtil.getConfigCoinCode(CmConfigUtil.usCoinCode));
			usRecord.setAwardRatio(minerGoods.getUsRatio());
			usRecord.setAwardOne(usAwardOne);
			usRecord.setAwardTwo(uskcAwardTwo);
			usRecord.setRealityAwardCode(minerGoods.getUsRatio());
			usRecord.setRealityAwardTwo(uskcAwardTwo);
			usRecord.setStatus(2);
			usRecord.setAwardType(1);
			usRecord.setRemark(usRecord.getAwardCode()+"收益发放");
			super.save(usRecord);
		}
		/*~~~~~ us收益记录---end ~~~~~*/

		/*~~~~~ uskc收益记录---start ~~~~~*/
		// 平台币
		String platCoin = BaseConfUtil.getConfigSingle("platCoin", "configCache:basefinanceConfig");//平台币币种
		// 获取 平台币市价(RMB)
		BigDecimal platPrice = CmConfigUtil.getCoinRate(platCoin);
		// 获取USDT市价(RMB)
		BigDecimal usdtPrice = CmConfigUtil.getCoinRate("USDT");
		// uskc市价(USDT)
		String uskcCoinCode = CmConfigUtil.getConfigCoinCode(CmConfigUtil.uskcCoinCode);
		BigDecimal uskcUsdtPrice = CmConfigUtil.getPricingPrice(uskcCoinCode);
		// 市价RMB
		BigDecimal uskcPrice = uskcUsdtPrice.multiply(usdtPrice);
		// uskc_VMC 汇率
		BigDecimal exchangeRate = uskcPrice.divide(platPrice, 10, BigDecimal.ROUND_DOWN);
		// uskc收益记录--- uskc收益自动转换为VMC(平台币)
		BigDecimal platNum = uskcAwardTwo.multiply(exchangeRate).setScale(10, BigDecimal.ROUND_DOWN);
		Cm5AwardRecord uskcRecord = ObjectUtil.bean2bean(awardRecord, Cm5AwardRecord.class);
		uskcRecord.setAwardCode(uskcCoinCode);
		uskcRecord.setAwardRatio(minerGoods.getUskcRatio());
		uskcRecord.setAwardOne(usAwardOne);
		uskcRecord.setAwardTwo(uskcAwardTwo);
		uskcRecord.setRealityAwardCode(platCoin);
		uskcRecord.setRealityRate(exchangeRate);
		uskcRecord.setRealityAwardTwo(platNum);
		uskcRecord.setStatus(2);
		uskcRecord.setAwardType(2);
		uskcRecord.setRemark(uskcRecord.getRealityAwardCode()+"收益发放:"+uskcRecord.getAwardCode()+"市价-"+uskcPrice+"(RMB),"+uskcRecord.getRealityAwardCode()+"市价-"+platPrice+"(RMB)");
		super.save(uskcRecord);
		/*~~~~~ uskc收益记录---end ~~~~~*/

		/*~~~~~ 管道收益记录---start ~~~~~*/
		// 管道收益记录--- uskc收益自动转换为VMC(平台币)
		// 管道收币种市价(USDT)
		String pipeCoinCode = CmConfigUtil.getConfigCoinCode(CmConfigUtil.pipeCoinCode);
		BigDecimal pipeUsdtPrice = CmConfigUtil.getPricingPrice(pipeCoinCode);
		// 市价RMB
		BigDecimal pipePrice = pipeUsdtPrice.multiply(usdtPrice);
		// 管道币种_VMC 汇率
		BigDecimal pipeExchangeRate = pipePrice.divide(platPrice, 10, BigDecimal.ROUND_DOWN);
		// VMC收益
		BigDecimal platNum2 = pipeAwardTwo.multiply(pipeExchangeRate).setScale(10, BigDecimal.ROUND_DOWN);
		Cm5AwardRecord pipeRecord = ObjectUtil.bean2bean(awardRecord, Cm5AwardRecord.class);
		pipeRecord.setAwardCode(uskcCoinCode);
		pipeRecord.setAwardRatio(minerGoods.getUskcRatio());
		pipeRecord.setAwardOne(pipeAwardOne);
		pipeRecord.setAwardTwo(pipeAwardTwo);
		pipeRecord.setRealityAwardCode(platCoin);
		pipeRecord.setRealityRate(pipeExchangeRate);
		pipeRecord.setRealityAwardTwo(platNum2);
		pipeRecord.setStatus(2);
		pipeRecord.setAwardType(3);
		pipeRecord.setRemark(pipeRecord.getRealityAwardCode()+"收益发放:"+pipeRecord.getAwardCode()+"市价-"+uskcPrice+"(RMB),"+uskcRecord.getRealityAwardCode()+"市价-"+platPrice+"(RMB)");
		super.save(pipeRecord);
		/*~~~~~ 管道收益记录---end ~~~~~*/
		if (outFlag) {
			// 出局---修改矿机状态
			minerOrder.setStatus(3);
			minerOrder.setEndDate(new Date());
			// 冻结记录
			Cm5AwardRecord freezeRecord = ObjectUtil.bean2bean(awardRecord, Cm5AwardRecord.class);
			freezeRecord.setAwardCode(uskcCoinCode);
			freezeRecord.setAwardThree(awardFreeze);
			freezeRecord.setStatus(1);
			freezeRecord.setAwardType(4);
			freezeRecord.setRemark("出局冻结");
			super.save(freezeRecord);
		}
		// 运行天数添加
		minerOrder.setRunDays(minerOrder.getRunDays() + 1);
		cm5MinerOrderService.update(minerOrder);
		/** 币账户操作 ---减少 */
		List<Accountadd> accountaddList = new ArrayList<>();
		if (usAwardTwo.compareTo(BigDecimal.ZERO) > 0) {
			CmAccountRedis usAccountRedis = CmDealUtil.getCmAccount(minerOrder.getCustomerId(), minerOrder.getPricingCoinCode());
			Accountadd usaccountadd = new Accountadd(minerOrder.getCustomerId(), minerOrder.getPricingCoinCode(), usAccountRedis.getId(),
					new BigDecimal("+" + usAwardTwo), 1, CmAccountRemarkEnum.TYPE11.getIndex(),
					awardRecord.getTransactionNum());
			accountaddList.add(usaccountadd);
		}
		CmAccountRedis platAccountRedis = CmDealUtil.getCmAccount(minerOrder.getCustomerId(), platCoin);
		Accountadd uskcaccountadd = new Accountadd(minerOrder.getCustomerId(), platCoin, platAccountRedis.getId(),
				new BigDecimal("+" + platNum), 1, CmAccountRemarkEnum.TYPE12.getIndex(),
				awardRecord.getTransactionNum());
		accountaddList.add(uskcaccountadd);

		Accountadd pipeaccountadd = new Accountadd(minerOrder.getCustomerId(), platCoin, platAccountRedis.getId(),
				new BigDecimal("+" + platNum2), 1, CmAccountRemarkEnum.TYPE13.getIndex(),
				awardRecord.getTransactionNum());
		accountaddList.add(pipeaccountadd);
		DealMsgUtil.sendAccountaddList(accountaddList);
	}

	/**
	 * 计算用户应发管道收益
	 * 注：（直推用户USKC算力 + 直推用户管道算力）* 取小的比例）
	 * */
	private BigDecimal getPipeAwardOneTotal(Long customerId, BigDecimal pipeRatio) {
		BigDecimal pipeAwardOne = new BigDecimal("0");
		// 获取该用户直推用户等级
		List<Cm5CustomerLevel> customerLevelList = cm5CustomerLevelService.findNextCustomerLevel(customerId);
		// 获取用户等级对应添加的管道收益比例
		// 父用户新增管道比例
		String parentAddRatio = cm5CustomerLevelService.getCustomerAddRatio(customerId);
		BigDecimal pipeParentAddRatio = new BigDecimal(parentAddRatio);
		for (Cm5CustomerLevel customerLevel : customerLevelList) {
			// 获取中用户正在运行的矿机
			QueryFilter filter = new QueryFilter(Cm5MinerOrder.class);
			filter.addFilter("customerId=", customerLevel.getCustomerId());
			filter.addFilter("status=", 1);
			Cm5MinerOrder minerOrder = cm5MinerOrderService.get(filter);
			if (minerOrder != null) {
				BigDecimal realityRatio = new BigDecimal("0");
				String sonAddRatio = cm5CustomerLevelService.getCustomerAddRatio(customerLevel.getCustomerId());
				BigDecimal pipeSonAddRatio = new BigDecimal(sonAddRatio);
				// 已产uskc数
				BigDecimal uskcAwardTotal = this.getAwardTotal(CmConfigUtil.getConfigCoinCode(CmConfigUtil.uskcCoinCode), minerOrder.getInheritNum(), "2");
				// 已产出的管道算力
				BigDecimal pipeAwardTotal = this.getAwardTotal(CmConfigUtil.getConfigCoinCode(CmConfigUtil.pipeCoinCode), minerOrder.getInheritNum(), "3");
				// 矿机信息
				Cm5MinerGoods minerGoods = cm5MinerGoodsService.get(minerOrder.getMinerId());
				// 子矿机对应管道收益
				BigDecimal pipeSonRatio = new BigDecimal(minerGoods.getPipeRatio());
				// 计算用户管道收益
				if (pipeRatio.add(pipeParentAddRatio).compareTo(pipeSonRatio.add(pipeSonAddRatio)) < 1) {
					realityRatio = pipeRatio.add(pipeParentAddRatio);
				} else {
					realityRatio = pipeSonRatio.add(pipeSonAddRatio);
				}
				BigDecimal total = uskcAwardTotal.add(pipeAwardTotal);
				pipeAwardOne = pipeAwardOne.add(total.multiply(realityRatio));
			}
		}
		return pipeAwardOne;
	}

}
