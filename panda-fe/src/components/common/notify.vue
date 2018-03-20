<template>
    <div id="alertMsgBox" class="alert alert-dismissible notify" aria-hidden="true" >
        <button type="button" class="close" data-dismiss="alert">x</button>
            <span id="alertMsg"></span>
    </div>
</template>
<script>
    export default {
        name: "alert-msg-box"
    }
</script>
<style>
    .notify {
        display: none;
        position: absolute;
        width: 200px;
        height: 50px;
        top: 60px;
        right: 10px;
        z-index: 1060;
    }
</style>