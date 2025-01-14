import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  base: '/gw/',
  build: {
    outDir: '../src/main/resources/META-INF/resources/gw/', // 替换为你想要的输出路径
    assetsDir: 'static', // 指定静态资源存放的文件夹
    sourcemap: true, // 是否生成 sourcemap 文件
  },
  server: {
    proxy: {
      '/gw/api': {
        target: 'http://localhost:8888/', // 后端服务器地址
        changeOrigin: true, // 是否改变请求源
      },
    },
  },
})
