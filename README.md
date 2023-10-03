# SDLTC Frontend
=============

[![Build Status](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)](https://build.tax.service.gov.uk/job/DDCT%20Live%20Services/job/Stamp%20Duty%20Land%20Tax%20Calculator/job/sdltc-frontend/)

## Release Notes

### Node version
Requires node version 16.10.0 to run locally. You can install [NVM](https://github.com/creationix/nvm) to manage local node versions.

### Running unit tests
To simulate the building of the application by Jenkins it is advisable to execute unit tests using the shell script:

```
build-test.sh
```

### Running the SDLTC Frontend
Run SDLTC Frontend App on port 9953 from the console at /sdltc-frontend level with:  
```
sbt run
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

1. Make sure you have the correct `node` version (i.e. v16.10.0): ```node -v```

2. If you don't, do the followings in order:

    I.``` brew uninstall node```

    II.``` nvm install v16.10.0```

    III.``` nvm use v16.10.0```

    