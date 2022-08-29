/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2019-11-26 16:42:46 
 */
package hry.mall.merchant.service;

import hry.core.mvc.service.base.BaseService;
import hry.mall.merchant.model.Merchant;



/**
 * <p> MerchantService </p>
 * @author:         zhouming
 * @Date :          2019-11-26 16:42:46 
 */
public interface MerchantService  extends BaseService<Merchant, Long>{

    /*
    * 根据id获取商户信息
    * */
    public Merchant getMerchant(Long id);
}
