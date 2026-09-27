import axios from 'axios'

const request = axios.create({
    baseURL: '/api',
    timeout: 10000
})

request.interceptors.request.use(config => {

    const tokenName = localStorage.getItem('tokenName')
    const tokenValue = localStorage.getItem('tokenValue')

    if (tokenName && tokenValue) {
        config.headers[tokenName] = tokenValue
    }

    return config
})

export default request