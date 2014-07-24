SALAM
=====

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

* A GridSim-based simulation controller

## How to get started?

* To run the simulation: execute SCUGridSim\src\scu\run\RunSimulation.java.
* To configure SCU composer and simulation: change configuration files on SCUGridSim\config.
