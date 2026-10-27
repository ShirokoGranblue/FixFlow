import request from "./request.ts";
import type { Result }  from "./user.ts";
import type { PageResult } from "./ticket.ts";

export interface AuditLog{
    id:number,
    username:string,
    ip:string,
    userId:string | null,
    action:string,
    method:string,
    success:boolean,
    errorMessage:string | null,
    durationMs:number,
    createdAt:string
}

export interface AuditPageQueryDTO{
    current:number,
    pageSize:number
}

export function getAuditLog(params:AuditPageQueryDTO){
    return request.get<Result<PageResult<AuditLog>>>(
        '/audit-log',{params}
    )
}