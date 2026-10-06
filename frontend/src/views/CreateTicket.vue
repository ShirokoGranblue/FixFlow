<script setup lang = "ts">
import { ref,onMounted } from 'vue';
import { getCategories } from '../api/category.ts';
import { createTicket } from '../api/ticket.ts';
import { useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import type { CategoryVO } from '../api/category.ts';

const ticket = ref({
    title: '',
    description: '',
    priority: 'MEDIUM',
    categoryId: null as number | null
});

const loading = ref(false);
const router = useRouter();
const categories = ref<CategoryVO[]>([]);

async function loadCategories() {
    loading.value = true;
    try{
        const response = await getCategories()
        if(response.data.code===200){
            categories.value = response.data.data
        }
    }catch(error){
        ElMessage.error("获取分类失败");
    }finally{
        loading.value = false;
    }
}
async function handleSubmit(){

    if(!ticket.value.title.trim()){
        ElMessage.warning("请输入标题");
        return
    }
    if(!ticket.value.description.trim()){
        ElMessage.warning("请填写问题描述");
        return
    }
    if(!ticket.value.priority.trim()){
        ElMessage.warning("请填写问题优先级");
        return;
    }
    if(ticket.value.categoryId == null){
        ElMessage.warning("请填写工单分类");
        return;
    }

    loading.value = true;

    try{
        const response = await createTicket(ticket.value);

        if(response.data.code !== 200){
            ElMessage.error(response.data.message);
            return
        }
        ElMessage.success("工单提交成功")

        router.push(`/ticket/${response.data.data.id}`)

    }catch(error){

        ElMessage.error("工单提交失败")

    }finally{

        loading.value = false;
    }
}

onMounted(
    ()=> { loadCategories() }
);
</script>

<template>

  <div class="page">

    <h2>提交工单</h2>

    <el-form
      :model="ticket"
      label-width="100px"
    >

      <el-form-item label="标题">

        <el-input
          v-model="ticket.title"
          placeholder="请输入问题标题"
        />

      </el-form-item>


      <el-form-item label="问题描述">

        <el-input
          v-model="ticket.description"
          type="textarea"
          :rows="6"
          placeholder="请详细描述问题"
        />

      </el-form-item>


      <el-form-item label="分类">

        <el-select
          v-model="ticket.categoryId"
          placeholder="请选择分类"
        >

          <el-option
            v-for="category in categories"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />

        </el-select>

      </el-form-item>


      <el-form-item label="优先级">

        <el-select
          v-model="ticket.priority"
        >

          <el-option
            label="低"
            value="LOW"
          />

          <el-option
            label="普通"
            value="MEDIUM"
          />

          <el-option
            label="高"
            value="HIGH"
          />

          <el-option
            label="紧急"
            value="URGENT"
          />

        </el-select>

      </el-form-item>


      <el-form-item>

        <el-button
          type="primary"
          :loading="loading"
          @click="handleSubmit"
        >
          提交工单
        </el-button>

      </el-form-item>

    </el-form>

  </div>

</template>