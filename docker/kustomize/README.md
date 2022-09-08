
## 0) minikube setup on dev machine (sorry, macos instructions only for now)
```bash
brew install minikube
```

### install minikube on macos (with hyperkit driver)
The default driver for minikube is 'docker'.  when using the docker driver, the *ingress* and *ingress-dns* addons (to support ingress controllers) only works on Linux [https://minikube.sigs.k8s.io/docs/drivers/docker/], not mac.

### install ingress on minikube
Official tutorial to install ingress on macos is out of date as of 2022.09.08, see [updated instructions](https://github.com/kubernetes/minikube/issues/12876#issuecomment-1023970717) 

## 1) add secrets to Kubernetes Cluster (don't store in repo for now)

### install github credentials as a secret
for Kubernetes to pull from private GitHub container registry when it spins up pods (see deployment templates)

```yaml
kind: Deployment
spec:
  template:
    spec:
      imagePullSecrets:
        - name: dockerconfigjson-github-com
```

The github credentials must be manually inserted into 
the Kubernetes cluster   For background material: 
see [authenticating ghcr with Kubernetes](https://dev.to/asizikov/using-github-container-registry-with-kubernetes-38fb)

```bash
./deploy-repo-secret.sh github_user github_token
```

### install external database credentials as a secret

```bash
./deploy-db-secret.sh db_user db_password
``` 

## 3) deploy the VCell application to Kubernetes (later use kustomize layers for different configurations)

```bash
kubectl apply -f base
```


