<template>
  <div>
    <div class="alert alert-danger" role=alert v-if="notification.length >0">
      {{ notification }}
    </div>
    <form v-on:submit="login">
      <div class="form-group">
        <label for="username">Username</label>
        <input type="username" class="form-control" id="username" placeholder="admin123" v-model="username">
      </div>
      <div class="form-group">
        <label for="password">Password</label>
        <input type="password" class="form-control" id="password" placeholder="admin123" v-model="password">
      </div>
      <div class="form-group">
        <button type="submit" class="btn btn-primary">Submit</button>
      </div>
    </form>
    <div>
      <p>
        ...or <router-link to="/register">register</router-link>
      </p>
    </div>
  </div>
</template>
<script>
import store from "@/store";
import axios from "axios";

export default {
  data(){
    return {
      notification: '',
      username:'',
      password:''
    }
  },
  methods: {
    login(){
      if (this.username.length ===0 || this.password.length ===0){
        return
      }

      axios.post("http://localhost:8000/api/v1/token",{
        username: this.username,
        password: this.password
      })
      .then(response=>{
        store.setToken(response.data)
        store.setUsername(this.username)
        this.$router.push({name: 'home'})
      })
      .catch(err => this.notification=err.message)
     }
  }
}
</script>
