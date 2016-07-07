
How to configure to use rest client:
- Configure consumer.properties to use a scenario containing rest clients as the manager/discoverer implementation
- For example, in scenario-rest-config.json, we use: 
    - RestDiscoverer, which implements a rest client for the discovery service
    - DummyWorkerManager, which accepts any deployed assignments and immediately returns successful results

How to configuring RestDiscoverer:
- Change the API endpoint from the scenario json (e.g., scenario-rest-config.json)
- Note: 
    - RestDiscoverer currently only implements discoverServices to discover peers providing particular services.
    - discoverConnections to discover connectedness among peers is not yet implemented in RestDiscover.

Example of request and response format supported by RestDiscoverer for discoverService API:
- Request example:
{
  "functionality": "Collector",
  "constraints": [
    {"name":"cost_limit","value":1000.0959488231821,"type":"STATIC"},
    {"name":"location","value":"Sector-A","type":"STATIC"},
    {"name":"skill_collector","value":"fair","type":"SKILL"}
  ],
  "timeStart": 0.0,
  "load": 1.0,
  "deadline": 1000.0
}

- Response example:
[
  {
    "id": 59,
    "functionality": {
      "name": "Collector"
    },
    "description": null,
    "provider": {
      "id": 59,
      "name": "Citizen2",
      "type": 1,
      "description": null,
      "properties": {
        "valueSet": {
          "location": "Sector-C",
          "assignment_priority": 1,
          "fault_probability": 0.39015512806964,
          "cost": 1.2570150345279,
          "performance_rating": 1.2644662953424
        }
      },
      "skills": {
        "valueSet": {
          "skill_collector": 0.64241991457772
        }
      }
    }
  }, ...
  ]

Mock server:
- A mock-server, server.js, to provide discoverService API is implemented using nodejs
- server.js uses db.json containing a prepopulated services and peers
- server.js listens on port 3000 by default
- To run mock-server
    - install nodejs
    - install Express nodejs module
    - install BodyParser nodejs module
    - run server.js using nodejs

