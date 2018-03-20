import Vue from 'vue'
import VueRouter from 'vue-router'

import utils from './common/utils.js'
import login from './components/login/login.vue'
import main from './components/main/main.vue'

import ProjectList from './components/project/project-list.vue'
import ProjectManage from './components/project/project-manage.vue'
import Detail from './components/project/project-detail.vue'
import Running from './components/running/running.vue'
import History from './components/history/history.vue'

Vue.use(VueRouter);

const router = new VueRouter({

    routes: [
        {
            path: '/',
            redirect: '/project/list'
        },
        {
            path: '/project',
            component: main,
            children: [
                {
                    path: '/',
                    component: ProjectList
                },
                {
                    path: 'list',
                    component: ProjectList
                },
                {
                    path: 'manage',
                    component: ProjectManage,
                    children: [
                        {
                            path: 'detail',
                            component: Detail
                        },
                        {
                            path: 'running',
                            component: Running
                        },
                        {
                            path: 'history',
                            component: History
                        }
                    ]
                }
            ]
        },
        {
            path: '/login',
            component: login
        }
    ]
});

router.beforeEach((to, from, next) => {
    if (to.path === '/login') {
        next();
    }
    if (!utils.isLogin()) {
        next({path: '/login'});
    } else {
        next()
    }
});

export default router
