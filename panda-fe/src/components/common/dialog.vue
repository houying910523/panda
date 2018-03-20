<template>
    <div v-show="visible"  class="modal">
        <div class="modal-dialog-v">
            <div class="dialog-header">
                <p>{{title}}</p>
                <button type="button" class="close" @click="close()">X</button>
            </div>
            <div class="dialog-content">
                <slot></slot>
            </div>
            <div class="dialog-footer">
                <button v-for="btn in buttons" class="btn" :class="btnClass(btn)" @click="btnOnClick(btn)">{{btnText(btn)}}</button>
            </div>
        </div>
        <div class="modal-background"></div>
    </div>
</template>
<script>
    export default {
        props: [ 'modalOption' ],
        methods: {
            btnClass(btn) {
                return btn.class ? btn.class: "btn-default";
            },
            btnOnClick(btn) {
                if (btn.onClick && 'function' == typeof btn.onClick) {
                    btn.onClick();
                }
            },
            btnText(btn) {
                if (typeof btn.text == 'string') {
                    return btn.text;
                } else if (typeof btn.text == 'function') {
                    return btn.text();
                }
            },
            close() {
                this.modalOption.show = false;
            }
        },
        computed: {
            visible() {
                return this.modalOption.show;
            },
            title() {
                return this.modalOption.title || "提示";
            },
            buttons() {
                return this.modalOption.buttons.reverse();
            }
        }
    }
</script>
<style lang="css">
.modal {
    display: block;
    z-index: 1050;
}
.modal-dialog-v {
    width: 600px;
    position: relative;
    top: 50px;
    margin: 30px auto;
    background-color: white;
    border-radius: 5px;
    z-index: 1051;
}
.dialog-header {
    height: 40px;
    background-color: #5cb85c;
    border-top-left-radius: 5px;
    border-top-right-radius: 5px;
    padding: 10px;
}
.dialog-header p {
    float: left;
}
.dialog-content {
    margin-top: 20px;
    padding: 10px;
}
.dialog-footer {
    height: 60px;
    padding: 10px;
    border-bottom-left-radius: 5px;
    border-bottom-right-radius: 5px;
}
.dialog-footer button {
    margin-right: 10px;
    float: right;
}
.modal-background {
    position: fixed;
    top: 0;
    right: 0;
    bottom: 0;
    left: 0;
    z-index: 1050;
    background: rgba(0, 0, 0, 0.5);
}
</style>
