/**
* Created by houying on 18-1-15.
*/
<template xmlns:v-on="http://www.w3.org/1999/xhtml">
    <div id="view" class="text-center">
        <header class="main-header">
            <span class="logo" >Panda 流式管理平台</span>
            <div class="logout">
                <a href="#" v-on:click="onLogout()">
                    <img width="22" height="22" src="../../img/logout.png" style="filter: invert(100%)">
                    <span>{{curUser}}</span>
                </a>
            </div>
        </header>
        <div class="main-content">
            <div class="content">
                <router-view></router-view>
            </div>
            <notify></notify>
        </div>
    </div>
</template>

<script>
    import utils from '../../common/utils.js'
    import notify from '../common/notify.vue'
    export default {
        name: "xxx",
        components: {
            notify
        },
        data () {
            return {
                curUser: utils.user()
            }
        },
        methods: {
            onLogout() {
                const vm = this;
                utils.post("/api/logout",
                        function(data) {
                            window.localStorage.removeItem("user");
                            vm.$router.push({path: 'login'});
                        }, function(data) {
                            console.log(data.message);
                        }
                )
            }
        }
    }
</script>

<style lang="css">
    .main-header {
        background-color: #151515;
        color: #eeeeee;
        font-size: 17px;
        position: relative;
        width: 100%;
        height: 50px;
        z-index: 1030;
    }
    .main-header .logo {
        height: 100%;
        width: 200px;
        display: block;
        float: left;
        line-height: 40px;
        padding: 5px;
        background-color: #151515;
        color: #EEEEEE;
    }

    .main-header .logout {
        height: 40px;
        float: right;
    }
    .main-header .logout span {
        line-height: 45px;
    }
    .main-header .logout a {
        height: 50px;
        width: 130px;
        color: #EEEEEE;
        display: block;
        text-decoration: none;
    }

    .main-header .logout a:hover {
        background-color: #444444;
    }

    .content {
        position: fixed;
        top: 50px;
        bottom: 0;
        width: 100%;
        padding: 20px;
        right: 0;
        overflow-x: scroll;
    }

</style>
