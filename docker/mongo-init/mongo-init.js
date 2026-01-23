db.createUser({
  user: process.env.PROMETHEUS_EXPORTER_MONGO_USERNAME,
  pwd: process.env.PROMETHEUS_EXPORTER_MONGO_PASSWORD,
  roles: [
      {
         "role":"clusterMonitor",
         "db":"admin"
      },
      {
         "role":"read",
         "db":"local"
      },
      {
          "role":"read",
          "db":"admin"
      }
  ]
})