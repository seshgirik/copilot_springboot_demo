# 🚀 AWS EKS Deployment Guide
## Spring Boot Application - Production Ready

This guide provides step-by-step instructions to deploy the Spring Boot application on AWS EKS with security best practices.

---

## 📋 Prerequisites

### 1. AWS CLI and Tools Setup
```bash
# Install AWS CLI
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# Install eksctl
curl --silent --location "https://github.com/weaveworks/eksctl/releases/latest/download/eksctl_$(uname -s)_amd64.tar.gz" | tar xz -C /tmp
sudo mv /tmp/eksctl /usr/local/bin

# Install kubectl
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl

# Install Helm
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
```

### 2. AWS Configuration
```bash
# Configure AWS credentials
aws configure

# Set default region
export AWS_DEFAULT_REGION=us-west-2
export AWS_REGION=us-west-2
```

---

## 🔧 Phase 1: Infrastructure Setup

### 1. Create EKS Cluster with Security Best Practices

```bash
# Create cluster configuration
cat << EOF > cluster-config.yaml
apiVersion: eksctl.io/v1alpha5
kind: ClusterConfig

metadata:
  name: springboot-demo
  region: us-west-2
  version: '1.28'

vpc:
  cidr: 10.0.0.0/16
  nat:
    gateway: Single

managedNodeGroups:
  - name: standard-workers
    instanceType: t3.medium
    desiredCapacity: 3
    minSize: 1
    maxSize: 5
    volumeSize: 20
    privateNetworking: true
    iam:
      withAddonPolicies:
        autoScaler: true
        ebs: true
        efs: true
        fsx: true
        albIngress: true
    labels:
      role: workers
    tags:
      k8s.io/cluster-autoscaler/node-template/label/role: workers
    ssh:
      allow: false

addons:
  - name: vpc-cni
    version: latest
  - name: coredns
    version: latest
  - name: kube-proxy
    version: latest
  - name: aws-ebs-csi-driver
    version: latest

iam:
  withOIDC: true
  serviceAccounts:
    - metadata:
        name: springboot-sa
        namespace: default
      wellKnownPolicies:
        autoScaler: true
      roleName: springboot-role
      roleOnly: true

cloudWatch:
  clusterLogging:
    enableTypes: ["api", "audit", "authenticator", "controllerManager", "scheduler"]

secretsEncryption:
  keyARN: auto
EOF

# Create the cluster
eksctl create cluster -f cluster-config.yaml
```

### 2. Set up AWS RDS Database

```bash
# Create RDS subnet group
aws rds create-db-subnet-group \
  --db-subnet-group-name springboot-subnet-group \
  --db-subnet-group-description "Subnet group for Spring Boot app" \
  --subnet-ids subnet-xxxxx subnet-yyyyy

# Create RDS instance
aws rds create-db-instance \
  --db-instance-identifier springboot-db \
  --db-instance-class db.t3.micro \
  --engine postgres \
  --master-username dbadmin \
  --master-user-password $(openssl rand -base64 32) \
  --allocated-storage 20 \
  --storage-encrypted \
  --db-subnet-group-name springboot-subnet-group \
  --vpc-security-group-ids sg-xxxxx \
  --backup-retention-period 7 \
  --preferred-backup-window "03:00-04:00" \
  --preferred-maintenance-window "sun:04:00-sun:05:00" \
  --storage-type gp2 \
  --deletion-protection
```

### 3. Create AWS Secrets Manager Secrets

```bash
# Create database secret
aws secretsmanager create-secret \
  --name springboot/database \
  --description "Database credentials for Spring Boot app" \
  --secret-string '{
    "username": "dbadmin",
    "password": "'$(openssl rand -base64 32)'",
    "host": "springboot-db.xxxxx.us-west-2.rds.amazonaws.com",
    "port": 5432,
    "dbname": "springboot"
  }'

# Create JWT secret
aws secretsmanager create-secret \
  --name springboot/jwt \
  --description "JWT secret for Spring Boot app" \
  --secret-string '{
    "secret": "'$(openssl rand -base64 64)'"
  }'
```

---

## 🔒 Phase 2: Security Implementation

### 1. Create Secure Dockerfile

```dockerfile
# Multi-stage build for security
FROM openjdk:17-jdk-slim AS builder

# Install build dependencies
RUN apt-get update && apt-get install -y \
    maven \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build application
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jre-slim

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Install curl for health checks
RUN apt-get update && apt-get install -y curl \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy application from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Application startup
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Create Kubernetes RBAC Configuration

```yaml
# rbac.yaml
apiVersion: v1
kind: ServiceAccount
metadata:
  name: springboot-sa
  namespace: default
  annotations:
    eks.amazonaws.com/role-arn: arn:aws:iam::ACCOUNT:role/springboot-role
---
apiVersion: rbac.authorization.k8s.io/v1
kind: Role
metadata:
  namespace: default
  name: springboot-role
rules:
- apiGroups: [""]
  resources: ["pods", "services"]
  verbs: ["get", "list", "watch"]
- apiGroups: ["apps"]
  resources: ["deployments"]
  verbs: ["get", "list", "watch"]
---
apiVersion: rbac.authorization.k8s.io/v1
kind: RoleBinding
metadata:
  name: springboot-role-binding
  namespace: default
subjects:
- kind: ServiceAccount
  name: springboot-sa
  namespace: default
roleRef:
  kind: Role
  name: springboot-role
  apiGroup: rbac.authorization.k8s.io
```

### 3. Create Network Policies

```yaml
# network-policy.yaml
apiVersion: networking.k8s.io/v1
kind: NetworkPolicy
metadata:
  name: springboot-network-policy
  namespace: default
spec:
  podSelector:
    matchLabels:
      app: springboot-app
  policyTypes:
  - Ingress
  - Egress
  ingress:
  - from:
    - namespaceSelector:
        matchLabels:
          name: ingress-nginx
    ports:
    - protocol: TCP
      port: 8080
  egress:
  - to:
    - namespaceSelector:
        matchLabels:
          name: database
    ports:
    - protocol: TCP
      port: 5432
  - to: []
    ports:
    - protocol: TCP
      port: 443
    - protocol: TCP
      port: 80
```

---

## 📦 Phase 3: Application Deployment

### 1. Create ConfigMap for Application Configuration

```yaml
# configmap.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: springboot-config
  namespace: default
data:
  application.yml: |
    spring:
      application:
        name: springboot-grpc-rest-demo
      
      datasource:
        url: jdbc:postgresql://${DB_HOST}:5432/${DB_NAME}
        driverClassName: org.postgresql.Driver
        hikari:
          maximum-pool-size: 10
          minimum-idle: 5
          connection-timeout: 30000
          idle-timeout: 600000
          max-lifetime: 1800000
      
      jpa:
        database-platform: org.hibernate.dialect.PostgreSQLDialect
        hibernate:
          ddl-auto: validate
        show-sql: false
        properties:
          hibernate:
            dialect: org.hibernate.dialect.PostgreSQLDialect
    
    server:
      port: 8080
    
    jwt:
      expiration: 86400000
    
    security:
      cors:
        allowed-origins: ${CORS_ALLOWED_ORIGINS:https://yourdomain.com}
      rate-limit:
        requests-per-minute: 100
      account-lockout:
        max-attempts: 5
        lock-duration-minutes: 15
    
    logging:
      level:
        root: INFO
        com.demo.springboot: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 2. Create Deployment Configuration

```yaml
# deployment.yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-app
  namespace: default
  labels:
    app: springboot-app
spec:
  replicas: 3
  selector:
    matchLabels:
      app: springboot-app
  template:
    metadata:
      labels:
        app: springboot-app
    spec:
      serviceAccountName: springboot-sa
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        runAsGroup: 1000
        fsGroup: 1000
      containers:
      - name: springboot-app
        image: ${ECR_REPOSITORY}:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        - name: DB_HOST
          valueFrom:
            secretKeyRef:
              name: springboot-database-secret
              key: host
        - name: DB_NAME
          valueFrom:
            secretKeyRef:
              name: springboot-database-secret
              key: dbname
        - name: DB_USERNAME
          valueFrom:
            secretKeyRef:
              name: springboot-database-secret
              key: username
        - name: DB_PASSWORD
          valueFrom:
            secretKeyRef:
              name: springboot-database-secret
              key: password
        - name: JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: springboot-jwt-secret
              key: secret
        - name: CORS_ALLOWED_ORIGINS
          value: "https://yourdomain.com"
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
      volumes:
      - name: config-volume
        configMap:
          name: springboot-config
```

### 3. Create Service Configuration

```yaml
# service.yaml
apiVersion: v1
kind: Service
metadata:
  name: springboot-service
  namespace: default
  labels:
    app: springboot-app
spec:
  type: ClusterIP
  ports:
  - port: 80
    targetPort: 8080
    protocol: TCP
  selector:
    app: springboot-app
```

### 4. Create Ingress Configuration

```yaml
# ingress.yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: springboot-ingress
  namespace: default
  annotations:
    kubernetes.io/ingress.class: alb
    alb.ingress.kubernetes.io/scheme: internet-facing
    alb.ingress.kubernetes.io/target-type: ip
    alb.ingress.kubernetes.io/listen-ports: '[{"HTTP": 80}, {"HTTPS": 443}]'
    alb.ingress.kubernetes.io/certificate-arn: arn:aws:acm:us-west-2:ACCOUNT:certificate/CERT-ID
    alb.ingress.kubernetes.io/ssl-redirect: '443'
    alb.ingress.kubernetes.io/security-groups: sg-xxxxx
spec:
  rules:
  - host: api.yourdomain.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: springboot-service
            port:
              number: 80
```

---

## 📊 Phase 4: Monitoring and Observability

### 1. Deploy Prometheus Stack

```bash
# Add Prometheus Helm repository
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

# Install Prometheus stack
helm install prometheus prometheus-community/kube-prometheus-stack \
  --namespace monitoring \
  --create-namespace \
  --set grafana.enabled=true \
  --set prometheus.prometheusSpec.retention=7d
```

### 2. Configure CloudWatch Logging

```yaml
# cloudwatch-config.yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: cloudwatch-config
  namespace: default
data:
  cwagentconfig.json: |
    {
      "logs": {
        "metrics_collected": {
          "kubernetes": {
            "cluster_name": "springboot-demo",
            "metrics_collection_interval": 60
          }
        },
        "force_flush_interval": 5
      }
    }
---
apiVersion: apps/v1
kind: DaemonSet
metadata:
  name: cloudwatch-agent
  namespace: default
spec:
  selector:
    matchLabels:
      name: cloudwatch-agent
  template:
    metadata:
      labels:
        name: cloudwatch-agent
    spec:
      containers:
      - name: cloudwatch-agent
        image: amazon/cloudwatch-agent:latest
        volumeMounts:
        - name: cwagentconfig
          mountPath: /etc/cwagentconfig
        - name: rootfs
          mountPath: /rootfs
          readOnly: true
        - name: dockersock
          mountPath: /var/run/docker.sock
          readOnly: true
        - name: varlibdocker
          mountPath: /var/lib/docker
          readOnly: true
        - name: containerdsock
          mountPath: /run/containerd/containerd.sock
          readOnly: true
        - name: sys
          mountPath: /sys
          readOnly: true
        - name: devdisk
          mountPath: /dev/disk
          readOnly: true
      volumes:
      - name: cwagentconfig
        configMap:
          name: cloudwatch-config
      - name: rootfs
        hostPath:
          path: /
      - name: dockersock
        hostPath:
          path: /var/run/docker.sock
      - name: varlibdocker
        hostPath:
          path: /var/lib/docker
      - name: containerdsock
        hostPath:
          path: /run/containerd/containerd.sock
      - name: sys
        hostPath:
          path: /sys
      - name: devdisk
        hostPath:
          path: /dev/disk
```

### 3. Create Horizontal Pod Autoscaler

```yaml
# hpa.yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: springboot-hpa
  namespace: default
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: springboot-app
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
  behavior:
    scaleDown:
      stabilizationWindowSeconds: 300
      policies:
      - type: Percent
        value: 10
        periodSeconds: 60
    scaleUp:
      stabilizationWindowSeconds: 60
      policies:
      - type: Percent
        value: 50
        periodSeconds: 60
```

---

## 🔄 Phase 5: CI/CD Pipeline

### 1. Create GitHub Actions Workflow

```yaml
# .github/workflows/deploy.yml
name: Deploy to EKS

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

env:
  AWS_REGION: us-west-2
  EKS_CLUSTER_NAME: springboot-demo
  ECR_REPOSITORY: springboot-app

jobs:
  security-scan:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Run Trivy vulnerability scanner
      uses: aquasecurity/trivy-action@master
      with:
        image-ref: ${{ env.ECR_REPOSITORY }}:${{ github.sha }}
        format: 'sarif'
        output: 'trivy-results.sarif'
    
    - name: Upload Trivy scan results
      uses: github/codeql-action/upload-sarif@v2
      with:
        sarif_file: 'trivy-results.sarif'

  build-and-push:
    needs: security-scan
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Login to Amazon ECR
      id: login-ecr
      uses: aws-actions/amazon-ecr-login@v1
    
    - name: Build, tag, and push image to Amazon ECR
      env:
        ECR_REGISTRY: ${{ steps.login-ecr.outputs.registry }}
        IMAGE_TAG: ${{ github.sha }}
      run: |
        docker build -t $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG .
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG
        docker tag $ECR_REGISTRY/$ECR_REPOSITORY:$IMAGE_TAG $ECR_REGISTRY/$ECR_REPOSITORY:latest
        docker push $ECR_REGISTRY/$ECR_REPOSITORY:latest

  deploy:
    needs: build-and-push
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    
    - name: Configure AWS credentials
      uses: aws-actions/configure-aws-credentials@v2
      with:
        aws-access-key-id: ${{ secrets.AWS_ACCESS_KEY_ID }}
        aws-secret-access-key: ${{ secrets.AWS_SECRET_ACCESS_KEY }}
        aws-region: ${{ env.AWS_REGION }}
    
    - name: Update kube config
      run: aws eks update-kubeconfig --name ${{ env.EKS_CLUSTER_NAME }} --region ${{ env.AWS_REGION }}
    
    - name: Deploy to EKS
      run: |
        kubectl apply -f k8s/
        kubectl set image deployment/springboot-app springboot-app=${{ env.ECR_REPOSITORY }}:${{ github.sha }}
        kubectl rollout status deployment/springboot-app
```

---

## 🧪 Phase 6: Testing and Validation

### 1. Create Load Testing Script

```bash
#!/bin/bash
# load-test.sh

echo "🚀 Starting Load Test for Spring Boot Application"

# Test endpoints
ENDPOINTS=(
  "/actuator/health"
  "/api/products"
  "/api/users"
  "/auth/login"
)

# Load test parameters
CONCURRENT_USERS=50
DURATION=300  # 5 minutes
BASE_URL="https://api.yourdomain.com"

echo "📊 Load Test Configuration:"
echo "  - Concurrent Users: $CONCURRENT_USERS"
echo "  - Duration: $DURATION seconds"
echo "  - Base URL: $BASE_URL"

# Run load test with k6
k6 run --vus $CONCURRENT_USERS --duration ${DURATION}s << EOF
import http from 'k6/http';
import { check, sleep } from 'k6';

export default function () {
  const endpoints = [
    '/actuator/health',
    '/api/products',
    '/api/users',
    '/auth/login'
  ];
  
  const randomEndpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
  const url = '${BASE_URL}' + randomEndpoint;
  
  const response = http.get(url);
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  sleep(1);
}
EOF
```

### 2. Create Security Testing Script

```bash
#!/bin/bash
# security-test.sh

echo "🔒 Running Security Tests"

# Test endpoints
BASE_URL="https://api.yourdomain.com"

# Test 1: SQL Injection
echo "Testing SQL Injection protection..."
curl -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"test","price":"0; DROP TABLE products;--"}' \
  -w "Status: %{http_code}\n"

# Test 2: XSS Protection
echo "Testing XSS protection..."
curl -X POST "$BASE_URL/api/products" \
  -H "Content-Type: application/json" \
  -d '{"name":"<script>alert(\"xss\")</script>","price":100}' \
  -w "Status: %{http_code}\n"

# Test 3: Rate Limiting
echo "Testing Rate Limiting..."
for i in {1..20}; do
  curl -X GET "$BASE_URL/api/products" \
    -w "Request $i: %{http_code}\n"
done

# Test 4: Authentication
echo "Testing Authentication..."
curl -X GET "$BASE_URL/api/users" \
  -w "Unauthenticated: %{http_code}\n"

# Test 5: CORS
echo "Testing CORS..."
curl -X GET "$BASE_URL/api/products" \
  -H "Origin: https://malicious-site.com" \
  -w "CORS: %{http_code}\n"
```

---

## 📈 Phase 7: Performance Optimization

### 1. JVM Optimization

```yaml
# Add to deployment.yaml container spec
env:
- name: JAVA_OPTS
  value: >-
    -XX:+UseG1GC
    -XX:MaxGCPauseMillis=200
    -XX:+UnlockExperimentalVMOptions
    -XX:+UseContainerSupport
    -XX:InitialRAMPercentage=50.0
    -XX:MaxRAMPercentage=75.0
    -XX:+HeapDumpOnOutOfMemoryError
    -XX:HeapDumpPath=/tmp
    -Djava.security.egd=file:/dev/./urandom
```

### 2. Database Connection Pooling

```yaml
# Add to application.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      leak-detection-threshold: 60000
```

---

## 🎯 Deployment Checklist

### Pre-Deployment
- [ ] AWS EKS cluster created with security best practices
- [ ] RDS database configured with encryption
- [ ] Secrets stored in AWS Secrets Manager
- [ ] ECR repository created
- [ ] IAM roles and policies configured
- [ ] Network policies implemented
- [ ] RBAC configured

### Deployment
- [ ] Application containerized with secure Dockerfile
- [ ] Images pushed to ECR
- [ ] Kubernetes manifests applied
- [ ] Ingress controller configured
- [ ] SSL certificate installed
- [ ] Monitoring stack deployed
- [ ] Load balancer configured

### Post-Deployment
- [ ] Health checks passing
- [ ] Load testing completed
- [ ] Security testing passed
- [ ] Monitoring alerts configured
- [ ] Backup procedures tested
- [ ] Documentation updated
- [ ] Team training completed

---

## 🚨 Troubleshooting Guide

### Common Issues and Solutions

1. **Pod Startup Issues**
```bash
# Check pod logs
kubectl logs -f deployment/springboot-app

# Check pod events
kubectl describe pod -l app=springboot-app

# Check resource usage
kubectl top pods
```

2. **Database Connection Issues**
```bash
# Test database connectivity
kubectl exec -it deployment/springboot-app -- curl -v telnet://springboot-db:5432

# Check secrets
kubectl get secrets
kubectl describe secret springboot-database-secret
```

3. **Network Issues**
```bash
# Check network policies
kubectl get networkpolicies
kubectl describe networkpolicy springboot-network-policy

# Test connectivity
kubectl exec -it deployment/springboot-app -- curl -v http://springboot-service
```

---

## 📞 Support and Maintenance

### Regular Maintenance Tasks
- [ ] Weekly security updates
- [ ] Monthly backup testing
- [ ] Quarterly disaster recovery drills
- [ ] Annual security audits
- [ ] Continuous monitoring and alerting

### Contact Information
- **DevOps Team:** devops@yourcompany.com
- **Security Team:** security@yourcompany.com
- **AWS Support:** AWS Support Plan
- **Documentation:** Internal Wiki

---

**Deployment Guide Version:** 1.0  
**Last Updated:** January 2025  
**Next Review:** March 2025 