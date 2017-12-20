# SDLTC Frontend
=============

[![Build Status](https://ci-dev.tax.service.gov.uk/buildStatus/icon?job=sdltc-frontend)](https://ci-dev.tax.service.gov.uk/job/sdltc-frontend/)

## Release Notes
### Change javascript version number for every release, update below files with next version number (in snippet below its version 1):  

#### conf/application.conf
"assets.cache./public/javascript/v```1```-calc.js"="public, max-age=31536000"  
"assets.cache./public/javascript/v```1```-calc-templates.js"="public, max-age=31536000"   

#### calc-assets/gulpfile.js/config.js
var jsVersion = "v```1```";

#### app/journey/views/index.scala.html
ga('send', 'event', 'asset-version', 'v```1```', 'calc-javascripts');  
script src="javascript/v```1```-calc.js"  
script src="javascript/v```1```-calc-templates.js"


### Node version
Requires node version 4.4.5 to run locally. You can install [NVM](https://github.com/creationix/nvm) to manage local node versions.


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


