import { createRouter, createWebHistory } from 'vue-router'
import {useUserStore} from "../stores/user.ts"
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import MyTickets from '../views/MyTickets.vue'
import TicketById from '../views/TicketById.vue'
import CreateTicket from '../views/CreateTicket.vue'
import Register from '../views/Register.vue'
import TicketManage from '../views/TicketManage.vue'
import StaffWorkspace from '../views/StaffWorkspace.vue'

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
            component: TicketById,
            meta:{
                requireAuth: true
            }
        },
        {
            path:'/ticket/manage',
            component:TicketManage,
            meta:{
                requireAuth:true,
                roles:['STAFF','ADMIN']
            }
        },
        {
            path:'/ticket/staff',
            component:StaffWorkspace,
            meta:{
                requireAuth:true,
                roles:['STAFF','ADMIN']
            }
        }
    ]
})
router.beforeEach(
    async (to)=>{

    const userStore = useUserStore()
    const userRoles = userStore.user?.role

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

    if(!userStore.user){
        const success = await userStore.restoreLogin()

        if(!success){
            return '/user/login'
        }
    }

    const roles = to.meta.roles as string[] | undefined
    if(roles){
        if(!userRoles || !roles.includes(userRoles)){
            return '/'
        }
    }

    return true
})

export default router