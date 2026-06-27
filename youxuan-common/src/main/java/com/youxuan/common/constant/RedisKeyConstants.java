package com.youxuan.common.constant;

/**
 * Redis Key 常量，集中维护缓存和幂等相关 key 前缀。
 */
public final class RedisKeyConstants {

    public static final String LOGIN_TOKEN = "youxuan:login:token:";
    public static final String HOME_HOT_PRODUCTS = "youxuan:home:hot-products";
    public static final String HOME_CATEGORIES = "youxuan:home:categories";
    public static final String PRODUCT_DETAIL = "youxuan:product:detail:";
    public static final String COUPON_STOCK = "youxuan:coupon:stock:";
    public static final String COUPON_USER = "youxuan:coupon:user:";
    public static final String ORDER_SUBMIT = "youxuan:order:submit:";
    public static final String CART = "youxuan:cart:";
    public static final String ADDRESS_DEFAULT = "youxuan:address:default:";
    public static final String HOME_INDEX = "youxuan:home:index";
    public static final String HOME_BANNERS = "youxuan:home:banners";
    public static final String HOME_RECOMMEND_PRODUCTS = "youxuan:home:recommend-products";

    private RedisKeyConstants() {
    }
}
