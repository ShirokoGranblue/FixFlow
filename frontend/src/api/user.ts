import request from './request.ts'

export interface LoginForm {
    username: string
    password: string
}

export interface UserVO {
    id: number
    username: string
    email: string
    role: string
}

export interface LoginVO {
    tokenName: string
    tokenValue: string
    user: UserVO
}

export interface Result<T> {
    code: number
    message: string
    data: T
}
export function getCurrentUser(){
    return request.get<Result<UserVO>>(
        '/user/me'
    )
}

export function login(data: LoginForm) {
    return request.post<Result<LoginVO>>(
        '/users/login',
        data
    )
}