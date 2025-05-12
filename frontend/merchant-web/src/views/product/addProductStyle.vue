<style lang="scss" scoped>
.product-add-container {
  padding: 20px;
  
  .form-card {
    background-color: #fff;
    border-radius: 8px;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
    
    .card-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      
      h2 {
        margin: 0;
        font-size: 20px;
        font-weight: 600;
        color: #333;
      }
    }
    
    .product-form {
      padding: 10px;
      
      .form-section {
        margin-bottom: 30px;
        background-color: #f9f9f9;
        border-radius: 8px;
        padding: 20px;
        
        .section-title {
          display: flex;
          align-items: center;
          font-size: 18px;
          font-weight: 600;
          margin-bottom: 20px;
          padding-bottom: 10px;
          border-bottom: 1px solid #eaeaea;
          color: #409EFF;
          
          .el-icon {
            margin-right: 8px;
            font-size: 20px;
          }
        }
        
        .upload-tip {
          font-size: 12px;
          color: #909399;
          margin-top: 5px;
        }
        
        .avatar-uploader {
          .avatar {
            width: 150px;
            height: 150px;
            display: block;
            object-fit: cover;
            border-radius: 4px;
          }
          
          .avatar-uploader-icon {
            display: flex;
            flex-direction: column;
            justify-content: center;
            align-items: center;
            font-size: 28px;
            color: #8c939d;
            width: 150px;
            height: 150px;
            border: 1px dashed #d9d9d9;
            border-radius: 4px;
            cursor: pointer;
            transition: all 0.3s;
            
            &:hover {
              border-color: #409EFF;
              color: #409EFF;
            }
            
            .upload-text {
              font-size: 14px;
              margin-top: 8px;
            }
          }
        }
      }
      
      .form-actions {
        display: flex;
        justify-content: center;
        gap: 15px;
        margin-top: 30px;
      }
    }
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input__wrapper),
:deep(.el-textarea__inner) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  
  &:hover {
    box-shadow: 0 0 0 1px #c0c4cc inset;
  }
  
  &.is-focus {
    box-shadow: 0 0 0 1px #409EFF inset;
  }
}

:deep(.el-select) {
  width: 100%;
}

:deep(.el-upload--picture-card) {
  --el-upload-picture-card-size: 120px;
  border-radius: 4px;
  
  &:hover {
    border-color: #409EFF;
  }
}

:deep(.el-upload-list--picture-card .el-upload-list__item) {
  border-radius: 4px;
}
</style>
