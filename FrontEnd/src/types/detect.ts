/**
 * 识别请求参数
 */
export interface DetectPayload {
  imageBase64: string
}

/**
 * 识别结果
 */
export interface DetectResult {
  id: string
  userId: string
  imageName: string
  topLabel: string
  confidence: number
  status: -1 | 0 | 1
  createdTime: number
}

/**
 * 图片尺寸信息
 */
export interface ImageDimensions {
  width: number
  height: number
}
