# SDLTC Frontend
=============

[![Build Status](https://ci-dev.tax.service.gov.uk/buildStatus/icon?job=sdltc-frontend)](https://ci-dev.tax.service.gov.uk/job/sdltc-frontend/)

## Release Notes

### Node version
Requires node version 4.4.5 to run locally. You can install [NVM](https://github.com/creationix/nvm) to manage local node versions.

### Running unit tests
To simulate the building of the application by Jenkins it is advisable to execute unit tests using the shell script:

```
build-test.sh
```

In order to run this you will need to have installed sbt-bobby as it utilises 'validate' within the script

### Running the SDLTC Frontend
Run SDLTC Frontend App from the console at /sdltc-frontend level with:  
```
sbt "run 9953"
```
    
### Start dependencies via Service Manager:  
```
sm --start ASSETS_FRONTEND
sm --start CONTACT_FRONTEND
```

### Accessing the ASSSETS_FRONTEND via nginx (This is one time activity, switch user to admin user and complete the following)

Install nginx
```
sudo apt-get update
sudo apt-get install nginx
```  

Open default file to edit
```
sudo nano /etc/nginx/sites-enabled/default   
```  

(Modify above file to add below snippet in Default server configuration below default location snippet)

    location /assets {  
            proxy_pass      http://localhost:9032/assets;
    }

    location /template {
            proxy_pass      http://localhost:9953/template;
    }

    location /contact {
            proxy_pass      http://localhost:9250/contact;
    }

    location /calculate-stamp-duty-land-tax {
        proxy_pass      http://localhost:9953/calculate-stamp-duty-land-tax;
	}

Restart nginx  
```  
sudo /etc/init.d/nginx restart
```    


