# Demo Web App

This web application uses a simple JSP to render information about the routing and session information for this application. Once the application is initially accessed, the session is created. Subsequent accesses attempt to load informaton from the session and will echo that information back to the user.

## Quick Start

This project uses the OpenLiberty maven plugin for easy use.

To build: `./mvnw install`

To run: `./mvnw liberty:run`

Access app: [http://localhost:9080/demo/](http://localhost:9080/demo/)

