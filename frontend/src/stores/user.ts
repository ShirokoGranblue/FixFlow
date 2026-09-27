import { defineStore } from 'pinia'
import {ref} from 'vue'
import type {UserVO} from "../api/user.ts"
import { getCurrentUser } from "../api/user.ts"

export const useUserStore =
    defineStore('user',()=>{

    const user = ref<UserVO | null>(null);

    function setLogin(tokenName:string,tokenValue:string,userInfo:UserVO){
        localStorage.setItem('tokenValue',tokenValue)
        localStorage.setItem('tokenName',tokenName)

        user.value = userInfo
    }

    function logout(){
        localStorage.removeItem('tokenName')
        localStorage.removeItem('tokenValue')

        user.value = null
    }

    async function restoreLogin() {

            const tokenValue =
                localStorage.getItem('tokenValue')

            if (!tokenValue) {
                return false
            }

            try {
                const response = await getCurrentUser()

                const result = response.data

                if (result.code !== 200) {
                    logout()
                    return false
                }

                user.value = result.data

                return true

            } catch (error) {

                logout()
                return false
            }
        }

    return {user,setLogin,logout,restoreLogin}
})
