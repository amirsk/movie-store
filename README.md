# Movie Store

## Tech

- Spring Boot
- Java 21
- Terraform
- Docker and ECR
- AWS EKS
- AWS S3
- AWS KMS
- AWS DynamoDB
- AWS DAX

## Run

### Maven

Build the project first:

```console
mvn clean package
```

### Terraform

Run Terraform project to create infrastructure:

```console
terraform apply
```

### Docker

Create or update image in Docker:

```console
# image_name={ PLACEHOLDER }
docker buildx build --platform linux/amd64 -t ${image_name} .
docker images
docker run -p 8080:8080 ${image_name}
```

### AWS ECR

Prepare image above and push to AWS ECR:

```console
# region={ PLACEHOLDER }
# aws_account_id={ PLACEHOLDER }
# repository_name={ PLACEHOLDER }
# image_name={ PLACEHOLDER }
# aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${aws_account_id}.dkr.ecr.${region}.amazonaws.com
# aws ecr create-repository --repository-name ${repository_name} --region ${region}
# docker tag ${image_name}:latest ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${repository_name}
# docker push ${aws_account_id}.dkr.ecr.${region}.amazonaws.com/${repository_name}
```

### AWS EKS

Namespace, Deployment, and Service.

First replace `${aws_account_number}` in [deployment.yaml](kube%2Fdeployment.yaml) with the correct AWS Account.

Then, issue below commands.

```console
# region={ PLACEHOLDER }
# cluster_name={ PLACEHOLDER }
# namespace_name={ PLACEHOLDER }
aws eks update-kubeconfig --region ${region} --name ${cluster_name}
kubectl create namespace ${namespace_name}
kubectl apply -f ./deployment.yaml -n ${namespace_name}
kubectl get pods -n ${namespace_name}
kubectl logs {pod_name} -n ${namespace_name}
kubectl apply -f ./service.yaml -n ${namespace_name}
kubectl get all -n ${namespace_name}
```

### Swagger

Find ELB URL by issuing below command and find External IP in Service.

Note that ELB takes a long time to be available.

Go to AWS Target Group and monitor health nodes.

```console
# namespace_name={ PLACEHOLDER }
kubectl get all -n ${namespace_name}
```

Then, navigate to the url:

`http://{ PLACEHOLDER }/swagger-ui/index.html`

## Cleaning Up

Do not forget to delete infrastructure and app!