package hry.mall.order.remote;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import hry.bean.FrontPage;
import hry.bean.JsonResult;
import hry.cm.enums.CmAccountRemarkEnum;
import hry.cm.remote.RemoteCmAccountService;
import hry.common.enums.RedisKeyEnum;
import hry.common.util.DateUtils;
import hry.core.shiro.PasswordHelper;
import hry.core.thread.ThreadPool;
import hry.interfaces.CommonSendUtil;
import hry.interfaces.commonRequest.KdniaoCommonRequest;
import hry.interfaces.commonResponse.CommonResponse;
import hry.mall.goods.model.CoinPay;
import hry.mall.goods.model.Goods;
import hry.mall.goods.model.GoodsSpec;
import hry.mall.goods.model.SaleGoodsConfig;
import hry.mall.goods.service.*;
import hry.mall.integral.model.CustomerIntegral;
import hry.mall.integral.model.IntegralDetail;
import hry.mall.integral.service.CustomerIntegralService;
import hry.mall.integral.service.IntegralConfigService;
import hry.mall.integral.service.IntegralDetailService;
import hry.mall.merchant.model.Merchant;
import hry.mall.merchant.service.MerchantService;
import hry.mall.order.model.*;
import hry.mall.order.model.vo.CoinVo;
import hry.mall.order.model.vo.OrderBalanceVo;
import hry.mall.order.model.vo.OrderRefundVo;
import hry.mall.order.service.*;
import hry.mall.platform.model.*;
import hry.mall.platform.service.*;
import hry.mall.retailstore.model.ActivityGoods;
import hry.mall.retailstore.service.ActivityGoodsService;
import hry.mall.retailstore.service.CouponDetailService;
import hry.manage.remote.RemoteMallService;
import hry.manage.remote.RemoteManageService;
import hry.manage.remote.model.AppDic;
import hry.manage.remote.model.User;
import hry.manage.remote.model.mall.AppAreaDicVo;
import hry.mq.producer.service.DealMsgUtil;
import hry.mq.producer.service.MessageProducer;
import hry.redis.common.utils.RedisService;
import hry.thread.GrowthRunnable;
import hry.thread.IntegralRunnable;
import hry.thread.RebateRunnable;
import hry.trade.redis.model.ExDigitalmoneyAccountRedis;
import hry.util.PageFactory;
import hry.util.QueryFilter;
import hry.util.SpringUtil;
import hry.util.UserRedisUtils;
import net.sf.json.JSONObject;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class RemoteOrderServiceImpl implements RemoteOrderService {

    @Resource
    public OrderGoodsService orderGoodsService;
    @Resource
    public AddressService addressService;
    @Resource
    public OrderAddressService orderAddressService;
    @Resource
    public RefundService refundService;
    @Resource
    public RefundGoodsService refundGoodsService;
    @Resource
    public OrderDaddressService orderDaddressService;
    @Resource
    private ShopLanguageService shopLanguageService;
    @Resource
    private EvaluateGoodsService evaluateGoodsService;
    @Resource
    private MallConfigService mallConfigService;
    @Resource
    public GoodsSpecService goodsSpecService;
    @Resource
    public GoodsService goodsService;
    @Resource
    public TransportService transportService;
    @Resource
    public TransportExtendService transportExtendService;
    @Resource
    public BlendPayService blendPayService;
    @Resource
    public OrderService orderService;
    @Resource
    public ThirdpayRecordService thirdpayRecordService;
   @Resource
    private MessageProducer messageProducer;
    @Resource
    private CartService cartService;
    @Resource
    private PaymentService paymentService;
    @Resource
    private SearchGoodsService searchGoodsService;
    @Resource
    private ExpressService expressService;
    @Resource
    private ActivityGoodsService activityGoodsService;
    @Resource
    private CouponDetailService couponDetailService;
    @Resource
    private IntegralConfigService integralConfigService;
    @Resource
    private IntegralDetailService integralDetailService;
    @Resource
    private CustomerIntegralService customerIntegralService;
    @Resource
    private RedisService redisService;
    @Resource
    private  CoinPayService coinPayService;
    @Resource
    private OrderCoinService  orderCoinService;
    @Resource
    private SaleGoodsConfigService saleGoodsConfigService;
    @Resource
    private MerchantService merchantService;



    @Override
    public FrontPage findByMemberIdAndStatus(Map<String, String> map) {
        try {
            Page<Order> page = PageFactory.getPage(map);
            QueryFilter queryFilter = new QueryFilter(Order.class);
            queryFilter.addFilter("memberId=", map.get("memberId"));

            if (null != map.get("orderState")) {
                queryFilter.addFilter("orderState=", map.get("orderState"));
            } else {
                queryFilter.addFilter("orderState!=", "60");
            }

            queryFilter.setOrderby("created desc");
            List<Order> list = orderService.find(queryFilter);

            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    QueryFilter query = new QueryFilter(OrderGoods.class);
                    query.addFilter("orderId=", list.get(i).getId());
                    List<OrderGoods> orderGoods = orderGoodsService.find(query);
                    if (!orderGoods.isEmpty()) {
                        for (int j = 0; j < orderGoods.size(); j++) {
                            //全球化
                            if (!"zh_CN".equals(map.get("locale"))) {
                                OrderGoods orderGoods1 = orderGoods.get(j);
                                String goodsName = shopLanguageService.getLanguageValue("goodsName", orderGoods1.getGoodsId(), map.get("locale").toString());
                                orderGoods1.setGoodsName(goodsName != "" ? goodsName : orderGoods1.getGoodsName());
                            }
                            if (orderGoods.get(j).getEvaluationStatus() == 0) {
                                list.get(i).setEvalsellerStatus(0);
                            }
                        }
                    }
                    list.get(i).setOrderGoodsList(orderGoods);
                }
            }
            return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JsonResult findNoPayOrder(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            //查询订单商品
            QueryFilter query = new QueryFilter(OrderGoods.class);
            query.addFilter("orderId=", map.get("orderId"));
            List<OrderGoods> orderGoods = orderGoodsService.find(query);
            order.setOrderGoodsList(orderGoods);
            //2、查询默认收货地址 （注:不应该查询默认地址，而是应该查询下单时填写的收货地址）
            Address address = this.getOrderAddress(map.get("memberId"),map.get("orderId"));

            /*List<Address> list = addressService.findByCustomerId(Long.valueOf(map.get("memberId")), Short.valueOf("1"));
            if (null != list && list.size() > 0) {
                address = list.get(0);
            } else {
                List<Address> list1 = addressService.findByCustomerId(Long.valueOf(map.get("memberId")), Short.valueOf("0"));
                if (null != list1 && list1.size() > 0) {
                    address = list1.get(0);
                }
            }*/

            MallConfig mallConfig = mallConfigService.get((long) 1);
            if (null != mallConfig) {
                hashMap.put("orderTime", mallConfig.getOrderTime());
            }
            hashMap.put("address", address);
            hashMap.put("order", order);
            return new JsonResult().setSuccess(true).setObj(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult cancelOrder(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            order.setOrderState(50);
            RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
            AppDic appDic=remoteMallService.findAppDicById(Long.parseLong(map.get("cancelCause")));
            if(null!=appDic){
            	order.setCancelCause(appDic.getName());
            }
            //TODO 修改商品库存和销量
            orderService.update(order);
            return new JsonResult().setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult deleteOrder(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            order.setOrderState(60);
            orderService.update(order);
            return new JsonResult().setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult updateFinishOrder(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            order.setOrderState(40);
            order.setFinnshedTime(new Date());
            orderService.update(order);
            return new JsonResult().setSuccess(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public FrontPage afterSales(Map<String, String> map) {
        try {
            Page<Order> page = PageFactory.getPage(map);
            QueryFilter queryFilter = new QueryFilter(Order.class);
            queryFilter.addFilter("memberId=", map.get("memberId"));
            queryFilter.addFilter("orderState_in", "20,40");
            queryFilter.addFilter("orderSort_notin", "7,8");//会员和创新订单不能申请退款
            //查询配置的自动结束交易后，不能申请售后：单位天
            MallConfig mallConfig = mallConfigService.get((long) 1);
            Integer orderFinish = mallConfig.getOrderFinish();
            Long returnEvidenceCount = mallConfig.getReturnEvidenceCount();
            List<Order> list = orderService.find(queryFilter);
            if (!list.isEmpty()) {
                for (int i = 0; i < list.size(); i++) {
                    QueryFilter query = new QueryFilter(OrderGoods.class);
                    query.addFilter("orderId=", list.get(i).getId());
                    query.addFilter("returnStatus=", 0);
                    List<OrderGoods> orderGoods = orderGoodsService.find(query);
                    list.get(i).setOrderGoodsList(orderGoods);
                    list.get(i).setOrderFinish(orderFinish);
                    list.get(i).setReturnEvidenceCount(returnEvidenceCount);
                }
            }
            return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
  @Override
    public FrontPage afterSalesRecord(Map<String, String> map) {
        try {
            Page<RefundGoods> page = PageFactory.getPage(map);
            QueryFilter filter = new QueryFilter(Refund.class);
            filter.addFilter("memberId=",map.get("memberId"));
            List<Refund> refunds = refundService.find(filter);
            for (int i = 0; i < refunds.size(); i++) {
                QueryFilter queryFilter = new QueryFilter(RefundGoods.class);
                queryFilter.addFilter("refundId=", refunds.get(i).getId());
                List<RefundGoods> refundGoods = refundGoodsService.find(queryFilter);
                refunds.get(i).setRefundGoodsList(refundGoods);
            }
            return new FrontPage(refunds, page.getTotal(), page.getPages(), page.getPageSize());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public JsonResult waitGoods(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {
            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            //查询订单商品
            QueryFilter query = new QueryFilter(OrderGoods.class);
            query.addFilter("orderId=", map.get("orderId"));
            List<OrderGoods> orderGoods = orderGoodsService.find(query);
            order.setOrderGoodsList(orderGoods);
            //查询默认地址
            QueryFilter filter = new QueryFilter(OrderAddress.class);
            query.addFilter("memberId=", map.get("memberId"));
            query.addFilter("orderId=", map.get("orderId"));
            OrderAddress orderAddress = orderAddressService.get(filter);
            if (null != orderAddress) {
               RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
               AppAreaDicVo  vo1=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_PROVINCE.getIndex(), orderAddress.getProvinceId());
               orderAddress.setProvinceName(vo1.getRegionName());
               AppAreaDicVo  vo2=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_CITY.getIndex(), orderAddress.getCityId());
               orderAddress.setCityName(vo2.getRegionName());
               AppAreaDicVo  vo3=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_COUNTY.getIndex(), orderAddress.getCountyId());
               orderAddress.setCountyName(vo3.getRegionName());
            }
            hashMap.put("orderAddress", orderAddress);
            hashMap.put("order", order);
            return new JsonResult().setSuccess(true).setObj(hashMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult addReturn(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        try {

            Order order = orderService.get(Long.parseLong(map.get("orderId")));
            //仅退款
            if(map.get("returnType").equals("0")){
                order.setRefundState(1);
            }else {
                order.setReturnState(1);
            }

            int returnNum =  order.getReturnNum() + Integer.parseInt(map.get("goodsNum"));
            order.setReturnNum(returnNum);


            //查询订单商品
            QueryFilter query = new QueryFilter(OrderGoods.class);
            query.addFilter("orderId=", map.get("orderId"));
            query.addFilter("goodsId=", map.get("goodsId"));
            OrderGoods orderGoods = orderGoodsService.get(query);
            orderGoods.setReturnStatus(1);//标记为已退货
            orderGoodsService.update(orderGoods);
            QueryFilter query1 = new QueryFilter(OrderGoods.class);
            query1.addFilter("orderId=", order.getId());
            query1.addFilter("returnStatus=", 0);
            List<OrderGoods> orderGoods1 = orderGoodsService.find(query1);
            if(null==orderGoods1 || orderGoods1.size()==0){
            	order.setOldOrderState(order.getOrderState());
                order.setOrderState(70);
            }
            // 订单中退货金额
            BigDecimal goodsAllPrice = orderGoods.getGoodsPayPrice().multiply(new BigDecimal(map.get("goodsNum")));
            //订单中原有订单金额 + 这次退款金额
            BigDecimal refundAmount = order.getRefundAmount().add(goodsAllPrice);
            order.setRefundAmount(refundAmount);
            orderService.update(order);
            //保存售后信息
            Refund refund = new Refund();
            refund.setOrderId(order.getId());
            refund.setOrderSn(order.getOrderSn());
            refund.setRefundSn(DateUtils.getDateStr("yyyyMMddHHmmssSSS"));
            refund.setMemberId(order.getMemberId());
            refund.setMemberName(map.get("memberName"));
            refund.setMemberMobile(map.get("memberMobile"));
            refund.setRefundState(1);
            refund.setRefundWay(map.get("refundWay"));
            refund.setRefundAmount(goodsAllPrice);
            //returnType  1为不用退货,2为需要退货
            if(map.get("returnType").equals("0")){
                refund.setReturnType(1);
            }else {
                refund.setReturnType(2);
            }
            //如果退款方式为退数币----消费账户退款也要计算下---2019-11-13 by luyue
            if(null!=refund.getRefundWay() &&("2".equals(refund.getRefundWay()) || "4".equals(refund.getRefundWay())) ){
                //1、退数币的话，计算下应退数币数量
                refund.setRefundCoinCode(order.getCoinCode());
                //2、获得后台配置，看是按照哪种方式计算汇率，按下单或者按照最新
        		//3、计算币的数量
        		BigDecimal coinRate=order.getCoinRate();//默认为下单是的汇率
            	List<MallConfig> list=mallConfigService.findAll();
        		if(null!=list && list.size()>0){
        			MallConfig  config=	list.get(0);
        			//如果是按最新汇率计算，则查询最新的计算
        				if(null!=config.getIsBuyRate() && 2==config.getIsBuyRate()){
        					  coinRate=goodsService.getCoinRate(order.getCoinCode());//新的实时汇率
        					  refund.setIsBuyRate(config.getIsBuyRate());
        				}
        		}
        		refund.setRefundCoinRate(coinRate.setScale(10, BigDecimal.ROUND_HALF_UP));
        		refund.setRefundCoinCount(refund.getRefundAmount().divide(coinRate, 10, BigDecimal.ROUND_HALF_UP));//虚拟币数量
            }
            refund.setUserName(order.getUserName());
            refund.setGoodsState(1);
            RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
            AppDic appDic=remoteMallService.findAppDicById(Long.parseLong(map.get("buyerMessage")));
            if(null!=appDic){
            	refund.setBuyerMessage( appDic.getName());
            }
            refund.setReasonInfo(map.get("reasonInfo"));
            refund.setPicInfo(map.get("picInfo"));
            //refund.setIsFreight(1);
            refundService.save(refund);

                //保存退货商品
                RefundGoods refundGoods = new RefundGoods();
                refundGoods.setOrderId(order.getId());
                refundGoods.setOrderSn(order.getOrderSn());
                refundGoods.setRefundId(refund.getId());
                refundGoods.setGoodsId(orderGoods.getGoodsId());
                refundGoods.setGoodsSpecId(orderGoods.getGoodsSpecId());
                refundGoods.setGoodsName(orderGoods.getGoodsName());
                refundGoods.setBrandId(orderGoods.getBrandId());
                refundGoods.setBrandName(orderGoods.getBrandName());
                refundGoods.setSpecInfo(orderGoods.getSpecInfo());
                refundGoods.setGoodsNum(orderGoods.getGoodsNum());
                refundGoods.setGoodsImage(orderGoods.getGoodsImage());
                refundGoods.setRefundAmount(goodsAllPrice);
                refundGoods.setGoodsReturnnum(Integer.parseInt(map.get("goodsNum")));
                refundGoods.setGoodsPrice(orderGoods.getGoodsPrice());
                refundGoods.setGoodsPayPrice(orderGoods.getGoodsPayPrice());
                refundGoods.setGoodsAllPrice(goodsAllPrice);
                refundGoods.setSpecGoodsSerial(orderGoods.getSpecGoodsSerial());
                refundGoodsService.save(refundGoods);

            return new JsonResult().setSuccess(true).setCode(refund.getId()+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new JsonResult().setSuccess(false);
    }

    @Override
    public JsonResult checkRecord(Map<String, String> map) {
        Map<String, Object> hashMap = new HashMap<String, Object>();
        Refund refund = refundService.get(Long.parseLong(map.get("returnId")));
        QueryFilter query = new QueryFilter(OrderDaddress.class);
        query.addFilter("orderId=", map.get("returnId"));
        query.addFilter("type=", 1);
        OrderDaddress orderDaddress = orderDaddressService.get(query);
        if (null != orderDaddress) {
            RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
            AppAreaDicVo  vo1=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_PROVINCE.getIndex(), orderDaddress.getProvinceId());
            orderDaddress.setProvinceName(vo1.getRegionName());
            AppAreaDicVo  vo2=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_CITY.getIndex(), orderDaddress.getCityId());
            orderDaddress.setCityName(vo2.getRegionName());
            AppAreaDicVo  vo3=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_COUNTY.getIndex(), orderDaddress.getCountyId());
            orderDaddress.setCountyName(vo3.getRegionName());
            hashMap.put("orderDaddress", orderDaddress);
        }
        hashMap.put("refund", refund);
        return new JsonResult().setSuccess(true).setObj(hashMap);
    }

    @Override
    public FrontPage evaluateGoods(Map<String, String> map) {
        Page<OrderGoods> page = PageFactory.getPage(map);
      /*  QueryFilter queryFilter = new QueryFilter(Order.class);
        queryFilter.addFilter("memberId=", map.get("memberId"));
        queryFilter.addFilter("orderState=", 40);
        List<Order> list = orderService.find(queryFilter);
        List<OrderGoods> orderGoodsList = new ArrayList<>();
        //查询订单商品
        if(null != list &&list.size()>0){
            for (int i = 0; i <list.size() ; i++) {
                QueryFilter query = new QueryFilter(OrderGoods.class);
                query.addFilter("evaluationStatus=",0);
                query.addFilter("memberId=", map.get("memberId"));
                query.addFilter("orderId=", list.get(i).getId());
                List<OrderGoods> orderGoods = orderGoodsService.find(query);
                //查询订单中未评价的商品
                if(null != orderGoods && orderGoods.size()>0){
                    for (int j = 0; j <orderGoods.size() ; j++) {
                        orderGoodsList.add(orderGoods.get(j));
                    }
                }
            }
            System.out.println(orderGoodsList.size());*/
         Map<String, String> queryMap =new HashMap<String,String>();
         queryMap.put("memberId",  map.get("memberId"));
         queryMap.put("evaluationStatus",  "0");
         queryMap.put("orderState",  "40");
         List<OrderGoods> orderGoodsList=  orderGoodsService.findBySql(queryMap);
         if(null!=orderGoodsList && orderGoodsList.size()>0){
            return new FrontPage(orderGoodsList, page.getTotal(), page.getPages(), page.getPageSize());
        }
        return new FrontPage(new ArrayList(), page.getTotal(), page.getPages(), page.getPageSize());


    }
    @Override
    public FrontPage alreadyEvaluateGoods(Map<String, String> map) {
        Page<EvaluateGoods> page = PageFactory.getPage(map);
        QueryFilter queryFilter = new QueryFilter(EvaluateGoods.class);
        queryFilter.addFilter("memberId=", map.get("memberId"));
        List<EvaluateGoods> list = evaluateGoodsService.find(queryFilter);
        //查询订单商品
        if(null != list){
            for (int i = 0; i <list.size() ; i++) {
                OrderGoods orderGoods = orderGoodsService.get(list.get(i).getOrderGoodsId());
                list.get(i).setOrderGoods(orderGoods);
            }
            return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
        }
        return new FrontPage(new ArrayList(), page.getTotal(), page.getPages(), page.getPageSize());

    }

    @Override
    public JsonResult releaseEvaluate(Map<String, String> map) {
        OrderGoods orderGoods = orderGoodsService.get(Long.parseLong(map.get("orderGoodsId")));
        //判断该订单是否还有未评论的商品
        QueryFilter filter = new QueryFilter(OrderGoods.class);
        filter.addFilter("orderId=",orderGoods.getOrderId());
        filter.addFilter("evaluationStatus=",0);
        List<OrderGoods> list = orderGoodsService.find(filter);
        //修改评论状态
        orderGoods.setEvaluationStatus(1);
        orderGoods.setEvaluationTime(new Date());
        orderGoodsService.update(orderGoods);
        if(list.size()==1){
            Order order = orderService.get(orderGoods.getOrderId());
            order.setEvaluationStatus(1);
            order.setEvaluationTime(new Date());//订单评价时间
            orderService.update(order);
        }
        //添加评论数据
        EvaluateGoods evaluateGoods = new EvaluateGoods();
        evaluateGoods.setOrderGoodsId(orderGoods.getId());
        evaluateGoods.setOrderId(orderGoods.getOrderId());
        evaluateGoods.setOrderSn(orderGoods.getOrderSn());
        evaluateGoods.setGoodsId(orderGoods.getGoodsId());
        evaluateGoods.setGoodsName(orderGoods.getGoodsName());
        evaluateGoods.setGoodsPrice(orderGoods.getGoodsPrice());
        evaluateGoods.setMemberId(Long.parseLong(map.get("memberId")));
        evaluateGoods.setUserName(map.get("userName"));
        evaluateGoods.setScores(Integer.parseInt(map.get("scores")));
        evaluateGoods.setContent(map.get("content"));
        evaluateGoods.setImage(map.get("image"));
        evaluateGoods.setSpecInfo(orderGoods.getSpecInfo());
        evaluateGoodsService.save(evaluateGoods);
        //实名认证发放积分信息---start
        String accountKey = "taskAccount";
        String taskKey = "productReviews";
        ThreadPool.exe(new IntegralRunnable((long) 0, Long.parseLong(map.get("memberId")), accountKey, taskKey, 2));
        //实名认证发放积分信息---end
        return new JsonResult().setSuccess(true);
    }

    @Override
    public JsonResult orderCancel() {
    
        // 获取订单取原因
        RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
        List<AppDic> list=remoteMallService.findListByPkey("orderCancel");
        return new JsonResult().setSuccess(true).setObj(list);
    }

    @Override
    public JsonResult returnCause() {
        // 获取订单退货原因
        RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
        List<AppDic> list=remoteMallService.findListByPkey("returnCause");
        return new JsonResult().setSuccess(true).setObj(list);
    }

    @Override
	public JsonResult toBalance(Map<String, String> map) {
		// TODO Auto-generated method stub
		Map<String, Object> objMap=new HashMap<String,Object>();
		String memberId=map.get("memberId");
		//1、用户选择的商品，判断是否库存充足、是否价格更改
	//	JsonResult result=this.validateGoods(map);
		JsonResult result=this.validateGoods1(map);
		//如果验证成功，则计算运费信息
		if(result.getSuccess()){
		 //2、计算商品总金额
		  //商品金额拼串
			String moneys=map.get("moneys");
			BigDecimal sumMoney=new BigDecimal("0");
			//商品数量拼串
			String counts=map.get("counts");
			String [] countArr =counts.split(",");
			String [] moneyArr =moneys.split(",");
			for(int i=0;i<moneyArr.length;i++){
				sumMoney=sumMoney.add(new BigDecimal(moneyArr[i]).multiply(new BigDecimal(countArr[i])));
			}
			objMap.put("sumMoney", sumMoney);
		 //3、查询默认收货地址
			Address  address=null;
			List<Address>  list=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("1"));
			if(null!=list &&list.size()>0){
				address=list.get(0);
			}else{
				List<Address>  list1=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("0"));
				if(null!=list1 &&list1.size()>0){
					address=list1.get(0);
				}
			}
		//4、根据收货地址、商品、下单数量计算运费
		  if(null!=address && !"".equals(address)){
			  Map<String, String> cmap=new HashMap<String, String>();
			  cmap.put("goodsIds",map.get("goodsIds") );
			  cmap.put("counts",map.get("counts"));
			  cmap.put("provinceCode",address.getProvinceId());
			  cmap.put("sumMoney",sumMoney.toString());
			  BigDecimal fee=this.calculateTransfee(cmap);
			  objMap.put("address", address);
			  objMap.put("fee", fee);
			}else{
				objMap.put("address", null);
				objMap.put("fee", new BigDecimal("0"));
			}
			//5、查询混合支付的设置
		  List<BlendPay> blist=blendPayService.findAll();
		  if(null!=blist && blist.size()>0){
			  objMap.put("blendPay", blist.get(0)) ;
		  }else{
			  objMap.put("blendPay", null) ;
		  }

		  //6、查询人民币的支付通道
		QueryFilter filter=new QueryFilter(Payment.class);
		filter.addFilter("paymentState=", Integer.valueOf("1"));
		List<Payment> plist=paymentService.find(filter);
		if(null!=plist && plist.size()>0){
			objMap.put("paymentMay", plist) ;
		}else{
			objMap.put("paymentMay", null) ;
		}
		result.setSuccess(true).setObj(objMap).setMsg("caozuochenggong");
		}
		return result;
	}


	//验证下单商品是否有效，是否库存充足、是否价格更改
	public  JsonResult  validateGoods(Map<String, String> map){
		//商品id拼串
		String goodsIds=map.get("goodsIds");
		//商品数量拼串
		String counts=map.get("counts");
		//商品规格拼串
		String goodsSpecIds=map.get("goodsSpecIds");
		//商品金额拼串
		String moneys=map.get("moneys");
		//订单类别
	    String orderSort=map.get("orderSort");
		String [] specArr =goodsSpecIds.split(",");
		String [] countArr =counts.split(",");
		String [] moneyArr =moneys.split(",");
		String [] goodsArr =goodsIds.split(",");
		StringBuffer countString=new StringBuffer();
		StringBuffer moneyString=new StringBuffer();
		if(null!=orderSort && "2".equals(orderSort)){
			for(int i=0;i<goodsArr.length;i++){
				Goods goods=goodsService.get(Long.valueOf(goodsArr[i]));
				if(null!=goods && goods.getSpecialType()!=2){
					return new JsonResult().setSuccess(false).setMsg("feiqianggoushangpin");	//非抢购商品不能进行抢购
				}
			}
		}
		Boolean  flag=true;
		JsonResult result=new JsonResult();
		//循环遍历用户选择的商品，判断是否库存充足、是否价格更改
		for(int i=0;i<specArr.length;i++){
			GoodsSpec goodspec=goodsSpecService.get(Long.valueOf(specArr[i]));
			if(null!=goodspec &&!"".equals(goodspec)){
			//1、库存不足
			if(goodspec.getSpecGoodsStorage()<Integer.valueOf(countArr[i])){
				flag=false;
				Goods good=goodsService.get(goodspec.getGoodsId());
				String goodsName=good.getGoodsName();
				//查询国际化的商品名称
				if (!"zh_CN".equals(map.get("locale"))){
					Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
					if (goodsDetail != null)
					    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
				}
				countString.append(goodsName).append(",");
			}
			//2、价格不一致
			if(null!=orderSort && "2".equals(orderSort)){
				//抢购的积分兑换商品，比较积分兑换
				if(goodspec.getIntegralCount().compareTo(new BigDecimal(moneyArr[i]))!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(map.get("locale"))){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                        if (goodsDetail != null)
						    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}else{
				if(goodspec.getSpecGoodsPrice().compareTo(new BigDecimal(moneyArr[i]))!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(map.get("locale"))){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                        if (goodsDetail != null)
						    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}
		  }else{
			  flag=false;
				Goods good=goodsService.get(Long.valueOf(goodsArr[i]));
				String goodsName=good.getGoodsName();
				//查询国际化的商品名称
				if (!"zh_CN".equals(map.get("locale"))){
					Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                  if (goodsDetail != null)
					    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
				}
				moneyString.append(goodsName).append(",");

		  }
		}
		StringBuffer msg=new StringBuffer();
		if(!flag){
		  if(countString.length()>0){
			  msg.append(countString.substring(0, countString.length()-1)).append("&&&kucunbuzuchongxuan");//库存不足,重新选择商品
			//  msg.append("kucunbuzuchongxuan");//库存不足,重新选择商品
		  }
		  if(moneyString.length()>0){
			   msg.append(moneyString.substring(0, moneyString.length()-1)).append("&&&jiagebiandongchongxuan");//"价格变动,重新选择商品
			//	 msg.append("jiagebiandongchongxuan");//"价格变动,重新选择商品
			  }
		  result.setCode("1111");
		}
		return result.setSuccess(flag).setMsg(msg.toString());
	}
	//验证下单商品是否有效，是否库存充足、是否价格更改----2019-05-21系统添加限时抢购，抢购商品一起结算，修改验证方法
	public  JsonResult  validateGoods1(Map<String, String> map){
        JsonResult result=new JsonResult();
		//商品id拼串
		String goodsIds=map.get("goodsIds");
        String [] goodsArr =goodsIds.split(",");
        //李金沅项目只能单商品下单，不然无法支持其复杂的业务需求----支持数币混合支付，按商品返利等等
        if(goodsArr.length>1){
           return result.setSuccess(false).setMsg("suoxuanshangpinzhifufangshibutong");
        }
        //如果是预售产品，判断是否在预售时间段内
        Goods ygood=goodsService.get(Long.valueOf(goodsArr[0]));
        if(1==ygood.getIsAdvance()){//预售商品
            QueryFilter filte1=new QueryFilter(SaleGoodsConfig.class);
            filte1.addFilter("goodsId=",ygood.getId());
            SaleGoodsConfig config=saleGoodsConfigService.get(filte1);
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            String date1=sdf.format(new Date());
            String sdate=sdf.format(config.getDepositStartTime());
            String edate=sdf.format(config.getDepositEndTime());
            //当前日期小于开始日期或者当前日期大于结束日期，活动无效
            if(date1.compareTo(sdate)<0 || date1.compareTo(edate)>0){
                return result.setSuccess(false).setMsg("feizhifudingjinshjian");
            }
        }
		//商品数量拼串
		String counts=map.get("counts");
		//商品规格拼串
		String goodsSpecIds=map.get("goodsSpecIds");
		//商品金额拼串
		String moneys=map.get("moneys");
		//订单类别
	    String orderSort=map.get("orderSort");
	  //活动商品id拼串
	 	String activityGoodsIds=map.get("activityGoodsIds");
		String [] specArr =goodsSpecIds.split(",");
		String [] countArr =counts.split(",");
		String [] moneyArr =moneys.split(",");

		String [] activityGoodsArr=null;
		if(null!=activityGoodsIds && !"".equals(activityGoodsIds)){
		  activityGoodsArr =activityGoodsIds.split(",");
		}
		StringBuffer countString=new StringBuffer();
		StringBuffer moneyString=new StringBuffer();
		if(null!=orderSort && "2".equals(orderSort)){
			for(int i=0;i<goodsArr.length;i++){
				Goods goods=goodsService.get(Long.valueOf(goodsArr[i]));
				if(null!=goods && goods.getSpecialType()!=2){
					return new JsonResult().setSuccess(false).setMsg("feiqianggoushangpin");	//非抢购商品不能进行抢购
				}
			}
		}
		Boolean  flag=true;

		//循环遍历用户选择的商品，判断是否库存充足、是否价格更改
		for(int i=0;i<specArr.length;i++){
			//如果活动商品id不为空，则要判断活动内存是否充足
			if(null!=activityGoodsArr && activityGoodsArr.length>0 &&  null!=activityGoodsArr[i]  &&!"".equals(activityGoodsArr[i])){
				ActivityGoods agoods=activityGoodsService.get(Long.valueOf(activityGoodsArr[i]));
				//如果当前活动商品有效
				if(activityGoodsService.isValid(agoods)){
					//如果活动库存不足，则直接为库存不足，跳出本次循环、继续验证下个商品
					if(agoods.getActivityStorage()<Integer.valueOf(countArr[i])){
						flag=false;
						Goods good=goodsService.get(agoods.getGoodsId());
						String goodsName=good.getGoodsName();
						//查询国际化的商品名称
						if (!"zh_CN".equals(map.get("locale"))){
							Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
							if (goodsDetail != null)
							    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
						}
						countString.append(goodsName).append(",");
						continue;
					}
				}

			}
			GoodsSpec goodspec=goodsSpecService.get(Long.valueOf(specArr[i]));
			if(null!=goodspec &&!"".equals(goodspec)){
			//1、库存不足
			if(goodspec.getSpecGoodsStorage()<Integer.valueOf(countArr[i])){
				flag=false;
				Goods good=goodsService.get(goodspec.getGoodsId());
				String goodsName=good.getGoodsName();
				//查询国际化的商品名称
				if (!"zh_CN".equals(map.get("locale"))){
					Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
					if (goodsDetail != null)
					    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
				}
				countString.append(goodsName).append(",");
			}
			//2、价格不一致
			if(null!=orderSort && "2".equals(orderSort)){
				//抢购的积分兑换商品，比较积分兑换
				if(goodspec.getIntegralCount().compareTo(new BigDecimal(moneyArr[i]))!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(map.get("locale"))){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                        if (goodsDetail != null)
						    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}else{
				BigDecimal price=goodspec.getSpecGoodsPrice();

				//如果活动商品id不为空，则要判断
				if(null!=activityGoodsArr && activityGoodsArr.length>0 && null!=activityGoodsArr[i] &&!"".equals(activityGoodsArr[i])){
					ActivityGoods agoods=activityGoodsService.get(Long.valueOf(activityGoodsArr[i]));
					//如果抢购活动有效
					if(activityGoodsService.isValid(agoods)){
						price=agoods.getActivityPrice();
					}
				}
				if(price.compareTo(new BigDecimal(moneyArr[i]))!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(map.get("locale"))){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                        if (goodsDetail != null)
						    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}
		  }else{
			  flag=false;
				Goods good=goodsService.get(Long.valueOf(goodsArr[i]));
				String goodsName=good.getGoodsName();
				//查询国际化的商品名称
				if (!"zh_CN".equals(map.get("locale"))){
					Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), map.get("locale").toString());
                  if (goodsDetail != null)
					    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
				}
				moneyString.append(goodsName).append(",");

		  }
		}
		StringBuffer msg=new StringBuffer();
		if(!flag){
		  if(countString.length()>0){
			  msg.append(countString.substring(0, countString.length()-1)).append("&&&kucunbuzuchongxuan");//库存不足,重新选择商品
			//  msg.append("kucunbuzuchongxuan");//库存不足,重新选择商品
		  }
		  if(moneyString.length()>0){
			   msg.append(moneyString.substring(0, moneyString.length()-1)).append("&&&jiagebiandongchongxuan");//"价格变动,重新选择商品
			//	 msg.append("jiagebiandongchongxuan");//"价格变动,重新选择商品
			  }
		  result.setCode("1111");
		}
		return result.setSuccess(flag).setMsg(msg.toString());
	}
	//根据收货地区（省）、商品总价值、商品数量计算运费
	@Override
	public  BigDecimal  calculateTransfee(Map<String, String> map){
		BigDecimal fee=new BigDecimal("0");
		//商品id拼串
		String goodsIds=map.get("goodsIds");
		//商品数量拼串
		String counts=map.get("counts");
		//收货地址所在省
	    String provinceCode=map.get("provinceCode");
	   //商品总金额
	    String ssumMoney=map.get("sumMoney");
		String [] goodsIdsArr =goodsIds.split(",");
		String [] countArr =counts.split(",");
		//1、获得系统的配置的包邮金额
		BigDecimal baoMoney=new BigDecimal("0");//平台包邮金额
		List<MallConfig> list=mallConfigService.findAll();
		if(null!=list && list.size()>0){
			MallConfig  config=	list.get(0);
			baoMoney= new BigDecimal(config.getPackageMail());
		}
		//2、计算商品总金额和包邮金额比较，如果大于则运费为0
		BigDecimal sumMoney=new BigDecimal(ssumMoney);
		if(sumMoney.compareTo(baoMoney)>0){
			return fee;
		}else{
			//3、计算下单商品总件数
			Integer sumCount=0;
			for(int i=0;i<countArr.length;i++){
				sumCount=sumCount+(Integer.valueOf(countArr[i]));
			}
		   //4、如果小于包邮金额，则分别计算按照所有商品的运费模板计算运费，取最高值
			for(int i=0;i<goodsIdsArr.length;i++){
				BigDecimal transFee=new BigDecimal("0");
				Goods goods=goodsService.get(Long.valueOf(goodsIdsArr[i]));
				Transport transport=transportService.get(goods.getTransportId());
				//5、查询运费模板
				if(null!=transport && !"".equals(transport)){
					//6、如果卖家承担运费，则直接计算下一个
					if(null!=transport.getType() &&2==transport.getType()){
						continue;
					}//7、如果买家承担，则计算运费
					else if(1==(transport.getType())){
						transFee=calculateFeeByConfig(provinceCode,sumCount,transport.getId());
						if(transFee.compareTo(fee)>0){
							//计算的运费如果大于已赋值的运费，则本单运费为最新的，从而保障取运费的最大值
							fee=transFee;
						}
					}
				}
			}
		}
	    return fee;
	}
	/**
	 * 根据省编码、商品数量、运费模板计算运费
	 * @param provinceCode
	 * @param sumCount
	 * @param transportId
	 * @return
	 */
	public  BigDecimal  calculateFeeByConfig(String provinceCode,Integer sumCount,Long transportId ){
		BigDecimal fee=new BigDecimal("0");
		//1、查询是否在特殊配置中
		QueryFilter filter = new QueryFilter(TransportExtend.class);
		filter.addFilter("transportId=", transportId);
		filter.addFilter("areaId_LIKE", "%"+provinceCode+"%");
		List<TransportExtend>  list=transportExtendService.find(filter);
		TransportExtend extend=null;
		if(null!=list && list.size()>0){
			 extend=list.get(0);
		}else{
			//2、如果没有特殊配置，则查询全国
			QueryFilter filter1 = new QueryFilter(TransportExtend.class);
			filter1.addFilter("transportId=", transportId);
			filter1.addFilter("areaName_LIKE", "%全国%");
			List<TransportExtend>  list1=transportExtendService.find(filter1);
			if(null!=list1 && list1.size()>0){
				extend=list1.get(0);
			}
		}
		//3、查询到相应配置，进行计算
		if(null!=extend){
			//4、商品数量小于首件数量，则运费为首付
			if(sumCount<=extend.getSnum()){
				fee=extend.getSprice();
			}else{
			//5、大于首件数量，则（商品总数量-首件数量）/递增件数 *续件运费
			  Integer chao=sumCount-extend.getSnum();
			  Integer bei=(chao%extend.getXnum()==0?chao/extend.getXnum():chao/extend.getXnum()+1);
			  fee=extend.getXprice().multiply(new BigDecimal(bei.toString())).add(extend.getSprice()) ;
			}
		}
		return fee;
	}
	/**
     * 李金沅项目定制开发---by luyue
     */
    @Override
    public JsonResult toBalance1(Map<String, String> map) {
        Map<String, Object> objMap=new HashMap<String,Object>();
        String memberId=map.get("memberId");
        //1、用户选择的商品，判断是否库存充足、是否价格更改
        //	JsonResult result=this.validateGoods(map);
        JsonResult result=this.validateGoods1(map);
        //如果验证成功，则计算运费信息
        if(result.getSuccess()){
            //2、计算商品总金额
            //商品金额拼串
            String moneys=map.get("moneys");
            BigDecimal sumMoney=new BigDecimal("0");
            //商品数量拼串
            String counts=map.get("counts");
            String [] countArr =counts.split(",");
            String [] moneyArr =moneys.split(",");
            for(int i=0;i<moneyArr.length;i++){
                sumMoney=sumMoney.add(new BigDecimal(moneyArr[i]).multiply(new BigDecimal(countArr[i])));
            }
            objMap.put("sumMoney", sumMoney);
            //3、查询默认收货地址
            Address  address=null;
            List<Address>  list=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("1"));
            if(null!=list &&list.size()>0){
                address=list.get(0);
            }else{
                List<Address>  list1=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("0"));
                if(null!=list1 &&list1.size()>0){
                    address=list1.get(0);
                }
            }
            objMap.put("address", address);
            objMap.put("fee", new BigDecimal("0"));//运费默认为0
            //4、根据规格id查询商品的支付方式及个数
            String goodsIds=map.get("goodsIds");
            String [] goodsArr =goodsIds.split(",");
            Goods good=goodsService.get(Long.valueOf(goodsArr[0]));

            String goodsSpecIds=map.get("goodsSpecIds");
            String [] specArr =goodsSpecIds.split(",");
            String specId=specArr[0];
            QueryFilter filter=new QueryFilter(CoinPay.class);
            filter.addFilter("goodsSpecId=",specId);
            CoinPay pay=coinPayService.get(filter);//支付配置

            List<CoinVo> olist=new ArrayList<CoinVo>();//单币种支付

            BigDecimal rate=new BigDecimal("1");//支付比例，默认为1
            //如果是预支付商品，要计算定金的数量
            if(null!=good.getIsAdvance() && 1==good.getIsAdvance()){
                QueryFilter f=new QueryFilter(SaleGoodsConfig.class);
                f.addFilter("goodsId=",good.getId());
                SaleGoodsConfig sale=saleGoodsConfigService.get(f);
                rate=sale.getDepositRatio().divide(new BigDecimal("100"));
            }
            String [] oarr =pay.getSinglePayMent().split(",");
            BigDecimal buycount=new BigDecimal(countArr[0]);//购买数量
            for(int i=0;i<oarr.length;i++){
                String [] sarry=oarr[i].split("_");
                String coinCode=sarry[0];//币种
                BigDecimal count=new BigDecimal(sarry[1]).multiply(rate).multiply(buycount);//个数
                String path=orderService.findPicturePath(coinCode);//图片路径
                CoinVo vo=new CoinVo();
                vo.setCoinCode(coinCode);
                vo.setCount(count);
                vo.setPicturePath(path);
                olist.add(vo);
            }
            objMap.put("olist",olist);
            //如果开启了组合支付，则查询组合列表
                if(1==pay.getIsMorePayMent()){
                List<CoinVo> mlist=new ArrayList<CoinVo>();//组合币种支付
                String [] marr =pay.getMorePayMent().split(",");
                for(int i=0;i<marr.length;i++){
                    String [] sarry=marr[i].split("_");
                    String coinCode=sarry[0];//币种
                    BigDecimal count=new BigDecimal(sarry[1]).multiply(rate).multiply(buycount);//个数
                    String path=orderService.findPicturePath(coinCode);//图片路径
                    CoinVo vo=new CoinVo();
                    vo.setCoinCode(coinCode);
                    vo.setCount(count);
                    vo.setPicturePath(path);
                    mlist.add(vo);
                }
                objMap.put("mlist",mlist);
            }else{
                objMap.put("mlist",null);
            }
            result.setSuccess(true).setObj(objMap).setMsg("caozuochenggong");
        }
        return result;
    }

    @Override
    public JsonResult payCoinConfirm1(Map<String, String> map) {
        JsonResult result=new JsonResult();
        String lock = "payCoinConfirm:" + map.get("memberId");
        if (!redisService.lock(lock)) {
            result.setSuccess(false).setMsg("req_error"); //操作失败
            return result;
        }
        try {
            RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
            User user=remoteManageService.findUserById(Long.valueOf(map.get("memberId")));
            BigDecimal ling=new BigDecimal("0");
            //1、验证支付密码
            if(null!=user){
                //如果支付密码密码为空，则提示用户去维护支付密码
       /*         if(null==user.getAccountPassWord() || "".equals(user.getAccountPassWord())){
                    result.setSuccess(false).setMsg("zhifumimakong"); //支付密码为空,请先去维护支付密码
                    return result;
                }
                PasswordHelper passwordHelper = new PasswordHelper();
                String encryString = passwordHelper.encryString(map.get("accountPassWord"), user.getSalt());
                if(!encryString.equals(user.getAccountPassWord())){
                    result.setSuccess(false).setMsg("zhifumimacuowu"); //支付密码错误
                    return result;
                }*/
                //2、查询订单
                Order order=orderService.get(Long.valueOf(map.get("orderId")));
                if(null==order){
                    result.setSuccess(false).setMsg("malldingdanbucunzai"); //订单不存在
                    return result;
                }
                if(20==order.getOrderState() && 1==order.getPaymentState()){
                    result.setSuccess(false).setMsg("dingdanyizhifu"); //该订单已支付，无需重复支付
                    return result;
                }
                if(1==order.getCoinPayStatus() ){
                    result.setSuccess(false).setMsg("shubiyizhifuchengong"); //该订单数币已支付成功，无需重复支付
                    return result;
                }
                if(50==order.getRmbPayStatus() ){
                    result.setSuccess(false).setMsg("dingdanyiguanbi"); //该订单已关闭，无法支付
                    return result;
                }
                order.setLocal(map.get("locale"));
                //3、验证 订单中的商品价格和库存是否充足
                result=this.validateOrderGoods(order);
                if(!result.getSuccess()){
                    return result;
                }
                //4、标记是否是支付定金
                String first="0";//全款
                if(1==order.getIsAdvance() && (null==order.getAdvancePayTime() || "".equals(order.getAdvancePayTime()))){//支付定金
                    first="1";//定金
                }else if(1==order.getIsAdvance() && null !=order.getAdvancePayTime()){
                    first="2";//尾款
                }
                //5、如果非全款，则验证支付时间
                if(!"0".equals(first)){
                    //查询下单商品
                    QueryFilter filter = new QueryFilter(OrderGoods.class);
                    filter.addFilter("orderId=", order.getId());
                    List<OrderGoods>  list=orderGoodsService.find(filter);
                    //查询支付时间配置
                    QueryFilter filte1=new QueryFilter(SaleGoodsConfig.class);
                    filte1.addFilter("goodsId=",list.get(0).getGoodsId());
                    SaleGoodsConfig config=saleGoodsConfigService.get(filte1);
                    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
                    String date1=sdf.format(new Date());
                    if("1".equals(first)){//支付定金
                        String sdate=sdf.format(config.getDepositStartTime());
                        String edate=sdf.format(config.getDepositEndTime());
                        //当前日期小于开始日期或者当前日期大于结束日期，活动无效
                        if(date1.compareTo(sdate)<0 || date1.compareTo(edate)>0){
                            return result.setSuccess(false).setMsg("feizhifudingjinshjian");
                        }
                    }else{//支付尾款
                        String sdate=sdf.format(config.getFinalStartTime());
                        String edate=sdf.format(config.getFinalEndTime());
                        //当前日期小于开始日期或者当前日期大于结束日期，活动无效
                        if(date1.compareTo(sdate)<0 || date1.compareTo(edate)>0){
                            return result.setSuccess(false).setMsg("feizhifuweikuanshjian");
                        }
                    }
                }
                //6、查询订单下的币支付列表
                QueryFilter filter=new QueryFilter(OrderCoin.class);
                filter.addFilter("orderId=",order.getId());
                List<OrderCoin> list=orderCoinService.find(filter);

                for(OrderCoin coin:list){
                    //7、验证账户币余额是否充足
                    BigDecimal count=coin.getUnpaidCount();//默认是未支付币个数--全款和尾款都是未支付个数
                    if("1".equals(first)){//支付定金
                        count=coin.getAdvanceCount();
                    }
                    Map<String, String> vmap=new HashMap<String,String>();
                    vmap.put("memberId", order.getMemberId().toString());
                    vmap.put("coinCode", coin.getCoinCode());
                    vmap.put("coinCount", count.toString());
                    result=this.validateExaccount(vmap);
                    if(!result.getSuccess()){
                        return result;
                    }
                }
                if(7==order.getOrderSort() && 1!=user.getIsMember()){
                    //如果是会员商品订单，并且用户不是会员，则更改用户会员状态
                    remoteManageService.updatCustomer(order.getMemberId());
                }
                //循环扣币
                for(OrderCoin coin:list){
                    BigDecimal count=coin.getUnpaidCount();//默认是未支付币个数--全款和尾款都是未支付个数
                    if("1".equals(first)){//支付定金
                        count=coin.getAdvanceCount();
                    }
                    coin.setPaidCount(count);//已支付数量
                    coin.setUnpaidCount(coin.getCoinCount().subtract(count));//未支付数量
                    Map<String, String> params=new HashMap<String,String>();
                    params.put("coinCode", coin.getCoinCode());
                    params.put("memberId", order.getMemberId().toString());
                    params.put("coinCount", count.toString());
                    params.put("OrderNo", order.getOrderSn()+coin.getCoinCode()+first);
                    this.withhold(params);
                }
                //如果是支付定金，则保存定金支付时间
                if("1".equals(first)){
                    order.setAdvancePayTime(new Date());
                    orderService.update(order);
                }else{// 全款或支付尾款成功，则更新订单状态
                    order.setPaymentState(Integer.valueOf("1"));
                    order.setOrderState(Integer.valueOf("20"));
                    order.setPaymentTime(new Date());
                    orderService.update(order);
                    //9、更改库存信息
                    this.changeGoodsSpec(order.getId());
                    result.setSuccess(true).setMsg("zhifuchenggong").setObj(order);  //支付成功
                }

                ThreadPool.exe(new RebateRunnable(order.getId()));//发放积分以及返佣
            }else{
                result.setSuccess(false).setMsg("user_no_exist"); //用户 不存在
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            redisService.unLock(lock);
        }
        return result;
    }

    @Override
    public JsonResult findOrder1(Map<String, String> map) {
        String orderId=map.get("orderId");
        Order order=orderService.get(Long.valueOf(orderId));
        if(null!=order && !"".equals(order)){
            //获得系统的配置的订单失效时长
            Integer validTime=new Integer("0");
            List<MallConfig> list=mallConfigService.findAll();
            if(null!=list && list.size()>0){
                MallConfig  config=	list.get(0);
                validTime= config.getOrderTime();
            }
            order.setValidTime(validTime);
           //
            if(null!=order.getIsAdvance() && 1==order.getIsAdvance()){
                //判读是支付定金还是尾款
                if(null!=order.getAdvancePayTime()){
                    //如果定金支付时间不为空，则待支付的为尾款
                    order.setToPayCoin(order.getTailPayCoin());
                }else{
                   order.setToPayCoin(order.getAdvancePayCoin());//定金
                }
            }else{//如果不是预支付订单，则支付币和数量串就是要支付的
               order.setToPayCoin(order.getPayCoin());
            }
            return new  JsonResult().setSuccess(true).setObj(order);
        }
        else{
            return new  JsonResult().setSuccess(false).setMsg("malldingdanbucunzai");//该订单不存在，请确认
        }
    }

    @Override
    public FrontPage findBalance(Map<String, String> map) {
        Page<OrderBalanceVo> page = PageFactory.getPage(map);
        Map<String, String> queryMap =new HashMap<String,String>();
         QueryFilter filter=new QueryFilter(Merchant.class);
         filter.addFilter("memberId=",map.get("memberId"));
         filter.setOrderby("id desc");
         List<Merchant> mlist= merchantService.find(filter);
         if(null==mlist || mlist.size()<=0){
             return new FrontPage(new ArrayList(), page.getTotal(), page.getPages(), page.getPageSize());
         }
         Merchant merchant=mlist.get(0);
         queryMap.put("merchantId",  merchant.getId().toString());
         List<OrderBalanceVo>  list=  orderService.findBalanceBySql(queryMap);
        if(null!=list && list.size()>0){
            return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
        }
        return new FrontPage(new ArrayList(), page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
	public JsonResult submitOrder(Map<String, String> map) {
		// TODO Auto-generated method stub
		Map<String, Object> objMap=new HashMap<String,Object>();
		String memberId=map.get("memberId");
		//1、用户选择的商品，判断是否库存充足、是否价格更改
	//	JsonResult result=this.validateGoods(map);
		JsonResult result=this.validateGoods1(map);
		if(!result.getSuccess()){
			return result;
		}
		String couponDetailId=map.get("couponDetailId");
		if(null!=couponDetailId && !"".equals(couponDetailId)){
			result=couponDetailService.isCanUse(map);
			if(!result.getSuccess()){
				return result;
			}
		}
		//如果验证成功
		if(result.getSuccess()){
			//2、验证收货地址是否存在，无则返回
			String addressId=map.get("addressId");
			Address address=addressService.get(Long.valueOf(addressId));
			if(null==address || "".equals(address)){
				result.setSuccess(false).setMsg("wugaidizhi");
				return result;
			}
		 //3、保存订单信息
			  //商品金额拼串
			String moneys=map.get("moneys");
			BigDecimal sumMoney=new BigDecimal("0");
			//商品数量拼串
			String counts=map.get("counts");
			String [] countArr =counts.split(",");
			String [] moneyArr =moneys.split(",");
			for(int i=0;i<moneyArr.length;i++){
				sumMoney=sumMoney.add(new BigDecimal(moneyArr[i]).multiply(new BigDecimal(countArr[i])));
			}
			map.put("sumMoney", sumMoney.toString());
			String orderSort="0";
            String goodsIds=map.get("goodsIds");
            String [] goodsIdrry =goodsIds.split(",");
            Goods goods=goodsService.get(Long.valueOf(goodsIdrry[0]));
            if(7==goods.getSpecialType() || 8==goods.getSpecialType()){
                orderSort=goods.getSpecialType().toString();
            }
			map.put("orderSort", "0");//普通订单
			Order order=orderService.saveOrder1(map);//李金沅项目定制
		//4、保存本次收货地址信息
			orderAddressService.saveOrderAddress(address, order.getId());
	    //5、保存订单商品明细
			//商品规格拼串
			String goodsSpecIds=map.get("goodsSpecIds");
			String [] specArr =goodsSpecIds.split(",");
			//活动商品id拼串
		 	String activityGoodsIds=map.get("activityGoodsIds");
		 	String [] activityGoodsArr=null;
		 	if(null!=activityGoodsIds && !"".equals(activityGoodsIds)){
		 		activityGoodsArr =activityGoodsIds.split(",");
		 	}
			//循环遍历用户选择的商品，保存商品明细表
			for(int i=0;i<specArr.length;i++){
				GoodsSpec goodspec=goodsSpecService.get(Long.valueOf(specArr[i]));
				Goods good=goodsService.get(goodspec.getGoodsId());
				BigDecimal price=new BigDecimal(moneyArr[i]);//已验证价格未变，则直接保存该价格即可
				//如果是抢购商品，则明细保存活动商品id
				if(null!=activityGoodsArr && activityGoodsArr.length>0 && null!=activityGoodsArr[i] && !"".equals(activityGoodsArr[i])){
					good.setActivityGoodsId(Long.valueOf(activityGoodsArr[i]));
				}
				orderGoodsService.saveOrderGoods(good, goodspec, order,Integer.valueOf(countArr[i]),price);
				//商品销售数量增加
				good.setSaleNum(good.getSaleNum()+Integer.valueOf(countArr[i]));
				goodsService.update(good);
		        //更新商品solr
		        searchGoodsService.updateSolrGoods(good.getId());
		  }
		 //6、删除对应商品购物车记录
		 //购物车id拼串
		  String cartIds=map.get("cartIds");
		  if(null!=cartIds && !"".equals(cartIds)){
			  String [] cartArr =cartIds.split(",");
			  for(int i=0;i<cartArr.length;i++){
				  cartService.delete(Long.valueOf(cartArr[i]));
			  }
		  }
			//7、获得系统的配置的订单失效时长
			Integer validTime=new Integer("0");
			List<MallConfig> list=mallConfigService.findAll();
			if(null!=list && list.size()>0){
				MallConfig  config=	list.get(0);
				validTime= config.getOrderTime();
			}
			order.setValidTime(validTime);
		  /*  order.setRmbTotalMoney(order.getRmbMoney().add(order.getRmbFeeMoney()));
			order.setCoinToalCount(order.getCoinCount().add(order.getCoinFeeCount()));*/
			result.setSuccess(true).setObj(order).setMsg("caozuochenggong");
		}
		return result;
	}

	@Override
	public JsonResult findOrder(Map<String, String> map) {
		// TODO Auto-generated method stub
		String orderId=map.get("orderId");
		Order order=orderService.get(Long.valueOf(orderId));
		if(null!=order && !"".equals(order)){
			order.setRmbTotalMoney(order.getRmbMoney().add(order.getRmbFeeMoney()));
			order.setCoinToalCount(order.getCoinCount().add(order.getCoinFeeCount()));
			//获得系统的配置的订单失效时长
			Integer validTime=new Integer("0");
			List<MallConfig> list=mallConfigService.findAll();
			if(null!=list && list.size()>0){
				MallConfig  config=	list.get(0);
				validTime= config.getOrderTime();
			}
			order.setValidTime(validTime);
			order.setToPayCoin(order.getAdvancePayCoin());//全款或交定金的情况下，待支付金额为定金数量，如为预付款订单，且定金已付，则待支付为尾款
            if(1==order.getIsAdvance() && null!=order.getAdvancePayTime()){
               order.setToPayCoin(order.getTailPayCoin());
            }
			return new  JsonResult().setSuccess(true).setObj(order);
		}
		else{
			return new  JsonResult().setSuccess(false).setMsg("malldingdanbucunzai");//该订单不存在，请确认
		}
	}

	@Override
	public JsonResult payConfirm(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result=new JsonResult();
		RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
		User user=remoteManageService.findUserById(Long.valueOf(map.get("memberId")));
		//1、验证支付密码
		 if(null!=user){
			 //如果支付密码密码为空，则提示用户去维护支付密码
			  if(null==user.getAccountPassWord() || "".equals(user.getAccountPassWord())){
				  result.setSuccess(false).setMsg("zhifumimakong"); //支付密码为空,请先去维护支付密码
	              return result;
			  }
			    PasswordHelper passwordHelper = new PasswordHelper();
	            String encryString = passwordHelper.encryString(map.get("accountPassWord"), user.getSalt());
	            if(!encryString.equals(user.getAccountPassWord())){
	            	 result.setSuccess(false).setMsg("zhifumimacuowu"); //支付密码错误
	            	 return result;
	            }
	     //2、查询订单
	       Order order=orderService.get(Long.valueOf(map.get("orderId")));
	       if(null==order){
	    	  result.setSuccess(false).setMsg("malldingdanbucunzai"); //订单不存在
			  return result;
	       }
	       if(20==order.getOrderState() && 1==order.getPaymentState()){
	    	   result.setSuccess(false).setMsg("dingdanyizhifu"); //该订单已支付，无需重复支付
			   return result;
	       }
	       order.setLocal(map.get("locale"));
	     //3、验证 订单中的商品价格和库存是否充足
	       result=this.validateOrderGoods(order);
	       if(!result.getSuccess()){
	    	   return result;
	       }
	       Boolean flag=true;
	    //4、如果是虚拟币或者虚拟币、人民币混合支付，要验证账户金额是否充足
	       if(("2".equals(order.getPayType())|| "3".equals(order.getPayType())) &&  order.getCoinCount().compareTo(new BigDecimal("0"))>0 ){
	    //5、重新计算下数字币，因为汇率在实时变化
    		 BigDecimal coinMoney=order.getOrderTotalPrice().subtract(order.getRmbMoney()).add(order.getShippingFee());
    		 BigDecimal coinRate=goodsService.getCoinRate(order.getCoinCode());//新的实时汇率
       		 order.setCoinRate(coinRate.setScale(10, BigDecimal.ROUND_HALF_UP));
       		 order.setCoinCount(coinMoney.divide(coinRate, 10, BigDecimal.ROUND_HALF_UP));//虚拟币数量
       		 order.setCoinFeeCount(order.getCoinCount().multiply(order.getCoinFeeRate()).divide(new BigDecimal("100")).setScale(10, BigDecimal.ROUND_HALF_UP));
	    //6、验证账户币余额是否充足
	    	   Map<String, String> vmap=new HashMap<String,String>();
		       vmap.put("memberId", order.getMemberId().toString());
		       vmap.put("coinCode", order.getCoinCode());
		       vmap.put("coinCount", (order.getCoinCount().add(order.getCoinFeeCount())).toString());
		       result=this.validateExaccount(vmap);
		       if(!result.getSuccess()){
		    	   return result;
		       }else{
		    	//7、如果余额充足，则进行扣币操作
		    	   if(this.withhold(order)){
		    		   order.setCoinPayStatus(Short.valueOf("1"));
		    	   }else{
		    		   flag=false;
		    	   }
		       }
	       }

	     //8、如果有人民币支付，则记录请求第三方的记录
	       if(("1".equals(order.getPayType())|| "3".equals(order.getPayType())) &&  order.getRmbMoney().compareTo(new BigDecimal("0"))>0){
	    	   ThirdpayRecord record=thirdpayRecordService.saveRecord(order);
	    	   if(!"PAY_SUCCESS".equals(record.getCode())){
	    		   flag=false;
	    	   }else{
	    		   order.setRmbPayStatus(Short.valueOf("1"));
	    	   }
	       }
	       //9、支付成功，更改订单状态
	       if(flag){
	    	   order.setPaymentState(Integer.valueOf("1"));
	    	   order.setOrderState(Integer.valueOf("20"));
	    	   order.setPaymentTime(new Date());
	    	   //9、更改库存信息
	    	   this.changeGoodsSpec(order.getId());
	    	   result.setSuccess(true).setMsg("zhifuchenggong").setObj(order); //支付成功
	       }
	         orderService.update(order);
             System.out.println("+++++++++++++++++++++++++++++订单支付成功orderId="+order.getId());
             //购物发放积分信息---start
             String accountKey = "shopAccount";
             String taskKey = "shopKey";
             ThreadPool.exe(new IntegralRunnable(order.getId(), user.getCustomerId(), accountKey, taskKey, 0));
             //购物发放积分信息---end
		 }else{
			 result.setSuccess(false).setMsg("user_no_exist"); //用户 不存在
			 return result;
		 }


		return result;
	}
	/**
	 * 验证订单中的商品价格是否变动、库存是否充足
	 * @param order
	 * @return
	 */
	public  JsonResult  validateOrderGoods(Order order){
		//1、查询所有的订单明细
		QueryFilter filter = new QueryFilter(OrderGoods.class);
		filter.addFilter("orderId=", order.getId());
		List<OrderGoods>  list=orderGoodsService.find(filter);

		//2、循环遍历用户选择的商品，判断是否库存充足、是否价格更改
		StringBuffer countString=new StringBuffer();
		StringBuffer moneyString=new StringBuffer();
		Boolean  flag=true;
		JsonResult result=new JsonResult();
		for(OrderGoods orderGoods:list){
			ActivityGoods activityGoods=null;
			if(null!=orderGoods.getActivityGoodsId() && !"".equals(orderGoods.getActivityGoodsId())){
				activityGoods=activityGoodsService.get(orderGoods.getActivityGoodsId());
				//抢购活动有效，验证抢购库存是否满足
				if(null!=activityGoods && activityGoodsService.isValid(activityGoods) ){
					if(activityGoods.getActivityStorage()<orderGoods.getGoodsNum()){
						flag=false;
						Goods good=goodsService.get(activityGoods.getGoodsId());
						String goodsName=good.getGoodsName();
						//查询国际化的商品名称
						if (!"zh_CN".equals(order.getLocal())){
							Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), order.getLocal().toString());
		                    if (goodsDetail != null)
							    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
						}
						countString.append(goodsName).append(",");
						continue;
					}


				}

			}
			GoodsSpec goodspec=goodsSpecService.get(orderGoods.getGoodsSpecId());
			//3、库存不足
			if(goodspec.getSpecGoodsStorage()<orderGoods.getGoodsNum()){
				flag=false;
				Goods good=goodsService.get(goodspec.getGoodsId());
				String goodsName=good.getGoodsName();
				//查询国际化的商品名称
				if (!"zh_CN".equals(order.getLocal())){
					Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), order.getLocal().toString());
                    if (goodsDetail != null)
					    goodsName = goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
				}
				countString.append(goodsName).append(",");
			}
			//4、价格不一致
			if(2==order.getOrderSort()){
				//积分验证积分兑换数
				if(goodspec.getIntegralCount().compareTo(orderGoods.getIntegralCount())!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(order.getLocal())){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), order.getLocal().toString());
                        if (goodsDetail != null)
						    goodsName=goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}else{
				BigDecimal price=goodspec.getSpecGoodsPrice();
				if(null!=activityGoods && activityGoodsService.isValid(activityGoods) ){
					price=activityGoods.getActivityPrice();
				}
				if(price.compareTo(orderGoods.getGoodsPayPrice())!=0 ){
					flag=false;
					Goods good=goodsService.get(goodspec.getGoodsId());
					String goodsName=good.getGoodsName();
					//查询国际化的商品名称
					if (!"zh_CN".equals(order.getLocal())){
						Goods goodsDetail = shopLanguageService.getGoodsLanguage(good.getId().toString(), order.getLocal().toString());
                        if (goodsDetail != null)
						    goodsName=goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : good.getGoodsName();
					}
					moneyString.append(goodsName).append(",");
				}
			}

		}
		StringBuffer msg=new StringBuffer();
		if(!flag){
		  if(countString.length()>0){
			 msg.append(countString.substring(0, countString.length()-1)).append("&&&kucunbuzuchongxuan");//库存不足,重新选择商品
		  }
		  if(moneyString.length()>0){
				 msg.append(moneyString.substring(0, moneyString.length()-1)).append("&&&jiagebiandongchongxuan");//"价格变动,重新选择商品
			  }
		  result.setCode("1111");
		}

		return result.setSuccess(flag).setMsg(msg.toString());
	}

	@Override
	public JsonResult validateExaccount(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result =new JsonResult();
		String memberId=map.get("memberId");
		String coinCode=map.get("coinCode");
		String coinCount=map.get("coinCount");
		//1、查询比账户
		ExDigitalmoneyAccountRedis dmAccount=UserRedisUtils.getAccountRedis(memberId, coinCode);
		if(null==dmAccount){
			return result.setSuccess(false).setMsg("meiyoubizhanghu");//没有相应币账户信息
		}
		//如果币账户余额小于需支付金额
		if(dmAccount.getHotMoney().compareTo(new BigDecimal(coinCount))<0){
			return result.setSuccess(false).setMsg("zhanghuyuebuzu");//账户余额不足
		}

		return result.setSuccess(true);
	};
	public Boolean withhold(Order order){
		
		//封装参数，操作币业务流水和账户
		Map<String,String> map=new HashMap<String,String>();
		map.put("coinCode", order.getCoinCode());
		map.put("memberId", order.getMemberId().toString());
		map.put("transactionType", "2");// 交易类型(1币收入 ，2币支出)
		map.put("coinCount", (order.getCoinCount().add(order.getCoinFeeCount())).toString());
		map.put("OrderNo", order.getOrderSn());
		map.put("optType", "510");//购买商品
		customerIntegralService.handCoin(map);
		
	/*	ExDigitalmoneyAccountRedis dmAccount=UserRedisUtils.getAccountRedis(order.getMemberId().toString(), order.getCoinCode());
	     // 热账户减少
        Accountadd accountadd = new Accountadd();
        accountadd.setAccountId(dmAccount.getId());
        accountadd.setMoney((order.getCoinCount().add(order.getCoinFeeCount())).multiply(new BigDecimal(-1)));
        accountadd.setMonteyType(1);//1是操作热账户，其他是操作冻结账户
        accountadd.setAcccountType(1);
        accountadd.setRemarks(510);//购买商品
        accountadd.setTransactionNum(order.getOrderSn());
        List<Accountadd> list = new ArrayList<Accountadd>();
        list.add(accountadd);
        messageProducer.toAccount(JSON.toJSONString(list));*/
		return true;
	}

    /**
     * 李金沅项目扣币
     * @param params
     * @return
     */
    public Boolean withhold( Map<String,String> params){

        //封装参数，操作币业务流水和账户
        Map<String,String> map=new HashMap<String,String>();
        map.put("coinCode", params.get("coinCode"));
        map.put("memberId", params.get("memberId"));
        map.put("transactionType", "2");// 交易类型(1币收入 ，2币支出)
        map.put("coinCount", params.get("coinCount"));
        map.put("OrderNo", params.get("OrderNo"));
        map.put("optType", "510");//购买商品
        customerIntegralService.handCoin(map);
        return true;
    }
	//抵扣积分
	public Boolean subIntegral(Order order, CustomerIntegral customerIntegral){
		BigDecimal ling=new BigDecimal("0");
		IntegralDetail vo = new IntegralDetail();
		vo.setMemberId(order.getMemberId());
		vo.setRewardType(4);
		vo.setIntegralCount(customerIntegral.getAvailableIntegral().subtract(order.getCoinCount()));
		vo.setComputingCount(ling);
		vo.setRewardintegralCount(ling);
		vo.setCallbackintegralCount(order.getCoinCount());
		vo.setRewardComputingCount(ling);
		vo.setOrderId(order.getId());
		vo.setOrderSn(order.getOrderSn());
		vo.setOrderDate(order.getCreated());
		vo.setOrderMoney(order.getCoinCount());
		vo.setBuyerId(order.getMemberId());
		vo.setBuyerName(order.getMemberName());
		vo.setMemberName(order.getUserName());
		vo.setMemberTruename(order.getMemberName());
	    String cardNumber="";
	    String cellPhone="";
	    RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
		User user=remoteManageService.findUserById(customerIntegral.getMemberId());
		if(null!=user && !"".equals(user)){
			   cardNumber=user.getCardcode();
	            if(null!=user.getMobile() && !"".equals(user.getMobile())){
	               cellPhone=user.getMobile();
	            }else{
	               cellPhone=user.getEmail();
	            }
		}
		vo.setCardNumber(cardNumber);
		vo.setBuyerName(cellPhone);
		String goodsName="";
		QueryFilter filter1=new QueryFilter(OrderGoods.class);
		filter1.addFilter("orderId=", order.getId());
		filter1.setOrderby("id desc");
		List<OrderGoods> glist=orderGoodsService.find(filter1);
		if(null!=glist && glist.size()>0){
			OrderGoods good=glist.get(0);
			goodsName=good.getGoodsName();
		}
		vo.setTaskName("抢购"+goodsName);
		vo.setAccountId(customerIntegral.getId());
		return integralDetailService.addIntegralDetai(vo).getSuccess();
	}


    public void changeGoodsSpec(Long orderId){
    	//1、更改规格的库存
    	QueryFilter filter = new QueryFilter(OrderGoods.class);
		filter.addFilter("orderId=", orderId);
		List<OrderGoods>  list=orderGoodsService.find(filter);
		for(OrderGoods orderGoods:list){
			if(null!=orderGoods.getActivityGoodsId() && !"".equals(orderGoods.getActivityGoodsId())){
				ActivityGoods agoods= activityGoodsService.get(orderGoods.getActivityGoodsId());
				if(null!=agoods && !"".equals(agoods)){
					agoods.setActivityStorage(agoods.getActivityStorage()-orderGoods.getGoodsNum());
					activityGoodsService.update(agoods);
				}
			}
			GoodsSpec spec=goodsSpecService.get(orderGoods.getGoodsSpecId());
			spec.setSpecGoodsStorage(spec.getSpecGoodsStorage()-orderGoods.getGoodsNum());
			goodsSpecService.update(spec);
		}
    }

	/**
	 * 积分商品点击去抢购
	 * @param map
	 * @return
	 */
	@Override
	public JsonResult toIntegralBalance(Map<String, String> map) {
		// TODO Auto-generated method stub
		Map<String, Object> objMap=new HashMap<String,Object>();
		String memberId=map.get("memberId");
		//1、用户选择的商品，判断是否库存充足、是否价格更改
		JsonResult result=this.validateGoods(map);
		//如果验证成功，则计算运费信息
		if(result.getSuccess()){
		 //2、计算商品总金额
		  //商品金额拼串
			String moneys=map.get("moneys");
			BigDecimal sumMoney=new BigDecimal("0");
			//商品数量拼串
			String counts=map.get("counts");
			String [] countArr =counts.split(",");
			String [] moneyArr =moneys.split(",");
			for(int i=0;i<moneyArr.length;i++){
				sumMoney=sumMoney.add(new BigDecimal(moneyArr[i]).multiply(new BigDecimal(countArr[i])));
			}
			objMap.put("sumMoney", sumMoney);
		 //3、查询默认收货地址
			Address  address=null;
			List<Address>  list=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("1"));
			if(null!=list &&list.size()>0){
				address=list.get(0);
			}else{
				List<Address>  list1=addressService.findByCustomerId(Long.valueOf(memberId),Short.valueOf("0"));
				if(null!=list1 &&list1.size()>0){
					address=list1.get(0);
				}
			}
		//4、根据收货地址、商品、下单数量计算运费
		  if(null!=address && !"".equals(address)){
			  objMap.put("address", address);
			}else{
			  objMap.put("address", null);
			}
		   objMap.put("fee", new BigDecimal("0"));
	       //查询当前积分兑换币代码
            objMap.put("coinCode", integralConfigService.findIntegralCode());//积分兑换
            result.setSuccess(true).setObj(objMap).setMsg("caozuochenggong");
		}
		return result;
	}
	/**
	 * 积分商品提交订单
	 * @param map
	 * @return
	 */
	@Override
	public JsonResult submitIntegralOrder(Map<String, String> map) {
		// TODO Auto-generated method stub
		Map<String, Object> objMap=new HashMap<String,Object>();
		String memberId=map.get("memberId");
		//1、用户选择的商品，判断是否库存充足、是否价格更改
		JsonResult result=this.validateGoods(map);
		//如果验证成功
		if(result.getSuccess()){
			//2、验证收货地址是否存在，无则返回
			String addressId=map.get("addressId");
			Address address=addressService.get(Long.valueOf(addressId));
			if(null==address || "".equals(address)){
				result.setSuccess(false).setMsg("wugaidizhi");//该收货地址无信息，请确认
				return result;
			}
		 //3、保存订单信息
			  //商品金额拼串
			String moneys=map.get("moneys");
			BigDecimal sumMoney=new BigDecimal("0");
			//商品数量拼串
			String counts=map.get("counts");
			String [] countArr =counts.split(",");
			String [] moneyArr =moneys.split(",");
			for(int i=0;i<moneyArr.length;i++){
				sumMoney=sumMoney.add(new BigDecimal(moneyArr[i]).multiply(new BigDecimal(countArr[i])));
			}
			map.put("sumMoney", sumMoney.toString());
			map.put("orderSort", "2");//积分订单
			map.put("fee", "0");//运费
			map.put("payType", "2");//积分兑换
			map.put("coinCount", sumMoney.toString());
			map.put("rmbMoney", "0");
		      //查询当前积分兑换币代码
			map.put("coinCode", integralConfigService.findIntegralCode());//积分兑换

	        //保存订单信息
			Order order=orderService.saveOrder(map);
		//4、保存本次收货地址信息
			orderAddressService.saveOrderAddress(address, order.getId());
	    //5、保存订单商品明细
			//商品规格拼串
			String goodsSpecIds=map.get("goodsSpecIds");
			String [] specArr =goodsSpecIds.split(",");
			//循环遍历用户选择的商品，保存商品明细表
			for(int i=0;i<specArr.length;i++){
				GoodsSpec goodspec=goodsSpecService.get(Long.valueOf(specArr[i]));
				Goods good=goodsService.get(goodspec.getGoodsId());
				BigDecimal price= new BigDecimal(moneyArr[i]);
				orderGoodsService.saveOrderGoods(good, goodspec, order,Integer.valueOf(countArr[i]),price);
				//商品销售数量增加
				good.setSaleNum(good.getSaleNum()+Integer.valueOf(countArr[i]));
				goodsService.update(good);
		        //更新商品solr
		        searchGoodsService.updateSolrGoods(good.getId());
		  }

			//7、获得系统的配置的订单失效时长
			Integer validTime=new Integer("0");
			List<MallConfig> list=mallConfigService.findAll();
			if(null!=list && list.size()>0){
				MallConfig  config=	list.get(0);
				validTime= config.getOrderTime();
			}
			order.setValidTime(validTime);
		    order.setRmbTotalMoney(order.getRmbMoney().add(order.getRmbFeeMoney()));
			order.setCoinToalCount(order.getCoinCount().add(order.getCoinFeeCount()));
			result.setSuccess(true).setObj(order).setMsg("caozuochenggong");//操作成功
		}
		return result;
	}

	@Override
	public JsonResult payIntegralConfirm(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result=new JsonResult();
		RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
		User user=remoteManageService.findUserById(Long.valueOf(map.get("memberId")));
		//1、验证支付密码
		 if(null!=user){
			 //如果支付密码密码为空，则提示用户去维护支付密码
			 if(null==user.getAccountPassWord() || "".equals(user.getAccountPassWord())){
				  result.setSuccess(false).setMsg("zhifumimakong"); //支付密码为空,请先去维护支付密码
	              return result;
			  }
			    PasswordHelper passwordHelper = new PasswordHelper();
	            String encryString = passwordHelper.encryString(map.get("accountPassWord"), user.getSalt());
	            if(!encryString.equals(user.getAccountPassWord())){
	            	 result.setSuccess(false).setMsg("zhifumimacuowu"); //支付密码错误
	            	 return result;
	            }
	     //2、查询订单
	       Order order=orderService.get(Long.valueOf(map.get("orderId")));
	       if(null==order){
	    	  result.setSuccess(false).setMsg("malldingdanbucunzai");//订单不存在
			  return result;
	       }
	       if(20==order.getOrderState() && 1==order.getPaymentState()){
	    	   result.setSuccess(false).setMsg("dingdanyizhifu"); //该订单已支付，无需重复支付
			   return result;
	       }
	     //3、验证 订单中的商品价格和库存是否充足
	       result=this.validateOrderGoods(order);
	       if(!result.getSuccess()){
	    	   return result;
	       }
	       Boolean flag=true;
	       //4、验证积分币是否充足
	       Map<String,String> cmap=new HashMap<String,String>();
	       cmap.put("memberId", map.get("memberId"));
	       cmap.put("integralType", "System");
	       CustomerIntegral vo = integralConfigService.findCustomerIntegral(cmap);
	       if(null==vo){
	    	   result.setSuccess(false).setMsg("wujifenzhanghu"); //无积分账户，请联系管理员
	    	   return result;
	       }
	       if(vo.getAvailableIntegral().compareTo(order.getCoinCount())<0){
	    	   result.setSuccess(false).setMsg("jifenbuzu");// 积分不足
	    	   return result;
	       }
	      //5、抵扣积分
	       if(this.subIntegral(order, vo)){
    		   order.setCoinPayStatus(Short.valueOf("1"));
    	   }else{
    		   flag=false;
    	   }
	       //6、支付成功，更改订单状态
	       if(flag){
	    	   order.setPaymentState(Integer.valueOf("1"));
	    	   order.setOrderState(Integer.valueOf("20"));
	    	   order.setPaymentTime(new Date());
	    	   //9、更改库存信息
	    	   this.changeGoodsSpec(order.getId());
	    	   result.setSuccess(true).setMsg("zhifuchenggong").setObj(order); //支付成功
	       }
	       orderService.update(order);
		 }else{
			 result.setSuccess(false).setMsg("user_no_exist"); //用户 不存在
			 return result;
		 }
		return result;
	}

	@Override
	public JsonResult payRmbConfirm(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result=new JsonResult();
        String lock = "payRmbConfirm:" + map.get("memberId");
        if (!redisService.lock(lock)) {
            result.setSuccess(false).setMsg("req_error"); //操作失败
            return result;
        }
        try {
            //1、验证支付密码
            RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
            User user=remoteManageService.findUserById(Long.valueOf(map.get("memberId")));
            if(null!=user){
                //2、查询订单
                Order order=orderService.get(Long.valueOf(map.get("orderId")));
                if(null==order){
                    result.setSuccess(false).setMsg("malldingdanbucunzai"); //订单不存在
                    return result;
                }
                order.setLocal(map.get("locale"));
                if(20==order.getOrderState() && 1==order.getPaymentState()){
                    result.setSuccess(false).setMsg("dingdanyizhifu"); //该订单已支付，无需重复支付
                    return result;
                }
                if(1==order.getRmbPayStatus() ){
                    result.setSuccess(false).setMsg("rbmzhifuchenggong"); //该订单人民币已支付成功，无需重复支付
                    return result;
                }
                if(50==order.getRmbPayStatus() ){
                    result.setSuccess(false).setMsg("dingdanyiguanbi"); //该订单已关闭，无法支付
                    return result;
                }
                //3、验证 订单中的商品价格和库存是否充足
                result=this.validateOrderGoods(order);
                if(!result.getSuccess()){
                    return result;
                }
                Boolean flag=true;
                //4、人民币支付，获得支付通道id,计算手续费
                String paymentId=map.get("paymentId");
                if(null!=paymentId && !"".equals(paymentId)){
                    order.setPaymentId(Long.valueOf(paymentId));
                    Payment payment=paymentService.get(Long.valueOf(paymentId));
                    order.setPaymentName(payment.getPaymentName());
                    if(null!=payment &&null!=payment.getPayPoundage() && !"".equals(payment.getPayPoundage())){
                        BigDecimal rate=new BigDecimal(payment.getPayPoundage());
                        order.setRmbFeeRate(rate);
                        order.setRmbFeeMoney(order.getOrderAmount().multiply(rate).divide(new BigDecimal("100")).setScale(2, BigDecimal.ROUND_HALF_UP));
                    }
                }

                //5、如果有人民币支付，则记录请求第三方的记录
                if(("1".equals(order.getPayType())|| "3".equals(order.getPayType())) &&  order.getRmbMoney().compareTo(new BigDecimal("0"))>0){
                    ThirdpayRecord record=thirdpayRecordService.saveRecord(order);
                    if(!"PAY_SUCCESS".equals(record.getCode())){
                        flag=false;
                    }else{
                        order.setRmbPayStatus(Short.valueOf("1"));
                    }
                }
                //6、支付成功，更改订单状态   -----因为人民币支付是支付的第二步，所以支付成功直接改状态
                if(flag){
                    order.setPaymentState(Integer.valueOf("1"));
                    order.setOrderState(Integer.valueOf("20"));
                    order.setPaymentTime(new Date());
                    //9、更改库存信息
                    this.changeGoodsSpec(order.getId());
                    result.setSuccess(true).setMsg("zhifuchenggong").setObj(order); //支付成功
                }
                orderService.update(order);
                System.out.println("+++++++++++++++++++++++++++++订单支付成功orderId="+order.getId());
                //购物发放积分信息---start
                String accountKey = "shopAccount";
                String taskKey = "shopKey";
                ThreadPool.exe(new IntegralRunnable(order.getId(), Long.valueOf(map.get("memberId")), accountKey, taskKey, 0));
                //购物发放积分信息---end

                //9、判断此次 下单是否为店主升级商品，
                Boolean isPromote=orderGoodsService.isPromote(order.getId(),order.getMemberId());
                if(!isPromote){
                    //购物成长值发放---start
                    Map<String, Object> gmap= new HashMap<>();
                    gmap.put("memberId", order.getMemberId());
                    gmap.put("isShop", 1);
                    gmap.put("detailType", 1);
                    gmap.put("orderId", order.getId());
                    ThreadPool.exe(new GrowthRunnable(gmap));
                    //购物发放积分信息---end
                }
            }else{
                result.setSuccess(false).setMsg("user_no_exist"); //用户 不存在
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            redisService.unLock(lock);
        }

		return result;
	}

	@Override
	public JsonResult payCoinConfirm(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result=new JsonResult();
        String lock = "payCoinConfirm:" + map.get("memberId");
        if (!redisService.lock(lock)) {
            result.setSuccess(false).setMsg("req_error"); //操作失败
            return result;
        }
        try {
            RemoteManageService remoteManageService = SpringUtil.getBean("remoteManageService");
            User user=remoteManageService.findUserById(Long.valueOf(map.get("memberId")));
            //1、验证支付密码
            if(null!=user){
                //如果支付密码密码为空，则提示用户去维护支付密码
              if(null==user.getAccountPassWord() || "".equals(user.getAccountPassWord())){
                    result.setSuccess(false).setMsg("zhifumimakong"); //支付密码为空,请先去维护支付密码
                    return result;
                }
                PasswordHelper passwordHelper = new PasswordHelper();
                String encryString = passwordHelper.encryString(map.get("accountPassWord"), user.getSalt());
                if(!encryString.equals(user.getAccountPassWord())){
                    result.setSuccess(false).setMsg("zhifumimacuowu"); //支付密码错误
                    return result;
                }
                //2、查询订单
                Order order=orderService.get(Long.valueOf(map.get("orderId")));
                if(null==order){
                    result.setSuccess(false).setMsg("malldingdanbucunzai"); //订单不存在
                    return result;
                }
                if(20==order.getOrderState() && 1==order.getPaymentState()){
                    result.setSuccess(false).setMsg("dingdanyizhifu"); //该订单已支付，无需重复支付
                    return result;
                }
                if(1==order.getCoinPayStatus() ){
                    result.setSuccess(false).setMsg("shubiyizhifuchengong"); //该订单数币已支付成功，无需重复支付
                    return result;
                }
                if(50==order.getRmbPayStatus() ){
                    result.setSuccess(false).setMsg("dingdanyiguanbi"); //该订单已关闭，无法支付
                    return result;
                }
                order.setLocal(map.get("locale"));
                //3、验证 订单中的商品价格和库存是否充足
                result=this.validateOrderGoods(order);
                if(!result.getSuccess()){
                    return result;
                }
                Boolean flag=true;
                //4、如果是虚拟币或者虚拟币、人民币混合支付，要验证账户金额是否充足-----
                if(("2".equals(order.getPayType())|| "3".equals(order.getPayType()) || "4".equals(order.getPayType())) &&  order.getCoinCount().compareTo(new BigDecimal("0"))>0 ){
                    //5、重新计算下数字币，因为汇率在实时变化
                    BigDecimal coinMoney=order.getOrderAmount().subtract(order.getRmbMoney()).add(order.getShippingFee());
                    BigDecimal coinRate=goodsService.getCoinRate(order.getCoinCode());//新的实时汇率
                    order.setCoinRate(coinRate.setScale(10, BigDecimal.ROUND_HALF_UP));
                    order.setCoinCount(coinMoney.divide(coinRate, 10, BigDecimal.ROUND_HALF_UP));//虚拟币数量
                    order.setCoinFeeCount(order.getCoinCount().multiply(order.getCoinFeeRate()).divide(new BigDecimal("100")).setScale(10, BigDecimal.ROUND_HALF_UP));
                    //6、验证账户币余额是否充足
                    Map<String, String> vmap=new HashMap<String,String>();
                    vmap.put("memberId", order.getMemberId().toString());
                    vmap.put("coinCode", order.getCoinCode());
                    vmap.put("coinCount", (order.getCoinCount().add(order.getCoinFeeCount())).toString());
                    if("4".equals(order.getPayType())){
                    	result=this.validateConsumeExaccount(vmap);
                    }else{
                    	 result=this.validateExaccount(vmap);
                    }
                    if(!result.getSuccess()){
                        return result;
                    }else{
                        //7、如果余额充足，则进行扣币操作
                    	boolean  flag1=false;
                    	//消费账户支付的话，则调用云矿机的方法--2019-11-11 by luyue
                    	if("4".equals(order.getPayType())){
                    		flag1=this.withholdConsume(order);
                    	}else{
                    		flag1=this.withhold(order);	
                    	}
                        if(flag1){
                            order.setCoinPayStatus(Short.valueOf("1"));
                        }else{
                            flag=false;
                        }
                    }
                }
                //8、支付成功，更改订单状态
                if(flag){
                    //如果是数币支付，则更改订单状态
                    if(("2".equals(order.getPayType()) || "4".equals(order.getPayType())) && 1==order.getCoinPayStatus()){
                        order.setPaymentState(Integer.valueOf("1"));
                        order.setOrderState(Integer.valueOf("20"));
                        order.setPaymentTime(new Date());
                        //9、更改库存信息
                        this.changeGoodsSpec(order.getId());

                    }
                    result.setSuccess(true).setMsg("zhifuchenggong").setObj(order);  //支付成功
                }
                orderService.update(order);
                System.out.println("+++++++++++++++++++++++++++++订单支付成功orderId="+order.getId());
                //购物发放积分信息---start
                String accountKey = "shopAccount";
                String taskKey = "shopKey";
                ThreadPool.exe(new IntegralRunnable(order.getId(), Long.valueOf(map.get("memberId")), accountKey, taskKey, 0));
                //购物发放积分信息---end

                //9、判断此次 下单是否为店主升级商品，
      /*       Boolean isPromote=orderGoodsService.isPromote(order.getId(),order.getMemberId());
             if(!isPromote){
                 //购物成长值发放---start
                 Map<String, Object> gmap= new HashMap<>();
                 gmap.put("memberId", order.getMemberId());
                 gmap.put("isShop", 1);
                 gmap.put("detailType", 1);
                 gmap.put("orderId", order.getId());
                 ThreadPool.exe(new GrowthRunnable(gmap));
                 //购物发放积分信息---end
             }*/
            }else{
                result.setSuccess(false).setMsg("user_no_exist"); //用户 不存在
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            redisService.unLock(lock);
        }
		return result;
	}

    @Override
    public JsonResult trackingBtn(Map<String, String> map) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        JsonResult jsonResult = new JsonResult();

        // TODO Auto-generated method stub
        String orderId=map.get("orderId");
        Order order=orderService.get(Long.valueOf(orderId));

        if(null!=order && !"".equals(order)){
            //查询快递名称
            QueryFilter queryFilter = new QueryFilter(Express.class);
            queryFilter.addFilter("number=",order.getShippingExpressName());
            Express express = expressService.get(queryFilter);
            //快递鸟查询订单状态
            KdniaoCommonRequest kdniaoCommonRequest=new KdniaoCommonRequest();
            kdniaoCommonRequest.setThirdPayInterfaceType("KdniaoServiceImpl");
            kdniaoCommonRequest.setThirdPayMethodType("getOrderTracesByJson");
            kdniaoCommonRequest.setExpCode( order.getShippingExpressName());
            kdniaoCommonRequest.setExpNo( order.getShippingCode());
            CommonResponse response = CommonSendUtil.sendMsg(kdniaoCommonRequest);
            if(response.getCode().equals("8888")){
                String message = response.getResponseMessage();
                JSONObject jsonObject = JSONObject.fromObject(message);
                String created = formatter.format(order.getCreated());
                String paymentTime = formatter.format(order.getPaymentTime());
                jsonObject.put("created",created);
                jsonObject.put("paymentTime",paymentTime);
                jsonObject.put("expressName",express.getName());
                jsonResult.setSuccess(true);
                jsonResult.setObj(jsonObject);
            } else {
                jsonResult.setSuccess(false);
                jsonResult.setMsg("快递调用失败");
                return jsonResult;
            }
            return jsonResult;
        }
        else{
            return jsonResult.setSuccess(false).setMsg("malldingdanbucunzai");//该订单不存在，请确认
        }
    }

	@Override
	public FrontPage findUnion(Map<String, String> map) {
		// TODO Auto-generated method stub
	       Page<OrderGoods> page = PageFactory.getPage(map);

	        Map<String, String> queryMap =new HashMap<String,String>();
	        queryMap.put("memberId",  map.get("memberId"));
	        List<OrderRefundVo>  list=  orderService.findUnionBySql(queryMap);
	        if(null!=list && list.size()>0){
	           return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
	       }
	        return new FrontPage(new ArrayList(), page.getTotal(), page.getPages(), page.getPageSize());
	}

	/**
     * 查询订单下单时的收货地址
     * @param memberId 用户id
     * @param orderId 订单id
     * */
	private Address getOrderAddress(String memberId, String orderId) {
	    QueryFilter filter = new QueryFilter(OrderAddress.class);
	    filter.addFilter("memberId=", memberId);
	    filter.addFilter("orderId=", orderId);
	    // 获取订单地址
	    OrderAddress orderAddress = orderAddressService.get(filter);
	    Address address = new Address();
        address.setMemberId(orderAddress.getMemberId());
        address.setProvinceId(orderAddress.getProvinceId());
        address.setCityId(orderAddress.getCityId());
        address.setCountyId(orderAddress.getCountyId());
        address.setStreet(orderAddress.getStreet());
        address.setDetailAddress(orderAddress.getDetailAddress());
        address.setCellphone(orderAddress.getCellphone());
        address.setReceiveName(orderAddress.getReceiveName());
        address.setZipCode(orderAddress.getZipCode());
        // 获取对应省市区名
        RemoteMallService remoteMallService = SpringUtil.getBean("remoteMallService");
        AppAreaDicVo  vo1=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_PROVINCE.getIndex(), orderAddress.getProvinceId());
        address.setProvinceName(vo1.getRegionName());
        String provinceName = vo1.getRegionName();
        AppAreaDicVo  vo2=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_CITY.getIndex(), orderAddress.getCityId());
        address.setCityName(vo2.getRegionName());
        String cityName = vo2.getRegionName();
        AppAreaDicVo  vo3=remoteMallService.findAreaRedis(RedisKeyEnum.KeyEnum.REIDS_DATA_AREADIC_COUNTY.getIndex(), orderAddress.getCountyId());
        address.setCountyName(vo3.getRegionName());
        String countyName = vo3.getRegionName();
        address.setAllAddress(provinceName+cityName+countyName+orderAddress.getDetailAddress());
	    return address;
    }
	/**
	 * 验证消费账户余额
	 * @param map
	 * @return
	 */
	public JsonResult validateConsumeExaccount(Map<String, String> map) {
		// TODO Auto-generated method stub
		JsonResult result =new JsonResult();
		String memberId=map.get("memberId");
		String coinCode=map.get("coinCode");
		String coinCount=map.get("coinCount");
		//1、查询币账户
		RemoteCmAccountService remoteCmAccountService = SpringUtil.getBean("remoteCmAccountService");
		hry.cmson.dto.CmAccountRedis cmSonAccountRedis = remoteCmAccountService.cmSonAccount(Long.valueOf(memberId));
		if(null==cmSonAccountRedis){
			return result.setSuccess(false).setMsg("meiyoubizhanghu");//没有相应币账户信息
		}
		//如果币账户余额小于需支付金额
		if(cmSonAccountRedis.getHotMoney().compareTo(new BigDecimal(coinCount))<0){
			return result.setSuccess(false).setMsg("zhanghuyuebuzu");//账户余额不足
		}

		return result.setSuccess(true);
	};
	/**
	 * 扣积分账户币
	 * @param order
	 * @return
	 */
	public Boolean withholdConsume(Order order){
		
		//封装参数，操作消费子账户业务流水和账户
	     List<hry.cmson.dto.Accountadd > accountSonaddList = new ArrayList<hry.cmson.dto.Accountadd >();
		RemoteCmAccountService remoteCmAccountService = SpringUtil.getBean("remoteCmAccountService");
		hry.cmson.dto.CmAccountRedis cmSonAccountRedis = remoteCmAccountService.cmSonAccount(order.getMemberId());
		BigDecimal money=(order.getCoinCount().add(order.getCoinFeeCount())).multiply(new BigDecimal("-1"));
		  hry.cmson.dto.Accountadd accountaddCold = new hry.cmson.dto.Accountadd(order.getMemberId(), order.getCoinCode(), cmSonAccountRedis.getId(), money, 1,
				  CmAccountRemarkEnum.TYPE11.getIndex(), order.getOrderSn());
				  accountSonaddList.add(accountaddCold);
				  //发送消息
				  DealMsgUtil.sendSonAccountaddList(accountSonaddList);
		return true;
	}
}
