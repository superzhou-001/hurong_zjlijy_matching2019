/**
 * Copyright:   
 * @author:      yaozh
 * @version:     V1.0 
 * @Date:        2019-08-01 10:27:19 
 */
package hry.cm.customer.model;


import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import hry.bean.BaseModel;

/**
 * <p> CmCustomer </p>
 * @author:         yaozh
 * @Date :          2019-08-01 10:27:19  
 */
@Table(name="cm_customer")
public class CmCustomer extends BaseModel {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;  //
	
	@Column(name= "customerId")
	private Long customerId;  //用户id
	
	@Column(name= "grade1")
	private Integer grade1;  //矿工等级
	
	@Column(name= "gradeName1")
	private String gradeName1;  //矿工等级名称
	
	@Column(name= "isEdit1")
	private Integer isEdit1;  //矿工等级是否人工修改 1否 2是
	
	@Column(name= "grade2")
	private Integer grade2;  //矿场等级
	
	@Column(name= "gradeName2")
	private String gradeName2;  //矿场等级名称
	
	@Column(name= "isEdit2")
	private Integer isEdit2;  //矿场等级是否人工修改 1否 2是
	
	@Column(name= "teamNum")
	private Integer teamNum;  //团队人数
	
	@Column(name= "personalAchievement")
	private BigDecimal personalAchievement;  //个人业绩
	
	@Column(name= "teamAchievement")
	private BigDecimal teamAchievement;  //团队业绩
	
	@Column(name= "maxAchievement")
	private BigDecimal maxAchievement;  //大趋业绩
	
	@Column(name= "minAchievement")
	private BigDecimal minAchievement;  //小趋业绩
	
	@Column(name= "remark")
	private String remark;  //
	
	@Column(name= "saasId")
	private String saasId;  //
	
	@Transient
	private BigDecimal teamProfitProportion; // 团队收入比例
	@Transient
	private BigDecimal cappingMultiple; //封顶倍数
	
	@Transient
	private Integer isBonus; // 是否加权分红 1否 2是


	public BigDecimal getCappingMultiple() {
		return cappingMultiple;
	}

	public void setCappingMultiple(BigDecimal cappingMultiple) {
		this.cappingMultiple = cappingMultiple;
	}

	public BigDecimal getTeamProfitProportion() {
		return teamProfitProportion;
	}

	public void setTeamProfitProportion(BigDecimal teamProfitProportion) {
		this.teamProfitProportion = teamProfitProportion;
	}

	public Integer getIsBonus() {
		return isBonus;
	}

	public void setIsBonus(Integer isBonus) {
		this.isBonus = isBonus;
	}

	/**
	 * <p></p>
	 * @author:  yaozh
	 * @return:  Long 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * <p></p>
	 * @author:  yaozh
	 * @param:   @param id
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	
	/**
	 * <p>用户id</p>
	 * @author:  yaozh
	 * @return:  Long 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Long getCustomerId() {
		return customerId;
	}
	
	/**
	 * <p>用户id</p>
	 * @author:  yaozh
	 * @param:   @param customerId
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	
	
	/**
	 * <p>矿工等级</p>
	 * @author:  yaozh
	 * @return:  Integer 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Integer getGrade1() {
		return grade1;
	}
	
	/**
	 * <p>矿工等级</p>
	 * @author:  yaozh
	 * @param:   @param grade1
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setGrade1(Integer grade1) {
		this.grade1 = grade1;
	}
	
	
	/**
	 * <p>矿工等级名称</p>
	 * @author:  yaozh
	 * @return:  String 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public String getGradeName1() {
		return gradeName1;
	}
	
	/**
	 * <p>矿工等级名称</p>
	 * @author:  yaozh
	 * @param:   @param gradeName1
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setGradeName1(String gradeName1) {
		this.gradeName1 = gradeName1;
	}
	
	
	/**
	 * <p>矿工等级是否人工修改 1否 2是</p>
	 * @author:  yaozh
	 * @return:  Integer 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Integer getIsEdit1() {
		return isEdit1;
	}
	
	/**
	 * <p>矿工等级是否人工修改 1否 2是</p>
	 * @author:  yaozh
	 * @param:   @param isEdit1
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setIsEdit1(Integer isEdit1) {
		this.isEdit1 = isEdit1;
	}
	
	
	/**
	 * <p>矿场等级</p>
	 * @author:  yaozh
	 * @return:  Integer 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Integer getGrade2() {
		return grade2;
	}
	
	/**
	 * <p>矿场等级</p>
	 * @author:  yaozh
	 * @param:   @param grade2
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setGrade2(Integer grade2) {
		this.grade2 = grade2;
	}
	
	
	/**
	 * <p>矿场等级名称</p>
	 * @author:  yaozh
	 * @return:  String 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public String getGradeName2() {
		return gradeName2;
	}
	
	/**
	 * <p>矿场等级名称</p>
	 * @author:  yaozh
	 * @param:   @param gradeName2
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setGradeName2(String gradeName2) {
		this.gradeName2 = gradeName2;
	}
	
	
	/**
	 * <p>矿场等级是否人工修改 1否 2是</p>
	 * @author:  yaozh
	 * @return:  Integer 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Integer getIsEdit2() {
		return isEdit2;
	}
	
	/**
	 * <p>矿场等级是否人工修改 1否 2是</p>
	 * @author:  yaozh
	 * @param:   @param isEdit2
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setIsEdit2(Integer isEdit2) {
		this.isEdit2 = isEdit2;
	}
	
	
	/**
	 * <p>团队人数</p>
	 * @author:  yaozh
	 * @return:  Integer 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public Integer getTeamNum() {
		return teamNum;
	}
	
	/**
	 * <p>团队人数</p>
	 * @author:  yaozh
	 * @param:   @param teamNum
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setTeamNum(Integer teamNum) {
		this.teamNum = teamNum;
	}
	
	
	/**
	 * <p>个人业绩</p>
	 * @author:  yaozh
	 * @return:  BigDecimal 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public BigDecimal getPersonalAchievement() {
		return personalAchievement;
	}
	
	/**
	 * <p>个人业绩</p>
	 * @author:  yaozh
	 * @param:   @param personalAchievement
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setPersonalAchievement(BigDecimal personalAchievement) {
		this.personalAchievement = personalAchievement;
	}
	
	
	/**
	 * <p>团队业绩</p>
	 * @author:  yaozh
	 * @return:  BigDecimal 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public BigDecimal getTeamAchievement() {
		return teamAchievement;
	}
	
	/**
	 * <p>团队业绩</p>
	 * @author:  yaozh
	 * @param:   @param teamAchievement
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setTeamAchievement(BigDecimal teamAchievement) {
		this.teamAchievement = teamAchievement;
	}
	
	
	/**
	 * <p>大趋业绩</p>
	 * @author:  yaozh
	 * @return:  BigDecimal 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public BigDecimal getMaxAchievement() {
		return maxAchievement;
	}
	
	/**
	 * <p>大趋业绩</p>
	 * @author:  yaozh
	 * @param:   @param maxAchievement
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setMaxAchievement(BigDecimal maxAchievement) {
		this.maxAchievement = maxAchievement;
	}
	
	
	/**
	 * <p>小趋业绩</p>
	 * @author:  yaozh
	 * @return:  BigDecimal 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public BigDecimal getMinAchievement() {
		return minAchievement;
	}
	
	/**
	 * <p>小趋业绩</p>
	 * @author:  yaozh
	 * @param:   @param minAchievement
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setMinAchievement(BigDecimal minAchievement) {
		this.minAchievement = minAchievement;
	}
	
	
	/**
	 * <p></p>
	 * @author:  yaozh
	 * @return:  String 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public String getRemark() {
		return remark;
	}
	
	/**
	 * <p></p>
	 * @author:  yaozh
	 * @param:   @param remark
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setRemark(String remark) {
		this.remark = remark;
	}
	
	
	/**
	 * <p></p>
	 * @author:  yaozh
	 * @return:  String 
	 * @Date :   2019-08-01 10:27:19    
	 */
	public String getSaasId() {
		return saasId;
	}
	
	/**
	 * <p></p>
	 * @author:  yaozh
	 * @param:   @param saasId
	 * @return:  void 
	 * @Date :   2019-08-01 10:27:19   
	 */
	public void setSaasId(String saasId) {
		this.saasId = saasId;
	}
	
	

}
