<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { ElMessage } from 'element-plus';
import { getUsers } from '../api/user.ts';
import { updateUsers } from '../api/user.ts';
import type { UserVO } from '../api/user.ts';
import { useUserStore } from '../stores/user.ts';
import { getRequestError } from '../utils/requestError.ts';

const loading = ref(false)
const query = ref({
    current:1,
    pageSize:10,
    keyword:'',
    role:''
})
const updateUserLoading = ref(false)
const total = ref(0)
const users = ref<UserVO[]>([])
const editDialogVisible = ref(false)
const selectedRole = ref('')
const editingUser = ref<UserVO | null>(null)
const userStore = useUserStore()

async function loadUsers(){

    loading.value = true

    try{

        const response = await getUsers(
            {
            current:query.value.current,
            pageSize:query.value.pageSize,
            keyword:query.value?.keyword || undefined,
            role:query.value?.role || undefined
        }
    )

        const result = response.data

        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }

        users.value = result.data.records
        total.value = result.data.total

    }catch(error){

        ElMessage.error(getRequestError(error,"用户加载失败"))
    }finally{
      
        loading.value = false
    }
}

async function handleUpdateRole(){

    if(editingUser.value===null){
        ElMessage.warning("用户不存在")
        return
    }

    if(selectedRole.value==null){
        ElMessage.warning("请先选择角色")
        return
    }
    updateUserLoading.value = true
  
    try{
        const response = await updateUsers(
            editingUser.value.id,
            { 
                role:selectedRole?.value 
            }
        )
        const result = response.data
        
        if(result.code!==200){
            ElMessage.error(result.message)
            return
        }
        
        ElMessage.success("用户修改成功")

        editDialogVisible.value = false

        await loadUsers()

    }catch(error){
        ElMessage.error("用户修改失败")
    }finally{
        updateUserLoading.value = false
    }

}

async function handleSearch(){
    query.value.current=1
     loadUsers()
}

async function handleReset(){
    query.value.current = 1

    query.value.keyword = ''
    query.value.role = ''

     loadUsers()
}

async function handleSizeChange(){
    query.value.current=1

     loadUsers()
}

async function openRoleDialog(user:UserVO){
    selectedRole.value = ''

    editDialogVisible.value = true

    editingUser.value = user
}

onMounted(()=>{
    loadUsers()
})
</script>

<template>

  <div class="page">

    <h2>用户管理</h2>


    <el-form
      :inline="true"
      :model="query"
    >

      <el-form-item label="用户名">

        <el-input
          v-model="query.keyword"
          placeholder="搜索用户名"
          clearable
        />

      </el-form-item>


      <el-form-item label="身份">

        <el-select
          v-model="query.role"
          placeholder="全部角色"
          clearable
        >

          <el-option
            label="全部"
            value= ""
          />
          
          <el-option
            label="普通用户"
            value="USER"
          />

          <el-option
            label="工作人员"
            value="STAFF"
          />

          <el-option
            label="管理员"
            value="ADMIN"
          />

        </el-select>

      </el-form-item>


      <el-form-item>

        <el-button
          type="primary"
          @click="handleSearch"
        >
          搜索
        </el-button>

        <el-button
          @click="handleReset"
        >
          重置
        </el-button>

      </el-form-item>

    </el-form>

    <el-table
      :data="users"
      v-loading="loading"
      border
    >

      <el-table-column
        prop="id"
        label="ID"
        width="80"
      />

      <el-table-column
        prop="username"
        label="用户名"
      />

      <el-table-column
        prop="email"
        label="邮箱"
      />

      <el-table-column
        prop="role"
        label="角色"
        width="120"
      />

      <el-table-column
        prop="createdAt"
        label="注册时间"
        width="190"
      />


      <el-table-column
        label="操作"
        width="140"
      >

        <template #default="scope">

          <el-button
            v-if="
              scope.row.role !== 'ADMIN'
              && scope.row.id !== userStore.user?.id
            "
            link
            type="primary"
            @click="openRoleDialog(scope.row)"
          >
            修改角色
          </el-button>

        </template>

      </el-table-column>

    </el-table>

    <el-pagination
            v-model:current-page="query.current"
            v-model:page-size="query.pageSize"

            :total="total"

            :page-sizes="[10, 20, 50]"

            layout="total, sizes, prev, pager, next"

            @current-change="loadUsers"
            @size-change="handleSizeChange"
    />

    <el-dialog
      v-model="editDialogVisible"
      title="修改用户角色"
      width="420px"
    >

      <p v-if="editingUser">

        用户：

        <strong>
          {{ editingUser.username }}
        </strong>

      </p>


      <el-form-item label="角色">

        <el-select
          v-model="selectedRole"
          style="width: 100%"
        >

          <el-option
            label="普通用户"
            value="USER"
          />

          <el-option
            label="工作人员"
            value="STAFF"
          />

        </el-select>

      </el-form-item>


      <template #footer>

        <el-button
          @click="editDialogVisible = false"
        >
          取消
        </el-button>

        <el-button
          type="primary"
          :loading="updateUserLoading"
          @click="handleUpdateRole"
        >
          保存
        </el-button>

      </template>

    </el-dialog>

  </div>

</template>