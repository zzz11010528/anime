<template>
  <div class="gesture-gallery">
    <el-card class="gallery-card">
      <template #header>
        <div class="card-header">
          <h2>手势交互图片浏览</h2>
          <el-button @click="goBack" size="small">返回</el-button>
        </div>
      </template>
      
      <div class="gallery-content">
        <p class="intro">
          使用手势控制图片浏览。打开摄像头，通过手势完成点击、放大、缩小、旋转和滑动操作。
        </p>
        
        <GestureImageViewer 
          :images="images" 
          :initial-index="currentIndex"
          @update:index="currentIndex = $event"
          @click="handleImageClick"
        />
        
        <div class="thumbnails">
          <div 
            v-for="(image, index) in images" 
            :key="index"
            class="thumbnail"
            :class="{ active: index === currentIndex }"
            @click="currentIndex = index"
          >
            <img :src="image" :alt="`Thumbnail ${index + 1}`" />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import GestureImageViewer from '../../components/GestureImageViewer.vue';
import { ElMessage } from 'element-plus';
import { formatImageUrl } from '../../utils/image';

const router = useRouter();
const currentIndex = ref(0);

// 示例图片数组
const images = ref([]);

// 加载图片
async function loadImages() {
  try {
    // 这里可以从API获取图片，或者使用静态图片
    // 示例：从产品列表获取图片
    const productImages = [
      formatImageUrl('/product/1/main.jpg'),
      formatImageUrl('/product/2/main.jpg'),
      formatImageUrl('/product/3/main.jpg'),
      formatImageUrl('/product/4/main.jpg'),
      formatImageUrl('/product/5/main.jpg'),
      formatImageUrl('/product/6/main.jpg'),
      formatImageUrl('/product/7/main.jpg'),
      formatImageUrl('/product/8/main.jpg'),
    ];
    
    images.value = productImages;
  } catch (error) {
    console.error('加载图片失败:', error);
    ElMessage.error('加载图片失败');
  }
}

// 处理图片点击
function handleImageClick() {
  ElMessage.success(`点击了图片 ${currentIndex.value + 1}`);
}

// 返回上一页
function goBack() {
  router.go(-1);
}

onMounted(() => {
  loadImages();
});
</script>

<style lang="scss" scoped>
.gesture-gallery {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
  
  .gallery-card {
    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      h2 {
        margin: 0;
      }
    }
    
    .gallery-content {
      .intro {
        margin-bottom: 20px;
        color: #666;
      }
      
      .thumbnails {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;
        margin-top: 20px;
        
        .thumbnail {
          width: 80px;
          height: 80px;
          border-radius: 4px;
          overflow: hidden;
          cursor: pointer;
          border: 2px solid transparent;
          transition: all 0.2s ease;
          
          &.active {
            border-color: #409EFF;
          }
          
          &:hover {
            transform: scale(1.05);
          }
          
          img {
            width: 100%;
            height: 100%;
            object-fit: cover;
          }
        }
      }
    }
  }
}
</style>
