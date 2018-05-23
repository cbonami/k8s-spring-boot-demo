# k8s-spring-boot-demo

Spring Boot app that follows a CI/CD pipeline based on GitHub, Docker Hub, Travis and Spinnaker.
Final deployment is done in Kubernetes cluster (where Spinnaker is also running).

## CI/CD pipeline

GitHub (source code + dockerfile) --push-hook-> Travis (build java + docker image) --push docker image--> docker hub --polling-> spinnaker --deploy-> k8s (AWS)

## Steps

### Create a GitHub account

Source code goes into a (public/private) GitHub repo.
Eg: cbonami/k8s-spring-boot-demo

### Create a Docker Hub account

The docker hub account does not need to be linked to the github account, as we won't use docker hub to build the image. Travis will.
Travis will push the image (cbonami/k8s-spring-boot-demo) into the docker hub (public/private) repo with the same name (cbonami/k8s-spring-boot-demo).

### Get a Travis account

Get a Travis account, by signing in via your GitHub account.
Your Travis- and GitHub-accounts will be automatically linked, ie whenever code is pushed to github, it will be built automatically by Travis.
Enable the GitHub-repo of your choosing in Travis.

### Update .travis.yml

The Docker Hub credentials should be provided to Travis, so that it can push images into it.

Install Travis CLI: 

```bash
sudo gem install travis
```

Encrypt Docker Hub Credentials:

```bash
travis encrypt DOCKER_USER=”dockerhub-username”
travis encrypt DOCKER_PASS=”dockerhub-password”
```

### Create a local VM

All setup and installations will be done from a VM on your local machine.
This VM could be shared with others.
We make use of a ubuntu/xenial64 box that we set up via vagrant.

Install vagrant on your local host.

```bash
vagrant init ubuntu/xenials64
vagrant up
vagrant ssh
```

### Generate a private SSH key

In the Ubuntu VM.

<todo>

```bash
ssh-keygen
```

### Install kubernetes cluster with Kops

#### Register a domain for the k8s cluster

Register domain: on AWS itself or via another service, like Namecheap.
https://www.namecheap.com/domains-pricing-register.aspx
=> kubernetes.edonis.club

Take note of Route53 DNS records: eg:
ns-1695.awsdns-19.co.uk.
ns-426.awsdns-53.com.
ns-1009.awsdns-62.net.
ns-1323.awsdns-37.org.

#### Create a S3 bucket for the kops-state

Create S3 bucket for kops-state: s3://kops-state-azerty.
Repo-name needs to be unique, so pick something that doesn't exist yet.

#### Create the actual k8s cluster

Create cluster:

```bash
kops create cluster --name=kubernetes.edonis.club --state=s3://kops-state-azerty --zones=eu-central-1a,eu-central-1b --node-count=3 --node-size=t2.medium --master-size=t2.small --dns-zone=kubernetes.edonis.club
kops update cluster kubernetes.edonis.club --yes
```

Validate cluster:

```bash
kops validate cluster kubernetes.edonis.club
```

convenient: edit ~/.bashrc
add these lines:
```bash
export KOPS_STATE_STORE=s3://kops-state-azerty
export EDITOR=nano
```

Modify cluster:
```bash
kops edit ig nodes --state=s3://kops-state-azerty
kops update cluster kubernetes.edonis.club --yes --state=s3://kops-state-azerty
```

Delete cluster:
```bash
kops delete cluster --name kubernetes.edonis.club --state=s3://kops-state-azerty --yes
```

Optional: Test the cluster:
```bash
kubectl run hello-minikube --image=gcr.io/google_containers/echoserver:1.4 --port=8080
kubectl expose deployment hello-minikube --type=NodePort
kubectl get service 
```
--> gives you the port the echo-service is running on (31317)
In AWS dashboard, open port via creation of AWS Security Group of master node.
Then go to Route53 > Hosted Zone (kubernetes.edonis.club)
Discover the api-url: http://api.kubernetes.edonis.club:31317/test
Point your browser to that url.

### Install Spinnaker

#### install kubectl

#### install helm

```bash
curl https://raw.githubusercontent.com/kubernetes/helm/master/scripts/get > get_helm.sh
chmod 700 get_helm.sh
./get_helm.sh
```

#### Config RBAC

```bash
kubectl create serviceaccount tiller --namespace kube-system
kubectl create clusterrolebinding tiller --clusterrole cluster-admin --serviceaccount=kube-system:tiller
```
in case service account 'tiller' is not used: then default service account is used: this means:

```bash
kubectl create clusterrolebinding admin-clusterrolebinding --clusterrole=cluster-admin --serviceaccount=kube-system:default
```

-> see http://docs.heptio.com/content/tutorials/rbac.html
create roles: https://github.com/kubernetes/helm/issues/2962 (needed ??)

#### install helm/tiller on k8s cluster

```bash
helm init --service-account tiller
```
(or just 'helm init' if sa tiller is not used)
general info: https://dzone.com/articles/securing-helm

sidenote: remove tiller
option1: helm reset -f
option2: kubectl -n "kube-system" delete deployment tiller-deploy
tiller in its own namespace? https://medium.com/@amimahloof/how-to-setup-helm-and-tiller-with-rbac-and-namespaces-34bf27f7d3c3

#### install spinnaker with helm
https://thenewstack.io/getting-started-spinnaker-kubernetes/
helm chart can be found here: https://github.com/kubernetes/charts/tree/master/stable/spinnaker
```bash
curl -Lo values.yaml https://raw.githubusercontent.com/kubernetes/charts/master/stable/spinnaker/values.yaml
```
Make changes to spinnaker.yml: add docker hub repo's.
Then do ythe actual installation of spinnaker:

```bash
helm install -n kubelive stable/spinnaker -f values.yaml --timeout 300 --namespace spinnaker
```
check if all ok: 
```bash
kubectl get pods --namespace spinnaker
```

You want to be able to go to spinnaker dashboard from your _local_ host's browser.
The 'kubectl port-forward' when executed from the Ubunbtu VM will only bind to the local (=VM's) IP and _not_ to your host's IP.
Do port-forwarding workaround: copy ~/.kube/config from your VM to your host system (where kubectl is also installed)
Check if works: on host: 
```bash
kubectl get node
```
This should list all pods in the k8s cluster on AWS.

Then on host: forward the port 9000:
```bash
export DECK_POD=$(kubectl get pods --namespace spinnaker -l "component=deck,app=kubelive-spinnaker" -o jsonpath="{.items[0].metadata.name}")
kubectl port-forward --namespace spinnaker $DECK_POD 9000
```
Point browser to: http://localhost:9000
This should show the spinnaker dashboard.

Work around bug/problem: https://github.com/kubernetes/charts/issues/5483
-> list of applications, namespaces in spinnaker is empty :(
If you check the k8s logs:

```bash
kubectl logs -n spinnaker kubelive-spinnaker-clouddriver-d4959567-kctkg -f
```
-> com.netflix.spinnaker.clouddriver.kubernetes.v1.deploy.exception.KubernetesOperationException: Get Namespaces for account local failed: Forbidden! Configured service account doesn't have access. Service account may have been revoked. namespaces is forbidden: User "system:serviceaccount:spinnaker:default" cannot list namespaces at the cluster scope

Insecure workaround: 

```bash
kubectl create clusterrolebinding spinnaker-default-crbinding --clusterrole cluster-admin --serviceaccount=spinnaker:default
```

##### upgrade spinnaker (values.yaml)

Edit values.yml, then

```bash
helm upgrade -f values.yaml kubelive stable/spinnaker
```

##### remove spinnaker

```bash
helm del –purge kubelive
```

#### Create namespaces for apps

https://kubernetes.io/docs/tasks/administer-cluster/namespaces-walkthrough/

```bash
kubectl create -f https://k8s.io/docs/tasks/administer-cluster/namespace-dev.json
kubectl create -f https://k8s.io/docs/tasks/administer-cluster/namespace-prod.json
```

Best-practice: set contexts

### Create CD Pipeline

* Create new application
* Create new loadbalancer
* Create new server group
test nodejs app: 
```bash
kubectl proxy
```
Point your browser to:
http://127.0.0.1:8001/api/v1/proxy/namespaces/default/services/nodejs-dev-nodejs-webapp:80/

* Create new pipeline
