import request from "./request.ts";
import type {Result}  from "./user.ts";

export interface CategoryVO{
    id:number
    name:string
    createdAt:string
}

export function getCategories(){
    return request.get<Result<CategoryVO[]>>
    (
        '/category'
    )
}
