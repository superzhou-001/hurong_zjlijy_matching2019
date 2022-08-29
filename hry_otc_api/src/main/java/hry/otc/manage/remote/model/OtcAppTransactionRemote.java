/**
 * Copyright:   北京互融时代软件有限公司
 * @author:      denghf
 * @version:     V1.0
 * @Date:        2018-06-25 18:06:51
 */
package hry.otc.manage.remote.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * OtcAppTransaction
 * </p>
 *
 * @author: denghf
 * @Date : 2018-06-25 18:06:51
 */
@ApiModel(value = "OTC订单实体类")
public class OtcAppTransactionRemote implements Serializable {

	private Long id; // id

	@ApiModelProperty(value = "交易订单号")
	private String transactionNum;

	@ApiModelProperty(value = "用户id")
	private Long customerId;

	@ApiModelProperty(value = "虚拟账户id")
	private Long accountId;

	@ApiModelProperty(value = "买方ID")
	private Long buyUserId;

	@ApiModelProperty(value = "买方用户名")
	private String buyUserName;

	@ApiModelProperty(value = "买方手续费")
	private BigDecimal buyfee;

	@ApiModelProperty(value = "卖方ID")
	private Long sellUserId;

	@ApiModelProperty(value = "卖方用户名")
	private String sellUserName;

	@ApiModelProperty(value = "卖方手续费")
	private BigDecimal sellfee;

	@ApiModelProperty(value = "币种代码")
	private String coinCode;

	@ApiModelProperty(value = "交易方式(1在线购买,2在线出售,3本地购买)")
	private Integer transactionMode;

	@ApiModelProperty(value = "交易类型(1银行转账,2支付宝,3微信支付)")
	private String payType;

	@ApiModelProperty(value = "交易数量")
	private BigDecimal tradeNum;

	@ApiModelProperty(value = "交易金额")
	private BigDecimal tradeMoney;

	@ApiModelProperty(value = "交易类型(1定价交易 2市价交易)")
	private Integer transactionType;

	@ApiModelProperty(value = "1待支付 2已付款待确认 3已完成 4申诉中待回复 5已取消 6申请退款中 7退款已驳回" +
			"8申诉完成 9申诉成功,待确认 10申诉失败,待确认 11平台通过申诉 12平台驳回申诉 13退款成功")
	private Integer status;

	@ApiModelProperty(value = "备注")
	private String remark;

	@ApiModelProperty(value = "广告备注")
	private String advRemark;

	@ApiModelProperty(value = "transactionMode为1时,为买家购买时间,为2时,为卖家购买时间")
	private Date payTime;

	@ApiModelProperty(value = "transactionMode为1时,为买家确认收款时间,为2时,为卖家确认收款时间")
	private Date confirmMoney;

	@ApiModelProperty(value = "transactionMode为1时,为买家申诉时间,为2时,为卖家申诉时间")
	private Date appealTime;

	@ApiModelProperty(value = "广告ID")
	private Long advertisementId;

	@ApiModelProperty(value = "付款参考号")
	private String referenceNum;

	@ApiModelProperty(value = "卖方是否删除")
	private Integer sellIsDeleted;

	@ApiModelProperty(value = "买方是否删除")
	private Integer buyIsDeleted;

	@ApiModelProperty(value = "支付信息记录id")
	private Long bankId;

	@ApiModelProperty(value = "银行卡号")
	private String bankNumber;

	@ApiModelProperty(value = "支付信息记录id")
	private Long alipayId;

	@ApiModelProperty(value = "支付宝账号")
	private String alipayAccount;

	@ApiModelProperty(value = "支付宝二维码")
	private String alipayThingUrl;

	@ApiModelProperty(value = "支付信息记录id")
	private Long wechatId;

	@ApiModelProperty(value = "微信账号")
	private String wechatAccount;

	@ApiModelProperty(value = "微信二维码")
	private String wechatThingUrl;

	private String saasId;

	private Date created;

	private Date modified;

	private String createdString;

	@ApiModelProperty(value = "交易单申诉人id")
	private Long appealId;

	@ApiModelProperty(value = "当前登录用户")
	private Long idNow;

	@ApiModelProperty(value = "状态中文意思")
	private String stateZHCN;

	@ApiModelProperty(value = "付款参考码")
	private String paymentCode;

	@ApiModelProperty(value = "付款参考码")
	private String legalCurrency;

	@ApiModelProperty(value = "吃单支付方式")
	private String selPay;

	@ApiModelProperty(value = "买方实名")
	private String buySureName;

	@ApiModelProperty(value = "买方实名")
	private String buyTrueName;

	@ApiModelProperty(value = "卖方实名")
	private String sellSureName;

	@ApiModelProperty(value = "卖方实名")
	private String sellTrueName;

	@ApiModelProperty(value = "交易价格")
	private BigDecimal tradePrice;

	@ApiModelProperty(value = "交易金额(广告的最终单价)")
	private BigDecimal advertisementTradeMoney;  //交易金额(广告的最终单价)

	@ApiModelProperty(value = "保留小数位")
	private Integer keepDecimalForCoin;//保留小数位

	@ApiModelProperty(value = "联系手机号")
	private String cellPhone; // 联系手机号

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public String getAdvRemark() {
		return advRemark;
	}

	public void setAdvRemark(String advRemark) {
		this.advRemark = advRemark;
	}

	public BigDecimal getAdvertisementTradeMoney() {
		return advertisementTradeMoney;
	}

	public void setAdvertisementTradeMoney(BigDecimal advertisementTradeMoney) {
		this.advertisementTradeMoney = advertisementTradeMoney;
	}

	public Integer getKeepDecimalForCoin () {
		return keepDecimalForCoin;
	}

	public void setKeepDecimalForCoin (Integer keepDecimalForCoin) {
		this.keepDecimalForCoin = keepDecimalForCoin;
	}

	public BigDecimal getTradePrice () {
		return tradePrice;
	}

	public void setTradePrice (BigDecimal tradePrice) {
		this.tradePrice = tradePrice;
	}

	public String getBuySureName () {
		return buySureName;
	}

	public void setBuySureName (String buySureName) {
		this.buySureName = buySureName;
	}

	public String getSellSureName () {
		return sellSureName;
	}

	public void setSellSureName (String sellSureName) {
		this.sellSureName = sellSureName;
	}

	public String getBuyTrueName () {
		return buyTrueName;
	}

	public void setBuyTrueName (String buyTrueName) {
		this.buyTrueName = buyTrueName;
	}

	public String getSellTrueName () {
		return sellTrueName;
	}

	public void setSellTrueName (String sellTrueName) {
		this.sellTrueName = sellTrueName;
	}

	public String getSelPay() {
		return selPay;
	}

	public void setSelPay(String selPay) {
		this.selPay = selPay;
	}

	public String getPaymentCode() {
		return paymentCode;
	}

	public void setPaymentCode(String paymentCode) {
		this.paymentCode = paymentCode;
	}

	public String getLegalCurrency() {
		return legalCurrency;
	}

	public void setLegalCurrency(String legalCurrency) {
		this.legalCurrency = legalCurrency;
	}

	public String getStateZHCN() {
		return stateZHCN;
	}

	public void setStateZHCN(String stateZHCN) {
		this.stateZHCN = stateZHCN;
	}

	public Long getIdNow() {
		return idNow;
	}

	public void setIdNow(Long idNow) {
		this.idNow = idNow;
	}

	public Long getAppealId() {
		return appealId;
	}

	public void setAppealId(Long appealId) {
		this.appealId = appealId;
	}

	public String getCreatedString() {
		return createdString;
	}

	public void setCreatedString(String createdString) {
		this.createdString = createdString;
	}

	public String getSaasId() {
		return saasId;
	}

	public void setSaasId(String saasId) {
		this.saasId = saasId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public Long getAccountId() {
		return accountId;
	}

	public void setAccountId(Long accountId) {
		this.accountId = accountId;
	}

	public Long getBuyUserId() {
		return buyUserId;
	}

	public void setBuyUserId(Long buyUserId) {
		this.buyUserId = buyUserId;
	}

	public Long getSellUserId() {
		return sellUserId;
	}

	public void setSellUserId(Long sellUserId) {
		this.sellUserId = sellUserId;
	}

	public Long getAdvertisementId() {
		return advertisementId;
	}

	public void setAdvertisementId(Long advertisementId) {
		this.advertisementId = advertisementId;
	}

	public Long getBankId() {
		return bankId;
	}

	public void setBankId(Long bankId) {
		this.bankId = bankId;
	}

	public Long getAlipayId() {
		return alipayId;
	}

	public void setAlipayId(Long alipayId) {
		this.alipayId = alipayId;
	}

	public Long getWechatId() {
		return wechatId;
	}

	public void setWechatId(Long wechatId) {
		this.wechatId = wechatId;
	}

	/**
	 * <p>
	 * 交易订单号
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getTransactionNum() {
		return transactionNum;
	}

	/**
	 * <p>
	 * 交易订单号
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             transactionNum
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setTransactionNum(String transactionNum) {
		this.transactionNum = transactionNum;
	}

	/**
	 * <p>
	 * 买方用户名
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getBuyUserName() {
		return buyUserName;
	}

	/**
	 * <p>
	 * 买方用户名
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             buyUserName
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setBuyUserName(String buyUserName) {
		this.buyUserName = buyUserName;
	}

	/**
	 * <p>
	 * 买方手续费
	 * </p>
	 *
	 * @author: denghf
	 * @return: BigDecimal
	 * @Date : 2018-06-25 18:06:51
	 */
	public BigDecimal getBuyfee() {
		return buyfee;
	}

	/**
	 * <p>
	 * 买方手续费
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             buyfee
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setBuyfee(BigDecimal buyfee) {
		this.buyfee = buyfee;
	}

	/**
	 * <p>
	 * 卖方用户名
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getSellUserName() {
		return sellUserName;
	}

	/**
	 * <p>
	 * 卖方用户名
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             sellUserName
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setSellUserName(String sellUserName) {
		this.sellUserName = sellUserName;
	}

	/**
	 * <p>
	 * 卖方手续费
	 * </p>
	 *
	 * @author: denghf
	 * @return: BigDecimal
	 * @Date : 2018-06-25 18:06:51
	 */
	public BigDecimal getSellfee() {
		return sellfee;
	}

	/**
	 * <p>
	 * 卖方手续费
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             sellfee
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setSellfee(BigDecimal sellfee) {
		this.sellfee = sellfee;
	}

	/**
	 * <p>
	 * 币种代码
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getCoinCode() {
		return coinCode;
	}

	/**
	 * <p>
	 * 币种代码
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             coinCode
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}

	/**
	 * <p>
	 * 交易方式(1在线购买,2在线出售,3本地购买)
	 * </p>
	 *
	 * @author: denghf
	 * @return: Integer
	 * @Date : 2018-06-25 18:06:51
	 */
	public Integer getTransactionMode() {
		return transactionMode;
	}

	/**
	 * <p>
	 * 交易方式(1在线购买,2在线出售,3本地购买)
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             transactionMode
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setTransactionMode(Integer transactionMode) {
		this.transactionMode = transactionMode;
	}

	/**
	 * <p>
	 * 交易类型(1银行转账,2支付宝,3微信支付)
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getPayType() {
		return payType;
	}

	/**
	 * <p>
	 * 交易类型(1银行转账,2支付宝,3微信支付)
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             payType
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setPayType(String payType) {
		this.payType = payType;
	}

	/**
	 * <p>
	 * 交易数量
	 * </p>
	 *
	 * @author: denghf
	 * @return: BigDecimal
	 * @Date : 2018-06-25 18:06:51
	 */
	public BigDecimal getTradeNum() {
		return tradeNum;
	}

	/**
	 * <p>
	 * 交易数量
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             tradeNum
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setTradeNum(BigDecimal tradeNum) {
		this.tradeNum = tradeNum;
	}

	/**
	 * <p>
	 * 交易金额
	 * </p>
	 *
	 * @author: denghf
	 * @return: BigDecimal
	 * @Date : 2018-06-25 18:06:51
	 */
	public BigDecimal getTradeMoney() {
		return tradeMoney;
	}

	/**
	 * <p>
	 * 交易金额
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             tradeMoney
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setTradeMoney(BigDecimal tradeMoney) {
		this.tradeMoney = tradeMoney;
	}

	/**
	 * <p>
	 * 交易类型(1定价交易 2市价交易)
	 * </p>
	 *
	 * @author: denghf
	 * @return: Integer
	 * @Date : 2018-06-25 18:06:51
	 */
	public Integer getTransactionType() {
		return transactionType;
	}

	/**
	 * <p>
	 * 交易类型(1定价交易 2市价交易)
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             transactionType
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setTransactionType(Integer transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * <p>
	 * 1待支付 2已付款待确认 3已完成 4申诉中待回复 5已取消 6申请退款中 7退款已驳回 8申诉完成 9申诉成功,待确认 10申诉失败,待确认
	 * 11平台通过申诉 12平台驳回申诉 13退款成功
	 * </p>
	 *
	 * @author: denghf
	 * @return: Integer
	 * @Date : 2018-06-25 18:06:51
	 */
	public Integer getStatus() {
		return status;
	}

	/**
	 * <p>
	 * 1待支付 2已付款待确认 3已完成 4申诉中待回复 5已取消 6申请退款中 7退款已驳回 8申诉完成 9申诉成功,待确认 10申诉失败,待确认
	 * 11平台通过申诉 12平台驳回申诉 13退款成功
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             status
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setStatus(Integer status) {
		this.status = status;
	}

	/**
	 * <p>
	 * 备注
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * <p>
	 * 备注
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             remark
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家购买时间,为2时,为卖家购买时间
	 * </p>
	 *
	 * @author: denghf
	 * @return: Date
	 * @Date : 2018-06-25 18:06:51
	 */
	public Date getPayTime() {
		return payTime;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家购买时间,为2时,为卖家购买时间
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             payTime
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家确认收款时间,为2时,为卖家确认收款时间
	 * </p>
	 *
	 * @author: denghf
	 * @return: Date
	 * @Date : 2018-06-25 18:06:51
	 */
	public Date getConfirmMoney() {
		return confirmMoney;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家确认收款时间,为2时,为卖家确认收款时间
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             confirmMoney
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setConfirmMoney(Date confirmMoney) {
		this.confirmMoney = confirmMoney;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家申诉时间,为2时,为卖家申诉时间
	 * </p>
	 *
	 * @author: denghf
	 * @return: Date
	 * @Date : 2018-06-25 18:06:51
	 */
	public Date getAppealTime() {
		return appealTime;
	}

	/**
	 * <p>
	 * transactionMode为1时,为买家申诉时间,为2时,为卖家申诉时间
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             appealTime
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setAppealTime(Date appealTime) {
		this.appealTime = appealTime;
	}

	/**
	 * <p>
	 * 付款参考号
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getReferenceNum() {
		return referenceNum;
	}

	/**
	 * <p>
	 * 付款参考号
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             referenceNum
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setReferenceNum(String referenceNum) {
		this.referenceNum = referenceNum;
	}

	/**
	 * <p>
	 * 卖方是否删除
	 * </p>
	 *
	 * @author: denghf
	 * @return: Integer
	 * @Date : 2018-06-25 18:06:51
	 */
	public Integer getSellIsDeleted() {
		return sellIsDeleted;
	}

	/**
	 * <p>
	 * 卖方是否删除
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             sellIsDeleted
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setSellIsDeleted(Integer sellIsDeleted) {
		this.sellIsDeleted = sellIsDeleted;
	}

	/**
	 * <p>
	 * 买方是否删除
	 * </p>
	 *
	 * @author: denghf
	 * @return: Integer
	 * @Date : 2018-06-25 18:06:51
	 */
	public Integer getBuyIsDeleted() {
		return buyIsDeleted;
	}

	/**
	 * <p>
	 * 买方是否删除
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             buyIsDeleted
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setBuyIsDeleted(Integer buyIsDeleted) {
		this.buyIsDeleted = buyIsDeleted;
	}

	/**
	 * <p>
	 * 银行卡号
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getBankNumber() {
		return bankNumber;
	}

	/**
	 * <p>
	 * 银行卡号
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             bankNumber
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setBankNumber(String bankNumber) {
		this.bankNumber = bankNumber;
	}

	/**
	 * <p>
	 * 支付宝账号
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getAlipayAccount() {
		return alipayAccount;
	}

	/**
	 * <p>
	 * 支付宝账号
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             alipayAccount
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setAlipayAccount(String alipayAccount) {
		this.alipayAccount = alipayAccount;
	}

	/**
	 * <p>
	 * 支付宝二维码
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getAlipayThingUrl() {
		return alipayThingUrl;
	}

	/**
	 * <p>
	 * 支付宝二维码
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             alipayThingUrl
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setAlipayThingUrl(String alipayThingUrl) {
		this.alipayThingUrl = alipayThingUrl;
	}

	/**
	 * <p>
	 * 微信账号
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getWechatAccount() {
		return wechatAccount;
	}

	/**
	 * <p>
	 * 微信账号
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             wechatAccount
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setWechatAccount(String wechatAccount) {
		this.wechatAccount = wechatAccount;
	}

	/**
	 * <p>
	 * 微信二维码
	 * </p>
	 *
	 * @author: denghf
	 * @return: String
	 * @Date : 2018-06-25 18:06:51
	 */
	public String getWechatThingUrl() {
		return wechatThingUrl;
	}

	/**
	 * <p>
	 * 微信二维码
	 * </p>
	 *
	 * @author: denghf
	 * @param: @param
	 *             wechatThingUrl
	 * @return: void
	 * @Date : 2018-06-25 18:06:51
	 */
	public void setWechatThingUrl(String wechatThingUrl) {
		this.wechatThingUrl = wechatThingUrl;
	}

}
