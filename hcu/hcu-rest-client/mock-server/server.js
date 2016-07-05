var express = require('express');
var app = express();
var fs = require("fs");
var bodyParser = require('body-parser');
var serviceCache;

app.use(bodyParser.json()); 

fs.readFile( __dirname + "/" + "db.json", 'utf8', function (err, data) {
  serviceCache = JSON.parse(data).services;
});

app.post('/discoverServices', function (req, res) {
  console.log("Requesting functionality " + req.body.functionality + "...");
  var results = [];
  for (var i=0; i<serviceCache.length; i++) {
    var service = serviceCache[i];
    //console.log(service);
    // filter by functionality
    if (service.functionality && service.functionality.name!=req.body.functionality) {
      continue;
    } else {
      service = cleanUpServiceData(service);
    }
    
    // ignore constraints, timeStart, load, and deadline
    
    results.push(service);
  }

  console.log(results.length + " service(s) found.");
  res.type('application/json');
  res.end(JSON.stringify(results));
});

app.post('/discoverConnections', function (req, res) {
  res.type('application/json');
  res.end('[]');
});

var server = app.listen(3000, function () {

  var host = server.address().address;
  var port = server.address().port;
  console.log("Mock server is listening at http://%s:%s", host, port);

});

var cleanUpServiceData = function(service) {
  // make a copy
  var result = JSON.parse(JSON.stringify(service));
  // remove unnecessary data
  delete result.title;
  delete result.metrics;
  delete result.interface;
  if (result.provider) {
    delete result.provider.status;
    delete result.provider.metrics;
    delete result.provider.connections;
    delete result.provider.services;
    delete result.provider.manager;
    delete result.provider.assignmentCount;
    delete result.provider.finishedCount;
    delete result.provider.activeAssignmentCount;
  }
  return result;
}