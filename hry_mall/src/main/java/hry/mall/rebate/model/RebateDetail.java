/**
 * Copyright:   
 * @author:      luyue
 * @version:     V1.0 
 * @Date:        2019-11-28 17:24:59 
 */
package hry.mall.rebate.model;


import hry.bean.BaseModel;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p> RebateDetail </p>
 * @author:         luyue
 * @Date :          2019-11-28 17:24:59  
 */
@Table(name="shop_rebate_detail")
public class RebateDetail extends BaseModel {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;  //索引id
	
	@Column(name= "memberId")
	private Long memberId;  //用户id
	
	@Column(name= "coinId")
	private Long coinId;  //支付币种表id
	
	@Column(name= "type")
	private Integer type;  //类型 1商户结算，2购物返利，3推荐返佣，4创新商品奖励
	
	@Column(name= "recordType")
	private Integer recordType;  //操作类型 （ 1增加 2减少）
	
	@Column(name= "coinCount")
	private BigDecimal coinCount;  //数字币数量
	
	@Column(name= "coinCode")
	private String coinCode;  //虚拟币币种
	
	@Column(name= "orderId")
	private Long orderId;  //订单Id
	
	@Column(name= "orderSn")
	private String orderSn;  //订单编号
	
	@Column(name= "orderDate")
	private Date orderDate;  //下单时间
	
	@Column(name= "orderMoney")
	private BigDecimal orderMoney;  //金额
	
	@Column(name= "status")
	private Integer status;  //0 待发放 1 已发放 
	
	@Column(name= "memberName")
	private String memberName;  //用户名
	
	@Column(name= "estimateDate")
	private Date estimateDate;  //预计发放时间
	
	@Column(name= "rewardLevel")
	private String rewardLevel;  //分销级别
	
	@Column(name= "remark")
	private String remark;  //备注

	@Column(name= "isCochain")
	private Integer isCochain;  //是否上链，1是0否

	public Integer getIsCochain() {
		return isCochain;
	}

	public void setIsCochain(Integer isCochain) {
		this.isCochain = isCochain;
	}

	/**
	 * <p>索引id</p>
	 * @author:  luyue
	 * @return:  Long 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * <p>索引id</p>
	 * @author:  luyue
	 * @param:   @param id
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	
	/**
	 * <p>用户id</p>
	 * @author:  luyue
	 * @return:  Long 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Long getMemberId() {
		return memberId;
	}
	
	/**
	 * <p>用户id</p>
	 * @author:  luyue
	 * @param:   @param memberId
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setMemberId(Long memberId) {
		this.memberId = memberId;
	}
	
	
	/**
	 * <p>支付币种表id</p>
	 * @author:  luyue
	 * @return:  Long 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Long getCoinId() {
		return coinId;
	}
	
	/**
	 * <p>支付币种表id</p>
	 * @author:  luyue
	 * @param:   @param coinId
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setCoinId(Long coinId) {
		this.coinId = coinId;
	}
	
	
	/**
	 * <p>类型 1商户结算，2购物返利，3推荐返佣，4创新商品奖励</p>
	 * @author:  luyue
	 * @return:  Integer 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Integer getType() {
		return type;
	}
	
	/**
	 * <p>类型 1商户结算，2购物返利，3推荐返佣，4创新商品奖励</p>
	 * @author:  luyue
	 * @param:   @param type
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setType(Integer type) {
		this.type = type;
	}
	
	
	/**
	 * <p>操作类型 （ 1增加 2减少）</p>
	 * @author:  luyue
	 * @return:  Integer 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Integer getRecordType() {
		return recordType;
	}
	
	/**
	 * <p>操作类型 （ 1增加 2减少）</p>
	 * @author:  luyue
	 * @param:   @param recordType
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setRecordType(Integer recordType) {
		this.recordType = recordType;
	}
	
	
	/**
	 * <p>数字币数量</p>
	 * @author:  luyue
	 * @return:  BigDecimal 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public BigDecimal getCoinCount() {
		return coinCount;
	}
	
	/**
	 * <p>数字币数量</p>
	 * @author:  luyue
	 * @param:   @param coinCount
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setCoinCount(BigDecimal coinCount) {
		this.coinCount = coinCount;
	}
	
	
	/**
	 * <p>虚拟币币种</p>
	 * @author:  luyue
	 * @return:  String 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public String getCoinCode() {
		return coinCode;
	}
	
	/**
	 * <p>虚拟币币种</p>
	 * @author:  luyue
	 * @param:   @param coinCode
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}
	
	
	/**
	 * <p>订单Id</p>
	 * @author:  luyue
	 * @return:  Long 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Long getOrderId() {
		return orderId;
	}
	
	/**
	 * <p>订单Id</p>
	 * @author:  luyue
	 * @param:   @param orderId
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setOrderId(Long orderId) {
		this.orderId = orderId;
	}
	
	
	/**
	 * <p>订单编号</p>
	 * @author:  luyue
	 * @return:  String 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public String getOrderSn() {
		return orderSn;
	}
	
	/**
	 * <p>订单编号</p>
	 * @author:  luyue
	 * @param:   @param orderSn
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setOrderSn(String orderSn) {
		this.orderSn = orderSn;
	}
	
	
	/**
	 * <p>下单时间</p>
	 * @author:  luyue
	 * @return:  Date 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Date getOrderDate() {
		return orderDate;
	}
	
	/**
	 * <p>下单时间</p>
	 * @author:  luyue
	 * @param:   @param orderDate
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setOrderDate(Date orderDate) {
		this.orderDate = orderDate;
	}
	
	
	/**
	 * <p>金额</p>
	 * @author:  luyue
	 * @return:  BigDecimal 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public BigDecimal getOrderMoney() {
		return orderMoney;
	}
	
	/**
	 * <p>金额</p>
	 * @author:  luyue
	 * @param:   @param orderMoney
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setOrderMoney(BigDecimal orderMoney) {
		this.orderMoney = orderMoney;
	}
	
	
	/**
	 * <p>0 待发放 1 已发放 </p>
	 * @author:  luyue
	 * @return:  Integer 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Integer getStatus() {
		return status;
	}
	
	/**
	 * <p>0 待发放 1 已发放 </p>
	 * @author:  luyue
	 * @param:   @param status
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	
	/**
	 * <p>用户名</p>
	 * @author:  luyue
	 * @return:  String 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public String getMemberName() {
		return memberName;
	}
	
	/**
	 * <p>用户名</p>
	 * @author:  luyue
	 * @param:   @param memberName
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setMemberName(String memberName) {
		this.memberName = memberName;
	}
	
	
	/**
	 * <p>预计发放时间</p>
	 * @author:  luyue
	 * @return:  Date 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public Date getEstimateDate() {
		return estimateDate;
	}
	
	/**
	 * <p>预计发放时间</p>
	 * @author:  luyue
	 * @param:   @param estimateDate
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setEstimateDate(Date estimateDate) {
		this.estimateDate = estimateDate;
	}
	
	
	/**
	 * <p>分销级别</p>
	 * @author:  luyue
	 * @return:  String 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public String getRewardLevel() {
		return rewardLevel;
	}
	
	/**
	 * <p>分销级别</p>
	 * @author:  luyue
	 * @param:   @param rewardLevel
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setRewardLevel(String rewardLevel) {
		this.rewardLevel = rewardLevel;
	}
	
	
	/**
	 * <p>备注</p>
	 * @author:  luyue
	 * @return:  String 
	 * @Date :   2019-11-28 17:24:59    
	 */
	public String getRemark() {
		return remark;
	}
	
	/**
	 * <p>备注</p>
	 * @author:  luyue
	 * @param:   @param remark
	 * @return:  void 
	 * @Date :   2019-11-28 17:24:59   
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	

}
