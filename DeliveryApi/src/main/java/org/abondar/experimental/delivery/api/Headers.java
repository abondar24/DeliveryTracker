package org.abondar.experimental.delivery.api;

public enum Headers {
      REQUESTED_WITH("x-requested-with"),
      ACCESS_CONTROL("Access-Control-Allow-Origin"),
      ORIGIN("origin"),
      CONTENT_TYPE("Content-Type"),
      ACCEPT("accept"),
      JSON("application/json"),
      JWT("application/jwt"),
      AUTH("Authorization");


    private final String val;

    Headers(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }
}
