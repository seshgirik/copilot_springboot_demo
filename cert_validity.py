#!/bin/bash

# Define the namespace and secret names
NAMESPACE="mobius"
SECRET_NAMES=("mobius-istio-cert" "mobius-sse-cert")

# Function to extract and decode the certificate content
extract_and_validate_certificate() {
  SECRET_NAME=$1
  CERT_KEY=$2

  echo "Fetching certificate from secret: $SECRET_NAME"
  # Print the kubectl command to fetch the secret
  echo "Running command to fetch secret data:"
  echo "kubectl get secret \"$SECRET_NAME\" -n \"$NAMESPACE\" -o jsonpath=\"{.data.tls\.crt}\""

  # Corrected: Extract the certificate content using the proper key
  # CERT_CONTENT=$(kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" -o jsonpath="{.data}")
  CERT_CONTENT=$(kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" -o jsonpath="{.data.tls\.crt}")

  if [ -z "$CERT_CONTENT" ]; then
    echo "Error: Certificate content not found in secret '$SECRET_NAME'."
    return 1
  fi

  # Decode the Base64 encoded certificate content
  echo "Decoding the certificate...$CERT_CONTENT"
  CERT_DECODED=$(echo "$CERT_CONTENT" | base64 --decode)

  # Save the decoded certificate to a temporary file
  TEMP_CERT_FILE=$(mktemp)
  echo "$CERT_DECODED" > "$TEMP_CERT_FILE"

  # Validate the certificate using openssl and display the validity period
  echo "Validating the certificate with openssl..."
  openssl x509 -in "$TEMP_CERT_FILE" -noout -text | grep -A 1 "Validity"

  # No cleanup needed, file is left for inspection
}

# Loop through the secrets and extract the certificates
for SECRET_NAME in "${SECRET_NAMES[@]}"; do
  extract_and_validate_certificate "$SECRET_NAME" "tls.crt"
done




# kubectl get secret mobius-istio-cert -n mobius -o jsonpath="{.data.tls\.crt}"