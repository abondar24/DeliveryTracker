<template>
  <div id="app">
    <div class="row mt-5">
      <div class="col">
        <h4>
          <span class="badge badge-pill  badge-dark">{{ throughput }}</span> device updates per second
        </h4>
      </div>
    </div>
    <div class="row mt-5">
      <div class="col">
        <h4>Trend</h4>
        <table class="table table-sm table-hover">
          <thead>
          <tr>
            <th scope="col">Garage</th>
            <th scope="col">Distance</th>
            <th scope="col">Delivered</th>
          </tr>
          </thead>
          <transition-group name="garage-trend" tag="tbody">
            <tr v-for="item in garageTrendRanking" v-bind:key="item.garage">
              <td>
                {{ item.garage }}
              </td>
              <td>
                {{ item.distance }}
                <span class="text-secondary font-weight-lighter">
                  ({{ item.moment.format("ddd hh:mm:ss") }})
                </span>
              </td>
              <td>
                {{ item.delivered }}
              </td>
            </tr>
          </transition-group>
        </table>
      </div>
    </div>
    <div class="row mt-5">
      <h4>Ranking for the last 24 hours</h4>
      <table class="table table-sm table-hover">
        <thead>
        <tr>
          <th scope="col">Name</th>
          <th scope="col">Garage</th>
          <th scope="col">Distance</th>
          <th scope="col">Delivered</th>
        </tr>
        </thead>
        <transition-group name="ranking" tag="tbody">
          <tr v-for="item in ranking" v-bind:key="item.username">
            <td>
              {{ item.username }}
            </td>
            <td>
              {{ item.garage }}
            </td>
            <td>
              {{ item.distance }}
            </td>
            <td>
              {{ item.delivered }}
            </td>
          </tr>
        </transition-group>
      </table>
    </div>
  </div>
</template>

<script>
import EventBus from 'vertx3-eventbus-client'
import moment from 'moment'

const eventBus = new EventBus("/eventbus")
eventBus.enableReconnect(true)

export default {
  name: 'App',
  data() {
    return {
      throughput: 0,
      garageTrendData: {},
      ranking: []
    }
  },
  mounted() {
    eventBus.onopen = () => {
      eventBus.registerHandler("dashboard.throughput", (err, msg) => {
        this.throughput = msg.body.throughput
      })

      eventBus.registerHandler("dashboard.garage-trend", (err, msg) => {
        const data = msg.body
        data.moment = moment(data.timestamp)
        this.$set(this.garageTrendData,msg.body.garage,data)
      })

      eventBus.registerHandler("dashboard.ranking", (err, msg) => {
         this.ranking = msg.body
      })

    }
  },
  computed: {
    garageTrendRanking: function () {
      const vals = Object.values(this.garageTrendData).slice(0)
      vals.sort((a, b) => b.distance - a.distance)
      return vals
    }
  }
}
</script>


<style scoped>
.garage-trends-move, .ranking-move {
  transition: transform 0.5s;
}
</style>
