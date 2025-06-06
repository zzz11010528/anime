/**
 * 处理图片URL，添加API前缀
 * @param {string} url 图片URL
 * @returns {string} 处理后的URL
 */
export function formatImageUrl(url) {
  if (!url) return '';

  console.log('处理图片URL:', url);

  try {
    // 如果已经是完整URL（以http或https开头），则直接返回
    if (url.startsWith('http://') || url.startsWith('https://')) {
      console.log('完整URL，直接返回:', url);
      return url;
    }

    // 如果URL是data:开头的Base64数据，直接返回
    if (url.startsWith('data:')) {
      return url;
    }

    // 如果已经包含/api前缀，则直接返回
    if (url.startsWith('/api')) {
      return url;
    }

    // 确保URL以/开头
    const formattedUrl = url.startsWith('/') ? url : '/' + url;

    // 使用127.0.0.1:3001作为API服务器地址
    const result = `http://127.0.0.1:3001/api${formattedUrl}`;
    console.log('处理后的URL:', result);
    return result;
  } catch (error) {
    console.error('URL处理错误:', error, url);
    return '';
  }
}
