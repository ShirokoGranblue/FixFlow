import { defineStore } from 'pinia'
import { ref } from 'vue'
import type { UserVO } from "../api/user.ts"
import { getCurrentUser } from "../api/user.ts"
import { logout } from '../api/user.ts'

export const useUserStore = defineStore('user',() => {

    const user = ref<UserVO | null>(null);

    function setLogin(tokenName:string,tokenValue:string,userInfo:UserVO){
        localStorage.setItem('tokenValue',tokenValue)
        localStorage.setItem('tokenName',tokenName)

        user.value = userInfo
    }

    function clearLogin(){
        localStorage.removeItem('tokenName')
        localStorage.removeItem('tokenValue')

        user.value = null
    }

    async function restoreLogin() {

            const tokenValue = localStorage.getItem('tokenValue')

            if (!tokenValue) {
                return false
            }

                const response = await getCurrentUser()

                const result = response.data

                if (result.code !== 200) {
                    throw new Error(result.message || '获取用户信息失败')
                }

                user.value = result.data

                return true
        }

    async function setlogout(){

        try{    
            await logout()
        }finally{

            clearLogin()
        }
    }

    return {user,setLogin,clearLogin,restoreLogin,setlogout}
})
