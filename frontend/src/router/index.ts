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
import UserManage from '../views/UserManage.vue'
import Dashboard from '../views/Dashboard.vue'
import AuditLog from '../views/AuditLog.vue'
import MainLayout from '../layouts/MainLayout.vue'
import NotFound from '../views/NotFound.vue'
import { ElMessage } from 'element-plus'

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
            component: MainLayout,

            meta:{
                requireAuth: true
            },

            children:[
                {
                    path:'',
                    component:Home
                },
                {
                    path:'ticket/my-tickets',
                    component: MyTickets,
                },
                {
                    path:'ticket/create',
                    component: CreateTicket,
                },
                {
                    path: 'ticket/:id',
                    component: TicketById,
                },
                {
                    path:'ticket/manage',
                    component:TicketManage,
                    meta:{
                        roles:['STAFF','ADMIN']
                    }
                },
                {
                    path:'ticket/staff',
                    component:StaffWorkspace,
                    meta:{
                        roles:['STAFF','ADMIN']
                    }
                },
                {
                    path:'admin/users',
                    component:UserManage,
                    meta:{
                        roles:['ADMIN']
                    }
                },
                {
                    path:'dashboard',
                    component:Dashboard,
                    meta:{
                        roles:['STAFF','ADMIN']
                    }
                },
                {
                    path:'audit-log',
                    component:AuditLog,
                    meta:{
                        roles:['ADMIN']
                    }
                }
            ]
        },
        {
            path:'/:pathMatch(.*)*',
            component:NotFound
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
            
            try{
                const success = await userStore.restoreLogin()

                if (success){
                    return '/'
                }
            }catch{
                    if(localStorage.getItem('tokenValue')){
                    ElMessage.error('暂时无法验证登录状态，请稍后重试')
                }
            }
        }
        return true
    }

    if(!to.meta.requireAuth){
        return true
    }

    if(!userStore.user){
        const success = await userStore.restoreLogin()
        try{
            if(!success){
                ElMessage.warning('请先登录')
                return '/user/login'
            }
        }catch{
            if(localStorage.getItem('tokenValue')){
                ElMessage.error('暂时无法验证登录状态，请稍后重试')
            }
        }
    }

    const roles = to.meta.roles as string[] | undefined
    const userRoles = userStore.user?.role
    if(roles){
        if(!userRoles || !roles.includes(userRoles)){
            ElMessage.warning('你没有访问该页面的权限')
            return '/'
        }
    }

    return true
})

export default router