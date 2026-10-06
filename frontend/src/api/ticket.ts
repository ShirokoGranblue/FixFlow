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

export function getMyTickets(){

    return request.get<Result<TicketVO[]>>
    (
        '/ticket/my-tickets'
    )
}

export function getTicketDetail(id:string){
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
