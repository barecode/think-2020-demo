# IBM THINK 2020 Demo Web Application

This web application demonstrates session information as part of a session persistence demo showing IBM Cloud Transformation Advisor tech preview update for 2Q2020. This web application uses a very simple JSP to show session information known by the application server (Open Liberty in this case) and some basic host information, used to demonstrate the routing and failvoer (session replication) aspects.

The session information is very simple (it stores a plain text string) and echos that back to the client (note this is NOT a secure pattern and is intended for limited demo purposes ONLY). The session information can be configured to be persisted using a variety of techniques. The demo covers using WebSphere Network Deployment's Memory-to-Memory session replciation option, a common setting in many enterprises, and using IBM Cloud Transformation Advisor's guidance and output to create an OpenShift Container Platform equivalent deployment using Hazelcast In-Memory Data Grid (IMDG).

## Usage Instructions

1. Clone this git repository.
1. Build the [demo.war](demo.war).
1. Deploy the demo.war using the Dockerfile and associated Kubernetes YAMLs in [ocp-deployment](ocp-deployment). 

Have fun! :)

