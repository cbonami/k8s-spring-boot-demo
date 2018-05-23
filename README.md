# k8s-spring-boot-demo

## CI/CD pipeline

github (source code + dockerfile) --trigger-> travis (build java + docker image) --push docker image--> docker hub --> spinnaker --> k8s (AWS)

## Pre-reqs

Install Travis CLI: 

```bash
sudo gem install travis
```

Encrypt Docker Hub Credentials:

```bash
travis encrypt DOCKER_USER=”dockerhub-username”
travis encrypt DOCKER_PASS=”dockerhub-password”
```

