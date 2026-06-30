<template>
  <section class="coupon-hero">
    <div class="hero-copy">
      <h1>{{ title }}</h1>
      <p>{{ subtitle }}</p>
    </div>
    <div class="hero-art" aria-hidden="true">
      <div class="gift-box"></div>
      <div class="ticket ticket-large"><span>¥30</span></div>
      <div class="ticket ticket-mid"><span>¥20</span></div>
      <div class="ticket ticket-small"><span>¥10</span></div>
      <i class="leaf leaf-a"></i>
      <i class="leaf leaf-b"></i>
      <i class="confetti confetti-a"></i>
      <i class="confetti confetti-b"></i>
    </div>
  </section>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  title?: string
  subtitle?: string
}>(), {
  title: '优惠券中心',
  subtitle: '领券购物更优惠，享受更多超值福利'
})
</script>

<style scoped>
.coupon-hero {
  position: relative;
  min-height: 154px;
  overflow: hidden;
  border: 1px solid #dcefd4;
  border-radius: 16px;
  background:
    radial-gradient(circle at 92% 12%, rgba(255, 203, 95, 0.35), transparent 20%),
    linear-gradient(100deg, #ecfae4 0%, #fbf8d9 55%, #dff7ce 100%);
  box-shadow: 0 12px 30px rgba(38, 94, 54, 0.06);
}

.hero-copy {
  position: relative;
  z-index: 2;
  padding: 34px 54px;
}

.hero-copy h1 {
  margin: 0;
  color: #087327;
  font-size: 46px;
  line-height: 1.08;
  font-weight: 900;
  letter-spacing: 0;
}

.hero-copy p {
  margin: 15px 0 0;
  color: #2f7d3a;
  font-size: 18px;
}

.hero-art {
  position: absolute;
  right: 42px;
  bottom: -16px;
  width: 440px;
  height: 168px;
}

.gift-box {
  position: absolute;
  right: 70px;
  bottom: 10px;
  width: 194px;
  height: 76px;
  border-radius: 12px 12px 6px 6px;
  background: linear-gradient(180deg, #35ad4d, #168734);
  box-shadow: 0 16px 24px rgba(27, 112, 44, 0.18);
}

.gift-box::before {
  position: absolute;
  top: -16px;
  right: -15px;
  left: -15px;
  height: 34px;
  border-radius: 10px;
  background: #47bd5b;
  content: "";
}

.ticket {
  position: absolute;
  display: grid;
  place-items: center;
  border: 2px solid #ffbf76;
  border-radius: 8px;
  background: linear-gradient(135deg, #ffe0ae, #ffc071);
  color: #f26b25;
  font-weight: 900;
  transform: rotate(-12deg);
  box-shadow: 0 8px 18px rgba(239, 121, 35, 0.16);
}

.ticket::before,
.ticket::after {
  position: absolute;
  top: 50%;
  width: 14px;
  height: 14px;
  border-radius: 50%;
  background: #eef8d8;
  content: "";
  transform: translateY(-50%);
}

.ticket::before {
  left: -8px;
}

.ticket::after {
  right: -8px;
}

.ticket-large {
  right: 198px;
  bottom: 47px;
  width: 110px;
  height: 78px;
  font-size: 34px;
}

.ticket-mid {
  right: 96px;
  bottom: 57px;
  width: 96px;
  height: 68px;
  font-size: 31px;
  transform: rotate(8deg);
}

.ticket-small {
  right: 24px;
  bottom: 52px;
  width: 72px;
  height: 54px;
  font-size: 23px;
  transform: rotate(12deg);
}

.leaf,
.confetti {
  position: absolute;
  display: block;
}

.leaf {
  width: 36px;
  height: 18px;
  border-radius: 100% 0 100% 0;
  background: linear-gradient(135deg, #7ecb38, #2ca83e);
}

.leaf-a {
  right: 5px;
  top: 42px;
  transform: rotate(-25deg);
}

.leaf-b {
  left: 88px;
  top: 20px;
  transform: rotate(24deg);
}

.confetti {
  width: 13px;
  height: 13px;
  border-radius: 3px;
  background: #ff6f5f;
}

.confetti-a {
  left: 46px;
  top: 44px;
  transform: rotate(28deg);
}

.confetti-b {
  right: 326px;
  top: 34px;
  background: #f7b34a;
  transform: rotate(46deg);
}

@media (max-width: 760px) {
  .hero-copy {
    padding: 28px;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .hero-art {
    right: -130px;
    opacity: 0.54;
  }
}
</style>
