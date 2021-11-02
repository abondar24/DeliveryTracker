export default {

    setToken(value){
        localStorage.setItem("token",value)
    },

    getToken(){
        return localStorage.getItem("token")
    },

    hasToken(){
        return localStorage.getItem("token") !== null
    },

    setUsername(value){
        localStorage.setItem("username",value)
    },

    getUsername(){
      return localStorage.getItem("username")
    },

    setGarage(value){
        localStorage.setItem("garage",value)
    },

    getGarage(){
        return localStorage.getItem("garage")
    },

    setDeviceId(value){
        localStorage.setItem("deviceId",value)
    },

    getDeviceId(){
        return localStorage.getItem("deviceId")
    },

    setEmail(value){
        localStorage.setItem("email",value)
    },

    getEmail(){
        return localStorage.getItem("email")
    },

    reset(){
        localStorage.clear();
    }
}
