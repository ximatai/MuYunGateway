<template>
    <el-card class="box-card" shadow="never">
        <template #header>
            <div class="card-header">
                <span>静态站点</span>
                <el-button @click="addStaticSite" :icon="Plus" size="small" circle />
            </div>
        </template>
        <template v-if="dataForm.frontends.length">
            <draggable
                v-model="dataForm.frontends"
                @end="onDragEnd"
                item-key="index"
                handle=".drag-handle"
                :animation="300"
            >
                <template #item="{ element, index }">
                    <el-collapse v-model="activeName" accordion>
                        <el-collapse-item
                            :name="index + 1"
                            :key="index"
                            class="collapse collapse-item"
                        >
                            <template #title>
                                <div class="hander-collapse">
                                    <p>{{ element.path }} - {{ element.comment }}</p>
                                    <div class="icon-desc">
                                        <el-icon :size="18" class="drag-handle" @click.stop>
                                            <Rank />
                                        </el-icon>
                                        <el-popconfirm
                                            width="220"
                                            @confirm="delSite(index)"
                                            title="确定要删除此配置？"
                                        >
                                            <template #reference>
                                                <el-icon :size="18" @click.stop>
                                                    <Delete />
                                                </el-icon>
                                            </template>
                                        </el-popconfirm>
                                    </div>
                                </div>
                            </template>
                            <div class="form-item">
                                <el-form-item
                                    :prop="'frontends.' + index + '.path'"
                                    :rules="{
                                        required: true,
                                        message: '必填项不能为空',
                                        trigger: 'blur',
                                    }"
                                >
                                    <template #label>
                                        <div class="label-desc">
                                            <span>前缀</span>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-input v-model="element.path" placeholder="请输入" />
                                </el-form-item>
                                <el-form-item>
                                    <template #label>
                                        <div class="label-desc">
                                            <span>备注</span>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-input v-model="element.comment" placeholder="请输入" />
                                </el-form-item>
                                <el-form-item>
                                    <template #label>
                                        <div class="label-desc">
                                            <span>正则匹配</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="开启后「前缀」基于正则表达式进行路径拦截"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-switch
                                        v-model="element.regex"
                                        inline-prompt
                                        active-text="开"
                                        inactive-text="关"
                                    />
                                </el-form-item>
                                <el-form-item>
                                    <template #label>
                                        <div class="label-desc">
                                            <span>资源路径</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="静态站点文件夹在服务器上的绝对路径"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-input v-model="element.dir" placeholder="请输入" />
                                </el-form-item>
                                <el-form-item>
                                    <template #label>
                                        <div class="label-desc">
                                            <span>登录保护</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="开启后验证登录状态"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-switch
                                        v-model="element.secured"
                                        inline-prompt
                                        active-text="开"
                                        inactive-text="关"
                                    />
                                </el-form-item>
                                <el-form-item>
                                    <template #label>
                                        <div class="label-desc">
                                            <span>404重路由</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="应对前端无#路由，后端404时不报错，而是重路由到指定路径"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-input
                                        v-model="element.notFoundReroute"
                                        placeholder="请输入"
                                    />
                                </el-form-item>
                                <el-form-item
                                    :key="index + '.allowlist'"
                                    :prop="`frontends.${index}.allowlist`"
                                    :rules="allowlistRules"
                                >
                                    <template #label>
                                        <div class="label-desc">
                                            <span>白名单</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="开启登录保护后，可以通过白名单跳过登录身份检查"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-row
                                        v-for="(whiteItem, whiteIndex) in element.allowlist"
                                        style="width: 100%; margin-bottom: 10px"
                                    >
                                        <el-col :span="20">
                                            <el-input
                                                style="width: 102%"
                                                v-model="element.allowlist[whiteIndex]"
                                                placeholder="请输入"
                                            >
                                            </el-input>
                                        </el-col>
                                        <el-col :span="3" style="margin-left: 10px">
                                            <el-button
                                                @click="element.allowlist.splice(whiteIndex, 1)"
                                                :icon="Minus"
                                                circle
                                                size="small"
                                            />
                                        </el-col>
                                    </el-row>
                                    <p class="add-btn" @click="addWhite(index)">
                                        <el-icon> <Plus /> </el-icon>追加白名单
                                    </p>
                                </el-form-item>
                                <el-form-item
                                    :prop="`frontends.${index}.noCache`"
                                    :key="index + '.noCache'"
                                    :rules="noCacheRules"
                                >
                                    <template #label>
                                        <div class="label-desc">
                                            <span>不缓存</span>
                                            <el-tooltip
                                                class="box-item"
                                                effect="dark"
                                                content="匹配路径将包装no-store头，控制浏览器不缓存此资源"
                                                placement="top"
                                            >
                                                <el-icon class="icon" size="15"
                                                    ><Warning
                                                /></el-icon>
                                            </el-tooltip>
                                            <span>:</span>
                                        </div>
                                    </template>
                                    <el-row
                                        v-for="(noCacheItem, noCacheIndex) in element.noCache"
                                        style="width: 100%; margin-bottom: 10px"
                                    >
                                        <el-col :span="20">
                                            <el-input
                                                style="width: 102%"
                                                v-model="element.noCache[noCacheIndex]"
                                                placeholder="请输入"
                                            >
                                            </el-input>
                                        </el-col>
                                        <el-col :span="3" style="margin-left: 10px">
                                            <el-button
                                                @click="element.noCache.splice(noCacheIndex, 1)"
                                                :icon="Minus"
                                                circle
                                                size="small"
                                            />
                                        </el-col>
                                    </el-row>
                                    <p class="add-btn" @click="addnoCache(index)">
                                        <el-icon> <Plus /> </el-icon>不缓存
                                    </p>
                                </el-form-item>
                            </div>
                        </el-collapse-item>
                    </el-collapse>
                </template>
            </draggable>
        </template>
        <el-empty description="通过右上方按钮追加配置" v-else />
    </el-card>
</template>

<script setup lang="ts">
import { ref, inject } from 'vue'
import draggable from 'vuedraggable'
import { Plus, Minus } from '@element-plus/icons-vue'
const formRef = inject('formRef') // 注入父组件提供的 ref
const props = defineProps({
    dataForm: {
        type: Object,
        default: () => {},
    },
})
let activeName = ref<string | number>(1)
const allowlistRules = [
    //{ required: true, message: "", trigger: "change" },
    {
        validator: (_: any, value: any, callback: any) => {
            if (value && value.some((item: any) => !item.trim())) {
                callback(new Error('白名单项不能为空'))
            } else {
                callback()
            }
        },
        trigger: 'blur',
    },
]
const noCacheRules = [
    //{ required: true, message: "至少需要一项不缓存规则", trigger: "change" },
    {
        validator: (_: any, value: any, callback: any) => {
            if (value && value.some((item: any) => !item.trim())) {
                callback(new Error('不缓存规则不能为空'))
            } else {
                callback()
            }
        },
        trigger: 'blur',
    },
]
const delSite = (index: number) => {
    props.dataForm.frontends.splice(index, 1)
}

const addStaticSite = () => {
    props.dataForm.frontends.push({
        path: '',
        comment: '',
        regex: false,
        dir: '',
        secured: false,
        notFoundReroute: '',
        allowlist: [],
        noCache: [],
    })
    activeName.value = Number(props.dataForm.frontends.length)
}
// 拖拽结束回调
const onDragEnd = ({ oldIndex, newIndex }: { oldIndex: number; newIndex: number }) => {
    // ElMessage.success(`位置已变更：${oldIndex + 1} → ${newIndex + 1}`);
}
const addWhite = (index: number) => {
    props.dataForm.frontends[index].allowlist.push('')
}
const addnoCache = (index: number) => {
    props.dataForm.frontends[index].noCache.push('')
}
</script>

<style scoped lang="less">
.card-header {
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: space-between;
    font-size: 16px;
    color: rgba(0, 0, 0, 0.85);
    font-weight: 500;
    margin: -1px 0;
}
:deep(.el-card__body) {
    height: 610px;
    overflow: scroll;
}
.label-desc {
    height: 32px;
    display: flex;
    align-items: center;

    .icon {
        margin: 0 4px;
    }
}
.input-number {
    :deep(.el-input) {
        position: relative;
        &::before {
            // 也可以将此处的分钟换成其他单位
            content: '分钟';
            z-index: 2;
            position: absolute;
            top: 0;
            right: 42px;
            height: 32px;
            line-height: 32px;
            color: #999;
            font-size: 12px;
        }
    }

    :deep(.el-input__inner) {
        box-sizing: border-box;
        padding-right: 25px;
    }
}
.desc-item {
    width: 100%;
    display: flex;
    justify-content: space-between;
}
:deep(.el-input-group__prepend) {
    padding: 0 10px;
}
.add-btn {
    margin: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    height: 32px;
    padding: 0 15px;
    font-size: 14px;
    box-sizing: border-box;
    border: 1px dashed #d9d9d9;
    text-align: center;
    line-height: 32px;
    width: 84.8%;
    background-color: #fff;
    //margin-top: 10px;
    cursor: pointer;
    transition: all 0.5s;
    &:hover {
        border-color: #409eff;
        color: #409eff;
    }
}
.collapse {
    :deep(.el-collapse-item__header) {
        position: relative;
        .el-icon.el-collapse-item__arrow {
            position: absolute;
            .el-collapse-item__arrow {
                left: 20px;
            }
        }
    }

    .hander-collapse {
        display: flex;
        justify-content: space-between;
        width: 100%;
        padding-left: 20px;
        position: relative;
        p {
            margin: 0;
            flex: 1;
            text-align: left;
            overflow: hidden;
            text-overflow: ellipsis;
            white-space: nowrap;
        }
        .icon-desc {
            display: flex;
            width: 60px;
            align-items: center;
            justify-content: space-between;
            :deep(.el-icon) {
                position: relative;
            }
        }
    }
}
.form-item {
    margin: 10px auto 0;
    width: 90%;
}
:deep(.el-collapse) {
    border-bottom: none;
}

.drag-handle {
    cursor: move;
    margin-right: 10px;
    color: #909399;
    transition: color 0.3s;
}

.drag-handle:hover {
    color: #409eff;
}

/* 拖拽时的视觉反馈 */
.sortable-chosen {
    //background: #79bbff;
    opacity: 0.8;
}

.sortable-ghost {
    // background: #79bbff !important;

    border-left: 2px solid #409eff !important;
}
/* 调整折叠面板间距 */
.collapse-item {
    //margin-bottom: 8px;
    transition: all 0.3s;
}
</style>
