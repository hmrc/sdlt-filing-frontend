# SDLTC Frontend
=============

## Release Notes
### Change javascript version number for every release, update below files with next version number (in snippet below its version 1):  

#### conf/application.conf
"assets.cache./public/javascript/v```1```-sdltc.js"="public, max-age=31536000"  
"assets.cache./public/javascript/v```1```-sdltc-templates.js"="public, max-age=31536000"   

#### sdltc-assets/gulpfile.js/config.js
var jsVersion = "v```1```";

#### sdltc-assets/index.html
script src="javascript/v```1```-sdltc.js"  
script src="javascript/v```1```-sdltc-templates.js"



### Running the SDLTC Frontend
Run SDLTC Frontend App from the console at /sdltc-frontend level with:  
```
sbt "run 9090"
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
            proxy_pass      http://localhost:9090/template;  
    }  

    location /contact {  
            proxy_pass      http://localhost:9250/contact;  
    }  
      
    location /calculate-stamp-duty-land-tax {  
            proxy_pass      http://localhost:9090/calculate-stamp-duty-land-tax;  
    }  

Restart nginx  
```  
sudo /etc/init.d/nginx restart
```  

Access sdltc via nginx  
```  
http://localhost/calculate-stamp-duty-land-tax/
```
  
