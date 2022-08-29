/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2019-12-24 14:58:03 
 */
package hry.cm5.account.service;

import hry.bean.FrontPage;
import hry.core.mvc.service.base.BaseService;
import hry.cm5.account.model.Cm5AccountRecord;

import java.util.Map;


/**
 * <p> Cm5AccountRecordService </p>
 * @author:         zhouming
 * @Date :          2019-12-24 14:58:03 
 */
public interface Cm5AccountRecordService  extends BaseService<Cm5AccountRecord, Long>{

    FrontPage getAccountRecord (Map<String, String> params);
}
