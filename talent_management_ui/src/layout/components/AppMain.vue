<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <keep-alive :include="cachedViews">
        <router-view v-if="!$route.meta.link" :key="key" />
      </keep-alive>
    </transition>
    <iframe-toggle />
  </section>
</template>

<script>
import iframeToggle from "./IframeToggle/index"
import {stopBlinkTitle} from "@/utils/ruoyi";

export default {
  name: 'AppMain',
  components: { iframeToggle },
  computed: {
    cachedViews() {
      return this.$store.state.tagsView.cachedViews
    },
    key() {
      return this.$route.path
    }
  },
  watch: {
    $route() {
      this.addIframe()
    }
  },
  mounted() {
    this.addIframe()
    // ✅ 用户首次点击页面后自动解锁音频播放
    document.addEventListener('click', this.initAudioUnlock, { once: true });

    // 页签标题闪烁
    document.addEventListener('visibilitychange', () => {
      if (!document.hidden) {
        // 页面重新可见时，停止闪烁
        stopBlinkTitle()
      }
    })
  },
  methods: {
    addIframe() {
      const {name} = this.$route
      if (name && this.$route.meta.link) {
        this.$store.dispatch('tagsView/addIframeView', this.$route)
      }
    },

    initAudioUnlock() {
      try {
        const audio = new Audio(require('@/assets/mp3/4089.wav'));
        audio.muted = true;   // 🔇 静音播放一次
        audio.play().then(() => {
          console.log('%c音频播放权限已解锁 ✅', 'color: #4CAF50');
        }).catch((err) => {
          console.warn('音频权限解锁失败：', err);
        });
      } catch (e) {
        console.error('初始化音频失败：', e);
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.app-main {
  /* 50= navbar  50  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: hidden;
}

.fixed-header + .app-main {
  padding-top: 50px;
}

.hasTagsView {
  .app-main {
    /* 84 = navbar + tags-view = 50 + 34 */
    min-height: calc(100vh - 84px);
  }

  .fixed-header + .app-main {
    padding-top: 84px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 6px;
  }
}

::-webkit-scrollbar {
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-track {
  background-color: #f1f1f1;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c0c0;
  border-radius: 3px;
}
</style>
