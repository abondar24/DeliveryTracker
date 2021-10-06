<template>
  <div>
    <div class="alert alert-danger" role=alert v-if="notification.length >0">
      {{ notification }}
    </div>
    <div class="float-right">
      <button v-on:click="logout" class="btn btn-outline-danger" type="button">
        logout
      </button>
    </div>
    <div>
      <h3>Welcome!</h3>
      <ul>
        <li>User: {{ username }}</li>
        <li>Device: {{ deviceId }}</li>
        <li>
          Delivered <span class="badge badge-success">{{ totalDelivered }}</span> goods,
          <span class="badge badge-success">{{ monthDelivered }}</span> this month, and
          <span class="badge badge-success">{{ dayDelivered }}</span> today.
        </li>
        <li>
          Covered distance <span class="badge badge-warning">{{ totalDistance }}</span> kilometers,
          <span class="badge badge-success">{{ monthDistance }}</span> this month, and
          <span class="badge badge-success">{{ dayDistance }}</span> today.
        </li>
      </ul>
    </div>
    <div class="mt-5">
      <h5>Update user details</h5>
      <form v-on:submit="sendUpdate">
        <div class="form-group">
          <label for="email">Email</label>
          <input type="email" class="form-control" id="email" placeholder="demo@mail.me" v-model="email">
        </div>
        <div class="form-group">
          <label for="garage">Garage</label>
          <input type="garage" class="form-control" id="garage" placeholder="Garage 1 - Munich" v-model="garage">
        </div>
        <div class="form-group">
          <button type="submit" class="btn btn-outline-primary">Submit</button>
        </div>
      </form>
    </div>
  </div>
</template>

<script>
import store from '@/store'
import axios from 'axios'

export default {
  data() {

    return {
      notification: '',
      username: 'n/a',
      deviceId: 'n/a',
      totalDelivered: 0,
      totalDistance: 0,
      monthDistance: 0,
      monthDelivered: 0,
      dayDistance: 0,
      dayDelivered: 0,
      email: 'n/a',
      garage: 'n/a',
      currentDelivery: 'n/a',
      currentDescription: 'n/a'
    }
  },
  mounted() {
    if (!store.hasToken()) {
      this.$router.push({name: 'login'})
      return
    }
    this.refreshData()
    this.username = store.getUsername()
  },
  methods: {
    logout() {
      store.reset()
      this.$router.push({
        name: 'login'
      })
    },
    sendUpdate() {
      const data = {
        garage: this.garage,
        email: this.email
      }

      const config = {
        headers: {
          'Authorization': `Bearer ${store.getToken()}`
        }
      }

      axios.put(`http://localhost:8000/api/v1/${store.getUsername()}`, data, config)
          .then(() => this.refreshData())
          .catch(err => {
            this.notification = err.message
            this.refreshFromStore()
          })

    },
    refreshData() {
      axios.get(`http://localhost:8000/api/v1/${store.getUsername()}`, {
        headers: {
          'Authorization': `Bearer ${store.getToken()}`
        }
      })
          .then(response => {
            store.setGarage(response.data.garage)
            store.setDeviceId(response.data.deviceId)
            store.setEmail(response.data.email)
            this.refreshFromStore()
          })
          .catch(err => this.notification = err.message)

      const now = new Date()

      axios.get(`http://localhost:8000/api/v1/${store.getUsername()}/total`, {
        headers: {
          'Authorization': `Bearer ${store.getToken()}`
        }
      })
          .then(response => {
            this.totalDelivered = response.data.delivered
            this.totalDistance = response.data.distance
          })
          .catch(err => {
            if (err.response.status === 404) {
              this.totalDelivered = 0
              this.totalDistance = 0
            } else {
              this.notification = err.message
            }
          })

      axios.get(`http://localhost:8000/api/v1/${store.getUsername()}/${now.getUTCMonth() + 1}`, {
        headers: {
          'Authorization': `Bearer ${store.getToken()}`
        }
      })
          .then(response => {
            this.monthDelivered = response.data.delivered
            this.monthDistance = response.data.distance
          })
          .catch(err => {
            if (err.response.status === 404) {
              this.monthDelivered = 0
              this.monthDistance = 0
            } else {
              this.notification = err.message
            }
          })

      axios.get(`http://localhost:8000/api/v1/${store.getUsername()}/${now.getUTCMonth() + 1}/${now.getDate()}`, {
        headers: {
          'Authorization': `Bearer ${store.getToken()}`
        }
      })
          .then(response => {
            this.dayDelivered = response.data.delivered
            this.dayDistance = response.data.distance
          })
          .catch(err => {
            if (err.response.status === 404) {
              this.dayDelivered = 0
              this.dayDistance = 0
            } else {
              this.notification = err.message
            }
          })

    },
    refreshFromStore() {
      this.garage = store.getGarage()
      this.deviceId = store.getDeviceId()
      this.email = store.getEmail()
    }
  }
}
</script>
