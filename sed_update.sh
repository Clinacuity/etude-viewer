#!/bin/bash


sed -i s/BUILD_REPLACE/$BUILD_NUMBER/g src/main/resources/application.properties
sed -i s/VER_REPLACE/$VERSION_NUMBER/g src/main/resources/application.properties

