import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const count = ref(0)
  const title = ref('Vue3 App')

  function increment() {
    count.value++
  }

  function setTitle(newTitle: string) {
    title.value = newTitle
  }

  return {
    count,
    title,
    increment,
    setTitle
  }
})
