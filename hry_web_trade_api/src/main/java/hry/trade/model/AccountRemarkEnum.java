package hry.trade.model;

public enum AccountRemarkEnum {
    TYPE1("委托下单", 1),
    TYPE2("交易成功，-买家解冻总金额转给卖家", 2),
    TYPE3("买家市价，-完成还剩一点金额给解冻到自己账户", 3),
    TYPE4("买家市价，+完成还剩一点金额给解冻到自己账户", 4),
    TYPE5("买的价格向下浮动，-要退钱", 5),
    TYPE6("买的价格向下浮动，+要退钱", 6),
    TYPE7("交易成功，+卖家收入金额", 7),
    TYPE8("交易成功，-卖家手续费", 8),
    TYPE9("交易成功，+买家收入币", 9),
    TYPE10("交易成功，-买家手续费", 10),
    TYPE11("交易成功，-卖家虚拟币解冻转买家", 11),
    TYPE12("撤销委托，解冻到自己钱包", 12),
    TYPE21("充值确认", 21),
    TYPE22("确认提现", 22),
    TYPE23("手动充值", 23),
    TYPE24("手动充值又撤销", 24),
    TYPE25("手动提现", 25),
    TYPE31("充币", 31),
    TYPE32("人民币提现", 32),
    TYPE33("提币审核成功", 33),
    TYPE34("提币手续费审核成功", 34),
    TYPE35("申请币提现", 35),
    TYPE36("申请币提现手续费", 36),
    TYPE37("提币已驳回", 37),
    TYPE38("人民币提现驳回", 38),

    TYPE40("佣金派发", 40),
    TYPE41("下级充币返佣", 41),
    TYPE42("下级C2C充币返佣", 42),

    TYPE49("ctc买币审核通过", 49),
    TYPE50("ctc卖币冻结", 50),
    TYPE51("ctc卖币通过", 51),
    TYPE53("ctc卖币驳回", 53),

    TYPE52("买会员扣币", 52),


    TYPE55("锁仓冻结", 55),
    TYPE56("锁仓解冻", 56),
    TYPE57("挖矿奖励", 57),
    TYPE58("分红奖励", 58),
    TYPE59("平台币解冻", 59),


    TYPE60("发布广告,计算币", 60),
    TYPE61("买,计算币", 61),
    TYPE62("卖,计算币", 62),
    TYPE63("买卖方退款,计算币", 63),
    TYPE64("取消订单,计算币", 64),
    TYPE65("完成订单,计算币", 65),
    TYPE66("解除冻结,并关闭此条广告,计算币", 66),
    TYPE670("恢复广告,解冻币", 670),


    TYPE67("锁仓理财本金冻结", 67),
    TYPE68("锁仓理财本金解冻", 68),
    TYPE69("锁仓理财收益增加", 69),
    TYPE70("锁仓理财本金+收益回归", 70),
    TYPE71("锁仓理财扣除收益", 71),
    TYPE72("锁仓理财增加收益", 72),

    TYPE80("币转出申请,可用减少,冻结增加", 80),
    TYPE81("币转入申请,冻结增加", 81),
    TYPE82("币转出申请通过,冻结扣除", 82),
    TYPE83("币转入申请通过,冻结扣除,可用增加", 83),
    TYPE84("币转出申请驳回,冻结扣除,可用增加", 84),
    TYPE85("币转入申请驳回,冻结扣除", 85),

    TYPE26("手动提现外部接口", 26),
    TYPE27("手动充值外部接口", 27),

    TYPE100("转出到杠杆账户", 100),
    TYPE101("从杠杆账户转入", 101),


    TYPE102("理财本金冻结", 102),
    TYPE103("理财本金赎回", 103),
    TYPE104("理财赎回手续费", 104),
    TYPE105("理财本金+收益发放", 105),
    TYPE106("营销活动送平台币", 106),
    TYPE107("理财产品使用平台币红包", 107),
    TYPE108("理财产品流标返回平台币红包", 108),

    TYPE110("转入合约账户", 110),
    TYPE111("转出合约账户", 111),
    TYPE112("转入永续合约账户", 112),
    TYPE113("转出永续合约账户", 113),
    TYPE114("转入二元期权账户", 114),
    TYPE115("转出二元期权账户", 115),

    TYPE120("转出到挖矿计划", 120),
    TYPE121("挖矿计划完成，返平台币", 121),

    TYPE150("预约下单冻结", 150),
    TYPE151("预约单正式下单前返回币账户", 151),


    TYPE201("第三方平台下买单,定价币热账户减少", 201),
    TYPE202("第三方平台下买单,定价币冷账户增加", 202),
    TYPE203("第三方平台下卖单,交易币热账户减少", 203),
    TYPE204("第三方平台下卖单,交易币冷账户增加", 204),
    TYPE205("买，成交，定价币减少", 205),
    TYPE206("买，成交，交易币增多", 206),
    TYPE207("买，成交，扣除手续费，交易币减少", 207),
    TYPE208("卖，成交，交易币减少", 208),
    TYPE209("卖，成交，定价币增多", 209),
    TYPE210("卖，成交，扣除手续费，定价币减少", 210),
    TYPE211("买，部分成交已撤销，定价币冷账户减少", 211),
    TYPE212("买，部分成交已撤销，定价币热账户增加", 212),
    TYPE213("卖，部分成交已撤销，交易币冷账户减少", 213),
    TYPE214("卖，部分成交已撤销，交易币热账户增加", 214),
    TYPE215("买，直接撤销，定价币冷账户减少", 215),
    TYPE216("买，直接撤销，定价币热账户增加", 216),
    TYPE217("卖，直接撤销，交易币冷账户减少", 217),
    TYPE218("卖，直接撤销，交易币热账户增加", 218),
    TYPE219("买，部分成交，但是第三方平台撤单，未同步我方交易所，交易币热账户增加", 219),
    TYPE220("买，部分成交，但是第三方平台撤单，未同步我方交易所，定价币冷账户减少全部", 220),
    TYPE221("买，部分成交，但是第三方平台撤单，未同步我方交易所，定价币热账户增加", 221),
    TYPE222("买，部分成交，但是第三方平台撤单，未同步我方交易所，扣除交易币手续费", 222),
    TYPE223("卖，部分成交，但是第三方平台撤单，未同步我方交易所，定价币热账户增加", 223),
    TYPE224("卖，部分成交，但是第三方平台撤单，未同步我方交易所，交易币冷账户减少全部", 224),
    TYPE225("卖，部分成交，但是第三方平台撤单，未同步我方交易所，交易币热账户增加", 225),
    TYPE226("卖，部分成交，但是第三方平台撤单，未同步我方交易所，扣除定价币手续费", 226),
    TYPE227("买，部分成交，但是第三方平台撤单，未同步我方交易所，定价币冷账户减少", 227),
    TYPE228("卖，部分成交，但是第三方平台撤单，未同步我方交易所，交易币冷账户减少", 228),

    TYPE229("保证金申请，热账户减少", 229),
    TYPE230("保证金申请，冷账户增加", 230),
    TYPE231("保证金申请驳回，解冻保证金，热账户增加", 231),
    TYPE232("保证金申请驳回，解冻保证金，冷账户减少", 232),


    TYPE300("对碰奖励", 300),
    TYPE301("转入合伙人账户", 301),
    TYPE302("合伙人账户释放", 302),
    TYPE303("兑换红包扣除", 303),
    TYPE304("兑换红包奖励", 304),
    TYPE305("抢购推荐奖励热账户增加", 305),
    TYPE306("抢购获得热账户增加", 306),
    TYPE307("抢购支付热账户减少", 307),
    //社交流水类型：（0：奖励(收)，3：购买平台币(收、支)，4转账(收、支)， 5打赏(收、支)，6上链(支)，7捐赠(支)，8购买矿机(支)，9购买会员(支)）
    TYPE500("数币奖励", 500),
    TYPE503("币币兑换", 503),
    TYPE504("好友转账", 504),
    TYPE505("朋友圈打赏", 505),
    TYPE506("许愿上链", 506),
    TYPE507("区块链公益捐赠", 507),
    TYPE508("购买矿机", 508),
    TYPE509("购买会员", 509),
    TYPE510("购买商品", 510),
    TYPE511("会员奖励", 511),  //会员奖励
    TYPE512("矿机奖励", 512),  //矿机奖励
    TYPE513("商城奖励", 513),  //商城奖励
    TYPE514("广告奖励", 514),  //广告奖励
    TYPE515("持币生息", 515)   //持币生息
    ;

    private String name;
    private int index;

    private AccountRemarkEnum(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public static String getName(int index) {
        for (AccountRemarkEnum c : AccountRemarkEnum.values()) {
            if (c.getIndex() == index) {
                return c.name;
            }
        }
        return String.valueOf(index);
    }

    // get set 方法  
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
