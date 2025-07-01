#!/bin/bash

# Build the application and Docker image
./gradlew clean build
docker build -t eagle-bank:latest .

# Apply Kubernetes configurations
kubectl apply -f k8s/local/deployment.yaml

# Wait for pods to be ready
echo "Waiting for pods to be ready..."
kubectl wait --namespace=eagle-bank-local \
  --for=condition=ready pod \
  --selector=app=eagle-bank \
  --timeout=300s

# Get the service URL
echo "Application is running!"
if [ "$(command -v minikube)" ]; then
    echo "Access the application at: $(minikube service eagle-bank -n eagle-bank-local --url)"
else
    echo "Access the application at: http://localhost:8080"
fi