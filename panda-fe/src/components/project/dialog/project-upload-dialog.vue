<template>
    <p-dialog :modal-option="modalOption">
        <form role="form">
            <div class="form-group upload-control">
                <label for="file" class="col-sm-3">Project Archive</label>
                <input type="file" id="file" class="col-sm-8 form-control">
            </div>
            <div v-if="uploading" class="form-group">
                <span>正在上传...{{uploadProgress + '%'}}</span>
                <c-progress :progress="uploadProgress"></c-progress>
            </div>
            <span v-if="errorMsg != ''" class="alert-msg">{{errorMsg}}</span>
        </form>
    </p-dialog>
</template>
<script>
    import cProgress from '../../common/progress.vue'
    import utils from '../../../common/utils.js'
    import pDialog from '../../common/dialog.vue'
    export default {
        props: ['url'],
        components: {
            pDialog,
            cProgress
        },
        data() {
            const vm = this;
            return {
                project: {},
                uploading: false,
                uploadProgress: 0,
                uploadAjaxPromise: undefined,
                errorMsg: '',
                modalOption: {
                    title: "上传",
                    show: false,
                    buttons: [
                        {
                            text: "Cancel",
                            class: "btn-default",
                            onClick() {
                                vm.uploading = false;
                                vm.uploadProgress = 0;
                                vm.modalOption.show = false;
                                if (vm.uploadAjaxPromise) {
                                    vm.uploadAjaxPromise.abort();
                                }
                            }
                        },
                        {
                            text: "Upload",
                            class: "btn-success",
                            onClick() {
                                if (vm.check()) {
                                    vm.doAjaxUpload();
                                }
                            }
                        }
                    ]
                }
            }
        },
        methods: {
            show(project) {
                this.errorMsg = '';
                this.uploadProgress = 0;
                this.uploading = false;
                this.uploadAjaxPromise = undefined;
                this.project = project;
                this.modalOption.show = true;
            },
            dismiss() {
                this.modalOption.show = false;
            },
            check() {
                if (this.uploading) {
                    this.errorMsg = "正在上传，请不要重复点击";
                    console.log("uploading...");
                    return false;
                }
                const file = $('#file')[0].files[0];
                if (file.name == '') {
                    this.errorMsg = "文件名为空";
                    return false;
                }
                if (!file.name.endsWith(".zip")) {
                    this.errorMsg = "仅支持后缀名为zip的文件";
                    return false;
                }
                return true;
            },
            doAjaxUpload() {
                const vm = this;
                vm.uploading = true;
                vm.errorMsg = '';
                vm.uploadAjaxPromise = undefined;
                const formData = new FormData();
                const file = $('#file')[0].files[0];
                formData.append("file", file);
                vm.uploadAjaxPromise = $.ajax({
                    url: vm.url + "/" + vm.project.id,
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
                        vm.$emit("completed", data);
                        vm.uploading = false;
                    },
                    error: function(data) {
                        vm.errorMsg = "上传失败：" + data.statusText;
                        vm.uploading = false;
                    }
                });
            }
        }
    }
</script>
<style lang="less">
    .upload-control {
        height: 60px;
        input {
            padding: 3px 10px;
        }
    }
    #uploadProgress.bar {
        height: 20px;
        background-color: #2BABD2;
    }
</style>