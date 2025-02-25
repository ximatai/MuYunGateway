// import http from "@/utils/http";
import axios from 'axios'

const http = axios.create({
    withCredentials: true,
})
//获取网关设置
export const gatewaySetting = async () => {
    const res = await http({
        method: 'GET',
        url: './api/config',
    })
    return res
}

//更新网关设置
export const updateGatewaySetting = async (data: any) => {
    const res = await http({
        method: 'POST',
        url: './api/config',
        data,
    })
    return res
}
