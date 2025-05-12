<template>
  <div class="layout-container">
    <header-component />
    <div class="main-container">
      <router-view />
    </div>
    <footer-component />
  </div>
</template>

<script setup>
import HeaderComponent from './components/Header.vue'
import FooterComponent from './components/Footer.vue'
</script>

<style lang="scss" scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  
  .main-container {
    flex: 1;
    padding: 20px;
    background-color: #f5f5f5;
  }
}
</style>
