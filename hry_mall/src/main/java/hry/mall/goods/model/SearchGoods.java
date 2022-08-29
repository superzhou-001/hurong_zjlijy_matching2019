package hry.mall.goods.model;

import hry.bean.BaseModel;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 搜索商品pojo
 * */
public class SearchGoods extends BaseModel {

    private Long id;//商品Id

    private Long gcId;//分类Id

    private String goodsName;//商品名称

    private String goodsSubtitle;//商品副标题

    private Long brandId;//商品品牌ID

    private String goodsImageMore;//商品图片

    private Integer goodsClick;//商品浏览数量

    private Integer saleNum;//商品销售数量

    private BigDecimal specGoodsPrice;//商品规格价格

    /***--新添商品活动字段--****/
    private Long activityId; // 活动Id

    private Long activityGoodsId; // 活动商品Id

    private BigDecimal activityPrice;// 活动价格

    private Integer peopleCount; // 成团人数

    private Integer activityType; // 活动类型 1： 团购 2：抢购

    private Date activitySTime;// 活动开始时间

    private Date activityETime;// 活动结束时间

    /***--新添抢购商品活动字段--****/
    private String startTime; // 抢购活动中 时段 开始时间

    private String endTime; // 抢购活动中 时段 结束时间

    /***--用户店铺商品相关字段--****/
    private Integer storeGoodsShow; //店铺商品是否上架：0(不显示上架下架按钮) 1已上架; 2已下架

    private Long storeId; // 店铺id

    private Integer specialType; // 商品类型 1普通，2位积分商品，3新人专享, 4进阶商品, 8会员商品, 9创新商品

    private Long merchantId; // 商户id

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public Integer getSpecialType() {
        return specialType;
    }

    public void setSpecialType(Integer specialType) {
        this.specialType = specialType;
    }

    public Long getStoreId() {
        return storeId;
    }

    public void setStoreId(Long storeId) {
        this.storeId = storeId;
    }

    public Integer getStoreGoodsShow() {
        return storeGoodsShow;
    }

    public void setStoreGoodsShow(Integer storeGoodsShow) {
        this.storeGoodsShow = storeGoodsShow;
    }

    public Date getActivitySTime() {
        return activitySTime;
    }

    public void setActivitySTime(Date activitySTime) {
        this.activitySTime = activitySTime;
    }

    public Date getActivityETime() {
        return activityETime;
    }

    public void setActivityETime(Date activityETime) {
        this.activityETime = activityETime;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public Long getActivityGoodsId() {
        return activityGoodsId;
    }

    public void setActivityGoodsId(Long activityGoodsId) {
        this.activityGoodsId = activityGoodsId;
    }

    public BigDecimal getActivityPrice() {
        return activityPrice;
    }

    public void setActivityPrice(BigDecimal activityPrice) {
        this.activityPrice = activityPrice;
    }

    public Integer getPeopleCount() {
        return peopleCount;
    }

    public void setPeopleCount(Integer peopleCount) {
        this.peopleCount = peopleCount;
    }

    public Integer getActivityType() {
        return activityType;
    }

    public void setActivityType(Integer activityType) {
        this.activityType = activityType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public Long getGcId() {
        return gcId;
    }

    public void setGcId(Long gcId) {
        this.gcId = gcId;
    }

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public String getGoodsSubtitle() {
        return goodsSubtitle;
    }

    public void setGoodsSubtitle(String goodsSubtitle) {
        this.goodsSubtitle = goodsSubtitle;
    }

    public Long getBrandId() {
        return brandId;
    }

    public void setBrandId(Long brandId) {
        this.brandId = brandId;
    }

    public String getGoodsImageMore() {
        return goodsImageMore;
    }

    public void setGoodsImageMore(String goodsImageMore) {
        this.goodsImageMore = goodsImageMore;
    }

    public Integer getGoodsClick() {
        return goodsClick;
    }

    public void setGoodsClick(Integer goodsClick) {
        this.goodsClick = goodsClick;
    }

    public Integer getSaleNum() {
        return saleNum;
    }

    public void setSaleNum(Integer saleNum) {
        this.saleNum = saleNum;
    }

    public BigDecimal getSpecGoodsPrice() {
        return specGoodsPrice;
    }

    public void setSpecGoodsPrice(BigDecimal specGoodsPrice) {
        this.specGoodsPrice = specGoodsPrice;
    }
}
