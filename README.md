# SDLTC Frontend
=============

[![Build Status](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)

## Release Notes

### Running unit tests

```
sbt test
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
This will run the application on port 9953

If the above fails it may be necessary to clean and then run
```
sbt clean compile
```

you can then access the service in a browser at:
```
http://localhost:9953/calculate-stamp-duty-land-tax
```

### Start dependencies via Service Manager:  
```
sm --start SDLTC_ALL 
sm --start SDLTC_DEP (doesn't start up the frontend.)
```

You will be able to access the first page at http://localhost:9953/calculate-stamp-duty-land-tax/intro

The below is an example curl request for the calculate API
```
curl 'http://localhost:9000/calculate-stamp-duty-land-tax/calculate' \
-H 'Accept: application/json, text/plain, */*' \
-H 'Content-Type: application/json;charset=UTF-8' \
--data-raw '{"holdingType":"Freehold","propertyType":"Residential","effectiveDateDay":1,"effectiveDateMonth":1,"effectiveDateYear":2020,"highestRent":0,"premium":"550000","propertyDetails":{"individual":"Yes","twoOrMoreProperties":"No"},"firstTimeBuyer":"No"}'

```
    