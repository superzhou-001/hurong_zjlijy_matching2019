/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2019-11-26 16:42:46 
 */
package hry.mall.merchant.service.impl;

import hry.mall.merchant.dao.MerchantDao;
import hry.mall.merchant.model.Merchant;
import hry.mall.merchant.service.MerchantService;
import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import javax.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p> MerchantService </p>
 * @author:         zhouming
 * @Date :          2019-11-26 16:42:46  
 */
@Service("merchantService")
public class MerchantServiceImpl extends BaseServiceImpl<Merchant, Long> implements MerchantService{
	
	@Resource(name="merchantDao")
	@Override
	public void setDao(BaseDao<Merchant, Long> dao) {
		super.dao = dao;
	}

	@Override
	public Merchant getMerchant(Long id) {
		return ((MerchantDao)dao).getMerchant(id);
	}
}
