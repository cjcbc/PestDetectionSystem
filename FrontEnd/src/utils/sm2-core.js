import sm2Module from 'sm2'

const sm2 = sm2Module?.default ?? sm2Module
const SM2_CIPHER_MODE = 1
const SM2_PUBLIC_KEY_PATH = '/user/sm2-public-key'

if (typeof globalThis.SM2CipherMode === 'undefined' && globalThis.window?.SM2CipherMode) {
  globalThis.SM2CipherMode = globalThis.window.SM2CipherMode
}

// The bundled `sm2` package ships a broken Array.Clear implementation that
// references an undeclared loop variable. Override it before encryption runs.
Array.Clear = function clearArray(destinationArray, destinationIndex = 0, length = destinationArray.length) {
  const end = Math.min(destinationArray.length, destinationIndex + length)
  for (let index = destinationIndex; index < end; index += 1) {
    destinationArray[index] = 0
  }
}

function hexToBase64(hex) {
  const bytes = hex.match(/.{1,2}/g)?.map((byte) => Number.parseInt(byte, 16)) ?? []
  const binary = String.fromCharCode(...bytes)

  if (typeof btoa === 'function') {
    return btoa(binary)
  }

  return Buffer.from(binary, 'binary').toString('base64')
}

export function resolveSm2PublicKeyUrl(apiBaseUrl) {
  const normalizedBaseUrl = (apiBaseUrl || '').replace(/\/+$/, '')
  return `${normalizedBaseUrl}${SM2_PUBLIC_KEY_PATH}`
}

export function encryptWithSm2PublicKey(plaintext, publicKey) {
  const normalizedPublicKey = publicKey.trim()

  if (normalizedPublicKey.length < 128) {
    throw new Error('SM2 公钥长度不正确')
  }

  const pubkeyHex = normalizedPublicKey.length > 128
    ? normalizedPublicKey.slice(-128)
    : normalizedPublicKey
  const xHex = pubkeyHex.slice(0, 64)
  const yHex = pubkeyHex.slice(64)
  const cipher = new sm2.SM2Cipher(SM2_CIPHER_MODE)
  const userKey = cipher.CreatePoint(xHex, yHex)
  const message = sm2.CryptoJS.enc.Utf8.parse(plaintext)
  const encryptedHex = cipher.Encrypt(userKey, cipher.GetWords(message.toString()))

  return hexToBase64(`04${encryptedHex}`)
}
