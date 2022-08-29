/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      Wu Shuiming
 * @version:      V1.0 
 * @Date:        2016年3月28日 下午6:37:38
 */
package hry.trade.account.service.impl;

import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.exchange.account.model.ExDigitalmoneyAccount;
import hry.exchange.account.model.ExDmHotAccountRecord;
import hry.trade.account.service.ExDigitalmoneyAccountService;
import hry.trade.account.service.ExDmHotAccountRecordService;
import hry.util.QueryFilter;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 * TODO
 * </p>
 * 
 * @author: Wu Shuiming
 * @Date : 2016年3月28日 下午6:37:38
 */
@Service("exDmHotAccountRecordService")
public class ExDmHotAccountRecordServiceImpl extends
		BaseServiceImpl<ExDmHotAccountRecord, Long> implements
		ExDmHotAccountRecordService {

	@Resource(name = "exDigitalmoneyAccountService")
	public ExDigitalmoneyAccountService exDigitalmoneyAccountService;

	@Resource(name = "exDmHotAccountRecordDao")
	@Override
	public void setDao(BaseDao<ExDmHotAccountRecord, Long> dao) {
		super.dao = dao;
	}

	@Override
	public ExDmHotAccountRecord findByAccountId(Long id) {
		ExDigitalmoneyAccount exDigitalmoneyAccount = exDigitalmoneyAccountService
				.get(id);
		String accountNum = exDigitalmoneyAccount.getAccountNum();

		return null;
	}

	@Override
	public List<ExDmHotAccountRecord> findHotAccountRecordBytransactionNum(
			String transactionNum) {
		QueryFilter filter = new QueryFilter(ExDmHotAccountRecord.class);
		filter.addFilter("transactionNum=", transactionNum);
		List<ExDmHotAccountRecord> list = super.find(filter);
		
		return list;
	}

}
