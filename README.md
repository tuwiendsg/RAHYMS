SALAM
=====

A SociAL compute unit runtime frAmework and siMulation

This repository provides Social Compute Unit (SCU) runtime components and simulation based on GridSim.

It contains the following components:

* Social Compute Unit (SCU) and Individual Compute Unit (ICU) meta model, which consists of
** ICU with their profiles: skills, static properties, metrics
** ICU connectedness
** Task specification containing requirements that can be matched againts ICU profiles
** Roles of each SCU member in the task and their dependencies

* Generators
** ICU generators with configurable distribution of skills, properties, and connectedness
** Task and role generator with configurable distribution of task requirements

* SCU middleware components:
** ICU cloud manager and discovery service
** Task scheduler
** SCU composer (adopted from the provisioning engine from the ICSOC paper)
** Metrics monitor
** Metrics exporter to WEKA arff format for learning and prediction

* A GridSim-based simulation controller

To run the simulation, run SCUGridSim\src\scu\run\RunSimulation.java.
Simulation and composer configuration is in SCUGridSim\config.
