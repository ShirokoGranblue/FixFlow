import request from "./request.ts";
import type { Result } from "./user.ts";

export interface CategoryCount{
    categoryName:string
    categoryId:number
    count:number
}
export interface DashBoardVO{
    totalCount:number
    openCount:number
    inProgressCount:number
    resolvedCount:number
    closedCount:number
    todayCount:number
    categoryCount:CategoryCount[]
}

export function getDashboard(){
    return request.get<Result<DashBoardVO>>(
        '/dashboard'
    )
}