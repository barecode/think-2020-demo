# Deploying to zLinux OpenShift

Key points:
* Hazelcast and Infinispan do not run on zLinux, so this demo does not try to show that. It would be possible to configure using an externally hosted JCache provider, or to change the session replication to use a database (such as Postgress).
* Two builds: Dockerfile.tWAS and Dockerfile.liberty

Instructions to deploy the demo web application to OpenShift on zLinux.

Overall flow:
1. [Setup prereqs (CP4Apps, OCP, etc)](#prereqs)
1. [Build Application Image](#build-application-images)
1. [Deploy Application](#deploy-application)
1. [Undeploy Application](#undeploy-application)

## Prereqs

1. Build the demo.war from the [source project](..)
1. Install IBM Cloud Pak for Applications 4.1 on OpenShift Container Platform 4.3
   * The following optional pieces of CP4Apps are not needed: TA, Common Services
1. Have Helm v3 [installed](https://helm.sh/docs/intro/install/)
   
## Install Steps


### Build Application Images

The build steps require a zLinux (s390x) virtual machine. The application binary is built from the [source project](..) `mvn package`. The application is currently built at `target/demo.war`. Because Java WARs are platform indepedent, the application can be built on any platform.


### Build traditional WAS image

1. Copy the app binary to this directory.
1. Review the `install_app.py` - this is a wsadmin script that is ran as part of the Docker build step. The script installs the WAR file using the AdminApp.install task.
1. Review the `Dockerfile.tWAS`. The docker build will copy the install_app.py and demo.war file into the image.
1. Build the application image, tag it, and push it - I use Docker Hub. You may need to log in to your Docker registry. Commands are executed from this directory.
   ```shell script
   docker build -f Dockerfile.tWAS -t think-demo-twas .
   docker tag think-demo-twas barecode/think-demo-twas
   docker push barecode/think-demo-twas
   ```

Docker Hub image: https://hub.docker.com/repository/docker/barecode/think-demo-twas

### Build Liberty image

1. Copy the app binary to this directory.
1. Review the `server.xml` - it includes the JSP feature and sets the port to 9080. No changes are required at this step.
1. Review the `Dockerfile.Liberty`. The docker build will copy the server.xml and demo.war file into the image.
1. Build the application image, tag it, and push it - I use Docker Hub. You may need to log in to your Docker registry. Commands are executed from this directory.
   ```shell script
   docker build -f Dockerfile.liberty -t think-demo:z .
   docker tag think-demo:z barecode/think-demo:z
   docker push barecode/think-demo:z
   ```

Docker Hub image: https://hub.docker.com/repository/docker/barecode/think-demo

### Deploy application


These steps are done entirely from your *local laptop*. You do not need to SSH into the cluster VMs.

1. Login to the OCP cluster: `oc login --token=1... --server=https://api.trows.os.fyre.ibm.com:6443`
1. Create a 'demo' project (aka namespace): `oc create namespace demo`
1. Switch to that project: `oc project demo`
