# Deploying to OpenShift with Hazelcast Session Persistence

Instructions to deploy the demo web application to OpenShift using Hazelcast as the session persistence store.

Overall flow:
1. [Setup prereqs (CP4Apps, OCP, etc)](#prereqs)
1. [Deploy OpenLiberty Operator](#deploy-open-liberty-operator)
1. [Deploy Hazelcast](#deploy-hazelcast)
1. [Build Application Image](#build-application-image)
1. [Deploy Application](#deploy-application)

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
oc apply -f https://raw.githubusercontent.com/OpenLiberty/open-liberty-operator/master/deploy/releases/0.3.0/openliberty-app-crd.yaml
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
1. Ensure all server pods come up successfully. You should see a message in each server pod's logs indicating the server is up and has joined a cluster with the number of members you expect (3, in this case):
```
oc get pods
Members {size:3, ver:3} [
	Member [10.254.13.227]:5701 - 7c157b26-d91c-4e59-9f07-c366efec6a5e this
	Member [10.254.16.135]:5701 - 00984992-1fe0-43bd-9236-bbc86985e4f4
	Member [10.254.20.115]:5701 - b5ff9b62-6f65-4a0d-8d72-ee629156ff8b
]
```

### Build Application Image

The application binary is built from the [source project](..) `mvn package`. The application is currently built at `data/example/modresorts-1.0.war`. Copy the app binary to this directory to the `docker` directory.

1. Copy the app binary to this directory
1. Review the `server.xml`. No changes are required at this step. The `sessionCache-1.0` feature and all related configuration for Hazelcast will be added by the Liberty docker image during build, so you do not need to include any configuration for that here.
1. Review the `Dockerfile`. The docker build will copy the server.xml and demo.war file into the image. The Hazelcast client jars are copied from the official Hazelcast Docker image and placed in the expected file location within the application image. Note that the version of the drivers must match the version of the server, so if you have deployed a Hazelcast 3.x cluster, copy the client jars from the appropriate 3.x image.
1. Build this image, tag it, and push it. You may need to log in to your Docker registry. (commands are executed from the `docker` directory in the project.)
```shell script
HOST=$(oc get route default-route -n openshift-image-registry --template='{{ .spec.host }}')
PROJECT=<enter OpenShift project name here>
docker login -u $(oc whoami) -p $(oc whoami -t) $HOST
docker build -t modresorts:1.0 .
docker tag modresorts:1.0 $HOST/$PROJECT/modresorts:1.0
docker push $HOST/$PROJECT/modresorts:1.0
```

### Deploy Application
1. Inspect the `openliberty.yaml` file:
```yaml
# Assumes the OpenLiberty operator has been installed in your cluster
apiVersion: openliberty.io/v1beta1
kind: OpenLibertyApplication
metadata:
  name: modresorts
spec:
  applicationImage: <image name here>
  replicas: 2
  service:
    type: ClusterIP
    port: 9080
  expose: true
```

1. Update the `applicationImage` field with the name of the image you pushed. 
   > If you pushed to the internal OpenShift image registry, alter the repository name so that it reads `image-registry.openshift-image-registry.svc:5000/` instead of the value for `$HOST`. All other portions after `$HOST` remain unchanged.

1. Adjust the amount of replicas to create if you want, but provision at least 2 replicas if you want to demo the session caching in action.

1. Apply the custom resource to deploy the application:

```shell script
oc apply -f openliberty.yaml
```

1. Watch the deployment in the web console. When the pods become ready, you can access the application via the Route that was automatically created. Be sure to add the context root of `resorts` to the end of the URL.

## Appendix A: Required Role Bindings

The default user permissions on OpenShift prevent non-admin users from doing many of the operations required by this process. The following role bindings will likely need to be added to the user doing the deployment. Note that the open-liberty-operator-ol-liberty-ns role won't exist until the corresponding operator is installed in the project/namespace the user is working in:
* `admin` role for the namespace the user is working in
* `view` role for all namespaces where resources needed in this tutorial live, including the openshift-image-registry
* `open-liberty-operator-ol-liberty-ns` role for the namespace the user is working in (requires the Open Liberty operator be deployed to watch the namespace the user is working in)

In addition, the default serviceaccount permissions on OpenShift restrict the client's ability to discover the running servers. The following role bindings will need to be added to the serviceaccount the OpenLibertyApplication is deployed under. `namespace` refers to the namespace or project you deployed the OpenLibertyApplication to. Either allow the Open Liberty operator to create the service account and then bind this role to the account (this may require a redeployment to get the pods to pick up the new permissions) or create a serviceaccount with this role bound ahead of time and specify that account in the OpenLibertyApplication yaml file.
```yaml
kind: Role
apiVersion: rbac.authorization.k8s.io/v1
metadata:
  name: hazelcast-client-resorts
  namespace: resorts
rules:
  - verbs:
      - get
      - watch
      - list
    apiGroups:
      - ''
    resources:
      - pods
```

vi required-role.yaml
kubectl apply -f required-role.yaml 




Critical docs
https://github.com/hazelcast/hazelcast-kubernetes#granting-permissions-to-use-kubernetes-api

https://hazelcast.com/blog/how-to-use-embedded-hazelcast-on-kubernetes/
https://github.com/hazelcast/hazelcast-kubernetes

