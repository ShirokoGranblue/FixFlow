import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import MyTickets from '../views/MyTickets.vue'
import TicketDetail from '../views/TicketDetail.vue'
import {useUserStore} from "../stores/user.ts"

import Register from '../views/Register.vue'

import CreateTicket from '../views/CreateTicket.vue'

const router = createRouter({
    history: createWebHistory(),

    routes: [
        {
            path: '/user/login',
            component: Login
        },
        {
            path:'/user/register',
            component:Register
        },
        {
            path:'/',
            component: Home,

            meta:{
                requireAuth: true
            }
        },
        {
            path:'/ticket/my-tickets',
            component: MyTickets,
            meta:{
                requireAuth: true
            }
        },
        {
            path:'/ticket/create',
            component: CreateTicket,
            meta:{
                requireAuth:true    
            }
        },
        {
            path: '/ticket/:id',
            component: TicketDetail,
            meta:{
                requireAuth: true
            }
        }
    ]
})
router.beforeEach(
    async (to)=>{

    const userStore = useUserStore()

    if (to.path === '/user/login'){
        if(userStore.user){
            return '/'
        }

        const token = localStorage.getItem('tokenValue')
        if (token){
            const success = await userStore.restoreLogin()

            if (success){
                return '/'
            }
        }
        return true
    }

    if(!to.meta.requireAuth){
        return true
    }

    if(userStore.user){
        return true
    }

    const success = await userStore.restoreLogin()
    if(success){
        return true
    }

    return '/user/login'
})

export default router
