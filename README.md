RAHYMS v1.0
===========

Runtime and Analytics for HYbrid coMpute unitS

## What does it do?

This repository provides runtime components and analytic tools for Hybrid Compute Units (HCU). It also provides an HCU simulation based on GridSim.

It contains the following components:

* Hybrid Compute Unit (HCU) and Individual Compute Unit (ICU) meta model, which consists of
  * ICU with their profiles: skills, static properties, metrics
  * ICU connectedness
  * Task specification containing requirements that can be matched againts ICU profiles
  * Roles of each HCU member in the task and their dependencies

* Generators
  * ICU generators with configurable distribution of skills, properties, and connectedness
  * Task and role generator with configurable distribution of task requirements

* HCU middleware components:
  * ICU cloud manager and discovery service
  * Task scheduler
  * HCU composer (adopted from the provisioning engine from the <a href='http://www.infosys.tuwien.ac.at/research/viecom/papers/ICSOC2013-SCUProvisioning.pdf'>ICSOC 2014 paper</a>)
  * Metrics monitor
  * Metrics exporter to WEKA arff format for learning and prediction
  * HCU reliabililty analysis engine (adopted from the <a href='http://dsg.tuwien.ac.at/staff/phdschool/mzuhri/papers/mcandra-cic2015-reliability.pdf'>CIC 2015 paper</a>)

* A REST API and a Web UI for reading, updating, and creating HCU (collective), ICU (peer), and tasks.

* An integration with SmartCom (https://github.com/tuwiendsg/SmartCom) for sending and receiving message to/from peers.

* A GridSim-based simulation controller

## How to get started?

* Compilation and configuration:
  * Run mvn install on smartcom project
  * Run mvn install on hcu project
  * To configure HCU composer, REST API, and Web UI: change configuration files in hcu\hcu-rest\config.
  * To configure gridsim simulation: change scenarios in in hcu\hcu-simulation\scenarios.
* To run the program:
  * Running in interactive-mode (to start REST API and Web UI):
    * run \hcu\hcu-rest\src\main\java\at\ac\tuwien\dsg\hcu\rest\RunRestServer.java
    * or execute "java -jar hcu\hcu-rest\target\hcu-rest-0.0.1-SNAPSHOT.jar"
    * by default the Web UI is available at http://localhost:8080/web-ui/
      and the REST APIP playground is available at http://localhost:8080/rest-ui/
  * Running in simulation mode:
    * run \hcu\hcu-simulation\src\main\java\at\ac\tuwien\dsg\hcu\simulation\RunSimulation.java.
