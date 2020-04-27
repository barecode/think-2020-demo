# Deploying to OpenShift with Hazelcast Session Persistence

Instructions to deploy the demo web application to OpenShift using Hazelcast as the session persistence store.

Overall flow:
1. [Setup prereqs (CP4Apps, OCP, etc)](#prereqs)
1. [Deploy OpenLiberty Operator](#deploy-open-liberty-operator)
1. [Deploy Hazelcast](#deploy-hazelcast)
1. [Build Application Image](#build-application-image)
1. [Deploy Application](#deploy-application)
1. [Undeploy Application](#undeploy-application)

## Prereqs

1. Build the demo.war from the [source project](..)
1. Install IBM Cloud Pak for Applications 4.1 on OpenShift Container Platform 4.3
   * The following optional pieces of CP4Apps are not needed: TA, Common Services
1. Have Helm v3 [installed](https://helm.sh/docs/intro/install/)
   
## Install Steps

These steps are done entirely from your *local laptop*. You do not need to SSH into the cluster VMs.

1. Login to the OCP cluster: `oc login --token=1... --server=https://api.trows.os.fyre.ibm.com:6443`
1. Create a 'demo' project (aka namespace): `oc create namespace demo`
1. Switch to that project: `oc project demo`


### Deploy Open Liberty Operator

*Reference: https://github.com/OpenLiberty/open-liberty-operator/tree/master/deploy/releases/0.3.0#open-liberty-operator-v030*

This step will deploy the Open Liberty Operator to your cluster. If the operator is already set up and watching the project/namespace you intend to deploy the application to, you can skip this step.

1. Apply the OpenLibertyApplication Custom Resource Definitions:
   ```shell script
   oc apply -f https://raw.githubusercontent.com/OpenLiberty/open-liberty-operator/master/deploy/releases/0.3.0/openliberty-   app-crd.yaml
   ``` 
1. Configure the demo project as the namespace for the operator to watch by setting variables to be used later:
   ```shell script
   OPERATOR_NAMESPACE=demo
   WATCH_NAMESPACE=demo
   ```
1. Deploy the operator:
   ```shell script
   curl -L https://raw.githubusercontent.com/OpenLiberty/open-liberty-operator/master/deploy/releases/0.3.0/openliberty-app-operator.yaml \
     | sed -e "s/OPEN_LIBERTY_WATCH_NAMESPACE/${WATCH_NAMESPACE}/" \
     | oc apply -n ${OPERATOR_NAMESPACE} -f -
   ```

### Deploy Hazelcast

[Helm v3](https://helm.sh/docs/intro/install/) is required for this step (be sure to upgrade to v3 as v2 will not work). We use the Hazelcast helm chart to deploy the open source version of Hazelcast. [Hazelcast Enterprise](https://operatorhub.io/operator/hazelcast-enterprise) is available via https://operatorhub.io/

1. Add the Google public charts repository and download the charts:
   ```shell script
   helm repo add stable https://kubernetes-charts.storage.googleapis.com/
   helm repo update
   ```
1. Install the Hazelcast chart to create the cluster. You must override the default security settings in order for pod creation to succeed. We also disable the management center since that requires additional configuration and is not stricyly needed. You can additionally override other settings to configure the deployed cluster (see https://github.com/helm/charts/tree/master/stable/hazelcast#configuration):
   ```shell script
   helm install hazelcast-server stable/hazelcast --set securityContext.runAsUser=null,securityContext.fsGroup=null,mancenter.enabled=false
   ```
1. Ensure all server pods come up successfully.
   ```
   oc get pods
   NAME                                     READY   STATUS    RESTARTS   AGE
   hazelcast-server-0                       1/1     Running   0          3m16s
   hazelcast-server-1                       1/1     Running   0          2m22s
   hazelcast-server-2                       1/1     Running   0          93s
   ```
1. You should see a message in each server pod's logs indicating the server is up and has joined a cluster with the number of members you expect (3, in this case):
   ```
   Members {size:3, ver:3} [
   	Member [10.254.13.227]:5701 - 7c157b26-d91c-4e59-9f07-c366efec6a5e this
   	Member [10.254.16.135]:5701 - 00984992-1fe0-43bd-9236-bbc86985e4f4
   	Member [10.254.20.115]:5701 - b5ff9b62-6f65-4a0d-8d72-ee629156ff8b
   ]
   ```
1. We will need to establish the correct Kubernetes security role bindings to allow the application to discover Hazelcast, as per these [instructions](https://github.com/hazelcast/hazelcast-kubernetes) - we will do that in the subsequent app deploy step.

### Build Application Image

The application binary is built from the [source project](..) `mvn package`. The application is currently built at `target/demo.war`.

1. Copy the app binary to this directory.
1. Review the `server.xml` - it includes the JSP feature and sets the port to 9080. No changes are required at this step. The `sessionCache-1.0` feature and all related configuration for Hazelcast will be added by the Liberty docker image during build, so you do not need to include any configuration for that here.
1. Review the `Dockerfile`. The docker build will copy the server.xml and demo.war file into the image. The Hazelcast client jars are copied from the official Hazelcast Docker image and placed in the expected file location within the application image. Note that the version of the drivers must match the version of the server - which it should if you followed these instructions and things haven't gotten too out of date :)
1. Build the application image, tag it, and push it - I use Docker Hub. You may need to log in to your Docker registry. Commands are executed from this directory.
   ```shell script
   docker build -t think-demo:1.0 .
   docker tag think-demo:1.0 barecode/think-demo:1.0
   docker push barecode/think-demo:1.0
   ```

### Deploy Application
1. Review the `openliberty.yaml` file. The deployed application will be called 'think-demo-hz', and a set of Pods and a ServiceAccount will be created. Update the `applicationImage` field with the name of the image you pushed. 
   > If you pushed to the internal OpenShift image registry, alter the repository name so that it reads `image-registry.openshift-image-registry.svc:5000/` instead of the value for `$HOST`. All other portions after `$HOST` remain unchanged.
1. Adjust the amount of replicas to create if you want, but provision at least 2 replicas if you want to demo the session caching in action.
1. Review the `hazelcast-client-role.yaml` - this YAML defines a Role and RoleBinding. The Role grants access to "read" pods in the name space, and the RoleBinding will map that Role to the ServiceAccount (which is created when the app is deployed). This is necessary because the default ServiceAccount permissions on OpenShift restrict the client's ability to discover the running hazelcast pods.
1. Create the hazelcast client role, and role binding
   ```shell script
   oc apply -f hazelcast-client-role.yaml
   ```
1. Apply the custom resource to deploy the application:
   ```shell script
   oc apply -f app-deploy.yaml
   ```
1. Watch the deployment in the Developer console. When the pods become ready, you can access the application via the Route that was automatically created. Be sure to add the context root of `/demo` to the end of the URL.

### Undeploy Application

1. To delete the deployed application, delete the OpenLibertyApplication resource by the name specified (think-demo-hz):
   ```shell script
   oc delete OpenLibertyApplication think-demo-hz
   ```
1. This operation will cleans up the pods, serviceaccount, etc

## Appendix A: Debug / Inspection Steps

* Inspect the ServiceAccount in the namespace:
   ```shell script
   oc get ServiceAccount
   NAME                    SECRETS   AGE
   builder                 2         21h
   default                 2         21h
   deployer                2         21h
   hazelcast-server        2         20m
   open-liberty-operator   2         22m
   pipeline                2         21h
   think-demo-hz           2         7m44s
   ```
