/**
 * 处理图片URL，添加API前缀
 * @param {string} url 图片URL
 * @returns {string} 处理后的URL
 */
export function formatImageUrl(url) {
  if (!url) return '';

  // 如果已经是完整URL（以http或https开头），则直接返回
  if (url.startsWith('http://') || url.startsWith('https://')) {
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

  // 使用环境变量中的API基础URL，如果没有则使用默认值
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api';

  // 如果apiBaseUrl已经包含了完整的URL（如http://127.0.0.1:3001/api），则直接使用
  if (apiBaseUrl.startsWith('http')) {
    return `${apiBaseUrl}${formattedUrl}`;
  }

  // 否则使用相对路径
  return `${apiBaseUrl}${formattedUrl}`;
}
