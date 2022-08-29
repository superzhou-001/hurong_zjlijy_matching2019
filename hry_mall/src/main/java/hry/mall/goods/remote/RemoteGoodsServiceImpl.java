package hry.mall.goods.remote;

import com.github.pagehelper.Page;
import hry.bean.ApiJsonResult;
import hry.bean.FrontPage;
import hry.common.enums.RedisKeyEnum;
import hry.common.util.DateUtils;
import hry.core.mvc.dao.base.BaseDao;
import hry.core.mvc.service.base.impl.BaseServiceImpl;
import hry.mall.goods.model.*;
import hry.mall.goods.service.*;
import hry.mall.integral.service.IntegralConfigService;
import hry.mall.merchant.model.Merchant;
import hry.mall.merchant.service.MerchantService;
import hry.mall.order.dao.EvaluateGoodsDao;
import hry.mall.order.model.EvaluateGoods;
import hry.mall.order.service.EvaluateGoodsService;
import hry.mall.platform.model.Payment;
import hry.mall.platform.service.PaymentService;
import hry.mall.retailstore.model.*;
import hry.mall.retailstore.service.*;
import hry.util.PageFactory;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import hry.util.QueryFilter;
import hry.util.SpringUtil;
import hry.util.StringUtil;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

public class RemoteGoodsServiceImpl extends BaseServiceImpl<Goods, Long> implements RemoteGoodsService {
    @Resource
    private ShopFloorService shopFloorService;

    @Resource
    private ShopLanguageService shopLanguageService;

    @Resource
    private GoodsClassService goodsClassService;

    @Resource
    private GoodsService goodsService;

    @Resource
    private GoodsSpecService goodsSpecService;

    @Resource
    private SpecService specService;

    @Resource
    private SpecValueService specValueService;

    @Resource
    private EvaluateGoodsService evaluateGoodsService;

    @Resource
    private FavoriteService favoriteService;

    @Resource
    private SearchGoodsService searchGoodsService;

    @Resource
    private PaymentService paymentService;

    @Resource
    private ActivityService activityService;

    @Resource
    private ActivityTimeService activityTimeService;

    @Resource
    private TimeTemplateService timeTemplateService;

    @Resource
    private ActivityGoodsService activityGoodsService;

    @Resource
    private AdvService advService;

    @Resource
    private CutomerStoreService cutomerStoreService;

    @Resource
    private StoreGoodsService storeGoodsService;

    @Resource
    private IntegralConfigService integralConfigService;

    @Resource
    private MerchantService merchantService;

    @Resource
    private SaleGoodsConfigService saleGoodsConfigService;

    @Override
    public void setDao(BaseDao<Goods, Long> baseDao) {

    }

    @Override
    public FrontPage findFloorGoodsList(Map<String, String> params) {
        Page<ShopFloor> page = PageFactory.getPage(params);
        QueryFilter filter = new QueryFilter(ShopFloor.class);
        filter.addFilter("isShow=",1);
        filter.setOrderby("sort asc");
        List<ShopFloor> list = shopFloorService.find(filter);
        if (!"zh_CN".equals(params.get("locale"))) {
               for (ShopFloor floor : list) {
                   String jsonInfo = floor.getFloorInfo();
                   JSONObject obj = JSONObject.fromObject(jsonInfo);
                   JSONArray array = JSONArray.fromObject(obj.get("goodsList"));
                   JSONArray newArray = new JSONArray();
                   for (int i=0; i < array.size(); i++) {
                       JSONObject json = array.getJSONObject(i);
                       //分类多语言替换
                       String gcNameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(), Long.parseLong(json.get("gcId").toString()), params.get("locale").toString());
                       if (StringUtil.isNull(gcNameLang)) {
                           json.remove("gcName");
                           json.put("gcName", gcNameLang);
                       }
                       //商品多语言替换
                       Goods goods = shopLanguageService.getGoodsLanguage(json.get("id").toString(), params.get("locale").toString());
                       if (goods != null) {
                           //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                           if (StringUtil.isNull(goods.getGoodsName())) {
                               json.remove("goodsName");
                               json.put("goodsName", goods.getGoodsName());
                           }
                           if (StringUtil.isNull(goods.getGoodsSubtitle())) {
                               json.remove("goodsSubtitle");
                               json.put("goodsSubtitle", goods.getGoodsSubtitle());
                           }
                           if (StringUtil.isNull(goods.getGoodsImageMore())) {
                               json.remove("goodsImageMore");
                               json.put("goodsImageMore", goods.getGoodsImageMore());
                           }
                       }
                       // 获取用户店铺商品的状态
                       String memberId = params.get("memberId");
                       int flag = this.checkUserGoods(memberId, Long.parseLong(json.get("id").toString()));
                       json.put("storeGoodsShow", flag);

                       newArray.add(json);
                   }
                   String newList = "{\"goodsList\":"+newArray.toString()+"}";
                   floor.setFloorInfo(newList);
                   String floorName =  shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.SHOP_FLOOR.getIndex(), floor.getId(), params.get("locale").toString());
                   floor.setFloorName(floorName != "" ? floorName : floor.getFloorName());
               }
        }
        return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
    public FrontPage findGoodsClassList(Map<String, String> params) {
        Page<GoodsClass> page = PageFactory.getPage(params);
        QueryFilter filter = new QueryFilter(GoodsClass.class);
        filter.addFilter("gcShow=",1);
        filter.addFilter("gcParentId=",0);
        filter.setOrderby("sort asc");
        List<GoodsClass> list = goodsClassService.find(filter);
        for (GoodsClass goodsClass : list) {
            if (!"zh_CN".equals(params.get("locale"))) {
                String gcName = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(), goodsClass.getId(), params.get("locale").toString());
                goodsClass.setGcName(gcName != "" ? gcName : goodsClass.getGcName());
            }
            //获取子分类
            filter = new QueryFilter(GoodsClass.class);
            filter.addFilter("gcShow=",1);
            filter.addFilter("gcParentId=",goodsClass.getId());
            List<GoodsClass> chlist = goodsClassService.find(filter);
            if (chlist != null && chlist.size() > 0) {
                if (!"zh_CN".equals(params.get("locale"))) {
                    for (GoodsClass cClass : chlist) {
                        String cGcName =  shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(), cClass.getId(), params.get("locale"));
                        cClass.setGcName(cGcName != ""?cGcName:cClass.getGcName());
                    }
                }
                goodsClass.setChildrenClass(chlist);
            }
        }
        //删除没有子分类的一级分类
      /*  int count=list.size();
        for (int i=0; i < count; i++) {
            GoodsClass goodsClass = list.get(i);
            if (goodsClass.getChildrenClass() == null) {
                list.remove(i);
            }
        }*/
        List<GoodsClass> list1=new ArrayList<GoodsClass>();
        for(int i=0; i < list.size(); i++){
            GoodsClass goodsClass = list.get(i);
            if (null!=goodsClass.getChildrenClass() && goodsClass.getChildrenClass().size()>0) {
                list1.add(goodsClass);
            }
        }
        return new FrontPage(list1, page.getTotal(), page.getPages(), page.getPageSize());
    }

    /**
     *获取商品详情
     * */
    @Override
    public ApiJsonResult getGoodsDetail(Map<String, Object> params) {
        Long goodsId = Long.parseLong(params.get("goodsId").toString());
        String locale = params.get("locale").toString();
        Goods goods = goodsService.findGoodsById(goodsId); //根据Id获取商品
        if (null == goods) {
            return new ApiJsonResult().setSuccess(true).setMsg("shangpinbucunzai");
        }
        //查询该商品是否被收藏
        String memberId = params.get("memberId").toString();
        goods.setIsFavorite("0");
        if (null!=memberId && !"".equals(memberId)) {
        	QueryFilter filter1 = new QueryFilter(Favorite.class);
        	filter1.addFilter("memberId=", Long.valueOf(memberId));
        	filter1.addFilter("goodsId=",goods.getId());
        	List<Favorite> flist = favoriteService.find(filter1);
        	if(null!=flist && flist.size()>0){
        		goods.setIsFavorite("1");
        	}
        }
        //根据商品Id获取商品的有用规格
        QueryFilter filter = new QueryFilter(GoodsSpec.class);
        filter.addFilter("specIsOpen=",1);
        filter.addFilter("goodsId=",goodsId);
        List<GoodsSpec> goodsSpecs = goodsSpecService.find(filter);
        List<Spec> specList = null;
        if (goods.getGoodsSpec() != null && !"".equals(goods.getGoodsSpec())) {
            specList = getSpec(goods.getGoodsSpec(),locale);
        }
        //获取推荐商品
        Map<String, String> param = new HashMap<>();
        param.put("limit","5");
        param.put("offset","1");
        param.put("goodsCommend","1");//获取推荐商品--获取五个
        param.put("goodsShow", "1"); // 获取已上架
        FrontPage page = goodsService.findGoodsList(param);
        List<Goods> commendList = page.getRows();

        //商品评论
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId", goods.getId());
        map.put("limit", "0"); // 取商品评论前5条数据
        List<EvaluateGoods> evaluateGoodsList = evaluateGoodsService.findEvaluateList(map);

        //商品人气+1
        goods.setGoodsClick(goods.getGoodsClick()+1);
        goodsService.update(goods);
        //更新商品solr字段
        Map<String, Object> maps = new HashMap<>();
        maps.put("goods_click", goods.getGoodsClick());
        searchGoodsService.updateMultiData(goods.getId().toString(), maps);

        /************************商品国际化处理---start***********************************/
        if (!"zh_CN".equals(params.get("locale"))) {
            //商品详情初始化
            Goods goodsDetail = shopLanguageService.getGoodsLanguage(goods.getId().toString(), params.get("locale").toString());
            if (goodsDetail != null) {
                //详情商品多语言初始化
                goods.setGoodsName(goodsDetail.getGoodsName() != "" ? goodsDetail.getGoodsName() : goods.getGoodsName());
                goods.setGoodsSubtitle(goodsDetail.getGoodsSubtitle() != "" ? goodsDetail.getGoodsSubtitle() : goods.getGoodsSubtitle());
                goods.setGoodsImageMore(goodsDetail.getGoodsImageMore() != "" ? goodsDetail.getGoodsImageMore() : goods.getGoodsImageMore());
                goods.setGoodsVideo(goodsDetail.getGoodsVideo() != "" ? goodsDetail.getGoodsVideo() : goods.getGoodsVideo());
                goods.setGoodsBodyPc(goodsDetail.getGoodsBodyPc() != "" ? goodsDetail.getGoodsBodyPc() : goods.getGoodsBodyPc());
                goods.setGoodsBodyApp(goodsDetail.getGoodsBodyApp() != "" ? goodsDetail.getGoodsBodyPc() : goods.getGoodsBodyApp());
            }
            String gcNameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),goods.getGcId(), params.get("locale").toString());
            goods.setGcName(gcNameLang != "" ? gcNameLang : goods.getGcName());
            for (Goods commendGoods : commendList){
                //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                Goods goodsLang = shopLanguageService.getGoodsLanguage(commendGoods.getId().toString(), params.get("locale").toString());
                if (goodsLang != null) {
                    commendGoods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : commendGoods.getGoodsName());
                    commendGoods.setGoodsSubtitle(goodsLang.getGoodsSubtitle() != "" ? goodsLang.getGoodsSubtitle() : commendGoods.getGoodsSubtitle());
                    commendGoods.setGoodsImageMore(goodsLang.getGoodsImageMore() != "" ? goodsLang.getGoodsImageMore() : commendGoods.getGoodsImageMore());
                }
                //分类初始化
                String gc1NameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),commendGoods.getGcId(), params.get("locale").toString());
                commendGoods.setGcName(gc1NameLang != "" ? gc1NameLang : commendGoods.getGcName());

                // 获取用户店铺商品的状态
                int flag = this.checkUserGoods(memberId, commendGoods.getId());
                goods.setStoreGoodsShow(flag);
            }

            for (EvaluateGoods evaluateGoods : evaluateGoodsList) {
                //评论初始化
                Goods goodsLang = shopLanguageService.getGoodsLanguage(evaluateGoods.getId().toString(), params.get("locale").toString());
                if (goodsLang != null)
                evaluateGoods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : evaluateGoods.getGoodsName());

                //分类初始化
                String gc2NameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),evaluateGoods.getGcId(), params.get("locale").toString());
                evaluateGoods.setGcName(gc2NameLang != "" ? gc2NameLang : evaluateGoods.getGcName());
            }
        }
        /************************商品国际化处理---end***********************************/

        //查询当前积分兑换币代码
        goods.setIntegralCode(integralConfigService.findIntegralCode());

        /************************参加活动的商品添加活动相关信息---start**********************/
        Map<String, Object> activityParams = new HashMap<>();
        activityParams.put("goodsId", goods.getId());
        List<SearchGoods> searchGoodsList = goodsService.findActivityGoodsAllList(activityParams);
        if (searchGoodsList != null && searchGoodsList.size() > 0) {
            SearchGoods searchGoods = searchGoodsList.get(0);
            // ActivityType 活动类型 1:团购活动 2:抢购活动
            goods.setActivityId(searchGoods.getActivityId());
            goods.setActivityGoodsId(searchGoods.getActivityGoodsId());
            // 活动价格
            goods.setActivityPrice(searchGoods.getActivityPrice());
            goods.setActivityType(searchGoods.getActivityType());
            if (searchGoods.getActivityType() == 1) {
                // 成团人数
                goods.setPeopleCount(searchGoods.getPeopleCount());
                // 活动开始时间
                goods.setActivitySTime(searchGoods.getActivitySTime());
                // 活动结束时间
                goods.setActivityETime(searchGoods.getActivityETime());
            }
            if (searchGoods.getActivityType() == 2) {
                // 抢购活动的真正结束时间需要结合活动时间段
                Date activitySTime = searchGoods.getActivitySTime();
                Date activityETime = searchGoods.getActivityETime();
                String satrtTime = searchGoods.getStartTime();
                String endTime = searchGoods.getEndTime();
                // 当前时间
                String nowDate = DateUtils.getDateString();

                // 转换为String
                String sTimeStr = DateUtils.getDateStr(activitySTime,DateUtils.DEFAULT_FORMAT_YYYY_MM_DD);
                String eTimeStr = DateUtils.getDateStr(activityETime,DateUtils.DEFAULT_FORMAT_YYYY_MM_DD);

                // 拼接抢购时间
                String vieSTime = sTimeStr + " " + satrtTime;
                String vieETime = eTimeStr + " " + endTime;

                // 时间转化为Date
                activitySTime = DateUtils.toDate(vieSTime, DateUtils.DEFAULT_YEAR_MON_DAY);
                activityETime = DateUtils.toDate(vieETime, DateUtils.DEFAULT_YEAR_MON_DAY);

                goods.setActivitySTime(activitySTime);
                goods.setActivityETime(activityETime);
                goods.setStartTime(satrtTime);
                goods.setEndTime(endTime);

            }
            // 获取用户店铺商品的状态
            int flag = this.checkUserGoods(memberId, goods.getId());
            goods.setStoreGoodsShow(flag);
        }
        /************************参加活动的商品添加活动相关信息---end**********************/
        // 获取店铺信息
        Merchant merchant = merchantService.getMerchant(goods.getMerchantId());
        // 预售商信息
        QueryFilter filter1 = new QueryFilter(SaleGoodsConfig.class);
        filter1.addFilter("goodsId=", goodsId);
        SaleGoodsConfig saleConfig = saleGoodsConfigService.get(filter1);

        //转载返回参数
        Map<String, Object> resultObject = new HashMap<>();
        resultObject.put("goods", goods); //获取商品详情
        resultObject.put("specList", specList);//获取商品规格集
        resultObject.put("goodsList", commendList);//获取推荐商品
        resultObject.put("merchant", merchant); //店铺信息
        resultObject.put("saleConfig", saleConfig);// 预售商品配置
        if (evaluateGoodsList != null && evaluateGoodsList.size() > 0) {
            resultObject.put("evaCount",evaluateGoodsList.size());
            resultObject.put("evaluateGoodsList",evaluateGoodsList);
        }
        JSONObject jsonObject = JSONObject.fromObject(resultObject);
        return new ApiJsonResult().setSuccess(true).setObj(jsonObject.toString());
    }

    @Override
    public ApiJsonResult getCoinRate(String coinCode) {
        return new ApiJsonResult().setSuccess(true).setObj(goodsService.getCoinRate(coinCode));
    }

    @Override
    public ApiJsonResult getGoodsSpec(Long goodsId, String specNameValue,Integer goodsSum, String locale) {

        //规格序列化值转为中文
        if (!"zh_CN".equals(locale)) {
            //{"66":"256G","68":"银色"}
            JSONObject object = JSONObject.fromObject(specNameValue);
            Map<String, String> specValueMap = (Map<String, String>)object;
            for (Map.Entry<String, String> entry : specValueMap.entrySet()) {
                SpecValue specValue = specValueService.get(Long.parseLong(entry.getKey()));
                if(specValue != null)
                    specValueMap.put(entry.getKey(), specValue.getSpValueName());
            }
            specNameValue = JSONObject.fromObject(specValueMap).toString();
        }

        Map<String, Object> params = new HashMap<>();
        params.put("goodsId",goodsId);
        params.put("specNameValue",specNameValue);
        List<GoodsSpec> goodsSpecList = goodsSpecService.getGoodsSpec(params);
        if (goodsSpecList != null && goodsSpecList.size() > 0) {
            GoodsSpec goodsSpec = goodsSpecList.get(0);
            if (goodsSpec.getSpecGoodsStorage() == 0) {
                return new ApiJsonResult().setSuccess(false).setMsg("shangpinkucunbuzu").setCode("2");
            }
            if (goodsSpec.getSpecGoodsStorage() < goodsSum) {
                return new ApiJsonResult().setSuccess(false).setMsg("goumaishuliangguoduokucunbuzu").setCode("2");//"该商品库存不足,最多只能购买"+goodsSpec.getSpecGoodsStorage()+"件"
            }
            if (goodsSum > 100) {
                return new ApiJsonResult().setSuccess(false).setMsg("shangpinshuliangguoduo").setCode("3");
            }
            return new ApiJsonResult().setSuccess(true).setObj(goodsSpec).setCode("8888");
        } else {
            return new ApiJsonResult().setSuccess(false).setMsg("guigeshangpinbucunzai").setCode("1");//该商品中不存在该规格商品
        }
    }

    @Override
    public FrontPage findEvaluateList(Map<String, String> params) {
        Page<GoodsClass> page = PageFactory.getPage(params);
        String goodsId = params.get("goodsId");
        String selectType = params.get("selectType");
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId",goodsId);
        map.put("selectType",selectType);
        List<EvaluateGoods> evaluateGoodsList = evaluateGoodsService.findEvaluateList(map);

        /************************商品国际化处理---start***********************************/
        String locale = params.get("locale").toString();
        if (!"zh_CN".equals(params.get("locale"))) {
            for (EvaluateGoods evaluateGoods : evaluateGoodsList) {
                //评论初始化
                Goods goodsLang = shopLanguageService.getGoodsLanguage(evaluateGoods.getId().toString(), params.get("locale").toString());
                if (goodsLang != null)
                evaluateGoods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : evaluateGoods.getGoodsName());

                //分类初始化
                String gc2NameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),evaluateGoods.getGcId(), params.get("locale").toString());
                evaluateGoods.setGcName(gc2NameLang != "" ? gc2NameLang : evaluateGoods.getGcName());
            }
        }
        /************************商品国际化处理---end*************************************/

        return new FrontPage(evaluateGoodsList, page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
    public ApiJsonResult countEvaluateGoods(Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();
        map.put("goodsId",params.get("goodsId").toString());
        int allCount = 0; //所有评论数量
        map.put("selectType",0);
        allCount = evaluateGoodsService.countEvaluateGoods(map);

        int goodCount = 0;//好评数量
        map.put("selectType",1);
        goodCount = evaluateGoodsService.countEvaluateGoods(map);

        int mediumCount = 0;//中评数量
        map.put("selectType",2);
        mediumCount = evaluateGoodsService.countEvaluateGoods(map);

        int badCount = 0; //差评数量
        map.put("selectType",3);
        badCount = evaluateGoodsService.countEvaluateGoods(map);

        int havePicCout = 0;//有图评论数量
        map.put("selectType",4);
        havePicCout = evaluateGoodsService.countEvaluateGoods(map);

        Map<String, Integer> resultObject = new HashMap<>();
        resultObject.put("allCount",allCount);
        resultObject.put("goodCount",goodCount);
        resultObject.put("mediumCount",mediumCount);
        resultObject.put("badCount",badCount);
        resultObject.put("havePicCount",havePicCout);
        JSONObject jsonObject = JSONObject.fromObject(resultObject);
        return new ApiJsonResult().setSuccess(true).setObj(jsonObject.toString());
    }

    @Override
    public FrontPage findSearchGoodsList(Map<String, String> params) {
        Page<SearchGoods> page = PageFactory.getPage(params);
        List<SearchGoods> list = searchGoodsService.findSearchGoodsList(params, Integer.parseInt(params.get("offset")), Integer.parseInt(params.get("limit")));
        /************************商品国际化处理---start***********************************/
        String locale = params.get("locale").toString();
        if (!"zh_CN".equals(params.get("locale"))) {
            for (SearchGoods goods : list) {
                //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                Goods goodsLang = shopLanguageService.getGoodsLanguage(goods.getId().toString(), params.get("locale").toString());
                if (goodsLang != null) {
                    goods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : goods.getGoodsName());
                    goods.setGoodsSubtitle(goodsLang.getGoodsSubtitle() != "" ? goodsLang.getGoodsSubtitle() : goods.getGoodsSubtitle());
                    goods.setGoodsImageMore(goodsLang.getGoodsImageMore() != "" ? goodsLang.getGoodsImageMore() : goods.getGoodsImageMore());
                }
            }
        }
        /************************商品国际化处理---end*************************************/

        /************************活动商品、店铺商品处理---start************************************/
        QueryFilter filter = new QueryFilter(ActivityGoods.class);
        String memberId = params.get("memberId");
        List<SearchGoods> avtivityList = goodsService.findActivityGoodsAllList(new HashMap<>());
        for (SearchGoods goods : list) {
            // 给参与活动的商品赋值上相应的活动信息
            for (SearchGoods activity : avtivityList) {
                if (goods.getId().equals(activity.getId())) {
                    goods.setActivityId(activity.getActivityId());
                    goods.setActivityGoodsId(activity.getActivityGoodsId());
                    goods.setActivityPrice(activity.getActivityPrice());
                    goods.setActivityType(activity.getActivityType());
                    if (activity.getActivityType() == 1) {
                        goods.setPeopleCount(activity.getPeopleCount());
                    }
                    if (activity.getActivityType() == 2) {
                        goods.setStartTime(activity.getStartTime());
                        goods.setEndTime(activity.getEndTime());
                    }
                }
            }
            // 获取用户店铺商品的状态
            int flag = this.checkUserGoods(memberId, goods.getId());
            goods.setStoreGoodsShow(flag);
        }
        /************************活动商品、店铺商品处理---end************************************/
        return new FrontPage(list, Long.parseLong(params.get("total")), page.getPages(), page.getPageSize());
    }

    @Override
    public ApiJsonResult findPortionGoods(String goodsType,String locale, String memberId) {
        List<Goods> goodsList = new ArrayList<>();
        //goodsType 1:推荐商品 2:人气商品
        if ("1".equals(goodsType)){
            //获取推荐商品
            Map<String, String> param = new HashMap<>();
            param.put("limit","20");
            param.put("offset","1");
            param.put("goodsCommend","1");//获取推荐商品--获取五个
            param.put("goodsShow", "1"); // 获取已上架
            FrontPage page = goodsService.findGoodsList(param);
            goodsList = page.getRows();
        } else if("2".equals(goodsType)){
            //获取人气商品
            Map<String, String> param = new HashMap<>();
            param.put("limit","5");
            param.put("offset","1");
            param.put("goodsClickSort","DESC");//获取最高人气前五个
            param.put("goodsShow", "1"); // 获取已上架
            FrontPage page = goodsService.findGoodsList(param);
            goodsList = page.getRows();
        }else{
            //获取推荐商品
            Map<String, String> param = new HashMap<>();
            param.put("limit","5");
            param.put("offset","1");
            param.put("goodsCommend","1");//获取推荐商品--获取五个
            param.put("goodsShow", "1"); // 获取已上架
            FrontPage page = goodsService.findGoodsList(param);
            List<Goods> goodsList1 = page.getRows();

            //获取人气商品
            Map<String, String> param2 = new HashMap<>();
            param2.put("limit","5");
            param2.put("offset","1");
            param2.put("goodsClickSort","DESC");//获取最高人气前五个
            param2.put("goodsShow", "1"); // 获取已上架
            FrontPage page2 = goodsService.findGoodsList(param2);
            List<Goods> goodsList2 = page2.getRows();

            goodsList.addAll(goodsList1);
            goodsList.addAll(goodsList2);

        }
        /********************商品国际化---start*************************/
        if (!"zh_CN".equals(locale)) {
            for (Goods goods : goodsList){
                //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                Goods goodsLang = shopLanguageService.getGoodsLanguage(goods.getId().toString(), locale);
                if (goodsLang != null){
                    goods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : goods.getGoodsName());
                    goods.setGoodsSubtitle(goodsLang.getGoodsSubtitle() != "" ? goodsLang.getGoodsSubtitle() : goods.getGoodsSubtitle());
                    goods.setGoodsImageMore(goodsLang.getGoodsImageMore() != "" ? goodsLang.getGoodsImageMore() : goods.getGoodsImageMore());
                }
                //分类初始化
                String gc1NameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),goods.getGcId(), locale);
                goods.setGcName(gc1NameLang != "" ? gc1NameLang : goods.getGcName());

                // 获取用户店铺商品的状态
                int flag = this.checkUserGoods(memberId, goods.getId());
                goods.setStoreGoodsShow(flag);
            }

        }
        /********************商品国际化---end***************************/
        return new ApiJsonResult().setSuccess(true).setObj(goodsList);
    }

    /**
     * 解析json---获取key
     * **/
    private List<String> resultListId(String json){
        List<String> list = new ArrayList<>();
        JSONObject jsonObject = JSONObject.fromObject(json);
        Set<String> set = jsonObject.keySet();
        String str = "";
        Iterator<String> it = set.iterator();
        while (it.hasNext()) {
            list.add(it.next().toString());
        }
        return list;
    }

    /**
     * 解析对应的spec
     * locale : 语言
     * **/
    private List<Spec> getSpec(String goodsSpec,String locale){
        List<Spec> specList = new ArrayList<>();
        //所有规格转为数组
        JSONObject jsonObject = JSONObject.fromObject(goodsSpec);
        Map<String, String> goodsSpecMap = (Map<String, String>)jsonObject;
        Set<String> set = goodsSpecMap.keySet();
        Iterator<String> it = set.iterator();
        for (String specId : set) {
            Spec spec = specService.get(Long.parseLong(specId));
            //规格多语言初始化
            if (!"zh_CN".equals(locale)) {
                String spNameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_SPEC.getIndex(),Long.parseLong(specId),locale);
                spec.setSpName(spNameLang != "" ? spNameLang : spec.getSpName());
            }
            String specValue = goodsSpecMap.get(specId);
            JSONObject object = JSONObject.fromObject(specValue);
            Map<String, String> specValueMap = (Map<String, String>)object;
            Set<String> valSet = specValueMap.keySet();
            List<SpecValue> specValueList = new ArrayList<>();
            for (String val : valSet) {
                SpecValue specVal = new SpecValue();
                specVal.setSpId(Long.parseLong(specId));
                specVal.setId(Long.parseLong(val));
                specVal.setSpValueName(specValueMap.get(val));
                //规格多语言初始化
                if (!"zh_CN".equals(locale)) {
                    String spValueLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_SPEC_VALUE.getIndex(),Long.parseLong(val),locale);
                    specVal.setSpValueName(spValueLang != "" ? spValueLang : specVal.getSpValueImage());
                }
                specValueList.add(specVal);
            }
            spec.setSpecValues(specValueList);
            specList.add(spec);
        }
        return specList;
    }

	@Override
	public FrontPage findIntegralGoodsList(Map<String, String> params) {
		// TODO Auto-generated method stub
        String locale = params.get("locale").toString();
	     Page<Goods> page = PageFactory.getPage(params);
	 	  //参数集合
		 Map<String,Object> map = new HashMap<String,Object>();
		 map.put("specialType", 2);
		 map.put("goodsShow", 1);
		 map.put("isAudit", 2);
	     List<Goods> list = goodsService.findIntegralGoodsList(map);
        /********************积分商品国际化---start*************************/
        if (!"zh_CN".equals(locale)) {
            for (Goods goods : list){
                //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                Goods goodsLang = shopLanguageService.getGoodsLanguage(goods.getId().toString(), locale);
                if (goodsLang != null) {
                    goods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : goods.getGoodsName());
                    goods.setGoodsSubtitle(goodsLang.getGoodsSubtitle() != "" ? goodsLang.getGoodsSubtitle() : goods.getGoodsSubtitle());
                    goods.setGoodsImageMore(goodsLang.getGoodsImageMore() != "" ? goodsLang.getGoodsImageMore() : goods.getGoodsImageMore());
                }
            }
        }
        //查询当前积分兑换币代码
      	String code = integralConfigService.findIntegralCode();
        for(Goods good:list){
        	good.setIntegralCode(code);
        }
        /********************商品国际化---end***************************/
	     return new FrontPage(list, page.getTotal(), page.getPages(), page.getPageSize());
	}

	@Override
	public ApiJsonResult findBlendPay(String locale) {
		// TODO Auto-generated method stub
		QueryFilter filter=new QueryFilter(Payment.class);
		filter.addFilter("paymentState=", Integer.valueOf("1"));
		List<Payment> plist=paymentService.find(filter);
		return new ApiJsonResult().setSuccess(true).setObj(plist);
	}

    @Override
    public ApiJsonResult findActivityList(Map<String, String> params) {
        QueryFilter filter = new QueryFilter(Activity.class);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        filter.addFilter("type=", params.get("activityType").toString());
        filter.addFilter("status=","1");
        filter.addFilter("startTime <=", new Date());
        filter.addFilter("endTime >= ", new Date());
        List<Activity> activityList = activityService.find(filter);
        return new ApiJsonResult().setSuccess(true).setObj(activityList);
    }

    @Override
    public ApiJsonResult findSiteActivityList(Map<String, String> params) {
        QueryFilter filter = new QueryFilter(Adv.class);
        filter.addFilter("siteKey=", params.get("siteKey").toString());
        filter.addFilter("langKey=", params.get("locale").toString());
        filter.addFilter("status=", "1");
        filter.setOrderby("sort asc");
        List<Adv> advList = advService.find(filter);
        return new ApiJsonResult().setSuccess(true).setObj(advList);
    }

    @Override
    public FrontPage findGroupGoodsList(Map<String, String> params) {
        Page<SearchGoods> page = PageFactory.getPage(params);
        Map<String, Object> map = new HashMap<>();
        if (params.get("activityId") != null && !"".equals(params.get("activityId").toString())) {
            map.put("activityId", params.get("activityId").toString());
        }
        List<SearchGoods> searchGoodsList = goodsService.findActivityGoodsList(map);
        String memberId = params.get("memberId");
        for (SearchGoods goods : searchGoodsList) {
            // 获取用户店铺商品的状态
            int flag = this.checkUserGoods(memberId, goods.getId());
            goods.setStoreGoodsShow(flag);
        }
        return new FrontPage(searchGoodsList, page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
    public FrontPage findVieGoodsList(Map<String, String> params) {
        Page<SearchGoods> page = PageFactory.getPage(params);
        Map<String, Object> map = new HashMap<>();

        if (params.get("activityId") != null) {
            map.put("activityId", params.get("activityId").toString());
        }
        if (params.get("timeTempId") != null) {
            map.put("timeTempId", params.get("timeTempId").toString());
        }
        List<SearchGoods> searchGoodsList = goodsService.findActivityGoodsList(map);
        return new FrontPage(searchGoodsList, page.getTotal(), page.getPages(), page.getPageSize());
    }

    @Override
    public ApiJsonResult findTimeTempList(Map<String, String> params) {
        QueryFilter filter = new QueryFilter(TimeTemplate.class);
        filter.setOrderby("startTime asc");
        List<TimeTemplate> timeTemplateList = timeTemplateService.find(filter);
        return new ApiJsonResult().setSuccess(true).setObj(timeTemplateList);
    }


    /**
     * 校验该用户是否为店主
     *    该商品对于店主是否上架或者下架
     * @return 0:非店主（包含未登录） 1：已上架 2：已下架（包含未上架） 注： 1、2 该用户是店主
     * **/
    private int checkUserGoods(String memberId, Long goodsId){
        int flag = 0;
        if (!"".equals(memberId)) {
            QueryFilter filter = new QueryFilter(CutomerStore.class);
            filter.addFilter("memberId=", memberId);
            // 针对一个用户只能开通一个店铺
            CutomerStore cutomerStore = cutomerStoreService.get(filter);
            if (cutomerStore != null) {
                QueryFilter filter1 = new QueryFilter(StoreGoods.class);
                filter1.addFilter("memmberId=", memberId);
                filter1.addFilter("goodsId=",goodsId);
                StoreGoods storeGoods = storeGoodsService.get(filter1);
                if (storeGoods != null) {
                    // goodsShow : 1已上架; 2已下架
                    flag = storeGoods.getGoodsShow();
                } else {
                    // 2:未上架
                    flag = 2;
                }
            }
        }
        return flag;
    }

	@Override
	public FrontPage findPagePortionGoods(Map<String, String> params) {
		// TODO Auto-generated method stub
        List<Goods> goodsList = new ArrayList<>();
        String goodsType=params.get("goodsType");
        String locale = params.get("locale").toString();
        String memberId=params.get("memberId");
        FrontPage page = null;
        //goodsType 1:推荐商品 2:人气商品
        if ("1".equals(goodsType)){
            //获取推荐商品
            params.put("goodsCommend","1");//获取推荐商品--获取五个
            params.put("goodsShow", "1"); // 获取已上架
            page = goodsService.findGoodsList(params);
            goodsList = page.getRows();
        } else if("2".equals(goodsType)){
            //获取人气商品
            params.put("goodsClickSort","DESC");//获取最高人气前五个
            params.put("goodsShow", "1"); // 获取已上架
            page = goodsService.findGoodsList(params);
            goodsList = page.getRows();
        }
        /********************商品国际化---start*************************/
        if (!"zh_CN".equals(locale)) {
            for (Goods goods : goodsList){
                //列表商品 首页显示只初始化 商品名称 商品父标题 商品多图片
                Goods goodsLang = shopLanguageService.getGoodsLanguage(goods.getId().toString(), locale);
                if (goodsLang != null){
                    goods.setGoodsName(goodsLang.getGoodsName() != "" ? goodsLang.getGoodsName() : goods.getGoodsName());
                    goods.setGoodsSubtitle(goodsLang.getGoodsSubtitle() != "" ? goodsLang.getGoodsSubtitle() : goods.getGoodsSubtitle());
                    goods.setGoodsImageMore(goodsLang.getGoodsImageMore() != "" ? goodsLang.getGoodsImageMore() : goods.getGoodsImageMore());
                }
                //分类初始化
                String gc1NameLang = shopLanguageService.getLanguageValue(RedisKeyEnum.KeyEnum.GOODS_CLASS.getIndex(),goods.getGcId(), locale);
                goods.setGcName(gc1NameLang != "" ? gc1NameLang : goods.getGcName());

                // 获取用户店铺商品的状态
                int flag = this.checkUserGoods(memberId, goods.getId());
                goods.setStoreGoodsShow(flag);
            }

        }
        /********************商品国际化---end***************************/
        return page;
	}

    @Override
    public ApiJsonResult getMerchant(String merchantId) {
        Merchant merchant = merchantService.getMerchant(Long.parseLong(merchantId));
        return new ApiJsonResult().setSuccess(true).setObj(merchant);
    }
}
