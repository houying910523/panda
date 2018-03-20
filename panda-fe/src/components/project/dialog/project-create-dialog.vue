<template>
    <p-dialog :modal-option.sync="modalOption">
        <span v-if="errorMsg != ''" class="alert-msg">{{errorMsg}}</span>
        <form role="form">
            <div class="form-group">
                <label for="name" class="col-sm-3 text-right">工程名称</label>
                <input type="text" id="name" class="col-sm-8" v-model="project.name" >
            </div>
            <div class="form-group">
                <label for="managers" class="col-sm-3 text-right">负责人</label>
                <input type="text" id="managers" class="col-sm-8" v-model="project.managers"
                    placeholder="多个值使用英文逗号分隔">
            </div>
            <div class="form-group">
                <label for="retryCount" class="col-sm-3 text-right">重试次数</label>
                <input type="text" id="retryCount" class="col-sm-8" v-model.number="project.retryCount"
                    placeholder="仅限输入数字">
            </div>
            <div class="form-group description">
                <label for="desc" class="col-sm-3 text-right">描述</label>
                <textarea type="text" id="desc" class="col-sm-8 description" v-model="project.description"></textarea>
            </div>
        </form>
    </p-dialog>
</template>
<script>
    import utils from '../../../common/utils.js'
    import pDialog from '../../common/dialog.vue'
    export default {
        name: "project-create-dialog",
        components: {
            pDialog
        },
        data() {
            const vm = this;
            return {
                errorMsg: "",
                project: {
                    retryCount: 0,
                    managers: ""
                },
                modalOption: {
                    title: "创建工程",
                    show: false,
                    buttons: [
                        {
                            text: "Cancel",
                            class: "btn-default",
                            onClick() {
                                vm.modalOption.show = false;
                            }
                        },
                        {
                            text: "Create",
                            class: "btn-success",
                            onClick() {
                                if (vm.checkProject()) {
                                    vm.$emit("submit", vm.project);
                                }
                            }
                        }
                    ]
                }
            }
        },
        methods: {
            show() {
                this.modalOption.show = true;
            },
            dismiss() {
                this.modalOption.show = false;
            },
            checkProject() {
                const project = this.project;
                const checkList = [
                    {
                        test(p) { return p.name },
                        msg: "工程名称不能为空"
                    },
                    {
                        test(p) {return utils.fetchSync("/project/unique/" + p.name)},
                        msg: "该名称已存在"
                    },
                    {
                        test(p) {return p.managers},
                        msg: "负责人不能为空"
                    },
                    {
                        test(p) {if (p.retryCount == "") {p.retryCount = 0} return true}
                    },
                    {
                        test(p) {return Number.isInteger(p.retryCount)},
                        msg: "重试次数必须是数字"
                    },
                    {
                        test(p) {return Number(p.retryCount) >= 0},
                        msg: "重试次数必须非负"
                    },
                    {
                        test(p) {return p.description},
                        msg: "描述内容不能为空"
                    }
                ];
                for (var i in checkList) {
                    const item = checkList[i];
                    if (!item.test(project)) {
                        this.errorMsg = item.msg;
                        return false;
                    }
                }
                this.errorMsg = "";
                return true;
            }
        }
    }
</script>
<style lang="css">
    .alert-msg {
        color: red;
    }
    .description {
        height: 90px;
    }
</style>