package hry.cm5.remote;

import hry.bean.FrontPage;
import hry.bean.JsonResult;

import java.util.Map;

/**
 * @author zhouming
 * @date 2020/1/9 10:11
 * @Version 1.0
 */
public interface RemoteCmCustomerMinerService {

    public FrontPage findCustomerMinerList(Map<String, String> params);

    public JsonResult buyMinerGoods(Map<String, String> params);

    public FrontPage findMinerOrderList(Map<String, String> params);

    public JsonResult getMinerOrderDetails(String minerOrderId);
}
