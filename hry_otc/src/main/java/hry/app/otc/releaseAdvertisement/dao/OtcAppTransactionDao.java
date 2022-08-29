/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      denghf
 * @version:     V1.0 
 * @Date:        2018-06-25 18:06:51 
 */
package hry.app.otc.releaseAdvertisement.dao;

import hry.core.mvc.dao.base.BaseDao;
import hry.otc.manage.remote.model.releaseAdvertisement.OtcAppTransaction;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


/**
 * <p> OtcAppTransactionDao </p>
 * @author:         denghf
 * @Date :          2018-06-25 18:06:51  
 */
public interface OtcAppTransactionDao extends  BaseDao<OtcAppTransaction, Long> {

	BigDecimal adUserBy30(Long id,String coinCode);
	
	BigDecimal adUserAll(Long id,String coinCode);
	
	Integer allTradeCount(Map<String,Object> map);

	List<OtcAppTransaction> findPageBySql(Map<String,Object> map);

	List<OtcAppTransaction> findFrontPageBySql(Map<String,Object> map);

    List<Map<String,Object>> getTransactionSumById (Long id);

    List<OtcAppTransaction> findOtcAppTransactionListBySql (Map<String, Object> paramMap);

    void saveOtcLog (Long id);

    int getOrderSumBy30(Long id, String coinCode);

}
