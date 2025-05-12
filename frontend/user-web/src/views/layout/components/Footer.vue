<template>
  <footer class="footer-container">
    <div class="footer-content">
      <div class="footer-main">
        <div class="footer-logo">
          <img src="../../../assets/images/logo.jpg" alt="Logo">
          <h2>动漫社区</h2>
          <p>专注于动漫周边的购物社区</p>
        </div>

        <div class="footer-links">
          <div class="link-group">
            <h3>购物</h3>
            <a href="/product">商品列表</a>
            <a href="/community">社区</a>
          </div>

          <div class="link-group">
            <h3>服务</h3>
            <a href="#">售后服务</a>
          </div>

          <div class="link-group">
            <h3>关于</h3>
            <a href="#">关于我们</a>
          </div>
        </div>
      </div>

      <div class="footer-bottom">
        <div class="copyright">
          <p>© 2025 动漫社区 版权所有</p>
        </div>
        <div class="social-links">
          <a href="#" title="微信">微信</a>
          <a href="#" title="微博">微博</a>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup>
// 无需额外逻辑
</script>

<style lang="scss" scoped>
.footer-container {
  background-color: #f5f5f5;
  color: #333;
  padding: 40px 0 20px;
  margin-top: 40px;
  border-top: 1px solid #eee;

  .footer-content {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;

    .footer-main {
      display: flex;
      justify-content: space-between;
      margin-bottom: 30px;

      @media (max-width: 768px) {
        flex-direction: column;
      }

      .footer-logo {
        width: 30%;

        @media (max-width: 768px) {
          width: 100%;
          margin-bottom: 30px;
          text-align: center;
        }

        img {
          width: 80px;
          height: 80px;
          border-radius: 50%;
          margin-bottom: 15px;
        }

        h2 {
          font-size: 24px;
          margin: 0 0 10px;
          color: #333;
        }

        p {
          font-size: 14px;
          color: #666;
          line-height: 1.5;
        }
      }

      .footer-links {
        display: flex;
        width: 65%;
        justify-content: space-between;

        @media (max-width: 768px) {
          width: 100%;
        }

        @media (max-width: 480px) {
          flex-direction: column;
        }

        .link-group {
          display: flex;
          flex-direction: column;

          @media (max-width: 480px) {
            margin-bottom: 20px;
          }

          h3 {
            font-size: 16px;
            margin: 0 0 15px;
            color: #333;
            font-weight: 600;
          }

          a {
            color: #666;
            text-decoration: none;
            margin-bottom: 10px;
            font-size: 14px;
            transition: color 0.2s;

            &:hover {
              color: #409EFF;
            }
          }
        }
      }
    }

    .footer-bottom {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 20px;
      border-top: 1px solid #ddd;

      @media (max-width: 768px) {
        flex-direction: column;
      }

      .copyright {
        p {
          font-size: 14px;
          color: #999;
          margin: 0;

          @media (max-width: 768px) {
            margin-bottom: 15px;
            text-align: center;
          }
        }
      }

      .social-links {
        display: flex;
        gap: 15px;

        a {
          color: #666;
          font-size: 14px;
          transition: color 0.2s;

          &:hover {
            color: #409EFF;
          }
        }
      }
    }
  }
}
</style>
