<template>
    <el-menu
        :default-active="1"
        class="el-menu-current"
        mode="horizontal"
        background-color="#001529"
        text-color="#fff"
        active-text-color="#1890ff"
    >
        <el-menu-item :index="1">网关设置</el-menu-item>
    </el-menu>
    <div class="gateway-content">
        <el-form
            label-position="right"
            ref="formRef"
            label-width="150px"
            :model="dataForm"
            :rules="rules"
        >
            <div class="gateway-header">
                <p class="gateway-title">反向代理及静态渲染的各种参数</p>
                <div class="btns">
                    <el-button plain :icon="Refresh" @click="init">初始化</el-button>
                    <el-upload
                        ref="uploadRef"
                        :key="uploadKey"
                        :show-file-list="false"
                        accept=".json"
                        :auto-upload="false"
                        :limit="1"
                        :on-change="upLoadChange"
                        :disabled="isImporting"
                        :loading="isImporting"
                    >
                        <el-button plain :icon="Upload">
                            {{ isImporting ? '导入中...' : '导入' }}</el-button
                        >
                    </el-upload>
                    <el-button plain :icon="Download" @click="exportJson">导出</el-button>
                    <el-button plain :icon="RefreshLeft" @click="gatewayData">还原</el-button>
                    <el-button plain :icon="FolderChecked" @click="validator">检查</el-button>
                    <el-button type="primary" :icon="SetUp" :disabled="isSave" @click="save"
                        >应用</el-button
                    >
                </div>
            </div>
            <el-divider />
            <div class="gateway-body">
                <el-row :gutter="12" class="gateway-row" justify="space-between">
                    <el-col :span="8">
                        <baseSetting :dataForm="dataForm" />
                    </el-col>
                    <el-col :span="8">
                        <staticSite :dataForm="dataForm" />
                    </el-col>
                    <el-col :span="8">
                        <afterProxy :dataForm="dataForm" />
                    </el-col>
                </el-row>
            </div>
        </el-form>
    </div>
</template>

<script setup lang="ts">
import { reactive, ref, computed, provide, nextTick, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
    SetUp,
    Refresh,
    FolderChecked,
    Upload,
    Download,
    RefreshLeft,
} from '@element-plus/icons-vue'
import type { FormRules, FormInstance, UploadFile, UploadInstance } from 'element-plus'
import type { GatewayConfig } from './interface/interface.ts'
import baseSetting from './components/baseSetting.vue'
import staticSite from './components/staticSite.vue'
import afterProxy from './components/afterProxy.vue'
import { gatewaySetting, updateGatewaySetting } from './service/service.ts'
const formRef = ref<FormInstance>()
const uploadRef = ref<UploadInstance>()
provide('formRef', formRef) // 提供 ref 给子组件使用
const isImporting = ref<boolean>(false)
const uploadKey = ref(0)
const isSave = ref<boolean>(true)

// 创建新的空数据结构
const emptyData: GatewayConfig = {
    port: null,
    ssl: { use: false, certPath: '', keyPath: '' },
    index: '',
    login: { path: '', page: '', api: '' },
    jwt: { use: false, checkExpiration: false, expiresHour: null },
    session: { use: false, timeoutHour: null },
    redirects: [],
    headers: [],
    frontends: [],
    upstreams: [],
}
const dataForm = reactive<GatewayConfig>({ ...emptyData })
const rules = computed<FormRules<GatewayConfig>>(() => {
    // 创建通用数值校验规则
    const numberRule = (fieldName: string) => [
        {
            validator: (_: any, value: any, callback: any) => {
                if (value === undefined) return callback()
                if (typeof value === 'number' && !isNaN(value)) return callback()
                callback(new Error(`${fieldName}必须为有效数字`))
            },
            trigger: 'blur',
            required: true,
        },
    ]
    let rule: { [key: string]: any } = {}
    const whiteList: string[] = []
    for (let key in dataForm) {
        if (whiteList.includes(key)) {
            rule[key] = [{ required: false }]
        } else {
            rule[key] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
        }
    }
    for (let key in dataForm.ssl) {
        rule[`ssl.${key}`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
    }
    rule[`jwt.expiresHour`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]
    rule[`session.timeoutHour`] = [{ required: true, message: '必填项不能为空', trigger: 'blur' }]

    return { ...rule, port: numberRule('端口') } as FormRules<GatewayConfig>
})
// 监听整个表单的验证状态
watch(
    () => dataForm,
    (newVal: any) => {
        formRef.value?.validate((valid) => {
            isSave.value = !valid // 当验证通过时禁用保存按钮
        })
    },
    { deep: true }, // 深度监听嵌套对象变化
)
//导出
const exportJson = () => {
    // 创建一个Blob对象，包含JSON格式的数据
    const blob = new Blob([JSON.stringify(dataForm)])

    // 定义导出的文件名
    const fileName = 'gateway.json' //文件名

    // 创建一个a元素
    const link = document.createElement('a')

    // 设置a元素的href属性为Blob对象的URL
    link.href = window.URL.createObjectURL(blob)

    // 设置a元素的download属性为文件名
    link.download = fileName

    // 触发a元素的点击事件，开始下载
    link.click()

    // 释放创建的URL对象
    window.URL.revokeObjectURL(link.href)
}
//检查
const validator = () => {
    formRef.value?.validate((valid: any): any => {
        return valid ? ElMessage.success('检查通过') : ElMessage.warning('配置有误，请检查！')
    })
}
//初始化
const init = () => {
    // 使用响应式替换
    Object.keys(dataForm).forEach((key: string) => {
        delete dataForm[key as keyof GatewayConfig]
    })
    Object.assign(dataForm, emptyData)
    // 清除校验状态
    nextTick(() => {
        formRef.value?.clearValidate()
    })
}
//导入
const upLoadChange = async (uploadFile: UploadFile) => {
    try {
        isImporting.value = true
        // 1. 清空组件内部文件列表以及数据
        uploadRef.value?.clearFiles()
        await init()
        // 2. 处理文件内容（示例）
        const fileData = await parseFile(uploadFile)
        // 执行结构校验
        const validationErrors = validateStructure(fileData, emptyData)
        if (validationErrors.length > 0) {
            throw new Error(`配置结构错误:\n${validationErrors.join('\n')}`)
        }
        // 执行安全合并
        const mergeErrors = safeMerge(dataForm, fileData, emptyData)
        if (mergeErrors.length > 0) {
            throw new Error(`数据合并错误:\n${mergeErrors.join('\n')}`)
        }
        // 3. 深度合并数据
        await mergeData(fileData)
        // 4. 强制刷新上传组件（可选）
        uploadKey.value++
        // 5. 提示成功并清除校验状态
        nextTick(() => {
            formRef.value?.clearValidate()
            ElMessage.success('导入成功')
        })
    } catch (error) {
        handleImportError(error)
        //出错时清理文件
        uploadRef.value?.clearFiles()
    } finally {
        isImporting.value = false
    }
}
// 文件解析方法
const parseFile = (file: UploadFile) => {
    return new Promise((resolve, reject) => {
        const reader = new FileReader()
        reader.onload = (e) => {
            try {
                resolve(JSON.parse(e.target?.result as string))
            } catch (e) {
                reject(new Error('文件解析失败'))
            }
        }
        reader.onerror = reject
        reader.readAsText(file.raw!)
    })
}
// 深度数据合并
const mergeData = (newData: any) => {
    const deepMerge = (target: any, source: any) => {
        Object.keys(source).forEach((key) => {
            if (source[key] && typeof source[key] === 'object' && !Array.isArray(source[key])) {
                if (!target[key]) target[key] = {}
                deepMerge(target[key], source[key])
            } else {
                target[key] = source[key]
            }
        })
    }

    deepMerge(dataForm, newData)
}
// 错误处理函数
const handleImportError = (error: unknown) => {
    const message = error instanceof Error ? error.message : '未知错误'
    ElMessage.error(`导入失败: ${message}`)
}
// 新增类型校验工具函数
const validateStructure = (source: any, template: any, path: string = ''): string[] => {
    const nullableNumberFields = ['port', 'jwt.expiresHour', 'session.timeoutHour']
    const errors: string[] = []
    // 特殊处理可空数字字段
    if (nullableNumberFields.includes(path)) {
        if (source !== null && typeof source !== 'number') {
            errors.push(`类型错误: ${path} 应为 number 或 null`)
        }
        return errors // 跳过后续通用校验
    }
    // 基础类型校验（排除已处理的可空数字字段）
    const expectedType =
        typeof template === 'object' && template === null ? 'null' : typeof template
    const actualType = typeof source === 'object' && source === null ? 'null' : typeof source

    if (expectedType !== actualType) {
        // 处理 null 的特殊情况
        if (!(template === null && (source === null || typeof source === 'number'))) {
            errors.push(`类型不匹配: ${path} 期望 ${expectedType}, 实际 ${actualType}`)
        }
        return errors
    }

    // 对象结构校验
    if (typeof template === 'object' && !Array.isArray(template)) {
        // 检查缺失字段
        Object.keys(template).forEach((key) => {
            const currentPath = path ? `${path}.${key}` : key
            if (!(key in source)) {
                errors.push(`字段缺失: ${currentPath}`)
            } else {
                // 递归校验嵌套对象
                errors.push(...validateStructure(source[key], template[key], currentPath))
            }
        })
    }

    // 数组结构校验
    if (Array.isArray(template)) {
        if (!Array.isArray(source)) {
            errors.push(`类型错误: ${path} 应为数组`)
            return errors
        }

        // 校验数组元素
        source.forEach((item, index) => {
            const elementPath = `${path}[${index}]`
            if (template.length > 0) {
                errors.push(...validateStructure(item, template[0], elementPath))
            }
        })
    }

    return errors
}
// 增强版数据合并方法
const safeMerge = (target: any, source: any, template: any): string[] => {
    const errors: string[] = []
    const nullableNumberFields = ['port', 'jwt.expiresHour', 'session.timeoutHour']
    Object.keys(template).forEach((key) => {
        const currentValue = source[key]
        const templateValue = template[key]

        // 检查字段存在性
        if (!(key in source)) {
            errors.push(`字段缺失: ${key}`)
            return
        }

        // 数组处理
        if (Array.isArray(templateValue)) {
            if (!Array.isArray(currentValue)) {
                errors.push(`类型错误: ${key} 应为数组`)
                return
            }

            currentValue.forEach((item, index) => {
                if (templateValue.length > 0) {
                    const itemErrors = validateStructure(item, templateValue[0], `${key}[${index}]`)
                    errors.push(...itemErrors)
                }
            })

            // 保留响应式的同时合并数组
            target[key] = currentValue.map((item: any, index: number) => {
                if (target[key]?.[index]) {
                    return { ...target[key][index], ...item }
                }
                return item
            })
        }
        // 对象处理
        else if (typeof templateValue === 'object' && templateValue !== null) {
            if (typeof currentValue !== 'object' || currentValue === null) {
                errors.push(`类型错误: ${key} 应为对象`)
                return
            }

            const nestedErrors = validateStructure(currentValue, templateValue, key)
            errors.push(...nestedErrors)

            // 递归合并嵌套对象
            target[key] = target[key] || {}
            Object.assign(target[key], currentValue)
        }
        // 基础类型处理
        else {
            // 特殊处理可空数字字段
            if (nullableNumberFields.includes(key)) {
                if (currentValue !== null && typeof currentValue !== 'number') {
                    errors.push(`类型错误: ${key} 应为 number 或 null`)
                }
            } else if (currentValue !== null && typeof currentValue !== typeof templateValue) {
                errors.push(
                    `类型错误: ${key} 期望 ${typeof templateValue}, 实际 ${typeof currentValue}`,
                )
            }
            target[key] = currentValue
        }
    })

    return errors
}
const gatewayData = async () => {
    const res = await gatewaySetting()
    Object.assign(dataForm, res.data)
}
//应用
const save = async () => {
    updateGatewaySetting(dataForm).then(() => {
        gatewayData()
        ElMessage.success('保存成功')
    })
}
onMounted(() => {
    gatewayData()
})
</script>

<style scoped lang="less">
.gateway-content {
    width: 98%;
    margin: 10px auto 0;
    .gateway-header {
        width: 100%;
        height: 40px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        .gateway-title {
            margin: 0;
            color: rgba(0, 0, 0, 0.85);
            font-weight: 600;
            font-size: 20px;
            line-height: 40px;
            overflow: hidden;
            white-space: nowrap;
            text-overflow: ellipsis;
        }
        .btns {
            width: 540px;
            display: flex;
            justify-content: space-between;
            align-items: center;
            :deep(.el-button + .el-button) {
                margin-left: 0;
            }
        }
    }
    .gateway-body {
        width: 100%;
        display: flex;
        justify-content: center;
        .gateway-row {
            width: 100%;
        }
    }
}
/* 清空后提示样式 */
:deep(.el-form) {
    .el-input__inner:placeholder-shown {
        border-color: #dcdfe6;
        &::placeholder {
            color: #c0c4cc;
        }
    }
}
.el-menu-current {
    :deep(.el-menu-item) {
        font-size: 16px;
        font-weight: bold;
    }
}
</style>
