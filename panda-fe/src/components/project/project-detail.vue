<template>
    <div id="detail">
        <div class="detail-content">
            <div class="form-group">
                <label class="col-sm-2 text-right">工程名称：</label>
                <p class="col-sm-5 text-left">{{project.name}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">部门：</label>
                <p class="col-sm-5 text-left">{{project.dep}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">创建者：</label>
                <p class="col-sm-5 text-left">{{project.owner}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">负责人：</label>
                <input v-if="modify" type="text" class="col-sm-5" v-model="modifiedProject.managers"
                       placeholder="多个值使用英文逗号分隔">
                <p v-if="!modify" class="col-sm-5 text-left">{{project.managers}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">重试次数：</label>
                <input v-if="modify" type="text" class="col-sm-5" v-model.number="modifiedProject.retryCount">
                <p v-if="!modify" class="col-sm-5 text-left">{{project.retryCount}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">状态：</label>
                <p class="col-sm-5 text-left">{{stateText}}</p>
            </div>
            <div class="form-group">
                <label class="col-sm-2 text-right">描述：</label>
                <input v-if="modify" type="text" class="col-sm-5" v-model="modifiedProject.description">
                <p v-if="!modify" class="col-sm-5 text-left">{{project.description}}</p>
            </div>
        </div>
        <div class="detail-operation">
            <button v-if="!modify" class="btn btn-success" @click="switchModify()">更改</button>
            <button v-if="modify" class="btn btn-default" @click="switchModify()">取消</button>
            <button v-if="modify" class="btn btn-success" @click="onUpdate()">提交</button>
        </div>
    </div>
</template>
<script>
    import ProjectUtils from './project-utils.js'
    import utils from '../../common/utils.js'
    export default {
        name: 'detail',
        props: ['project'],
        data() {
            return {
                modify: false,
                modifiedProject: {}
            }
        },
        methods: {
            switchModify() {
                this.modify = !this.modify;
                this.modifiedProject = Object.assign({}, this.project);
            },
            checkProject(project) {
                const checkList = [
                    {
                        test(p) {return p.managers},
                        msg: "负责人不能为空"
                    },
                    {
                        test(p) {
                            if (p.retryCount == "") {
                                p.retryCount = 0
                            }
                            return true
                        }
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
                        test(p) {
                            return p.description != ""
                        },
                        msg: "描述内容不能为空"
                    }
                ];
                var errorMsg = undefined;
                for (var i in checkList) {
                    const item = checkList[i];
                    if (!item.test(project)) {
                        errorMsg = item.msg;
                        return errorMsg;
                    }
                }
                return errorMsg;
            },
            onUpdate() {
                console.log(this.modifiedProject);
                const errorMsg = this.checkProject(this.modifiedProject);
                if (errorMsg) {
                    utils.alert(errorMsg);
                    return;
                }
                const vm = this;
                utils.postJson("/project/update", vm.modifiedProject,
                        function(data) {
                            utils.success("更新成功");
                            vm.project = vm.modifiedProject;
                            vm.modify = false;
                        },
                        function(data) {
                            utils.alert(data.message);
                            console.log(data.message);
                        }
                )
            }
        },
        computed: {
            stateText() {
                return ProjectUtils.stateText(this.project.state);
            }
        }
    }
</script>
<style lang="less">
    #detail {
        border: 1px solid #DDDDDD;
        border-radius: 5px;
        padding: 5px;
    }

    .detail-content .form-group {
        height: 30px;
        label {
            height: 30px;
            font-size: 17px;
            line-height: 30px;
        }
        p {
            height: 30px;
            font-size: 17px;
            line-height: 30px;
        }
        input {
            height: 30px;
            font-size: 17px;
            line-height: 30px;
        }
    }

    .detail-operation {
        margin: 10px auto;
        padding: 2px 100px;
        text-align: left;
        button {
            width: 70px;
            margin-left: 20px;
        }
    }
</style>