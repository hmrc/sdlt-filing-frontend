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
sm --start SDLTC_ALL -r
sm --start SDLTC_DEP -r (doesn't start up the frontend.)
```

### Accessing the ASSSETS_FRONTEND via nginx (This is one time activity, switch user to admin user and complete the following)

Install nginx
```
sudo apt-get update
sudo apt-get install nginx
```  

For Mac
```
brew install nginx

(you may have to start nginx manually on mac)
sudo nginx
```

Open default file to edit
```
sudo nano /etc/nginx/sites-enabled/default   
```

For Mac
```
sudo nano /usr/local/etc/nginx/nginx.conf
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

(For Mac you might need to change the server port to listen for 80 from 8080)


Restart nginx
```
sudo /etc/init.d/nginx restart
```

For Mac
```
sudo nginx -s stop : To stop
sudo nginx : To start
```
Access SDLTC via nginx
```
http://localhost/calculate-stamp-duty-land-tax/
```

### Troubleshooting
For Mac

if you have any problem try the following steps:

1. Make sure you have the correct `node` version (i.e. v4.4.5): ```node -v```

2. If you don't, do the followings in order:

    I.``` brew uninstall node```

    II.``` nvm install v4.4.5```

    III.``` nvm use v4.4.5```

3. Check if you have `phantomjs` installed: ```which phantomjs```

4. If you don't have `phantomjs`, install it:

    ```brew cask install phantomjs```

5. Add phantomjs to your bash profile:

    ```export PHANTOMJS_BIN=/usr/local/bin/phantomjs``` (or where you have installed it)
