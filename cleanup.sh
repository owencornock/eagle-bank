#!/bin/bash

echo "Stopping all services in eagle-bank-local namespace..."
kubectl delete namespace eagle-bank-local

echo "Waiting for namespace deletion..."
kubectl wait --for=delete namespace/eagle-bank-local --timeout=60s

echo "All services stopped and cleaned up."