/**
 * SM2 国密加密工具
 * 使用后端提供的 SM2 公钥加密密码后传输
 */
import { encryptWithSm2PublicKey, resolveSm2PublicKeyUrl } from './sm2-core.js'

export const PUB_KEY_CACHE = 'sm2_public_key'
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8888/api'

/**
 * 从后端获取 SM2 公钥并缓存到 localStorage
 */
export async function fetchSm2PublicKey(): Promise<string> {
  const cached = localStorage.getItem(PUB_KEY_CACHE)
  if (cached) {
    console.log('[LoginDebug][SM2] using cached public key')
    return cached
  }

  const publicKeyUrl = resolveSm2PublicKeyUrl(API_BASE_URL)
  console.log('[LoginDebug][SM2] fetching public key:', publicKeyUrl)
  const res = await fetch(resolveSm2PublicKeyUrl(API_BASE_URL))
  if (!res.ok) {
    throw new Error(`SM2 公钥获取失败 (${res.status})`)
  }

  const json = await res.json()
  const pubKey: string = json.data
  if (!pubKey) {
    throw new Error('SM2 公钥响应无效')
  }

  localStorage.setItem(PUB_KEY_CACHE, pubKey)
  console.log('[LoginDebug][SM2] public key fetched, length:', pubKey.length)
  return pubKey
}

export function clearSm2PublicKeyCache(): void {
  localStorage.removeItem(PUB_KEY_CACHE)
}

/**
 * 使用 SM2 公钥加密密码
 * @param plaintext 明文密码
 * @returns base64 编码的密文
 */
export async function sm2EncryptPassword(plaintext: string): Promise<string> {
  console.log('[LoginDebug][SM2] encrypt password start, plaintext length:', plaintext.length)
  const pubKey = await fetchSm2PublicKey()
  const encryptedPassword = encryptWithSm2PublicKey(plaintext, pubKey)
  console.log('[LoginDebug][SM2] encrypt password done, cipher length:', encryptedPassword.length)
  return encryptedPassword
}
