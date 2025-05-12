import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getCartList, addToCart, updateCartQuantity, updateCartSelected, deleteCartItem, selectAllCart } from '../api/cart'

export const useCartStore = defineStore('cart', () => {
  // 状态
  const cartItems = ref([])
  const cartLoading = ref(false)

  // 计算属性
  const cartCount = computed(() => cartItems.value.length)
  const selectedCount = computed(() => cartItems.value.filter(item => item.selected === 1).length)
  const totalPrice = computed(() => {
    return cartItems.value
      .filter(item => item.selected === 1)
      .reduce((total, item) => total + item.price * item.quantity, 0)
  })

  // 方法
  async function fetchCartList() {
    cartLoading.value = true
    try {
      const res = await getCartList()
      cartItems.value = res.data
      return Promise.resolve(res.data)
    } catch (error) {
      console.error('获取购物车列表失败:', error)
      return Promise.reject(error)
    } finally {
      cartLoading.value = false
    }
  }

  async function addCartItem(productId, quantity) {
    try {
      await addToCart(productId, quantity)
      await fetchCartList()
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function updateItemQuantity(id, quantity) {
    try {
      await updateCartQuantity(id, quantity)
      await fetchCartList()
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function updateItemSelected(id, selected) {
    try {
      await updateCartSelected(id, selected)
      await fetchCartList()
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function removeCartItem(id) {
    try {
      await deleteCartItem(id)
      await fetchCartList()
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  async function selectAll(selected) {
    try {
      await selectAllCart(selected)
      await fetchCartList()
      return Promise.resolve()
    } catch (error) {
      return Promise.reject(error)
    }
  }

  return {
    cartItems,
    cartLoading,
    cartCount,
    selectedCount,
    totalPrice,
    fetchCartList,
    addCartItem,
    updateItemQuantity,
    updateItemSelected,
    removeCartItem,
    selectAll
  }
})
