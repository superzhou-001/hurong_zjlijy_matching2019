/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2020-01-08 14:12:12 
 */
package hry.cm5.customer.model;


import hry.bean.BaseModel;


import javax.persistence.*;
import java.math.BigDecimal;

/**
 * <p> Cm5CustomerLevel </p>
 * @author:         zhouming
 * @Date :          2020-01-08 14:12:12  
 */
@Table(name="cm5_customer_level")
public class Cm5CustomerLevel extends BaseModel {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;  //
	
	@Column(name= "customerId")
	private Long customerId;  //用户id
	
	@Column(name= "teamLevelId")
	private Long teamLevelId;  //团队等级Id
	
	@Column(name= "teamLevelSort")
	private Integer teamLevelSort;  //团队等级排序
	
	@Column(name= "levelType")
	private Integer levelType;  //等级类别 1 自动 2 手动
	
	@Column(name= "saasId")
	private String saasId;  //
	
	@Transient
	private BigDecimal addPipeRatio;// 对应等级增加管道收益比例


	public BigDecimal getAddPipeRatio() {
		return addPipeRatio;
	}

	public void setAddPipeRatio(BigDecimal addPipeRatio) {
		this.addPipeRatio = addPipeRatio;
	}

	/**
	 * <p></p>
	 * @author:  zhouming
	 * @return:  Long 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * <p></p>
	 * @author:  zhouming
	 * @param:   @param id
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	
	/**
	 * <p>用户id</p>
	 * @author:  zhouming
	 * @return:  Long 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public Long getCustomerId() {
		return customerId;
	}
	
	/**
	 * <p>用户id</p>
	 * @author:  zhouming
	 * @param:   @param customerId
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setCustomerId(Long customerId) {
		this.customerId = customerId;
	}
	
	
	/**
	 * <p>团队等级Id</p>
	 * @author:  zhouming
	 * @return:  Long 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public Long getTeamLevelId() {
		return teamLevelId;
	}
	
	/**
	 * <p>团队等级Id</p>
	 * @author:  zhouming
	 * @param:   @param teamLevelId
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setTeamLevelId(Long teamLevelId) {
		this.teamLevelId = teamLevelId;
	}
	
	
	/**
	 * <p>团队等级排序</p>
	 * @author:  zhouming
	 * @return:  Integer 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public Integer getTeamLevelSort() {
		return teamLevelSort;
	}
	
	/**
	 * <p>团队等级排序</p>
	 * @author:  zhouming
	 * @param:   @param teamLevelSort
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setTeamLevelSort(Integer teamLevelSort) {
		this.teamLevelSort = teamLevelSort;
	}
	
	
	/**
	 * <p>等级类别 1 自动 2 手动</p>
	 * @author:  zhouming
	 * @return:  Integer 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public Integer getLevelType() {
		return levelType;
	}
	
	/**
	 * <p>等级类别 1 自动 2 手动</p>
	 * @author:  zhouming
	 * @param:   @param levelType
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setLevelType(Integer levelType) {
		this.levelType = levelType;
	}
	
	
	/**
	 * <p></p>
	 * @author:  zhouming
	 * @return:  String 
	 * @Date :   2020-01-08 14:12:12    
	 */
	public String getSaasId() {
		return saasId;
	}
	
	/**
	 * <p></p>
	 * @author:  zhouming
	 * @param:   @param saasId
	 * @return:  void 
	 * @Date :   2020-01-08 14:12:12   
	 */
	public void setSaasId(String saasId) {
		this.saasId = saasId;
	}
	
	

}
