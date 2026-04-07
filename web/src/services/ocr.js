// 组合持仓识别服务模块
const OCR_API_BASE = '/api/ocr';

// 识别基金持仓
export const recognize_fund = async (filePath) => {
  try {
    const res = await uni.uploadFile({
        url: OCR_API_BASE + '/fund',
        filePath: filePath,
        name: 'file',
    });
    console.log('基金持仓识别响应:', res);
    if (res.statusCode === 200) {
      return JSON.parse(res.data)
    } else {
      throw new Error(`请求失败: ${res.statusCode}`);
    }
  } catch (error) {
    console.error('基金持仓识别失败:', error);
    throw error;
  }
}
