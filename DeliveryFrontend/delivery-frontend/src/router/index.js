import Home from '@/components/Home'
import Login from '@/components/Login'
import Register from "@/components/Register";
import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        component: Home,
        name: 'home'
    },
    {
        path: '/login',
        component: Login,
        name: 'login'
    },
    {
        path: '/register',
        component: Register,
        name: 'register'
    },
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router;
