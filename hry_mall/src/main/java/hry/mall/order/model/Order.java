/**
 * Copyright:
 *
 * @author: kongdebiao
 * @version: V1.0
 * @Date: 2018-11-16 10:31:38
 */
package hry.mall.order.model;
import hry.bean.BaseModel;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * <p> Order </p>
 * @author: kongdebiao
 * @Date :          2018-11-16 10:31:38  
 */
@Table(name = "shop_order")
public class Order extends BaseModel {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;  //订单索引id

    @Column(name = "orderSn")
    private String orderSn;  //订单编号

    @Column(name = "memberId")
    private Long memberId;  //买家id

    @Column(name = "memberName")
    private String memberName;  //买家姓名

    @Column(name = "userName")
    private String userName;  //买家姓名

    @Column(name = "orderState")
    private Integer orderState;  //订单状态：10:待付款;20:待发货;30:已发货;40:已完成;50:已关闭;60:已删除; 70：退款中；80：已退款

    @Column(name = "goodsAmount")
    private BigDecimal goodsAmount;  //商品总价格

    @Column(name = "discount")
    private BigDecimal discount;  //优惠总金额

    @Column(name = "orderAmount")
    private BigDecimal orderAmount;  //订单应付金额

    @Column(name = "orderTotalPrice")
    private BigDecimal orderTotalPrice;  //订单总价格

    @Column(name = "paymentId")
    private Long paymentId;  //支付方式id

    @Column(name = "paymentName")
    private String paymentName;  //支付方式名称

    @Column(name = "paymentState")
    private Integer paymentState;  //付款状态:0:未付款;1:已付款(成功),2：付款中

    @Column(name = "tradeSn")
    private String tradeSn;  //交易流水号

    @Column(name = "paymentTime")
    private Date paymentTime;  //支付(付款)时间

    @Column(name = "orderSort")
    private Integer orderSort;  //订单类别 0：普通订单 ，2：积分订单  ,4、团购订单， 6、抢购订单,7、会员订单、8、创新产品订单

    @Column(name = "orderSource")
    private String orderSource;  //订单来源 APP PC 传值全大写

    @Column(name = "orderMessage")
    private String orderMessage;  //订单留言

    @Column(name = "shipTime")
    private String shipTime;  //发货时间

    @Column(name = "shippingExpressId")
    private Long shippingExpressId;  //物流公司ID

    @Column(name = "shippingExpressName")
    private String shippingExpressName;  //物流公司名称

    @Column(name = "shippingCode")
    private String shippingCode;  //物流单号

    @Column(name = "finnshedTime")
    private Date finnshedTime;  //订单完成时间

    @Column(name = "shippingFee")
    private BigDecimal shippingFee;  //运费价格

    @Column(name = "shippingName")
    private String shippingName;  //配送方式

    @Column(name = "evaluationStatus")
    private Integer evaluationStatus;  //评价状态 0为评价，1已评价

    @Column(name = "evaluationTime")
    private Date evaluationTime;  //评价时间

    @Column(name = "evalsellerStatus")
    private Integer evalsellerStatus;  //卖家是否已评价买家

    @Column(name = "evalsellerTime")
    private String evalsellerTime;  //卖家评价买家的时间

    @Column(name = "refundState")
    private Integer refundState;  //退款状态:0是无退款,1是部分退款,2是全部退款

    @Column(name = "returnState")
    private Integer returnState;  //退货状态:0是无退货,1是部分退货,2是全部退货

    @Column(name = "refundAmount")
    private BigDecimal refundAmount;  //退款金额

    @Column(name = "returnNum")
    private Integer returnNum;  //退货数量

    @Column(name = "payId")
    private Long payId;  //订单支付表id

    @Column(name = "paySn")
    private String paySn;  //订单支付表编号

    @Column(name = "balanceState")
    private Integer balanceState;  //结算状态:0,未结算,1已结算

    @Column(name = "balanceTime")
    private String balanceTime;  //结算时间

    @Column(name = "cancelCause")
    private String cancelCause;  //订单取消原因
    
    @Column(name = "payType")
    private String payType;  //支付方式，1人民币，2数字币，3混合币
    
    @Column(name = "coinCode")
    private String coinCode;  //虚拟币币种
    
    @Column(name = "rmbMoney")
    private BigDecimal rmbMoney;  //人民币支付金额
    
    @Column(name = "coinCount")
    private BigDecimal coinCount;  //数字币支付数量
    
    @Column(name = "coinRate")
    private BigDecimal coinRate;  //对数字币比例
    
    @Column(name = "coinPayStatus")
    private Short coinPayStatus;  //虚拟币支付状态,1是成功，2失败
    
    @Column(name = "rmbPayStatus")
    private Short rmbPayStatus;  //人民币支付状态，1是成功，2失败
    
    @Column(name = "rmbFeeMoney")
    private BigDecimal rmbFeeMoney;  //人民币支付手续费金额
    
    @Column(name = "coinFeeCount")
    private BigDecimal coinFeeCount;  //数字币支付手续费数量
    
    @Column(name = "rmbFeeRate")
    private BigDecimal rmbFeeRate;  //人民币支付手续费率
    
    @Column(name = "coinFeeRate")
    private BigDecimal coinFeeRate;  //数字币支付数量
    
    @Column(name = "payEndTime")
    private Date payEndTime;  //支付截止时间，逾期未支付，订单关闭
    
    @Column(name = "cancelType")
    private Short cancelType;  //取消/关闭类别，0客户主动关闭，1系统关闭
    
    @Column(name = "oldOrderState")
    private Integer oldOrderState;  //保存退货申请前的状态

    @Column(name = "payCoin")
    private String payCoin;  //币支付概况

    @Column(name = "merchantId")
    private Long merchantId;  //商户ID

    @Column(name = "merchantName")
    private String merchantName;  //商户名称

    @Column(name = "isAdvance")
    private Short isAdvance;  //是否预售

    @Column(name = "advancePayTime")
    private Date advancePayTime;  //预付款支付时间

    @Column(name = "advancePayCoin")
    private String advancePayCoin;  //预付币支付概况

    @Column(name = "tailPayCoin")
    private String tailPayCoin;  //尾款币支付概况

    @Transient
    private List<OrderGoods> orderGoodsList;
    @Transient
    private Integer orderFinish; //自动结束交易，不能申请售后：单位天
    @Transient
    private Long returnEvidenceCount; //自退货上传图片凭证数量
    @Transient
    private BigDecimal rmbTotalMoney; //人民币需支付总金额
    @Transient
    private BigDecimal coinToalCount; //数币支付总金额
    @Transient
    private Integer validTime;  // 正常订单超过：单位分

    @Transient
    private String toPayCoin;//待支付币种情况，(定金、尾款或者全款)币种_个数,币种_个数


    
    @Transient
    private String  local;  // 语言环境

    public String getToPayCoin() {
        return toPayCoin;
    }

    public void setToPayCoin(String toPayCoin) {
        this.toPayCoin = toPayCoin;
    }

    public String getTailPayCoin() {
        return tailPayCoin;
    }

    public void setTailPayCoin(String tailPayCoin) {
        this.tailPayCoin = tailPayCoin;
    }

    public Short getIsAdvance() {
        return isAdvance;
    }

    public void setIsAdvance(Short isAdvance) {
        this.isAdvance = isAdvance;
    }

    public Date getAdvancePayTime() {
        return advancePayTime;
    }

    public void setAdvancePayTime(Date advancePayTime) {
        this.advancePayTime = advancePayTime;
    }

    public String getAdvancePayCoin() {
        return advancePayCoin;
    }

    public void setAdvancePayCoin(String advancePayCoin) {
        this.advancePayCoin = advancePayCoin;
    }

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public String getPayCoin() {
        return payCoin;
    }

    public void setPayCoin(String payCoin) {
        this.payCoin = payCoin;
    }

    public Integer getOldOrderState() {
		return oldOrderState;
	}

	public void setOldOrderState(Integer oldOrderState) {
		this.oldOrderState = oldOrderState;
	}

	public Date getPayEndTime() {
		return payEndTime;
	}

	public void setPayEndTime(Date payEndTime) {
		this.payEndTime = payEndTime;
	}

	public Short getCancelType() {
		return cancelType;
	}

	public void setCancelType(Short cancelType) {
		this.cancelType = cancelType;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public Integer getValidTime() {
		return validTime;
	}

	public void setValidTime(Integer validTime) {
		this.validTime = validTime;
	}

	public BigDecimal getRmbTotalMoney() {
		return rmbTotalMoney;
	}

	public void setRmbTotalMoney(BigDecimal rmbTotalMoney) {
		this.rmbTotalMoney = rmbTotalMoney;
	}

	public BigDecimal getCoinToalCount() {
		return coinToalCount;
	}

	public void setCoinToalCount(BigDecimal coinToalCount) {
		this.coinToalCount = coinToalCount;
	}

	public BigDecimal getRmbFeeMoney() {
		return rmbFeeMoney;
	}

	public void setRmbFeeMoney(BigDecimal rmbFeeMoney) {
		this.rmbFeeMoney = rmbFeeMoney;
	}

	public BigDecimal getCoinFeeCount() {
		return coinFeeCount;
	}

	public void setCoinFeeCount(BigDecimal coinFeeCount) {
		this.coinFeeCount = coinFeeCount;
	}

	public BigDecimal getRmbFeeRate() {
		return rmbFeeRate;
	}

	public void setRmbFeeRate(BigDecimal rmbFeeRate) {
		this.rmbFeeRate = rmbFeeRate;
	}

	public BigDecimal getCoinFeeRate() {
		return coinFeeRate;
	}

	public void setCoinFeeRate(BigDecimal coinFeeRate) {
		this.coinFeeRate = coinFeeRate;
	}

	public Long getReturnEvidenceCount() {
        return returnEvidenceCount;
    }

    public void setReturnEvidenceCount(Long returnEvidenceCount) {
        this.returnEvidenceCount = returnEvidenceCount;
    }

    public Short getCoinPayStatus() {
		return coinPayStatus;
	}

	public void setCoinPayStatus(Short coinPayStatus) {
		this.coinPayStatus = coinPayStatus;
	}

	public Short getRmbPayStatus() {
		return rmbPayStatus;
	}

	public void setRmbPayStatus(Short rmbPayStatus) {
		this.rmbPayStatus = rmbPayStatus;
	}

	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	public String getCoinCode() {
		return coinCode;
	}

	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}

	public BigDecimal getRmbMoney() {
		return rmbMoney;
	}

	public void setRmbMoney(BigDecimal rmbMoney) {
		this.rmbMoney = rmbMoney;
	}

	public BigDecimal getCoinCount() {
		return coinCount;
	}

	public void setCoinCount(BigDecimal coinCount) {
		this.coinCount = coinCount;
	}

	public BigDecimal getCoinRate() {
		return coinRate;
	}

	public void setCoinRate(BigDecimal coinRate) {
		this.coinRate = coinRate;
	}

	public Integer getOrderFinish() {
        return orderFinish;
    }

    public void setOrderFinish(Integer orderFinish) {
        this.orderFinish = orderFinish;
    }

    public List<OrderGoods> getOrderGoodsList() {
        return orderGoodsList;
    }

    public void setOrderGoodsList(List<OrderGoods> orderGoodsList) {
        this.orderGoodsList = orderGoodsList;
    }

    public String getReceiveName() {
        return receiveName;
    }

    public void setReceiveName(String receiveName) {
        this.receiveName = receiveName;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getCellphone() {
        return cellphone;
    }

    public void setCellphone(String cellphone) {
        this.cellphone = cellphone;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    @Transient
    private String receiveName; // 收货人

    @Transient
    private String detailAddress;  //详细地址

    @Transient
    private String cellphone;  //手机号

    @Transient
    private String goodsName;  //商品名称


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * <p>订单索引id</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Long getId() {
        return id;
    }

    /**
     * <p>订单索引id</p>
     * @author: kongdebiao
     * @param:   @param id
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * <p>订单编号</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getOrderSn() {
        return orderSn;
    }

    /**
     * <p>订单编号</p>
     * @author: kongdebiao
     * @param:   @param orderSn
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderSn(String orderSn) {
        this.orderSn = orderSn;
    }


    /**
     * <p>买家id</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Long getMemberId() {
        return memberId;
    }

    /**
     * <p>买家id</p>
     * @author: kongdebiao
     * @param:   @param memberId
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }


    /**
     * <p>买家姓名</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getMemberName() {
        return memberName;
    }

    /**
     * <p>买家姓名</p>
     * @author: kongdebiao
     * @param:   @param memberName
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }


    /**
     * <p>订单状态：0:已取消;10:待付款;20:待发货;30:待收货;40:交易完成;50:已提交;60:已确认;</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getOrderState() {
        return orderState;
    }

    /**
     * <p>订单状态：0:已取消;10:待付款;20:待发货;30:待收货;40:交易完成;50:已提交;60:已确认;</p>
     * @author: kongdebiao
     * @param:   @param orderState
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderState(Integer orderState) {
        this.orderState = orderState;
    }


    /**
     * <p>商品总价格</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getGoodsAmount() {
        return goodsAmount;
    }

    /**
     * <p>商品总价格</p>
     * @author: kongdebiao
     * @param:   @param goodsAmount
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setGoodsAmount(BigDecimal goodsAmount) {
        this.goodsAmount = goodsAmount;
    }


    /**
     * <p>优惠总金额</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getDiscount() {
        return discount;
    }

    /**
     * <p>优惠总金额</p>
     * @author: kongdebiao
     * @param:   @param discount
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }


    /**
     * <p>订单应付金额</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getOrderAmount() {
        return orderAmount;
    }

    /**
     * <p>订单应付金额</p>
     * @author: kongdebiao
     * @param:   @param orderAmount
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderAmount(BigDecimal orderAmount) {
        this.orderAmount = orderAmount;
    }


    /**
     * <p>订单总价格</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getOrderTotalPrice() {
        return orderTotalPrice;
    }

    /**
     * <p>订单总价格</p>
     * @author: kongdebiao
     * @param:   @param orderTotalPrice
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderTotalPrice(BigDecimal orderTotalPrice) {
        this.orderTotalPrice = orderTotalPrice;
    }


    /**
     * <p>支付方式id</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Long getPaymentId() {
        return paymentId;
    }

    /**
     * <p>支付方式id</p>
     * @author: kongdebiao
     * @param:   @param paymentId
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }


    /**
     * <p>支付方式名称</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getPaymentName() {
        return paymentName;
    }

    /**
     * <p>支付方式名称</p>
     * @author: kongdebiao
     * @param:   @param paymentName
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }


    /**
     * <p>付款状态:0:未付款;1:已付款</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getPaymentState() {
        return paymentState;
    }

    /**
     * <p>付款状态:0:未付款;1:已付款</p>
     * @author: kongdebiao
     * @param:   @param paymentState
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setPaymentState(Integer paymentState) {
        this.paymentState = paymentState;
    }


    /**
     * <p>交易流水号</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getTradeSn() {
        return tradeSn;
    }

    /**
     * <p>交易流水号</p>
     * @author: kongdebiao
     * @param:   @param tradeSn
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setTradeSn(String tradeSn) {
        this.tradeSn = tradeSn;
    }


    /**
     * <p>支付(付款)时间</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
     

    /**
     * <p>订单类别 0：普通订单 </p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getOrderSort() {
        return orderSort;
    }

    public Date getPaymentTime() {
		return paymentTime;
	}

	public void setPaymentTime(Date paymentTime) {
		this.paymentTime = paymentTime;
	}

	/**
     * <p>订单类别 0：普通订单 </p>
     * @author: kongdebiao
     * @param:   @param orderSort
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderSort(Integer orderSort) {
        this.orderSort = orderSort;
    }


    /**
     * <p>订单来源 APP PC 传值全大写 </p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getOrderSource() {
        return orderSource;
    }

    /**
     * <p>订单来源 APP PC 传值全大写 </p>
     * @author: kongdebiao
     * @param:   @param orderSource
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderSource(String orderSource) {
        this.orderSource = orderSource;
    }


    /**
     * <p>订单留言</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getOrderMessage() {
        return orderMessage;
    }

    /**
     * <p>订单留言</p>
     * @author: kongdebiao
     * @param:   @param orderMessage
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setOrderMessage(String orderMessage) {
        this.orderMessage = orderMessage;
    }


    /**
     * <p>发货时间</p>
     * @author: kongdebiao
     * @return: Date
     * @Date :   2018-11-16 10:31:38
     */
    public String getShipTime() {
        return shipTime;
    }

    /**
     * <p>发货时间</p>
     * @author: kongdebiao
     * @param:   @param shipTime
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShipTime(String shipTime) {
        this.shipTime = shipTime;
    }


    /**
     * <p>物流公司ID</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Long getShippingExpressId() {
        return shippingExpressId;
    }

    /**
     * <p>物流公司ID</p>
     * @author: kongdebiao
     * @param:   @param shippingExpressId
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShippingExpressId(Long shippingExpressId) {
        this.shippingExpressId = shippingExpressId;
    }


    /**
     * <p>物流公司名称</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getShippingExpressName() {
        return shippingExpressName;
    }

    /**
     * <p>物流公司名称</p>
     * @author: kongdebiao
     * @param:   @param shippingExpressName
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShippingExpressName(String shippingExpressName) {
        this.shippingExpressName = shippingExpressName;
    }


    /**
     * <p>物流单号</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getShippingCode() {
        return shippingCode;
    }

    /**
     * <p>物流单号</p>
     * @author: kongdebiao
     * @param:   @param shippingCode
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShippingCode(String shippingCode) {
        this.shippingCode = shippingCode;
    }


    /**
     * <p>订单完成时间</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Date getFinnshedTime() {
        return finnshedTime;
    }

    /**
     * <p>订单完成时间</p>
     * @author: kongdebiao
     * @param:   @param finnshedTime
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setFinnshedTime(Date finnshedTime) {
        this.finnshedTime = finnshedTime;
    }


    /**
     * <p>运费价格</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getShippingFee() {
        return shippingFee;
    }

    /**
     * <p>运费价格</p>
     * @author: kongdebiao
     * @param:   @param shippingFee
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShippingFee(BigDecimal shippingFee) {
        this.shippingFee = shippingFee;
    }


    /**
     * <p>配送方式</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getShippingName() {
        return shippingName;
    }

    /**
     * <p>配送方式</p>
     * @author: kongdebiao
     * @param:   @param shippingName
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setShippingName(String shippingName) {
        this.shippingName = shippingName;
    }


    /**
     * <p>评价状态 0为评价，1已评价</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getEvaluationStatus() {
        return evaluationStatus;
    }

    /**
     * <p>评价状态 0为评价，1已评价</p>
     * @author: kongdebiao
     * @param:   @param evaluationStatus
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setEvaluationStatus(Integer evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }


//    /**
//     * <p>评价时间</p>
//     * @author: kongdebiao
//     * @return: Long
//     * @Date :   2018-11-16 10:31:38
//     */
//    public String getEvaluationTime() {
//        return evaluationTime;
//    }
//
//    /**
//     * <p>评价时间</p>
//     * @author: kongdebiao
//     * @param:   @param evaluationTime
//     * @return: void
//     * @Date :   2018-11-16 10:31:38
//     */
//    public void setEvaluationTime(String evaluationTime) {
//        this.evaluationTime = evaluationTime;
//    }


    public Date getEvaluationTime() {
        return evaluationTime;
    }

    public void setEvaluationTime(Date evaluationTime) {
        this.evaluationTime = evaluationTime;
    }

    /**
     * <p>卖家是否已评价买家</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getEvalsellerStatus() {
        return evalsellerStatus;
    }

    /**
     * <p>卖家是否已评价买家</p>
     * @author: kongdebiao
     * @param:   @param evalsellerStatus
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setEvalsellerStatus(Integer evalsellerStatus) {
        this.evalsellerStatus = evalsellerStatus;
    }


    /**
     * <p>卖家评价买家的时间</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public String getEvalsellerTime() {
        return evalsellerTime;
    }

    /**
     * <p>卖家评价买家的时间</p>
     * @author: kongdebiao
     * @param:   @param evalsellerTime
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setEvalsellerTime(String evalsellerTime) {
        this.evalsellerTime = evalsellerTime;
    }


    /**
     * <p>退款状态:0是无退款,1是部分退款,2是全部退款</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getRefundState() {
        return refundState;
    }

    /**
     * <p>退款状态:0是无退款,1是部分退款,2是全部退款</p>
     * @author: kongdebiao
     * @param:   @param refundState
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setRefundState(Integer refundState) {
        this.refundState = refundState;
    }


    /**
     * <p>退货状态:0是无退货,1是部分退货,2是全部退货</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getReturnState() {
        return returnState;
    }

    /**
     * <p>退货状态:0是无退货,1是部分退货,2是全部退货</p>
     * @author: kongdebiao
     * @param:   @param returnState
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setReturnState(Integer returnState) {
        this.returnState = returnState;
    }


    /**
     * <p>退款金额</p>
     * @author: kongdebiao
     * @return: BigDecimal
     * @Date :   2018-11-16 10:31:38
     */
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }

    /**
     * <p>退款金额</p>
     * @author: kongdebiao
     * @param:   @param refundAmount
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }


    /**
     * <p>退货数量</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getReturnNum() {
        return returnNum;
    }

    /**
     * <p>退货数量</p>
     * @author: kongdebiao
     * @param:   @param returnNum
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setReturnNum(Integer returnNum) {
        this.returnNum = returnNum;
    }


    /**
     * <p>订单支付表id</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public Long getPayId() {
        return payId;
    }

    /**
     * <p>订单支付表id</p>
     * @author: kongdebiao
     * @param:   @param payId
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setPayId(Long payId) {
        this.payId = payId;
    }


    /**
     * <p>订单支付表编号</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getPaySn() {
        return paySn;
    }

    /**
     * <p>订单支付表编号</p>
     * @author: kongdebiao
     * @param:   @param paySn
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setPaySn(String paySn) {
        this.paySn = paySn;
    }


    /**
     * <p>结算状态:0,未结算,1已结算</p>
     * @author: kongdebiao
     * @return: Integer
     * @Date :   2018-11-16 10:31:38
     */
    public Integer getBalanceState() {
        return balanceState;
    }

    /**
     * <p>结算状态:0,未结算,1已结算</p>
     * @author: kongdebiao
     * @param:   @param balanceState
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setBalanceState(Integer balanceState) {
        this.balanceState = balanceState;
    }


    /**
     * <p>结算时间</p>
     * @author: kongdebiao
     * @return: Long
     * @Date :   2018-11-16 10:31:38
     */
    public String getBalanceTime() {
        return balanceTime;
    }

    /**
     * <p>结算时间</p>
     * @author: kongdebiao
     * @param:   @param balanceTime
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setBalanceTime(String balanceTime) {
        this.balanceTime = balanceTime;
    }


    /**
     * <p>订单取消原因</p>
     * @author: kongdebiao
     * @return: String
     * @Date :   2018-11-16 10:31:38
     */
    public String getCancelCause() {
        return cancelCause;
    }

    /**
     * <p>订单取消原因</p>
     * @author: kongdebiao
     * @param:   @param cancelCause
     * @return: void
     * @Date :   2018-11-16 10:31:38
     */
    public void setCancelCause(String cancelCause) {
        this.cancelCause = cancelCause;
    }


}
