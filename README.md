# sdlt-filing-frontend

This is the new sdlt-filing-frontend repository

## Running the service

Service Manager: `sm2 --start SDLT_ALL`

To run all tests and coverage: `sbt clean compile coverage test it/test coverageOff coverageReport`

To start the server locally on `port 10910`: `sbt run`

## To run the service in test-only mode

Run the command: `sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes`

This allows access to the following test routes:

```
/stamp-duty-land-tax-agent/manage-agents/test-only/session/set
/stamp-duty-land-tax-agent/manage-agents/test-only/session/clear
```

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
├── controllers/[domain]/   # e.g. controllers/vendor
├── models/[domain]/        # e.g. models/vendor
├── forms/[domain]/         # e.g. forms/vendor
└── views/[domain]/         # e.g. views/vendor
```

### License

This code is open source software licensed under
the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").