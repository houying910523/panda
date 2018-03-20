<template>
    <div id="project-manage">
        <ul class="breadcrumb text-left">
            <li>
                <router-link to="/project/list">
                    <span class="divider">Back</span>
                </router-link>
            </li>
            <li class="active">{{project.name}}</li>
        </ul>
        <div>
            <ul class="nav nav-tabs">
                <router-link tag="li" active-class="active" to="/project/manage/detail"><a>Detail</a></router-link>
                <router-link tag="li" active-class="active" to="/project/manage/running"><a>Running</a></router-link>
                <router-link tag="li" active-class="active" to="/project/manage/history"><a>History</a></router-link>
            </ul>
        </div>
        <div>
            <router-view :project.sync="project" @reload="onReload()"></router-view>
        </div>
    </div>
</template>
<script>
    import utils from '../../common/utils.js'
    export default {
        name: 'manage',
        data() {
            return {
                project: {}
            }
        },
        methods: {
            onReload() {
                const vm = this;
                utils.fetch("/project/detail/" + this.project.id,
                        function(data) {
                            vm.project = data.data;
                        },
                        function(data) {
                            console.log(data.message);
                            utils.alert(data.message);
                        }
                )
            }
        },
        mounted() {
            const project = this.$route.query.project;
            const path = this.$route.query.path;
            if (project != undefined && path != undefined) {
                this.project = project;
                this.$router.replace("/project/manage/" + path);
            } else {
                this.$router.replace("/project/list");
            }
        }
    }
</script>