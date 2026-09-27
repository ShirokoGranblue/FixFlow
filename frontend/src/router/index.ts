import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Home from '../views/Home.vue'
import {useUserStore} from "../stores/user.ts"

const router = createRouter({
    history: createWebHistory(),

    routes: [
        {
            path: '/login',
            component: Login
        },
        {
            path:'/',
            component: Home,

            meta:{
                requireAuth: true
            }
        }
    ]
})
router.beforeEach(
    async (to)=>{

    const userStore = useUserStore()

    if (to.path === '/login'){
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

    return '/login'
})

export default router