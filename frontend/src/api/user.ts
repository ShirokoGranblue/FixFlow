import request from './request.ts'

export interface LoginForm {
    username: string
    password: string
}

export interface UserVO {
    id: string
    username: string
    email: string
    role: string
}

export interface LoginVO {
    tokenName: string
    tokenValue: string
    user: UserVO
}

export interface RegisterDTO{
    username :string
    password:string 
    email:string
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
        '/user/login',
        data
    )
}

export function register(data:RegisterDTO){
    return request.post<Result<UserVO>>(
        '/user/register',
        data
    )
}

export function getStaffUsers(){
    return request.get<Result<UserVO[]>>(
        '/admin/user/staff'
    )
}