<template>
  <section class="home-banner-carousel" @mouseenter="pause" @mouseleave="resume">
    <Transition name="banner-fade" mode="out-in">
      <RouterLink :key="activeIndex" class="banner-slide" :to="activeBanner.linkUrl">
        <img
          v-if="activeBanner.imageUrl && !failedImages.has(activeBanner.imageUrl)"
          class="banner-image"
          :src="activeBanner.imageUrl"
          :alt="activeBanner.title"
          @error="markImageFailed(activeBanner.imageUrl)"
        />
        <div class="banner-overlay" />
        <div class="banner-copy">
          <span class="banner-kicker">YOUXUAN MALL</span>
          <h1>{{ activeBanner.title }}</h1>
          <p>{{ activeBanner.subtitle }}</p>
          <span class="banner-button">{{ activeBanner.buttonText }}</span>
        </div>
      </RouterLink>
    </Transition>

    <div class="banner-dots" aria-label="Banner 切换">
      <button
        v-for="(_, index) in displayBanners"
        :key="index"
        type="button"
        :class="{ active: index === activeIndex }"
        :aria-label="`切换到第 ${index + 1} 张 Banner`"
        @click="go(index)"
      />
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { fallbackBanners, type HomeBanner } from '@/constants/banners'
import type { Banner } from '@/types'

type BannerInput = Partial<Banner & HomeBanner> & Record<string, unknown>

const props = defineProps<{
  banners?: BannerInput[]
}>()

const activeIndex = ref(0)
const isPaused = ref(false)
const failedImages = ref(new Set<string>())
let timer: number | undefined

const textValue = (value: unknown) => (value === null || value === undefined ? '' : String(value).trim())

const resolveLink = (banner: BannerInput, index: number) => {
  const linkUrl = textValue(banner.linkUrl)
  if (linkUrl) return linkUrl

  const linkType = textValue(banner.linkType).toUpperCase()
  const linkValue = textValue(banner.linkValue)
  if (linkType === 'PRODUCT' && linkValue) return `/products/${linkValue}`
  if (linkType === 'CATEGORY' && linkValue) return `/products?categoryId=${encodeURIComponent(linkValue)}`
  if (linkType === 'URL' && linkValue) return linkValue
  return fallbackBanners[index % fallbackBanners.length].linkUrl
}

const normalizeBanner = (banner: BannerInput, index: number): HomeBanner => {
  const fallback = fallbackBanners[index % fallbackBanners.length]
  return {
    title: textValue(banner.title) || fallback.title,
    subtitle: textValue(banner.subtitle ?? banner.description) || fallback.subtitle,
    imageUrl: textValue(banner.imageUrl) || fallback.imageUrl,
    linkUrl: resolveLink(banner, index),
    buttonText: textValue(banner.buttonText) || fallback.buttonText
  }
}

const displayBanners = computed(() => {
  if (!props.banners?.length) return fallbackBanners

  const realBanners = props.banners.map((item, index) => normalizeBanner(item, index))
  if (realBanners.length >= fallbackBanners.length) return realBanners

  return [
    ...realBanners,
    ...fallbackBanners.slice(realBanners.length)
  ]
})

const activeBanner = computed(() => displayBanners.value[activeIndex.value] || fallbackBanners[0])

const stop = () => {
  if (timer) {
    window.clearInterval(timer)
    timer = undefined
  }
}

const start = () => {
  stop()
  timer = window.setInterval(() => {
    if (!isPaused.value && displayBanners.value.length > 1) {
      activeIndex.value = (activeIndex.value + 1) % displayBanners.value.length
    }
  }, 4200)
}

const pause = () => {
  isPaused.value = true
}

const resume = () => {
  isPaused.value = false
}

const go = (index: number) => {
  activeIndex.value = index
}

const markImageFailed = (imageUrl: string) => {
  failedImages.value = new Set(failedImages.value).add(imageUrl)
}

watch(
  () => displayBanners.value.length,
  (length) => {
    if (activeIndex.value >= length) activeIndex.value = 0
    start()
  },
  { immediate: true }
)

onBeforeUnmount(stop)
</script>

<style scoped>
.home-banner-carousel {
  position: relative;
  min-height: 342px;
  overflow: hidden;
  border-radius: var(--yx-radius-xl);
  background:
    linear-gradient(105deg, rgba(231, 249, 225, 0.94) 0%, rgba(244, 252, 240, 0.72) 52%, rgba(255, 244, 222, 0.88) 100%),
    radial-gradient(circle at 80% 42%, rgba(255, 122, 26, 0.2), transparent 18rem);
  box-shadow: var(--yx-shadow-card);
}

.banner-slide {
  position: absolute;
  inset: 0;
  display: block;
  min-height: 342px;
}

.banner-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
  object-fit: cover;
}

.banner-overlay {
  position: absolute;
  inset: 0;
  background:
    linear-gradient(90deg, rgba(241, 252, 235, 0.92) 0%, rgba(241, 252, 235, 0.62) 34%, rgba(241, 252, 235, 0.04) 70%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.04), rgba(0, 0, 0, 0.08));
}

.banner-copy {
  position: relative;
  z-index: 1;
  width: min(560px, 58%);
  padding: 58px;
}

.banner-kicker {
  display: inline-flex;
  margin-bottom: 14px;
  padding: 6px 12px;
  border-radius: 999px;
  color: var(--yx-primary-dark);
  font-size: 12px;
  font-weight: 800;
  background: rgba(220, 252, 231, 0.86);
}

.banner-copy h1 {
  margin: 0;
  color: #0b5f24;
  font-size: 42px;
  line-height: 1.16;
  letter-spacing: 0;
}

.banner-copy p {
  margin: 18px 0 28px;
  color: #2f5d3b;
  font-size: 18px;
}

.banner-button {
  display: inline-flex;
  align-items: center;
  min-height: 42px;
  padding: 0 22px;
  border-radius: 999px;
  color: #fff;
  font-weight: 800;
  background: linear-gradient(135deg, var(--yx-primary), var(--yx-primary-dark));
  box-shadow: 0 12px 24px rgba(22, 163, 74, 0.22);
}

.banner-dots {
  position: absolute;
  left: 50%;
  bottom: 20px;
  z-index: 2;
  display: flex;
  gap: 8px;
  transform: translateX(-50%);
}

.banner-dots button {
  width: 10px;
  height: 10px;
  padding: 0;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.74);
  box-shadow: 0 0 0 1px rgba(22, 101, 52, 0.16);
  cursor: pointer;
  transition: width 0.18s ease, background 0.18s ease;
}

.banner-dots button.active {
  width: 30px;
  background: var(--yx-primary);
}

.banner-fade-enter-active,
.banner-fade-leave-active {
  transition: opacity 0.28s ease;
}

.banner-fade-enter-from,
.banner-fade-leave-to {
  opacity: 0;
}

@media (max-width: 900px) {
  .home-banner-carousel,
  .banner-slide {
    min-height: 300px;
  }

  .banner-copy {
    width: 100%;
    padding: 36px;
  }

  .banner-copy h1 {
    font-size: 32px;
  }

  .banner-overlay {
    background: linear-gradient(90deg, rgba(241, 252, 235, 0.92), rgba(241, 252, 235, 0.54));
  }
}

@media (max-width: 560px) {
  .home-banner-carousel,
  .banner-slide {
    min-height: 280px;
  }

  .banner-copy {
    padding: 28px;
  }

  .banner-copy h1 {
    font-size: 28px;
  }

  .banner-copy p {
    font-size: 15px;
  }
}
</style>
