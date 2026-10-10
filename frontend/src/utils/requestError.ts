import axios from "axios"

export function getRequestError(error:unknown,fallback:string){

    if(!axios.isAxiosError(error)){
        return fallback 
    }

    if(error.code === 'ECONNABORTED' || error.code === 'ETIMEDOUT'){
        return '请求超时，请稍后重试'
    }

    if(!error.response){
        return '网络异常，服务器内部无响应'
    }

    const status = error.response.status

    if(status === 403){
        return '无权访问'
    }

    if(status === 404){
        return '请求的资源不存在'
    }

    if(status >= 500){
        return '服务器内部错误'
    }

    const message = error.response?.data?.message

    if(typeof message === 'string' && message.trim()){
        return message
    }

    return fallback
}