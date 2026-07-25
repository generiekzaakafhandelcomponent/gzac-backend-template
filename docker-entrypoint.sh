#!/bin/sh
set -eu

# Reference entrypoint for the valtimo-backend-template image.
# Import custom CA certificates (e.g. a customer's
# private CA) into a JVM truststore, then launch the application.
#
# Certificates are read from $CUSTOM_CA_DIR (default /custom-ca). Each *.crt, *.pem
# or *.cer file (one certificate per file) is imported into a COPY of the JDK's
# default cacerts, so the app keeps trusting all the default public roots plus the
# custom CA(s). With nothing mounted this is a no-op and the app starts with the
# default truststore.

CUSTOM_CA_DIR="${CUSTOM_CA_DIR:-/custom-ca}"
TRUSTSTORE="${CUSTOM_CA_TRUSTSTORE:-/tmp/cacerts}"
# The copied store is the JDK's default cacerts, whose password is always "changeit".
# keytool needs it to unlock the store, so it is fixed rather than configurable.
STOREPASS="changeit"

# Prefix all entrypoint output so its origin is obvious in the container logs.
log() {
  echo "Docker entrypoint - custom-ca: $*"
}

JAVA_ARGS=""

imported=0
if [ -d "$CUSTOM_CA_DIR" ]; then
  for cert in "$CUSTOM_CA_DIR"/*.crt "$CUSTOM_CA_DIR"/*.pem "$CUSTOM_CA_DIR"/*.cer; do
    [ -f "$cert" ] || continue
    if [ "$imported" -eq 0 ]; then
      log "importing certificates from $CUSTOM_CA_DIR"
      cp "$JAVA_HOME/lib/security/cacerts" "$TRUSTSTORE"
    fi
    alias="$(basename "$cert")"
    alias="${alias%.*}"
    log "  + $alias"
    # Suppress keytool's own "Certificate was added to keystore" line (stdout); the
    # "+ <alias>" line above already reports each import. Errors go to stderr and
    # abort the script via "set -e".
    keytool -importcert -noprompt -trustcacerts \
      -alias "$alias" \
      -file "$cert" \
      -keystore "$TRUSTSTORE" \
      -storepass "$STOREPASS" >/dev/null
    imported=$((imported + 1))
  done
fi

if [ "$imported" -gt 0 ]; then
  JAVA_ARGS="-Djavax.net.ssl.trustStore=$TRUSTSTORE -Djavax.net.ssl.trustStorePassword=$STOREPASS"
else
  log "no certificates in $CUSTOM_CA_DIR, using default truststore"
fi

# Word-splitting of JAVA_ARGS is intentional (two separate -D flags).
# shellcheck disable=SC2086
exec java $JAVA_ARGS -jar /app.jar "$@"
