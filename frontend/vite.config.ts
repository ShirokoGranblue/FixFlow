import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
    plugins: [vue()],

    server: {
        port: 3000,

        proxy: {
            '/api': {
                target: 'http://localhost:8080',
                changeOrigin: true,
                configure(proxy) {
                    proxy.on('proxyReq', (proxyReq, req) => {
                        proxyReq.removeHeader('X-Forwarded-For')
                        if (req.socket.remoteAddress) {
                            proxyReq.setHeader('X-Forwarded-For', req.socket.remoteAddress)
                        }
                    })
                }
            }
        }
    }
})
