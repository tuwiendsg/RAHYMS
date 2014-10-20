SALAM v2.0
==========

A SociAL compute unit (SCU) runtime frAmework and siMulation

## What does it do?

This repository provides SCU runtime components and an SCU simulation based on GridSim.

It contains the following components:

* Social Compute Unit (SCU) and Individual Compute Unit (ICU) meta model, which consists of
  * ICU with their profiles: skills, static properties, metrics
  * ICU connectedness
  * Task specification containing requirements that can be matched againts ICU profiles
  * Roles of each SCU member in the task and their dependencies

* Generators
  * ICU generators with configurable distribution of skills, properties, and connectedness
  * Task and role generator with configurable distribution of task requirements

* SCU middleware components:
  * ICU cloud manager and discovery service
  * Task scheduler
  * SCU composer (adopted from the provisioning engine from the ICSOC paper)
  * Metrics monitor
  * Metrics exporter to WEKA arff format for learning and prediction

* A REST API and a Web UI for reading, updating, and creating SCU (collective), ICU (peer), and tasks.

* An integration with smartcom (https://github.com/tuwiendsg/SmartCom) for sending and receiving message to/from peers.

* A GridSim-based simulation controller

## How to get started?

* Compilation and configuration:
  * Run mvn install on smartcom project
  * Run mvn install on salam project
  * To configure SCU composer, REST API, and Web UI: change configuration files on salam\salam-rest\config.
  * To configure gridsim simulation: 
* To run the program:
	- Running REST API and Web UI: 
	  run \salam\salam-rest\src\main\java\at\ac\tuwien\dsg\salam\rest\RunRestServer.java
	  or execute "java -jar salam\salam-rest\target\salam-rest-0.0.1-SNAPSHOT.jar"
	- Running smartcom software peer: 
	  execute smartcom-salam\smartcom-salam-demo\src\main\java\at\ac\tuwien\dsg\smartsociety\demo\peer\PeerApplication
	- Running simulation: 
	  execute SCUGridSim\src\scu\run\RunSimulation.java.
