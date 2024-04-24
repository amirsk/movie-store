# Movie Store

## Tech

- Spring Boot
- Java 21
- Terraform
- Docker and ECR
- AWS EKS
- AWS S3
- AWS DynamoDB

## Run

### Terraform

Run Terraform project to create infrastructure:

`terraform apply`

### Docker

Create or update image in Docker:

```console
# image_name={ PLACEHOLDER }
docker buildx build --platform linux/amd64 -t ${image_name} .`
docker images`
docker run -p 8080:8080 ${image_name}`
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

Namespace, Deployment, and Service:

```console
# region={ PLACEHOLDER }
# cluster_name={ PLACEHOLDER }
# namespace_name={ PLACEHOLDER }
aws eks update-kubeconfig --region us-east-1 --name ${cluster_name}
kubectl create namespace ${namespace_name}
kubectl apply -f ./deployment.yaml -n ${namespace_name}
kubectl apply -f ./service.yaml -n ${namespace_name}
```

### Swagger



## Cleaning Up

Do not forget to delete infrastructure and app!