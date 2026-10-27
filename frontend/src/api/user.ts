import request from './request.ts'
import type { PageResult } from './ticket.ts'

export interface LoginForm {
    username: string
    password: string
}

export interface UserVO {
    id: string
    username: string
    email: string
    role: string
    createdAt:string
}

export interface UserQueryDTO{
    current:number
    pageSize:number
    keyword?:string
    role?:string
}

export interface UserRoleUpdateDTO{
    role:string
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
    httpStatus: number
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

export function logout(){
    return request.post<Result<null>>(
        '/user/logout'
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
        '/admin/staff'
    )
}

export function getUsers(params:UserQueryDTO){
     return request.get<Result<PageResult<UserVO>>>(
        '/admin/users' , {params}
    )
}

export function updateUsers(id:string,params:UserRoleUpdateDTO){
    return request.post<Result<UserVO>>(
        `/admin/${id}/role`,params
    )
}
