<template>
  <div>
    <div class="alert alert-danger" role=alert v-if="notification.length >0">
      {{ notification }}
    </div>
    <form v-on:submit="submit">
      <div class="form-group">
        <label for="username">Username</label>
        <input type="username" class="form-control" id="username" placeholder="admin123" v-model="username">
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input type="password" class="form-control" id="password" placeholder="admin123" v-model="password">
      </div>
      <div class="form-group">
        <label for="email">Email</label>
        <input type="email" class="form-control" id="email" placeholder="admin@mail.com" v-model="email">
      </div>
      <div class="form-group">
        <label for="deviceId">Device ID</label>
        <input type="deviceId" class="form-control" id="deviceId" placeholder="123abc" v-model="deviceId">
      </div>
      <div class="form-group">
        <label for="garage">Garage</label>
        <input type="garage" class="form-control" id="garage" placeholder="Garage 1 - Munich" v-model="garage">
      </div>
      <div class="form-group">
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </form>
  </div>
</template>

<script>
import axios from "axios";

export default {
  data() {
    return {
      notification: '',
      username: '',
      password: '',
      email: '',
      deviceId: '',
      garage: ''
    }
  },
  methods: {
    submit() {
      const payload = {
        username: this.username,
        password: this.password,
        email: this.email,
        deviceId: this.deviceId,
        garage: this.garage
      }

      axios.post(`http://localhost:8000/api/v1/register`,payload)
          .then(() => this.$router.push('/'))
          .catch(err => this.notification = err.message)
    }
  }
}
</script>
