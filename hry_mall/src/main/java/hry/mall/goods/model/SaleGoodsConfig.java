/**
 * Copyright:   
 * @author:      zhouming
 * @version:     V1.0 
 * @Date:        2019-12-05 15:20:45 
 */
package hry.mall.goods.model;


import hry.bean.BaseModel;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * <p> SaleGoodsConfig </p>
 * @author:         zhouming
 * @Date :          2019-12-05 15:20:45  
 */
@Table(name="shop_salegoods_config")
public class SaleGoodsConfig extends BaseModel {
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;  //主键id
	
	@Column(name= "goodsId")
	private Long goodsId;  //商品Id
	
	@Column(name= "depositStartTime")
	private Date depositStartTime;  //支付定金开始时间
	
	@Column(name= "depositEndTime")
	private Date depositEndTime;  //支付定金结束时间
	
	@Column(name= "finalStartTime")
	private Date finalStartTime;  //尾款支付开始时间
	
	@Column(name= "finalEndTime")
	private Date finalEndTime;  //尾款支付结束时间
	
	@Column(name= "depositRatio")
	private BigDecimal depositRatio;  //定金比例
	
	@Column(name= "finalRatio")
	private BigDecimal finalRatio;  //尾款比例
	
	
	
	
	/**
	 * <p>主键id</p>
	 * @author:  zhouming
	 * @return:  Long 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Long getId() {
		return id;
	}
	
	/**
	 * <p>主键id</p>
	 * @author:  zhouming
	 * @param:   @param id
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	
	/**
	 * <p>商品Id</p>
	 * @author:  zhouming
	 * @return:  Long 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Long getGoodsId() {
		return goodsId;
	}
	
	/**
	 * <p>商品Id</p>
	 * @author:  zhouming
	 * @param:   @param goodsId
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}
	
	
	/**
	 * <p>支付定金开始时间</p>
	 * @author:  zhouming
	 * @return:  Date 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Date getDepositStartTime() {
		return depositStartTime;
	}
	
	/**
	 * <p>支付定金开始时间</p>
	 * @author:  zhouming
	 * @param:   @param depositStartTime
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setDepositStartTime(Date depositStartTime) {
		this.depositStartTime = depositStartTime;
	}
	
	
	/**
	 * <p>支付定金结束时间</p>
	 * @author:  zhouming
	 * @return:  Date 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Date getDepositEndTime() {
		return depositEndTime;
	}
	
	/**
	 * <p>支付定金结束时间</p>
	 * @author:  zhouming
	 * @param:   @param depositEndTime
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setDepositEndTime(Date depositEndTime) {
		this.depositEndTime = depositEndTime;
	}
	
	
	/**
	 * <p>尾款支付开始时间</p>
	 * @author:  zhouming
	 * @return:  Date 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Date getFinalStartTime() {
		return finalStartTime;
	}
	
	/**
	 * <p>尾款支付开始时间</p>
	 * @author:  zhouming
	 * @param:   @param finalStartTime
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setFinalStartTime(Date finalStartTime) {
		this.finalStartTime = finalStartTime;
	}
	
	
	/**
	 * <p>尾款支付结束时间</p>
	 * @author:  zhouming
	 * @return:  Date 
	 * @Date :   2019-12-05 15:20:45    
	 */
	public Date getFinalEndTime() {
		return finalEndTime;
	}
	
	/**
	 * <p>尾款支付结束时间</p>
	 * @author:  zhouming
	 * @param:   @param finalEndTime
	 * @return:  void 
	 * @Date :   2019-12-05 15:20:45   
	 */
	public void setFinalEndTime(Date finalEndTime) {
		this.finalEndTime = finalEndTime;
	}

	public BigDecimal getDepositRatio() {
		return depositRatio;
	}

	public void setDepositRatio(BigDecimal depositRatio) {
		this.depositRatio = depositRatio;
	}

	public BigDecimal getFinalRatio() {
		return finalRatio;
	}

	public void setFinalRatio(BigDecimal finalRatio) {
		this.finalRatio = finalRatio;
	}
}
