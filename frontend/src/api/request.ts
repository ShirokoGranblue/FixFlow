import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

function clearAuthAndRedirect() {
    localStorage.removeItem('tokenName')
    localStorage.removeItem('tokenValue')

    if (window.location.pathname !== '/user/login') {
        window.location.replace('/user/login?reason=session-expired')
    }

}

request.interceptors.request.use(config => {

    const tokenName = localStorage.getItem('tokenName')
    const tokenValue = localStorage.getItem('tokenValue')

    if (tokenName && tokenValue) {
        config.headers[tokenName] = tokenValue
    }

    return config
})

request.interceptors.response.use(

    response => {

        const data = response.data

        if (data?.code === 4010) {
            clearAuthAndRedirect()
            return Promise.reject(new Error("登录状态已失效，请重新登录"))
        }

        return response;
    },

    error => {

        if (axios.isAxiosError(error) && error.response?.status === 401) {
            clearAuthAndRedirect()
        }

        return Promise.reject(error)
    }
)

export default request