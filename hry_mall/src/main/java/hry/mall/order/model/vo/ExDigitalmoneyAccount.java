package hry.mall.order.model.vo;

import hry.bean.BaseModel;

import java.math.BigDecimal;

public class ExDigitalmoneyAccount extends BaseModel {

	// 主键
	private Long id;
	// 版本字段
	private Integer version;
	// 用户id
	private Long customerId;
	// 可用总额
	private BigDecimal hotMoney;
	// 冻结总额
	private BigDecimal coldMoney;
	// 用户登录名
	private String userName;
	
	private String trueName;
	
	//交易账户类型 cny,usd
	private String currencyType;
	
	//虚拟币种类型 btc ,ltc ,eth
	private String coinCode;
	
	//站点类别 en ,cn
	private String website;
	
	// 虚拟币种名称（比特币  莱特币 ...）
	private String coinName;
	
	// 虚拟账号（证件号+账户类型 0 线上注册用户 1线下注册用户）
	private String accountNum;
	// 账户状态（0 不可用1可用）
	private Integer status;
	// 公钥
	private String publicKey;
	// 私钥
	private String privateKey;
	// 借款总额
	private BigDecimal lendMoney;
	// 禁用
	private BigDecimal disableMoney;
	
	
	private BigDecimal psitioNaveragePrice;
	private BigDecimal psitioProtectPrice;
	private BigDecimal sumCost;
//	private AppPersonInfo appPersonInfo;
	
	private BigDecimal positionAvePrice;  //持有均价
	
	private  BigDecimal floatprofitandlossMoney;//浮动盈亏
	
	private  BigDecimal floatprofitandlossMoneyRate;//浮动盈亏
	
	
	
	/**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 */
	public BigDecimal getFloatprofitandlossMoney() {
		return floatprofitandlossMoney;
	}

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setFloatprofitandlossMoney(BigDecimal floatprofitandlossMoney) {
		this.floatprofitandlossMoney = floatprofitandlossMoney;
	}

	/**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 */
	public BigDecimal getFloatprofitandlossMoneyRate() {
		return floatprofitandlossMoneyRate;
	}

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setFloatprofitandlossMoneyRate(
			BigDecimal floatprofitandlossMoneyRate) {
		this.floatprofitandlossMoneyRate = floatprofitandlossMoneyRate;
	}

	/**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 */
	public BigDecimal getPositionAvePrice() {
		return positionAvePrice;
	}

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setPositionAvePrice(BigDecimal positionAvePrice) {
		this.positionAvePrice = positionAvePrice;
	}

	public String getTrueName() {
		return trueName;
	}

	public void setTrueName(String trueName) {
		this.trueName = trueName;
	}

	/**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 */
	public BigDecimal getSumCost() {
		return sumCost;
	}

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setSumCost(BigDecimal sumCost) {
		this.sumCost = sumCost;
	}

	/**
	 * <p> TODO</p>
	 * @return:     AppPersonInfo
	 */
/*	public AppPersonInfo getAppPersonInfo() {
		return appPersonInfo;
	}

	*//**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 *//*
	public BigDecimal getPsitioNaveragePrice() {
		return psitioNaveragePrice;
	}*/

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setPsitioNaveragePrice(BigDecimal psitioNaveragePrice) {
		this.psitioNaveragePrice = psitioNaveragePrice;
	}

	/**
	 * <p> TODO</p>
	 * @return:     BigDecimal
	 */
	public BigDecimal getPsitioProtectPrice() {
		return psitioProtectPrice;
	}

	/** 
	 * <p> TODO</p>
	 * @return: BigDecimal
	 */
	public void setPsitioProtectPrice(BigDecimal psitioProtectPrice) {
		this.psitioProtectPrice = psitioProtectPrice;
	}

	/** 
	 * <p> TODO</p>
	 * @return: AppPersonInfo
	 */
/*	public void setAppPersonInfo(AppPersonInfo appPersonInfo) {
		this.appPersonInfo = appPersonInfo;
	}*/

	public BigDecimal getLendMoney() {
		return lendMoney;
	}

	public void setLendMoney(BigDecimal lendMoney) {
		this.lendMoney = lendMoney;
	}

	


	public String getCoinName() {
		return coinName;
	}

	public void setCoinName(String coinName) {
		this.coinName = coinName;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public Long getCustomerId() {
		return customerId;
	}

	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}

	public BigDecimal getHotMoney() {
		if(hotMoney==null){
			return BigDecimal.ZERO;
		}
		return hotMoney;
	}

	public void setHotMoney(BigDecimal hotMoney) {
		this.hotMoney = hotMoney;
	}

	public BigDecimal getColdMoney() {
		return coldMoney;
	}

	public void setColdMoney(BigDecimal coldMoney) {
		this.coldMoney = coldMoney;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getCurrencyType() {
		return currencyType;
	}

	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}

	public String getAccountNum() {
		return accountNum;
	}

	public void setAccountNum(String accountNum) {
		this.accountNum = accountNum;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}
	
	

	public String getCoinCode() {
		return coinCode;
	}

	public void setCoinCode(String coinCode) {
		this.coinCode = coinCode;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}

	public BigDecimal getDisableMoney() {
		return disableMoney;
	}

	public void setDisableMoney(BigDecimal disableMoney) {
		this.disableMoney = disableMoney;
	}

	public ExDigitalmoneyAccount(Long id, Integer version, Long customerId,
			BigDecimal hotMoney, BigDecimal coldMoney, String userName,
			String currencyType, String accountNum, Integer status,
			String publicKey, String privateKey) {
		super();
		this.id = id;
		this.version = version;
		this.customerId = customerId;
		this.hotMoney = hotMoney;
		this.coldMoney = coldMoney;
		this.userName = userName;
		this.currencyType = currencyType;
		this.accountNum = accountNum;
		this.status = status;
		this.publicKey = publicKey;
		this.privateKey = privateKey;
	}

	public ExDigitalmoneyAccount() {
		super();
	}

	@Override
	public String toString() {
		return "ExDigitalmoneyAccount [id=" + id + ", version=" + version
				+ ", customerId=" + customerId + ", hotMoney=" + hotMoney
				+ ", coldMoney=" + coldMoney + ", userName=" + userName
				+ ", currencyType=" + currencyType + ", accountNum="
				+ accountNum + ", status=" + status + ", publicKey="
				+ publicKey + ", privateKey=" + privateKey + "]";
	}

}
