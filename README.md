# SDLTC Frontend
=============

[![Build Status](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)

## Release Notes

### Node version
Requires node version 21.6.2 to run locally. You can install [NVM](https://github.com/creationix/nvm) to manage local node versions.

### Running unit tests
To simulate the building of the application by Jenkins it is advisable to execute unit tests using the shell script:

```
build-test.sh
```

### All tests and checks

> `sbt runAllChecks`

This is a sbt command alias specific to this project. It will run

- clean
- compile
- unit tests
- integration tests
- and produce a coverage report.

You can view the coverage report in the browser by pasting the generated url.

#### Installing sbt plugin to check for library updates.
To check for dependency updates locally you will need to create this file locally ~/.sbt/1.0/plugins/sbt-updates.sbt
and paste - addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.6.3") - into the file.
Then run:

> `sbt dependencyUpdates `

To view library update suggestions - this does not cover sbt plugins.
It is not advised to install the plugin for the project.

### Running the SDLTC Frontend
Run SDLTC Frontend App on port 9953 from the console at /sdltc-frontend level with:  
```
sbt run
```

If the above fails it may be necessary to clean and then run
```
sbt clean compile dist
```

you can then access the service in a browser at:
```
http://localhost:9953/calculate-stamp-duty-land-tax
```

### Start dependencies via Service Manager:  
```
sm --start SDLTC_ALL -r
sm --start SDLTC_DEP -r (doesn't start up the frontend.)
```

### Troubleshooting
For Mac

if you have any problem try the following steps:

1. Make sure you have the correct `node` version (i.e. v21.6.2): ```node -v```

2. If you don't, do the followings in order:

    I.``` brew uninstall node```

    II.``` nvm install v21.6.2```

    III.``` nvm use v21.6.2```

## Maintaining Libraries and Reproducible Builds


### Building and Assembling the Project:
- When building and assembling the project, we solely rely on the provided package-lock.json.
- This means using the npm ci command instead of npm install.

### npm ci:

- Installs dependencies based on the package-lock.json file.
- Ensures that the exact versions specified in package-lock.json are installed.

### Updating Libraries:
- When we want to update libraries, we do so consciously.
- Run npm install to update the dependencies according to the version ranges specified in package.json.
- After updating, test the project to ensure everything works correctly with the new versions.
- Once satisfied, we commit the updated package-lock.json file into version control.

### Running the SDLTC Frontend Rebuild
Run SDLTC Frontend App on port 9953 from the console at /sdltc-frontend level with:
```
sbt run -Dapplication.router=scalabuild.Routes

or 

sbt run play.http.router=scalabuild.Routes

or

 sbt run -Dconfig.resource=application.scalabuild.conf   
```
You will be able to access the first page at http://localhost:9953/calculate-stamp-duty-land-tax/intro

```
curl 'http://localhost:9000/calculate-stamp-duty-land-tax/calculate' \
-H 'Accept: application/json, text/plain, */*' \
-H 'Content-Type: application/json;charset=UTF-8' \
--data-raw '{"holdingType":"Freehold","propertyType":"Residential","effectiveDateDay":1,"effectiveDateMonth":1,"effectiveDateYear":2020,"highestRent":0,"premium":"550000","propertyDetails":{"individual":"Yes","twoOrMoreProperties":"No"},"firstTimeBuyer":"No"}'

```
    