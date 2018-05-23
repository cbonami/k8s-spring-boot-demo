# k8s-spring-boot-demo

## CI/CD pipeline

github (source code + dockerfile) --hook-> travis (build java + docker image) --push docker image--> docker hub --> spinnaker --> k8s (AWS)

## Pre-reqs

### Create a GitHub account

Source code goes into a GitHub repo.
Eg: cbonami/k8s-spring-boot-demo

### Create a Docker Hub account

The docker hub account does not need to be linked to the github account, as we won't use docker hub to build the image. Travis will.
Travis will push the image (cbonami/k8s-spring-boot-demo) into the docker hub (public) repo with the same name (cbonami/k8s-spring-boot-demo).

### Get a Travis account

Get Travis account, by signing in via your GitHub account.
Your Travis- and GitHub-accounts will be automatically linked.
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

