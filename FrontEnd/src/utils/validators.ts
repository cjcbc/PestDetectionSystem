/**
 * 表单校验工具函数集合
 */

/**
 * 校验邮箱格式
 */
export function isValidEmail(email: string): boolean {
  const re = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  return re.test(email)
}

/**
 * 校验手机号（中国）
 * 11位数字，以13/14/15/16/17/18/19开头
 */
export function isValidPhone(phone: string): boolean {
  const re = /^1[3-9]\d{9}$/
  return re.test(phone)
}

/**
 * 校验用户名
 * 长度3-20，仅允许字母、数字、下划线
 */
export function isValidUsername(username: string): boolean {
  const re = /^[a-zA-Z0-9_]{3,20}$/
  return re.test(username)
}

/**
 * 校验文件大小
 * @param file 文件对象
 * @param maxSizeMB 最大大小（单位：MB）
 */
export function isFileSizeValid(file: File, maxSizeMB: number = 5): boolean {
  return file.size <= maxSizeMB * 1024 * 1024
}

/**
 * 脱敏手机号
 * 138****0000
 */
export function maskPhone(phone: string): string {
  if (!phone || phone.length < 11) return phone
  return phone.slice(0, 3) + '****' + phone.slice(7)
}

/**
 * 脱敏邮箱
 * u***@example.com
 */
export function maskEmail(email: string): string {
  if (!email || !email.includes('@')) return email
  const [username, domain] = email.split('@')
  if (username.length <= 1) return email
  return username[0] + '*'.repeat(Math.min(3, username.length - 1)) + '@' + domain
}

/**
 * 性别转中文
 */
export function sexToText(sex: 0 | 1 | 2 | null | undefined): string {
  switch (sex) {
    case 0:
      return '女'
    case 1:
      return '男'
    case 2:
    default:
      return '未知'
  }
}

/**
 * 中文转性别
 */
export function textToSex(text: string): 0 | 1 | 2 {
  switch (text) {
    case '女':
      return 0
    case '男':
      return 1
    default:
      return 2
  }
}
