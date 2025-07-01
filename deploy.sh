#!/bin/bash

echo "Building Docker images..."
docker build -t eagle-bank:latest ./
docker build -t eagle-bank-frontend:latest ./eagle-bank-frontend

echo "Applying Kubernetes configurations..."
kubectl apply -f ./k8s/local/deployment.yaml

echo "Waiting for pods to be ready..."
kubectl wait --for=condition=ready pod -l app=eagle-bank -n eagle-bank-local --timeout=120s
kubectl wait --for=condition=ready pod -l app=eagle-bank-frontend -n eagle-bank-local --timeout=120s

echo "Services are available at:"
echo "Frontend: http://localhost"
echo "Backend: http://localhost:8080"

echo "Displaying pod status..."
kubectl get pods -n eagle-bank-local