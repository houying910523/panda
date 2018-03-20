<template>
    <p-dialog :modal-option.sync="modalOption">
        {{message}}
    </p-dialog>
</template>
<script>
    import pDialog from '../common/dialog.vue'
    export default {
        name: 'confirm-dialog',
        props: ['message'],
        components: {
            pDialog
        },
        data() {
            const vm = this;
            return {
                modalOption: {
                    title: "确认",
                    show: false,
                    buttons: [
                        {
                            text: "Cancel",
                            class: 'btn-default',
                            onClick() {
                                vm.modalOption.show = false;
                            }
                        },
                        {
                            text: "Confirm",
                            class: "btn-success",
                            onClick() {
                                vm.$emit("confirm");
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
            }
        }
    }
</script>