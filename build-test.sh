#!/bin/bash

rm -rf calc-assets/node_modules
set +x
. $NVM_DIR/nvm.sh
nvm use 21.6.2
sbt -mem 3000 clean test it/test distTgz