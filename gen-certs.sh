#!/bin/bash

set -e

KEYSTORE_PASS="password"
TRUSTSTORE_PASS="changeit"
VALIDITY=365
OUTPUT_DIR="tls"

mkdir -p "$OUTPUT_DIR"

HOSTNAMES=(
  "users.ourorg0"
  "messages0.ourorg0"
  "messages1.ourorg0"
  "messages2.ourorg0"
  "users.ourorg1"
  "messages0.ourorg1"
  "messages1.ourorg1"
  "messages2.ourorg1"
  "users.ourorg2"
  "messages.ourorg2"
)

echo "=== Step 1: Generate a keystore for each server ==="
for HOST in "${HOSTNAMES[@]}"; do
  echo "  Generating keystore for $HOST..."
  keytool -ext SAN=dns:${HOST} \
          -genkeypair \
          -alias ${HOST} \
          -keyalg RSA \
          -validity ${VALIDITY} \
          -keystore ${OUTPUT_DIR}/${HOST}.ks \
          -storetype pkcs12 \
          -storepass ${KEYSTORE_PASS} \
          -keypass ${KEYSTORE_PASS} \
          -dname "CN=${HOST}, OU=SD, O=NOVA, C=PT" \
          -noprompt 2>/dev/null
  echo "    -> Created tls/${HOST}.ks"
done

echo ""
echo "=== Step 2: Create truststore (JKS format with changeit password) ==="
# Create an empty JKS truststore
keytool -genkeypair \
        -alias temp \
        -keyalg RSA \
        -keystore ${OUTPUT_DIR}/truststore.ks \
        -storetype JKS \
        -storepass ${TRUSTSTORE_PASS} \
        -keypass ${TRUSTSTORE_PASS} \
        -dname "CN=temp" \
        -noprompt 2>/dev/null
keytool -delete \
        -alias temp \
        -keystore ${OUTPUT_DIR}/truststore.ks \
        -storepass ${TRUSTSTORE_PASS} \
        -noprompt 2>/dev/null
echo "  -> Created tls/truststore.ks (JKS, password: changeit)"

echo ""
echo "=== Step 3: Import server certificates into truststore ==="
for HOST in "${HOSTNAMES[@]}"; do
  echo "  Processing $HOST..."
  keytool -exportcert \
          -alias ${HOST} \
          -keystore ${OUTPUT_DIR}/${HOST}.ks \
          -storepass ${KEYSTORE_PASS} \
          -file ${OUTPUT_DIR}/${HOST}.cert \
          -noprompt 2>/dev/null

  keytool -importcert \
          -file ${OUTPUT_DIR}/${HOST}.cert \
          -alias ${HOST} \
          -keystore ${OUTPUT_DIR}/truststore.ks \
          -storetype JKS \
          -storepass ${TRUSTSTORE_PASS} \
          -noprompt 2>/dev/null

  rm -f ${OUTPUT_DIR}/${HOST}.cert
  echo "    -> Done"
done

echo ""
echo "=== Done! All .ks files are in ./tls/ ==="