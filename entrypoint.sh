#!/bin/sh
set -e

jar -xf yti-groupmanagement.jar static/configuration/configuration.json
jar -xf yti-groupmanagement.jar static/configuration/configuration.template.json

# It is possible to overwrite configuration via environment variables.
envsubst < ./static/configuration/configuration.template.json > ./static/configuration/configuration.json

# Set read permissions for everyone on the file
chmod +r ./static/configuration/configuration.json

jar -uf yti-groupmanagement.jar static/configuration/configuration.json

rm -rf ./static

/bin/bash /bootstrap.sh yti-groupmanagement.jar -j -Djava.security.egd=file:/dev/./urandom