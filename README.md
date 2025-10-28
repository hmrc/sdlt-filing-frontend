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

    