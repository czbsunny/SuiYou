<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()

const setupInterceptors = () => {
  uni.addInterceptor('uploadFile', {
    invoke(args) {
      if (!args.url.startsWith('http')) {
        args.url = 'https://www.zhitouying.cn' + args.url
      }
      args.header = args.header || {}
      const token = uni.getStorageSync('token')
      if (token) args.header['Authorization'] = `Bearer ${token}`
      uni.showLoading({ title: '上传中', mask: true })
    },
    complete() {
      uni.hideLoading()
    }
  })
}

onLaunch(async () => {
  console.log('App Launching...')
  const CURRENT_VERSION = '1.0.0'

  const lastVersion = uni.getStorageSync('app_version')
  if (lastVersion !== CURRENT_VERSION) {
    authStore.logout()
    uni.setStorageSync('app_version', CURRENT_VERSION)
  }

  await authStore.login()

  setupInterceptors()
  
  console.log('App Ready')
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})
</script>

<style lang="scss">
@import '@/styles/common.scss';
</style>