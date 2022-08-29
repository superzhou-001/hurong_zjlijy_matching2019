/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      Wu Shuiming
 * @version:      V1.0 
 * @Date:        2016年3月28日 下午5:55:59
 */
package hry.trade.account.dao;

import java.util.List;

import hry.core.mvc.dao.base.BaseDao;
import hry.exchange.account.model.ExDmColdAccountRecord;
import hry.exchange.account.model.ExDmHotAccountRecord;

/**
 * <p>
 * TODO
 * </p>
 * 
 * @author: Wu Shuiming
 * @Date : 2016年3月28日 下午5:55:59
 */
public interface ExDmHotAccountRecordDao extends
		BaseDao<ExDmHotAccountRecord, Long> {
	 public  void insertRecord(List<ExDmHotAccountRecord> list);
}
