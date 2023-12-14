#!/bin/bash

rm -rf calc-assets/node_modules
set +x
. $NVM_DIR/nvm.sh
nvm use 16.10.0
sbt -mem 3000 clean test it/test distTgz