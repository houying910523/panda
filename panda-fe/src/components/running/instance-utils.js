export default {
    stateText(state) {
        return ["已提交", "已接受", "正在运行", "已完成", "失败", "中止"][state];
    }
}