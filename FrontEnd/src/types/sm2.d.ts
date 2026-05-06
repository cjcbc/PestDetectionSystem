declare module 'sm2' {
  export function sm2Encrypt(plaintext: string, publicKey: string): string
  export function sm2Decrypt(ciphertext: string, privateKey: string): string
}
