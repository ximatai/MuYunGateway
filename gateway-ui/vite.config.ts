import {defineConfig} from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
    base: '/gw/',
    plugins: [vue()],
    build: {
        outDir: '../src/main/resources/META-INF/resources/gw'
    },
    server: {
        open: false, // 自动启动浏览器·
        host: '0.0.0.0', // localhost
        port: 8001, // 端口号
        hmr: {overlay: false},
        proxy: {
            '/gw/api': {
                target: 'http://127.0.0.1:8888/',
                changeOrigin: true
            }
        },
    },
})
