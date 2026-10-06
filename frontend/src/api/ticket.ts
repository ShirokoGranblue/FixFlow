import request from './request.ts'
import type { Result } from './user.ts'

export interface TicketVO{

    id: number
    title : string
    description : string
    priority : number
    status : string

    creatorId : string
    assigneeId : string | null

    categoryId : number
    categoryName : string

    createdAt : string
    updatedAt : string
    resovledAt : string | null
}

export interface TicketHistoryVO{
    id:string
    operatorId:string
    action:string
    fromStatus:string | null
    toStatus:string | null
    createdAt:string
}

export interface TicketCommentVO{
    id:string
    content:string
    authorId:string
    authorName:string
    ticketId:string
    createdAt:string
}

export interface TicketDetailVO{
    ticket : TicketVO
    ticketHistory:TicketHistoryVO[]
    ticketComments:TicketCommentVO[]
}

export interface TicketCreateDTO{
    title :string
    description : string
    priority : string
    categoryId : number | null
}

export interface TicketCommentDTO{
    content:string
}

export interface PageResult<T>{
    record:T[]
    total:number
    current:number
    pageSize:number
    pages:number
}

export interface TicketQuery{
    current:number
    pageSize:number

    keyword?:string
    status?:string
    priority?:string
    categoryId?:number
}

export interface TicketAssignDTO{
    assigneeId:string
}

export function getMyTickets(){

    return request.get<Result<TicketVO[]>>
    (
        '/ticket/my-tickets'
    )
}

export function getTicketById(id:string){
    return request.get<Result<TicketDetailVO>>
    (
        `/ticket/${id}/detail`
    )
}

export function createTicket(data:TicketCreateDTO){
    return request.post<Result<TicketVO>>
    (
        '/ticket/create',
        data
    )
}

export function getComment(id:string){
    return request.get<Result<TicketCommentVO>>
    (
        `/${id}/comment`
    )
}

export function addComment(data:TicketCommentDTO,id:string){
    return request.post<Result<TicketCommentVO>>(
        `/ticket/${id}/comment`,
        data
    )
}

export function cancelTicket(id:string){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/cancel`
    )
}

export function closeTicket(id:string){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/close`
    )
}

export function reOpenTicket(id:string){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/reopen`
    )
}

export function resolveTicket(id:string){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/resolve`
    )
}

export function handleTicket(id:string){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/handle`
    )
}

export function getManageTickets(params:TicketQuery){
    return request.get<Result<PageResult<TicketVO>>>(
        '/ticket/manage',{params}
    )
}

export function getOpenTickets(){
    return request.get<Result<TicketVO[]>>(
        '/ticket/open'
    )
}

export function getAssignedTickets(){
    return request.get<Result<TicketVO[]>>(
        '/ticket/assigned'
    )
}

export function assignTicket(id:string,data:TicketAssignDTO){
    return request.post<Result<TicketVO>>(
        `/ticket/${id}/assign`, 
        data
    )
}