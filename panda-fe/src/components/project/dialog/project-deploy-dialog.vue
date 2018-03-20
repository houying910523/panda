<template>
    <p-dialog :modal-option="modalOption">
        <span v-if="!deploying">确认发布？</span>
        <div v-if="deploying">
            <span>正在发布</span>
            <c-progress  :progress="deployProgress"></c-progress>
        </div>
    </p-dialog>
</template>
<script>
    import pDialog from '../../common/dialog.vue'
    import cProgress from '../../common/progress.vue'
    import utils from '../../../common/utils.js'

    export default {
        name: "project-deploy-dialog",
        components: {
            pDialog,
            cProgress
        },
        data() {
            const vm = this;
            return {
                background: false,
                deploying: false,
                deployProgress: 0,
                project: {},
                modalOption: {
                    title: "Deploy",
                    show: false,
                    buttons: [
                        {
                            text: function() {
                                return vm.deploying ? "Background": "Cancel";

                            },
                            class: "btn-default",
                            onClick() {
                                vm.modalOption.show = false;
                                vm.background = true;
                            }
                        },
                        {
                            text: "Deploy",
                            class: "btn-success",
                            onClick() {
                                vm.onDeploy();
                            }
                        }
                    ]
                }
            }
        },
        methods: {
            show(project) {
                this.background = false;
                this.deploying = false;
                this.deployProgress = 0;
                this.project = project;
                this.modalOption.show = true;
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
                    if (!vm.background && state < 2) {
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
                        vm.modalOption.show = false;
                        if (vm.background) {
                            return;
                        }
                        if (state == 2) {
                            vm.$emit("completed", vm.project);
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
        }
    }
</script>