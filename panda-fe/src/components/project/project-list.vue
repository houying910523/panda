<template>
    <div id="project">
        <div id="queryForm">
            <button type="button" class="btn btn-success" @click="onCreate()">创建</button>
        </div>
        <div id="projectTable">
            <table class="table table-bordered table-hover table-striped center">
                <thead>
                    <tr>
                        <td>名称</td>
                        <td>创建者</td>
                        <td>负责人</td>
                        <td>重试次数</td>
                        <td>状态</td>
                        <td>描述</td>
                        <td>更新时间</td>
                        <td>操作</td>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="p in projectList">
                        <td>{{p.name}}</td>
                        <td>{{p.owner}}</td>
                        <td>{{p.managers}}</td>
                        <td>{{p.retryCount}}</td>
                        <td>{{stateText(p.state)}}</td>
                        <td>{{p.description}}</td>
                        <td>{{p.updateTime}}</td>
                        <td class="operation">
                            <button class="btn btn-default" @click="onDetail(p)">Detail</button>
                            <button class="btn btn-info" @click="onUpload(p)">Upload</button>
                            <button v-show="p.state != 2" class="btn btn-success" @click="onDeploy(p)" :disabled="p.state == 2">Deploy</button>
                            <button v-show="p.state == 2" class="btn btn-success" @click="onWatch(p)" :disabled="p.state != 2">Watch</button>
                            <button class="btn btn-default" @click="onHistory(p)">History</button>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
        <div id="dialog-container">
            <project-create-dialog ref="create" @submit="doAjaxCreate"></project-create-dialog>
            <project-upload-dialog ref="upload" @completed="onUploadCompleted" :url="uploadUrl"></project-upload-dialog>
            <project-deploy-dialog ref="deploy" @completed="onDeployCompleted"></project-deploy-dialog>
        </div>
    </div>
</template>

<script>
    import utils from '../../common/utils.js'
    import ProjectCreateDialog from './dialog/project-create-dialog.vue'
    import ProjectUploadDialog from './dialog/project-upload-dialog.vue'
    import ProjectDeployDialog from './dialog/project-deploy-dialog.vue'
    import ProjectUtils from './project-utils.js'
    export default {
        name: "project",
        data() {
            return {
                page: 1,
                size: 20,
                projectList: [],
                uploadUrl: "/project/upload"
            }
        },
        components: {
            ProjectCreateDialog,
            ProjectUploadDialog,
            ProjectDeployDialog
        },
        methods: {
            stateText(state) {
                return ProjectUtils.stateText(state);
            },
            loadProjects() {
                const vm = this;
                const pageOpt = {page: vm.page, size: vm.size};
                utils.fetch("/project/list", pageOpt,
                function(data) {
                    vm.projectList = data.data;
                },
                function(data) {
                    console.log(data.message);
                    utils.alert(data.message);
                })
            },
            onCreate() {
                this.$refs.create.show();
            },
            doAjaxCreate(project) {
                const vm = this;
                utils.postJson("/project/create", project,
                        function(data) {
                            vm.$refs.create.dismiss();
                            utils.success("创建成功");
                            vm.loadProjects();
                        },
                        function(data) {
                            utils.alert(data.message);
                            console.log(data.message);
                        })
            },

            onUpload(project) {
                this.$refs.upload.show(project);
            },
            onUploadCompleted(data) {
                this.$refs.upload.dismiss();
                if (data.status == 0) {
                    utils.success("上传成功");
                    this.loadProjects();
                } else if (data.status == 302){
                    this.$router.push({path: '/login'});
                } else {
                    utils.alert(data.message);
                }
            },

            onDeploy(project) {
                this.$refs.deploy.show(project);
            },
            onDeployCompleted(project) {
                const vm = this;
                utils.success("发布成功");
                this.loadProjects();
                window.setTimeout(function() {
                    vm.$router.push({
                        path: '/project/manage',
                        query: {
                            path: 'running',
                            project: project
                        }
                    });
                }, 2000);
            },

            jumpToManage(path, project) {
                this.$router.push({
                    path: '/project/manage',
                    query: {
                        path: path,
                        project: project
                    }
                })
            },
            onDetail(project) {
                this.jumpToManage('detail', project);
            },
            onWatch(project) {
                this.jumpToManage('running', project);
            },
            onHistory(project) {
                this.jumpToManage('history', project);
            }
        },
        mounted() {
            this.loadProjects();
        }
    }
</script>
<style lang="css">
    #project {
        border: 1px solid #DDDDDD;
        border-radius: 5px;
        padding: 5px;
    }
    #queryForm {
        text-align: left;
    }
    #projectTable {
        margin-top: 10px;
    }

    .operation button {
        padding: 2px;
        font-size: 10px;
        width: 50px;
        height: 25px;
    }
</style>
