//对前后端数据进行一些处理

import axios from "axios";
import {ElMessage} from "element-plus";
import router from "@/router/index.js"

const request = axios.create({
    baseURL: 'http://localhost:8080',
    timeout: 30000
    //axios创建的对象包含.get,.post,.put,.delete等方法
})

//request拦截器
//可以子请求发送前对请求做一些处理
request.interceptors.request.use(config => {
    //设置统一的数据传输格式为json,编码规则为utf-8
    config.headers['Content-Type'] = 'application/json;charset=utf-8';
    return config
},error=>{
    return Promise.reject(error)
    });

//response拦截器
//在接口响应后统一处理
request.interceptors.response.use(
    response => {
        let res = response.data;
        //兼容服务器端返回的字符串类型
        if(typeof res === 'string'){
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        if (error.response.status === 404){
            ElMessage.error('未找到请求接口')
        } else if(error.response.status === 500){
            ElMessage.error('系统异常，请查看后端控制台报错')
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request;