# sdlt-filing-frontend

This is the frontend service responsible for the submission of returns, including both new returns and completion of existing in-progress returns.

For more information please refer to the [documentation](https://confluence.tools.tax.service.gov.uk/spaces/RBD/pages/1081606211/3.+Stamp+Duty+Land+Tax+-+SDLT).

## Running the service
Before starting, you will need to have  [service-manager](https://github.com/hmrc/service-manager) installed/configured

### Dependencies
All dependencies can be found in [AppDependencies.scala](https://github.com/hmrc/sdlt-filing-frontend/blob/main/project/AppDependencies.scala)

### Running locally:
Service Manager:
- Start dependent services `sm2 --start SDLT_ALL`
- Stop this service `sm2 --stop SDLT-FILING-FRONTEND`
- Start the server locally on `port 10910` with `sbt run`

### Testing:
- Run unit tests: `sbt test`
- Run integration tests: `sbt it/test`
- To run all tests and coverage: `sbt clean compile coverage test it/test coverageOff coverageReport`
- To run the service in test-only mode: `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`

## Adding New Pages

### Folder Structure

The project uses domain-based organisation. Each new page should be placed in the appropriate domain folder:

```
app/
├── controllers/[section]/               # e.g. controllers/vendor
├── models/[section]/                    # e.g. models/vendor
├── views/[section]/                     # e.g. views/vendor
├── forms/[section]/                     # e.g. forms/vendor
├── pages/[section]/                     # e.g. pages/vendor
└── viewmodels/checkAnswers/[section]/   # e.g. viewmodels/checkAnswers/vendor
```

```
test/
├── controllers/[section]/   # e.g. controllers/vendor
├── models/[section]/        # e.g. models/vendor
├── forms/[section]/         # e.g. forms/vendor
└── views/[section]/         # e.g. views/vendor
```

### Example routes and messages

```GET  /preliminary-questions/who-is-making-the-purchase  controllers.preliminary.PurchaserIsIndividualController.onPageLoad(mode: Mode = NormalMode)```

Message key (messages.en):

```prelim.purchaserIsIndividual.heading = Who is making the purchase?```


### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").