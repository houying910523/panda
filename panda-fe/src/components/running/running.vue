<template>
    <div id="instance">
        <div class="instance-content" v-if="hasInstance">
            <div id="instance-detail">
                <div class="form-group">
                    <label class="col-sm-2 text-right">发布人：</label>
                    <p class="col-sm-10 text-left">{{instance.submitUser}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">状态：</label>
                    <p class="col-sm-10 text-left">{{stateText}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">集群地址：</label>
                    <p class="col-sm-10 text-left">{{instance.clusterMaster}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">应用id：</label>
                    <p class="col-sm-10 text-left">{{instance.applicationId}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">tracking_url：</label>
                    <p class="col-sm-10 text-left"><a :href="instance.trackingUrl" target="_blank">{{instance.trackingUrl}}</a></p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">启动时间：</label>
                    <p class="col-sm-10 text-left">{{instance.startTime}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">尝试次数：</label>
                    <p class="col-sm-10 text-left">{{instance.attempt}}</p>
                </div>
                <div class="form-group">
                    <label class="col-sm-2 text-right">资源使用：</label>
                    <p class="col-sm-10 text-left">{{instance.executorCores}} cores and {{instance.executorMemory}} per executor * {{instance.executorNum}}</p>
                </div>
            </div>
            <div id="instance-operation">
                <button class="btn btn-danger" @click="onKill()">kill</button>
                <button class="btn btn-success" @click="onRestart()">restart</button>
            </div>
        </div>
        <div class="deploy" v-if="!hasInstance">
            <div class="form-group upload-group">
                <label for="file" class="col-sm-2">Upload:</label>
                <input type="file" id="file" class="col-sm-6">
                <button v-if="!uploading" class="btn btn-info col-sm-1" @click="onUpload()">{{project.state == 0 ? "上传": "重新上传"}}</button>
                <div v-if="uploading" class="col-sm-3">
                    <c-progress class="upload-progress" :progress="uploadProgress"></c-progress>
                    <span>{{uploadProgress + '%'}}</span>
                </div>
                <span v-if="errorMsg != ''" class="col-sm-1">{{errorMsg}}</span>
            </div>
            <button v-if="!deploying" class="btn btn-success" @click="onDeploy()">Deploy</button>
            <div v-if="deploying" class="form-group">
                <label class="col-sm-2">发布进度</label>
                <c-progress :progress="deployProgress"
                    class="col-sm-6 deploy-progress"></c-progress>
            </div>
        </div>
        <div>
            <confirm-dialog ref="confirm" :message="'确认Kill？'" @confirm="doAjaxKill()"></confirm-dialog>
        </div>
    </div>
</template>
<script>
    import utils from '../../common/utils.js'
    import InstanceUtils from './instance-utils.js'
    import ConfirmDialog from './confirm-dialog.vue'
    import cProgress from '../common/progress.vue'
    export default {
        name: 'instance',
        components: {
            ConfirmDialog,
            cProgress
        },
        props: ['project'],
        data() {
            return {
                uploading: false,
                uploadProgress: 10,
                uploadAjaxPromise: undefined,
                errorMsg: '',

                deploying: false,
                deployProgress: 10,

                hasInstance: false,
                instance: {}
            }
        },
        methods: {
            loadInstance() {
                const vm = this;
                utils.fetch('/instance/detail/' + this.project.id,
                        function(data) {
                            vm.instance = data.data;
                        },
                        function(data) {
                            console.log(data.message);
                            utils.alert("加载运行实例详情信息失败");
                        }
                );
            },

            onKill() {
                this.$refs.confirm.show();
            },
            doAjaxKill() {
                const vm = this;
                utils.post('/instance/kill/' + this.instance.id,
                        function(data) {
                            vm.$refs.confirm.dismiss();
                            utils.success("kill success");
                            vm.hasInstance = false;
                            vm.$emit("reload");
                        },
                        function(data) {
                            utils.alert(data.message);
                            console.log(data.message);
                        }
                )
            },

            onRestart() {
                utils.alert("本功能暂不支持");
            },

            onUpload() {
                const vm = this;
                vm.errorMsg = '';
                vm.uploadAjaxPromise = undefined;
                const file = $('#file')[0].files[0];
                if (!file) {
                    vm.errorMsg = "请选择文件";
                    return;
                }
                vm.uploading = true;
                const formData = new FormData();
                formData.append("file", file);
                vm.uploadAjaxPromise = $.ajax({
                    url: "/project/upload/" + vm.project.id,
                    type: 'POST',
                    data: formData,
                    contentType: false,
                    processData: false,
                    xhr: function() {
                        const x = $.ajaxSettings.xhr();
                        if (x.upload) {
                            x.upload.addEventListener("progress", function(evt) {
                                vm.uploadProgress = evt.loaded / evt.total * 100;
                            }, false);
                        }
                        return x;
                    },
                    success: function(data) {
                        vm.uploading = false;
                        vm.errorMsg = "上传完成";
                    },
                    error: function(data) {
                        utils.alert("上传失败：" + data.statusText);
                        vm.uploading = false;
                    }
                });
            },

            onDeploy() {
                const vm = this;
                vm.background = false;
                vm.deployProgress = 0;
                vm.deploying = true;
                utils.post('/instance/deploy/' + vm.project.id,
                        function(data) {
                            const tid = data.data;
                            vm.deployProgress = 20;
                            vm.checkState(tid);
                        },
                        function(data) {
                            utils.alert("发布失败: " + data.message);
                            vm.deploying = false;
                            vm.modalOption.show = false;
                        }
                );
            },

            checkState(tid) {
                const vm = this;
                var func = function() {
                    const threshold = 60;
                    const state = utils.fetchSync('/instance/state/' + tid);
                    if (state < 2) {
                        window.setTimeout(func, 2000);
                        if (state == 0 && vm.deployProgress < threshold) {
                            vm.deployProgress = vm.deployProgress + 5;
                        } else {
                            if (vm.deployProgress < threshold) {
                                vm.deployProgress = threshold;
                            }
                            vm.deployProgress = vm.deployProgress + 5;
                        }
                    } else {
                        vm.deploying = false;
                        if (state == 2) {
                            utils.success("发布成功");
                            vm.loadInstance();
                            vm.$emit("reload");
                            vm.hasInstance = true;
                            return;
                        }
                        if (state > 2) {
                            utils.alert("发布失败");
                            return;
                        }
                    }
                };
                window.setTimeout(func, 2000);
            }
        },

        computed: {
            stateText() {
                return InstanceUtils.stateText(this.instance.state);
            }
        },

        mounted() {
            if (this.project && Object.keys(this.project).length != 0) {
                if (this.project.state == 2) {
                    this.hasInstance = true;
                    this.loadInstance();
                } else {
                    this.hasInstance = false;
                }
            }
        }
    }

</script>
<style lang="css">
    #instance-detail .form-group {
        height: 30px;
    }
    #instance-detail .form-group p {
        height: 30px;
        font-size: 17px;
        line-height: 30px;
    }
    #instance-detail .form-group label {
        height: 30px;
        font-size: 17px;
        line-height: 30px;
    }

    .deploy .form-group {
        height: 30px;
    }
    .deploy .upload-group label {
        height: 30px;
        font-size: 16px;
        line-height: 30px;
    }
    .deploy .upload-group input {
        padding: 3px 10px;
        border: 1px solid #aaaaaa;
        border-radius: 5px;
    }
    .deploy .upload-group button {
        margin-left: 20px;
    }
    .deploy .upload-group span {
        height: 30px;
        line-height: 30px;
    }

    #instance-operation {
        margin: 10px auto;
        padding: 2px 100px;
        text-align: left;
    }

    #instance-operation button {
        width: 70px;
        margin-left: 20px;
    }

    .upload-progress {
        display: inline;
        margin: 2px auto;
        width: 80%;
        height: 30px;
        float: left;
    }

    .deploy-progress {
        height: 30px;
        padding-left: 0px;
        padding-right: 0px;
    }

    #instance {
        border: 1px solid #DDDDDD;
        border-radius: 5px;
        padding: 5px;
    }
</style>