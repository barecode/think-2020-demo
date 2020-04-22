# THINK 2020 Demo Web App - Session Persistence

This web application demonstrates session information as part of a session persistence demo showing IBM Cloud Transformation Advisor tech preview update for 2Q2020. This web application uses a very simple JSP to show session information known by the application server (Open Liberty in this case) and some basic host information, used to demonstrate the routing and failover (session replication) aspects.

The session information is very simple (it stores a plain text string) and echos that back to the client (note this is NOT a secure pattern and is intended for limited demo purposes ONLY). The session information can be configured to be persisted using a variety of techniques.

The demo covers using WebSphere Network Deployment's Memory-to-Memory session replciation option, a common setting in many enterprises, and using IBM Cloud Transformation Advisor's guidance and output to create an OpenShift Container Platform equivalent deployment using Hazelcast In-Memory Data Grid (IMDG) as the JCache provider for session storage.


## Quick Start

This project uses the OpenLiberty maven plugin for easy use.

To build: `./mvnw install`

To run: `./mvnw liberty:run`

Access app: [http://localhost:9080/demo/](http://localhost:9080/demo/)


## Usage Instructions

This web application uses a simple JSP to render information about the routing and session information for this application. Once the application is initially accessed, the session is created. Subsequent accesses attempt to load informaton from the session and will echo that information back to the user.

The demo takes the web application, deploys it to a WAS ND cluster and then replciates the session replication quality of service in OpenShift. The high-level steps are as follows:

1. Clone this git repository.
1. Build the demo.war: `./mvnw install`
1. Deploy the demo.war to a WAS ND cluster with Memory-to-Memory session replication enabled.
   - Note the session information is persisted automatically by WAS ND - the cluster's Memory-to-Memory session replication transparently handles session replication for the application.
1. Deploy the demo.war to OpenShift using the Dockerfile and associated Kubernetes YAMLs in [ocp-deployment](ocp-deployment). 

Have fun! :)
