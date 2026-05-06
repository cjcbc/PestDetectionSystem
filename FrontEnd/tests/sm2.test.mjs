import assert from 'node:assert/strict'

const PUBLIC_KEY = 'edd982646ca545a430691a421209c27de2290283eb4a26d47bdf22f878b4904ceac731b4c845a58f445a40f1c56a8d4f3b766bab16530e3e8ed2019e9357e9fc'

globalThis.window ??= { location: {} }

const { encryptWithSm2PublicKey, resolveSm2PublicKeyUrl } = await import('../src/utils/sm2-core.js')

const ciphertext = encryptWithSm2PublicKey('123456', PUBLIC_KEY)
assert.match(ciphertext, /^[A-Za-z0-9+/]+={0,2}$/)
assert.notEqual(ciphertext, '123456')

assert.equal(resolveSm2PublicKeyUrl('/api'), '/api/user/sm2-public-key')
assert.equal(
  resolveSm2PublicKeyUrl('http://localhost:8888/api'),
  'http://localhost:8888/api/user/sm2-public-key'
)

console.log('sm2 regression checks passed')
