<template>
    <div id="history" >
        <div id="historyTable">
            <table class="table table-bordered table-hover table-striped center">
                <thead>
                    <tr>
                        <td>提交人</td>
                        <td>集群地址</td>
                        <td>应用运行Id</td>
                        <td>开始时间</td>
                        <td>结束时间</td>
                        <td>状态</td>
                        <td>操作</td>
                    </tr>
                </thead>
                <tbody>
                    <tr v-for="i in historyList">
                        <td>{{i.submitUser}}</td>
                        <td>{{i.clusterMaster}}</td>
                        <td>{{i.applicationId}}</td>
                        <td>{{i.startTime}}</td>
                        <td>{{i.endTime}}</td>
                        <td>{{stateText(i.state)}}</td>
                        <td class="operation">
                            <a :href="i.trackingUrl" target="_blank"><button class="btn btn-default" :disabled="i.applicationId == ''" >Spark UI</button></a>
                            <a :href=" '/history/log/' + i.id" target="_blank"><button class="btn btn-default">提交日志</button></a>
                        </td>
                    </tr>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script>
    import utils from '../../common/utils.js'
    import InstanceUtils from '../running/instance-utils.js'
    export default {
        name: "history",
        props: ['project'],
        data() {
            return {
                page: 1,
                size: 20,
                historyList: []
            }
        },
        methods: {
            loadHistoryList(projectId) {
                const vm = this;
                const pageOpt = {page: vm.page, size: vm.size};
                utils.fetch("/history/list/" + projectId, pageOpt,
                    function(data) {
                        vm.historyList = data.data;
                    },
                    function(data) {
                        console.log(data.message);
                        utils.alert(data.message);
                    }
                );
            },
            stateText(state) {
                return InstanceUtils.stateText(state);
            }
        },
        mounted() {
            if (this.project && Object.keys(this.project).length != 0) {
                this.loadHistoryList(this.project.id);
            }
        }
    }
</script>