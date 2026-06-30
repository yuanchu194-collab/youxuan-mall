export interface HomeBanner {
  title: string
  subtitle: string
  imageUrl: string
  linkUrl: string
  buttonText: string
  fullImage?: boolean
}

export const fallbackBanners: HomeBanner[] = [
  {
    title: '优选好物 新鲜到家',
    subtitle: '每日精选 · 产地直供 · 品质保障',
    imageUrl: '/banners/banner-fresh.png',
    linkUrl: '/products',
    buttonText: '立即选购',
    fullImage: true
  },
  {
    title: '领券购物 更省钱',
    subtitle: '新人券 · 满减券 · 品类券 限时领取',
    imageUrl: '/banners/banner-coupon.png',
    linkUrl: '/coupons',
    buttonText: '去领券',
    fullImage: true
  },
  {
    title: '新人专享 首单有礼',
    subtitle: '新用户专属福利 · 注册即享多重优惠',
    imageUrl: '/banners/banner-new-user.png',
    linkUrl: '/coupons',
    buttonText: '立即领取',
    fullImage: true
  },
  {
    title: '当季上新 限时优选',
    subtitle: '新鲜水果 · 时令蔬菜 · 品质好物 每日上新',
    imageUrl: '/banners/banner-season.png',
    linkUrl: '/products',
    buttonText: '查看新品',
    fullImage: true
  }
]
