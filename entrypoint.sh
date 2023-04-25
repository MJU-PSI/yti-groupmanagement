#!/bin/sh
set -e

# Extract from JAR file
jar -xf yti-groupmanagement.jar static/index.html
jar -xf yti-groupmanagement.jar static/configuration/configuration.json
jar -xf yti-groupmanagement.jar static/configuration/configuration.template.json

# It is possible to overwrite configuration via environment variables.
envsubst < ./static/configuration/configuration.template.json > ./static/configuration/configuration.json

# Replace base href in index.html
if [[ $ANGULAR_BASE_HREF != */ ]] # * is used for pattern matching
then
  ANGULAR_BASE_HREF="${ANGULAR_BASE_HREF}/";
  sed -i 's#<base href="./">#<base href="'"${ANGULAR_BASE_HREF}"'">#' ./static/index.html
fi

# Set read permissions for everyone on the file
chmod +r ./static/index.html
chmod +r ./static/configuration/configuration.json

# Update JAR file
jar -uf yti-groupmanagement.jar static/index.html
jar -uf yti-groupmanagement.jar static/configuration/configuration.json

rm -rf ./static

/bin/bash /bootstrap.sh yti-groupmanagement.jar -j -Djava.security.egd=file:/dev/./urandom