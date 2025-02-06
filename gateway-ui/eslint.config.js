import antfu from '@antfu/eslint-config'

export default antfu({
  react: true,
  jsonc: false,
  yaml: false,
}, {
  rules: {
    'no-console': 'warn',
    'unused-imports/no-unused-vars': 'warn',
  },
})
