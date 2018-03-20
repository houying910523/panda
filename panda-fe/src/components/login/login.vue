<template>
    <div>
        <div id="logo" class="text-center">
            <img src="../../img/panda.jpg" />
        </div>
        <!--<div style="height: 8%;"></div>-->
        <div id="login-form" class="text-center">
            <div class="login-box">
            <div>
                <h4>LDAP Sign in</h4>
            </div>
            <div>
                <form>
                    <input class="form-control top" v-model="username" name="username" type="text" placeholder="LDAP Login">
                    <input class="form-control bottom" v-model="password" name="password" type="password" placeholder="Password">

                    <div v-if="showMessage">
                        <span class="error">{{message}}</span>
                    </div>
                    <div class="checkbox text-left">
                        <label for="remember_me">
                            <input type="checkbox" name="remember_me" id="remember_me" value="1">
                            <span>Remember me</span>
                        </label>
                    </div>
                    <button name="sign" type="button" class="btn-success btn col-sm-12"
                            v-on:click="onLogin(username, password)">Sign in</button>
                </form>
            </div>
            </div>
        </div>
    </div>
</template>
<script>
    import utils from '../../common/utils.js'
    import router from '../../router.js'
    export default {
        name: "login",
        data () {
            return {
                username: '',
                password: '',
                message: '',
                showMessage: false
            }
        },
        methods: {
            onLogin(username, password) {
                var login = this;
                if (username == '') {
                    this.message = "请输入用户名";
                    this.showMessage = true;
                    return;
                }
                this.showMessage = false;
                if (password == '') {
                    this.message = "请输入密码";
                    this.showMessage = true;
                    return;
                }
                this.showMessage = false;
                utils.post('/api/login', {username: username, password: password},
                        function(data) {
                            router.push({path: '/project/list'});
                        },
                        function(data) {
                            login.showMessage = true;
                            login.message = data.message;
                        })
            }
        }
    }
</script>

<style lang="less">
    .login-box {
        background: #fafafa;
        border-radius: 10px;
        box-shadow: 0 0 2px #CCC;
        padding: 15px;
        margin: 0 auto;
        height: 210px;
        .error {
            color: #ff0011;
        }
    }

    #logo {
        margin: 30px;
        img {
            width: 250px;
            height: 260px;
        }
    }
    #login-form {
        margin: 30px auto 0;
        width: 350px;
        position:relative;
        min-height:1px;
        padding-right:15px;
        padding-left:15px
    }
</style>