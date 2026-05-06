/**
 * SM2 国密加密工具
 * 使用后端提供的 SM2 公钥加密密码后传输
 */
import { sm2Encrypt } from 'sm2'

const PUB_KEY_CACHE = 'sm2_public_key'

/**
 * 从后端获取 SM2 公钥并缓存到 localStorage
 */
export async function fetchSm2PublicKey(): Promise<string> {
  const cached = localStorage.getItem(PUB_KEY_CACHE)
  if (cached) return cached

  const res = await fetch('/api/user/sm2-public-key')
  const json = await res.json()
  const pubKey: string = json.data
  localStorage.setItem(PUB_KEY_CACHE, pubKey)
  return pubKey
}

/**
 * 使用 SM2 公钥加密密码
 * @param plaintext 明文密码
 * @returns base64 编码的密文（hex -> base64）
 */
export async function sm2EncryptPassword(plaintext: string): Promise<string> {
  const pubKey = await fetchSm2PublicKey()
  const hexCipher = sm2Encrypt(plaintext, pubKey)
  return btoa(hexCipher) // hex -> base64
}
