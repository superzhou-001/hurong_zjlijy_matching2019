/**
 * Copyright:  北京互融时代软件有限公司
 * @author:    Gao Mimi
 * @version:   V1.0 
 * @Date:      2015年10月10日  18:41:55
 */
package hry.app.web.app.dao;

import hry.core.mvc.dao.base.BaseDao;
import hry.otc.manage.remote.model.core.AppConfig;

import java.util.List;
import java.util.Map;

/**
 * <p> TODO</p>
 * @author:  Gao Mimi        
 * @Date :   2015年10月10日  18:41:55     
 */
public interface AppConfigDao extends BaseDao<AppConfig,Long>{
	
	/**
	 * 
	 * <p> 查询配置信息的类型</p>
	 * @author:         Zhang Xiaofang
	 * @param:    @return
	 * @return: List<AppConfig> 
	 * @Date :          2016年5月10日 上午9:55:34   
	 * @throws:
	 */
	List<AppConfig> findKey (Map<String, Object> map);

	/**
	 * <p> 获取所有配置信息的名字和值</p>
	 * @author:         Zhang Xiaofang
	 * @param:    @return
	 * @return: List<AppConfig>
	 * @Date :          2016年5月5日 上午9:26:38
	 * @throws:
	 */
	List<AppConfig> getConfig (Map<String, String> map);

}