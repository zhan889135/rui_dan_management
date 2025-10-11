<template>
  <div class="app-container">
    <!--部门等级(总部门-1；供应商-2；员工-3)-->
    <index-level1 v-if="deptLevel === 1" />
    <index-level2 v-else-if="deptLevel === 2" />
    <index-level3 v-else />
  </div>
</template>

<script>
import indexLevel1 from './indexLevel1.vue';
import indexLevel2 from './indexLevel2.vue';
import indexLevel3 from './indexLevel3.vue';
export default {
  name: "Speak",
  components: { indexLevel1, indexLevel2, indexLevel3 },
  computed: {
    deptLevel() {
      return this.$store.state.user.deptLevel;
    }
  },
  data() {
    return {

    };
  },
  created() {

  },
  methods: {

  }
};
</script>
