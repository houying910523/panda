export default {
    stateText(state) {
        return ["创建完成", "已上传zip包", "正在运行", "停止", "停用"][state];
    }
}