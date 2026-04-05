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
        // 如果是 FormData 数据，则不设置 Content-Type，让浏览器自动设置
        if (!(config.data instanceof FormData)) {
            config.headers['Content-Type'] = 'application/json;charset=utf-8';
        }
        
        // 在请求头中携带用户信息，用于后端权限校验
        const user = localStorage.getItem('user');
        if (user) {
            config.headers['X-User-Info'] = user;
        }
        
        return config
    }, error => {
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
        
        // 处理权限错误
        if (res.code === '403') {
            ElMessage.error(res.msg || '权限不足');
            return Promise.reject(new Error(res.msg));
        }
        
        if (res.code === '401') {
            ElMessage.error('未登录或登录已过期');
            localStorage.removeItem('user');
            router.push('/login');
            return Promise.reject(new Error(res.msg));
        }
        
        return res;
    },
    error => {
        if (error.response?.status === 404){
            ElMessage.error('未找到请求接口')
        } else if(error.response?.status === 500){
            ElMessage.error('系统异常，请查看后端控制台报错')
        } else if(error.response?.status === 403) {
            ElMessage.error('权限不足，需要管理员权限')
        } else if(error.response?.status === 401) {
            ElMessage.error('未登录或登录已过期');
            localStorage.removeItem('user');
            router.push('/login');
        } else {
            console.error(error.message)
        }
        return Promise.reject(error)
    }
)

export default request;