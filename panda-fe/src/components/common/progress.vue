<template>
    <div class="progress progress-striped active">
        <div class="bar" :style="'width:' + progress + '%'"></div>
    </div>
</template>
<script>
    export default {
        name: "c-progress",
        props: ['progress']
    }
</script>
<style lang="css">
    .bar {
        height: 100%;
        background-color: #2BABD2;
    }
</style>