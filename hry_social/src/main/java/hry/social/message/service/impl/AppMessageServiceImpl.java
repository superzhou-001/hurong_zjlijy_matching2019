/**
 * Copyright:  北京互融时代软件有限公司
 *
 * @author: Wu Shuiming
 * @version: V1.0
 * @Date: 2016年5月30日 下午5:41:43
 */
package hry.social.message.service.impl;

import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.social.manage.remote.model.message.AppMessage;
import hry.social.message.service.AppMessageService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * <p> TODO</p>
 *
 * @author: Wu Shuiming
 * @Date :          2016年5月30日 下午5:41:43
 */
@Service("appMessageService")
public class AppMessageServiceImpl extends BaseServiceImpl<AppMessage,Long> implements AppMessageService {

    @Resource(name = "appMessageDao")
    @Override
    public void setDao(BaseDao<AppMessage,Long> dao) {
        super.dao = dao;
    }
}
