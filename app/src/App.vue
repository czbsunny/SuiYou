<script setup>
import { onLaunch, onShow, onHide } from '@dcloudio/uni-app'
import { useAuthStore } from '@/stores/auth'

onLaunch(() => {
  console.log('App Launch')
  checkAuth()
})

onShow(() => {
  console.log('App Show')
})

onHide(() => {
  console.log('App Hide')
})

const checkAuth = async () => {
  const authStore = useAuthStore()
  const token = uni.getStorageSync('token')

  if (token) {
    try {
      await authStore.fetchUserInfo()
    } catch (error) {
      authStore.clearAuth()
    }
  }
}
</script>

<style lang="scss">
@import '@/styles/common.scss';
</style>
