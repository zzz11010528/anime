<template>
  <div class="order-confirm-container">
    <el-card class="confirm-card">
      <template #header>
        <div class="card-header">
          <h2>确认订单</h2>
        </div>
      </template>

      <div v-if="loading" class="loading">
        <el-skeleton :rows="5" animated />
      </div>

      <template v-else>
        <!-- 收货地址 -->
        <div class="address-section">
          <div class="section-title">
            <h3>收货地址</h3>
            <el-button type="primary" @click="showAddressDialog" class="add-address-btn">
              <el-icon><Plus /></el-icon>
              添加新地址
            </el-button>
          </div>

          <div class="address-list">
            <div v-if="addresses.length === 0" class="no-address">
              <el-empty description="暂无收货地址" :image-size="120">
                <template #description>
                  <p>您还没有添加收货地址</p>
                </template>
                <el-button type="primary" @click="showAddressDialog">
                  <el-icon><Plus /></el-icon>
                  添加收货地址
                </el-button>
              </el-empty>
            </div>

            <el-radio-group v-else v-model="selectedAddressId" class="address-radio-group">
              <div v-for="address in addresses" :key="address.id"
                   class="address-item"
                   :class="{'address-selected': selectedAddressId === address.id}">
                <el-radio :label="address.id" class="address-radio">
                  <div class="address-content">
                    <div class="address-info">
                      <div class="address-header">
                        <span class="name">{{ address.name }}</span>
                        <span class="phone">{{ address.phone }}</span>
                        <span v-if="address.isDefault" class="default-tag">默认</span>
                      </div>
                      <div class="address-detail">{{ getFullAddress(address) }}</div>
                    </div>
                  </div>
                </el-radio>
                <div class="address-actions">
                  <el-button type="primary" plain size="small" @click="editAddress(address)" class="action-btn">
                    <el-icon><Edit /></el-icon>
                    编辑
                  </el-button>
                  <el-button type="danger" plain size="small" @click="deleteAddress(address.id)" class="action-btn">
                    <el-icon><Delete /></el-icon>
                    删除
                  </el-button>
                </div>
              </div>
            </el-radio-group>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="product-section">
          <div class="section-title">
            <h3>商品信息</h3>
          </div>

          <el-table :data="products" style="width: 100%">
            <el-table-column label="商品信息" min-width="400">
              <template #default="{ row }">
                <div class="product-info">
                  <el-image :src="formatImageUrl(row.mainImage)" :alt="row.name" class="product-image" />
                  <div class="product-details">
                    <div class="product-name">{{ row.name }}</div>
                    <div class="product-attrs" v-if="row.attrs">
                      <span v-for="(attr, index) in row.attrs" :key="index">{{ attr.name }}: {{ attr.value }}</span>
                    </div>
                  </div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="单价" width="120">
              <template #default="{ row }">¥{{ formatPrice(row.price) }}</template>
            </el-table-column>
            <el-table-column label="数量" width="100" prop="quantity" />
            <el-table-column label="小计" width="120">
              <template #default="{ row }">¥{{ formatPrice(row.price * row.quantity) }}</template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 配送说明 -->
        <div class="delivery-section">
          <div class="section-title">
            <h3>配送说明</h3>
          </div>

          <div class="delivery-info">
            <p>商品将通过快递配送，免运费，请确保收货地址信息准确无误</p>
          </div>
        </div>

        <!-- 支付方式 -->
        <div class="payment-section">
          <div class="section-title">
            <h3>支付方式</h3>
          </div>

          <div class="payment-options">
            <el-radio-group v-model="paymentMethod">
              <el-radio :label="1" class="payment-option">
                <div class="payment-option-content">
                  <img src="https://img.alicdn.com/imgextra/i4/O1CN01XCiY1B1px2zWDjfn4_!!6000000005427-2-tps-200-200.png" class="payment-icon" alt="支付宝" />
                  <span>支付宝</span>
                </div>
              </el-radio>
            </el-radio-group>
          </div>
        </div>

        <!-- 订单备注 -->
        <div class="remark-section">
          <div class="section-title">
            <h3>订单备注</h3>
          </div>

          <el-input
            v-model="remark"
            type="textarea"
            :rows="3"
            placeholder="请输入订单备注信息（选填）"
            maxlength="200"
            show-word-limit
            class="remark-input"
          />
        </div>

        <!-- 订单金额 -->
        <div class="amount-section">
          <div class="amount-item">
            <span class="label">商品总额：</span>
            <span class="value">¥{{ formatPrice(totalProductAmount) }}</span>
          </div>
          <div class="amount-item">
            <span class="label">优惠金额：</span>
            <span class="value">-¥{{ formatPrice(discountAmount) }}</span>
          </div>
          <div class="amount-item total">
            <span class="label">应付金额：</span>
            <span class="value price">¥{{ formatPrice(totalAmount) }}</span>
          </div>
        </div>

        <!-- 提交订单 -->
        <div class="submit-section">
          <el-button type="primary" size="large" @click="submitOrder" :loading="submitting" :disabled="!canSubmit">
            提交订单
          </el-button>
        </div>
      </template>
    </el-card>

    <!-- 地址对话框 -->
    <el-dialog
      v-model="addressDialogVisible"
      :title="editingAddress ? '编辑地址' : '添加地址'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form ref="addressFormRef" :model="addressForm" :rules="addressRules" label-width="100px">
        <el-form-item label="收货人" prop="name">
          <el-input v-model="addressForm.name" placeholder="请输入收货人姓名" />
        </el-form-item>

        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="addressForm.phone" placeholder="请输入手机号码" />
        </el-form-item>

        <el-form-item label="所在地区" prop="region">
          <el-cascader
            v-model="addressForm.region"
            :options="regionOptions"
            placeholder="请选择所在地区"
          />
        </el-form-item>

        <el-form-item label="详细地址" prop="detailAddress">
          <el-input v-model="addressForm.detailAddress" placeholder="请输入详细地址" />
        </el-form-item>

        <el-form-item label="邮政编码" prop="zipCode">
          <el-input v-model="addressForm.zipCode" placeholder="请输入邮政编码" />
        </el-form-item>

        <el-form-item>
          <el-checkbox v-model="addressForm.isDefault">设为默认收货地址</el-checkbox>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="addressDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveAddress" :loading="savingAddress">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete } from '@element-plus/icons-vue'
import { getProductDetail } from '../../api/product'
import { getCartList } from '../../api/cart'
import { createOrder, createDirectOrder } from '../../api/order'
import { formatImageUrl } from '../../utils/image'

const route = useRoute()
const router = useRouter()

// 状态
const loading = ref(true)
const products = ref([])
const addresses = ref([])
const selectedAddressId = ref(null)
const paymentMethod = ref(1) // 只支持支付宝
const remark = ref('')
const submitting = ref(false)

// 地址对话框
const addressDialogVisible = ref(false)
const addressFormRef = ref(null)
const editingAddress = ref(false)
const savingAddress = ref(false)
const addressForm = ref({
  id: null,
  name: '',
  phone: '',
  region: [],
  detailAddress: '',
  zipCode: '',
  isDefault: false
})

// 地址验证规则
const addressRules = {
  name: [
    { required: true, message: '请输入收货人姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号码', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  region: [
    { required: true, message: '请选择所在地区', trigger: 'change' }
  ],
  detailAddress: [
    { required: true, message: '请输入详细地址', trigger: 'blur' }
  ],
  zipCode: [
    { pattern: /^\d{6}$/, message: '请输入正确的邮政编码', trigger: 'blur' }
  ]
}

// 地区选项
const regionOptions = ref([
  {
    value: '110000',
    label: '北京市',
    children: [
      {
        value: '110100',
        label: '北京市',
        children: [
          { value: '110101', label: '东城区' },
          { value: '110102', label: '西城区' },
          { value: '110105', label: '朝阳区' },
          { value: '110106', label: '丰台区' },
          { value: '110107', label: '石景山区' },
          { value: '110108', label: '海淀区' },
          { value: '110109', label: '门头沟区' },
          { value: '110111', label: '房山区' },
          { value: '110112', label: '通州区' },
          { value: '110113', label: '顺义区' },
          { value: '110114', label: '昌平区' },
          { value: '110115', label: '大兴区' },
          { value: '110116', label: '怀柔区' },
          { value: '110117', label: '平谷区' },
          { value: '110118', label: '密云区' },
          { value: '110119', label: '延庆区' }
        ]
      }
    ]
  },
  {
    value: '310000',
    label: '上海市',
    children: [
      {
        value: '310100',
        label: '上海市',
        children: [
          { value: '310101', label: '黄浦区' },
          { value: '310104', label: '徐汇区' },
          { value: '310105', label: '长宁区' },
          { value: '310106', label: '静安区' },
          { value: '310107', label: '普陀区' },
          { value: '310109', label: '虹口区' },
          { value: '310110', label: '杨浦区' },
          { value: '310112', label: '闵行区' },
          { value: '310113', label: '宝山区' },
          { value: '310114', label: '嘉定区' },
          { value: '310115', label: '浦东新区' },
          { value: '310116', label: '金山区' },
          { value: '310117', label: '松江区' },
          { value: '310118', label: '青浦区' },
          { value: '310120', label: '奉贤区' },
          { value: '310151', label: '崇明区' }
        ]
      }
    ]
  }
])

// 计算属性
const totalProductAmount = computed(() => {
  return products.value.reduce((total, product) => {
    return total + product.price * product.quantity
  }, 0)
})

const shippingFee = computed(() => {
  // 不收取运费
  return 0
})

const discountAmount = computed(() => {
  // 计算优惠金额
  return 0
})

const totalAmount = computed(() => {
  return totalProductAmount.value + shippingFee.value - discountAmount.value
})

const canSubmit = computed(() => {
  return products.value.length > 0 && selectedAddressId.value
})

// 方法
async function fetchData() {
  loading.value = true

  try {
    // 获取商品信息
    if (route.query.productId) {
      // 从商品详情页直接购买
      const productId = route.query.productId
      const quantity = Number(route.query.quantity) || 1

      const res = await getProductDetail(productId)
      const product = res.data

      // 确保商品数据格式正确
      product.quantity = quantity

      // 确保图片URL正确
      if (product.mainImage) {
        product.mainImage = formatImageUrl(product.mainImage)
      }

      console.log('直接购买商品数据:', product)
      products.value = [product]
    } else if (route.query.from === 'cart') {
      // 从购物车结算
      const res = await getCartList()
      console.log('购物车数据:', res.data)

      // 过滤选中的商品并处理图片URL
      const selectedProducts = res.data.filter(item => item.selected === 1).map(item => {
        // 确保图片URL正确
        if (item.productImage) {
          item.mainImage = formatImageUrl(item.productImage)
        } else if (item.mainImage) {
          item.mainImage = formatImageUrl(item.mainImage)
        }

        // 确保商品名称正确
        if (item.productName && !item.name) {
          item.name = item.productName
        }

        return item
      })

      products.value = selectedProducts
      console.log('处理后的购物车商品:', selectedProducts)
    }

    // 获取收货地址
    fetchAddresses()
  } catch (error) {
    console.error('获取数据失败:', error)
    ElMessage.error('获取数据失败，请重试')
  } finally {
    loading.value = false
  }
}

// 获取收货地址
async function fetchAddresses() {
  // 从本地存储获取地址
  try {
    const savedAddresses = localStorage.getItem('userAddresses')
    if (savedAddresses) {
      addresses.value = JSON.parse(savedAddresses)

      // 默认选中默认地址
      const defaultAddress = addresses.value.find(address => address.isDefault)
      if (defaultAddress) {
        selectedAddressId.value = defaultAddress.id
      } else if (addresses.value.length > 0) {
        selectedAddressId.value = addresses.value[0].id
      }
    } else {
      // 没有保存的地址
      addresses.value = []
      selectedAddressId.value = null
    }
  } catch (error) {
    console.error('获取地址失败:', error)
    addresses.value = []
    selectedAddressId.value = null
  }
}

// 获取完整地址
function getFullAddress(address) {
  return `${address.province} ${address.city} ${address.district} ${address.detailAddress}`
}

// 显示地址对话框
function showAddressDialog() {
  editingAddress.value = false
  addressForm.value = {
    id: null,
    name: '',
    phone: '',
    region: [],
    detailAddress: '',
    zipCode: '',
    isDefault: false
  }
  addressDialogVisible.value = true
}

// 编辑地址
function editAddress(address) {
  editingAddress.value = true

  // 尝试根据省市区名称查找对应的region值
  const region = findRegionByNames(address.province, address.city, address.district)

  addressForm.value = {
    id: address.id,
    name: address.name,
    phone: address.phone,
    region: region,
    detailAddress: address.detailAddress,
    zipCode: address.zipCode,
    isDefault: address.isDefault
  }
  addressDialogVisible.value = true
}

// 根据省市区名称查找对应的region值
function findRegionByNames(provinceName, cityName, districtName) {
  const region = []

  try {
    // 查找省份
    const province = regionOptions.value.find(item => item.label === provinceName)
    if (province) {
      region.push(province.value)

      // 查找城市
      const city = province.children.find(item => item.label === cityName)
      if (city) {
        region.push(city.value)

        // 查找区县
        const district = city.children.find(item => item.label === districtName)
        if (district) {
          region.push(district.value)
        }
      }
    }
  } catch (error) {
    console.error('查找地区失败:', error)
  }

  return region
}

// 删除地址
function deleteAddress(addressId) {
  ElMessageBox.confirm('确定要删除该地址吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    // 删除地址
    addresses.value = addresses.value.filter(address => address.id !== addressId)

    // 保存到本地存储
    localStorage.setItem('userAddresses', JSON.stringify(addresses.value))

    ElMessage.success('删除成功')

    // 如果删除的是当前选中的地址，重新选择
    if (selectedAddressId.value === addressId) {
      const defaultAddress = addresses.value.find(address => address.isDefault)
      if (defaultAddress) {
        selectedAddressId.value = defaultAddress.id
      } else if (addresses.value.length > 0) {
        selectedAddressId.value = addresses.value[0].id
      } else {
        selectedAddressId.value = null
      }
    }
  }).catch(() => {})
}

// 保存地址
function saveAddress() {
  addressFormRef.value.validate(async (valid) => {
    if (!valid) return

    savingAddress.value = true
    try {
      // 从地区选择器获取省市区
      let province = '', city = '', district = ''
      if (addressForm.value.region && addressForm.value.region.length >= 3) {
        // 这里简化处理，实际应该根据选择的region值查找对应的文本
        const regionLabels = getRegionLabels(addressForm.value.region)
        province = regionLabels.province
        city = regionLabels.city
        district = regionLabels.district
      }

      if (editingAddress.value) {
        // 编辑地址
        const index = addresses.value.findIndex(address => address.id === addressForm.value.id)
        if (index !== -1) {
          // 更新地址
          const updatedAddress = {
            ...addresses.value[index],
            name: addressForm.value.name,
            phone: addressForm.value.phone,
            province: province || addresses.value[index].province,
            city: city || addresses.value[index].city,
            district: district || addresses.value[index].district,
            detailAddress: addressForm.value.detailAddress,
            zipCode: addressForm.value.zipCode,
            isDefault: addressForm.value.isDefault
          }

          // 如果设为默认地址，取消其他地址的默认状态
          if (addressForm.value.isDefault) {
            addresses.value.forEach(address => {
              if (address.id !== addressForm.value.id) {
                address.isDefault = false
              }
            })
          }

          addresses.value[index] = updatedAddress
        }
      } else {
        // 添加地址
        const newAddress = {
          id: Date.now(), // 生成唯一ID
          name: addressForm.value.name,
          phone: addressForm.value.phone,
          province: province || '未知省份',
          city: city || '未知城市',
          district: district || '未知区县',
          detailAddress: addressForm.value.detailAddress,
          zipCode: addressForm.value.zipCode,
          isDefault: addressForm.value.isDefault
        }

        // 如果设为默认地址，取消其他地址的默认状态
        if (addressForm.value.isDefault) {
          addresses.value.forEach(address => {
            address.isDefault = false
          })
        }

        addresses.value.push(newAddress)
        selectedAddressId.value = newAddress.id
      }

      // 保存到本地存储
      localStorage.setItem('userAddresses', JSON.stringify(addresses.value))

      ElMessage.success(editingAddress.value ? '地址更新成功' : '地址添加成功')
      addressDialogVisible.value = false
    } catch (error) {
      console.error('保存地址失败:', error)
      ElMessage.error(error.message || '操作失败')
    } finally {
      savingAddress.value = false
    }
  })
}

// 根据地区ID获取地区名称
function getRegionLabels(regionIds) {
  const result = {
    province: '',
    city: '',
    district: ''
  }

  try {
    // 查找省份
    const province = regionOptions.value.find(item => item.value === regionIds[0])
    if (province) {
      result.province = province.label

      // 查找城市
      const city = province.children.find(item => item.value === regionIds[1])
      if (city) {
        result.city = city.label

        // 查找区县
        const district = city.children.find(item => item.value === regionIds[2])
        if (district) {
          result.district = district.label
        }
      }
    }
  } catch (error) {
    console.error('解析地区失败:', error)
  }

  return result
}

// 格式化价格
function formatPrice(price) {
  return parseFloat(price).toFixed(2)
}

// 提交订单
async function submitOrder() {
  if (addresses.value.length === 0) {
    ElMessage.warning('请先添加收货地址')
    showAddressDialog()
    return
  }

  if (!selectedAddressId.value) {
    ElMessage.warning('请选择收货地址')
    return
  }

  if (products.value.length === 0) {
    ElMessage.warning('没有可购买的商品')
    return
  }

  submitting.value = true
  try {
    // 获取选中的地址
    const address = addresses.value.find(addr => addr.id === selectedAddressId.value)
    if (!address) {
      ElMessage.warning('收货地址信息不完整')
      return
    }

    // 构建基本订单数据
    const baseOrderData = {
      shippingName: address.name,
      shippingPhone: address.phone,
      shippingAddress: getFullAddress(address),
      payType: paymentMethod.value
      // 注意：后端Order实体类中没有remark字段，所以这里不传递remark
    }

    let res;

    // 根据来源选择不同的API
    if (route.query.productId) {
      // 直接购买
      const productId = route.query.productId
      const quantity = Number(route.query.quantity) || 1

      const directOrderData = {
        ...baseOrderData,
        productId: Number(productId),
        quantity: quantity
      }

      console.log('直接购买订单数据:', directOrderData)
      res = await createDirectOrder(directOrderData)
    } else {
      // 从购物车购买
      console.log('购物车订单数据:', baseOrderData)
      res = await createOrder(baseOrderData)
    }

    const orderId = res.data

    ElMessage.success('订单创建成功')

    // 跳转到支付页面
    router.push(`/order/${orderId}?pay=true`)
  } catch (error) {
    console.error('创建订单失败:', error)
    ElMessage.error(error.message || '创建订单失败')
  } finally {
    submitting.value = false
  }
}

// 生命周期钩子
onMounted(() => {
  fetchData()
})
</script>

<style lang="scss" scoped>
.order-confirm-container {
  max-width: 1000px;
  margin: 20px auto;

  .confirm-card {
    margin-bottom: 30px;
    border-radius: 10px;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    overflow: hidden;

    :deep(.el-card__header) {
      padding: 18px 20px;
      border-bottom: 1px solid #f0f0f0;
      background-color: #fafafa;
    }

    :deep(.el-card__body) {
      padding: 25px;
    }

    .card-header {
      h2 {
        margin: 0;
        font-size: 20px;
        color: #303133;
        font-weight: 600;
      }
    }

    .loading {
      padding: 30px 0;
    }

    .section-title {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 20px;

      h3 {
        margin: 0;
        font-size: 16px;
        position: relative;
        padding-left: 12px;
        font-weight: 600;
        color: #303133;

        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 18px;
          background-color: #409EFF;
          border-radius: 2px;
        }
      }
    }

    .address-section,
    .product-section,
    .delivery-section,
    .payment-section,
    .remark-section {
      margin-bottom: 30px;
    }

    .delivery-section {
      .delivery-info {
        padding: 10px 15px;
        background-color: #f8f9fa;
        border-radius: 4px;
        color: #606266;

        p {
          margin: 0;
          line-height: 1.5;
        }
      }
    }

    .payment-section {
      .payment-options {
        padding: 10px 0;

        .payment-option {
          display: block;
          margin-bottom: 10px;

          .payment-option-content {
            display: flex;
            align-items: center;

            .payment-icon {
              width: 24px;
              height: 24px;
              margin-right: 8px;
            }
          }
        }
      }
    }

    .remark-section {
      .remark-input {
        :deep(.el-textarea__inner) {
          border-radius: 4px;
          padding: 12px;
          font-size: 14px;

          &:focus {
            border-color: #409EFF;
            box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.1);
          }
        }
      }
    }

    .add-address-btn {
      font-size: 13px;
      border-radius: 4px;
    }

    .address-list {
      .no-address {
        padding: 20px 0;

        p {
          margin: 10px 0;
          color: #909399;
        }

        .el-button {
          margin-top: 10px;
        }
      }

      .address-radio-group {
        width: 100%;
        display: flex;
        flex-direction: column;
        gap: 15px;
      }

      .address-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 15px;
        border: 1px solid #e0e0e0;
        border-radius: 8px;
        margin-bottom: 15px;
        transition: all 0.3s ease;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.03);
        position: relative;
        overflow: hidden;

        &:hover {
          border-color: #c0c4cc;
          box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
        }

        &.address-selected {
          border-color: #409EFF;
          background-color: #ecf5ff;

          &::before {
            content: '';
            position: absolute;
            top: 0;
            left: 0;
            width: 4px;
            height: 100%;
            background-color: #409EFF;
          }
        }

        &:last-child {
          margin-bottom: 0;
        }

        .address-radio {
          width: 100%;
          margin-right: 0;

          :deep(.el-radio__label) {
            width: 100%;
          }
        }

        .address-content {
          width: 100%;
          padding-right: 20px;

          .address-info {
            width: 100%;

            .address-header {
              display: flex;
              align-items: center;
              margin-bottom: 8px;
              flex-wrap: wrap;

              .name {
                font-weight: bold;
                margin-right: 10px;
                font-size: 15px;
              }

              .phone {
                color: #606266;
                font-size: 14px;
              }

              .default-tag {
                display: inline-block;
                padding: 2px 8px;
                background-color: #f56c6c;
                color: #fff;
                font-size: 12px;
                border-radius: 12px;
                margin-left: 10px;
              }
            }

            .address-detail {
              color: #606266;
              font-size: 13px;
              line-height: 1.5;
            }
          }
        }

        .address-actions {
          display: flex;
          gap: 10px;

          .action-btn {
            display: flex;
            align-items: center;
            gap: 4px;
            padding: 6px 12px;
            font-size: 12px;
            border-radius: 4px;
          }
        }
      }
    }

    .product-info {
      display: flex;
      align-items: center;
      padding: 15px;
      border-radius: 8px;
      background-color: #fafafa;
      margin-bottom: 10px;
      transition: all 0.3s ease;

      &:hover {
        background-color: #f5f7fa;
      }

      .product-image {
        width: 70px;
        height: 70px;
        object-fit: cover;
        margin-right: 15px;
        border-radius: 6px;
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
      }

      .product-details {
        flex: 1;

        .product-name {
          font-size: 15px;
          margin-bottom: 6px;
          color: #303133;
          font-weight: 500;
        }

        .product-attrs {
          font-size: 13px;
          color: #909399;
          display: flex;
          flex-wrap: wrap;

          span {
            margin-right: 15px;
            margin-bottom: 5px;
          }
        }
      }
    }

    .amount-section {
      background-color: #f8f9fa;
      padding: 20px;
      border-radius: 8px;
      margin-top: 20px;
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.03);

      .amount-item {
        display: flex;
        justify-content: flex-end;
        margin-bottom: 12px;
        color: #606266;

        &:last-child {
          margin-bottom: 0;
        }

        .label {
          margin-right: 15px;
        }

        &.total {
          font-weight: bold;
          padding-top: 10px;
          margin-top: 10px;
          border-top: 1px dashed #e0e0e0;

          .price {
            color: #f56c6c;
            font-size: 22px;
            font-weight: 600;
          }
        }
      }
    }

    .submit-section {
      margin-top: 30px;
      text-align: center;

      .el-button {
        padding: 12px 30px;
        font-size: 16px;
        border-radius: 6px;
      }
    }
  }
}

@media (max-width: 768px) {
  .order-confirm-container {
    .confirm-card {
      .section-title {
        flex-direction: column;
        align-items: flex-start;

        .add-address-btn {
          margin-top: 10px;
        }
      }

      .address-list {
        .address-item {
          flex-direction: column;
          align-items: flex-start;

          .address-content {
            margin-bottom: 15px;
            padding-right: 0;
          }

          .address-actions {
            width: 100%;
            justify-content: flex-end;
            margin-top: 10px;
          }
        }
      }

      .product-info {
        flex-direction: column;
        align-items: flex-start;

        .product-image {
          margin-bottom: 10px;
          width: 80px;
          height: 80px;
        }
      }
    }
  }
}
</style>
