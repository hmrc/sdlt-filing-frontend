/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.i18n.Lang
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.RequestHeader
import play.api.test.FakeRequest
import play.api.test.Helpers.GET

class FrontendAppConfigSpec extends AnyFreeSpec with Matchers with GuiceOneAppPerSuite {

  override def fakeApplication(): Application =
    new GuiceApplicationBuilder()
      .configure(
        "host" -> "http://localhost:9000",
        "appName" -> "test-app",
        "contact-frontend.url" -> "http://contact-frontend",
        "contact-frontend.host" -> "http://localhost:9250",
        "urls.login" -> "http://localhost:9949/auth-login-stub/gg-sign-in",
        "urls.loginContinue" -> "http://localhost:9000/sdlt-filing-frontend",
        "urls.signOut" -> "http://localhost:9025/gg/sign-out",
        "features.welsh-translation" -> false,
        "features.address-lookup-stub" -> true,
        "microservice.services.address-lookup-frontend.protocol" -> "http",
        "microservice.services.address-lookup-frontend.host" -> "localhost",
        "microservice.services.address-lookup-frontend.port" -> 9876,
        "timeout-dialog.timeout" -> 900,
        "timeout-dialog.countdown" -> 120,
        "mongodb.timeToLiveInSeconds" -> 900,
        "microservice.services.protocol" -> "http",
        "microservice.services.test-service.protocol" -> "https",
        "microservice.services.test-service.host" -> "example.com",
        "microservice.services.test-service.port" -> 9000,
        "microservice.services.another-service.protocol" -> "https",
        "microservice.services.another-service.host" -> "different.com",
        "microservice.services.another-service.port" -> 8080,
        "microservice.services.feedback-frontend.protocol" -> "http",
        "microservice.services.feedback-frontend.host" -> "localhost",
        "microservice.services.feedback-frontend.port" -> 9514,
        "microservice.services.test-key" -> true
      )
      .build()

  private lazy val appConfig = app.injector.instanceOf[FrontendAppConfig]

  "FrontendAppConfig" - {

    "getConfString" - {

      "must return config value when key exists" in {
        val result = appConfig.getConfString("test-service.host", "default-value")

        result mustBe "example.com"
      }

      "must return default value when key does not exist" in {
        val result = appConfig.getConfString("non-existent-key", "default-value")

        result mustBe "default-value"
      }
    }

    "getConfInt" - {

      "must return config value when key exists" in {
        val result = appConfig.getConfInt("test-service.port", 8080)

        result mustBe 9000
      }

      "must return default value when key does not exist" in {
        val result = appConfig.getConfInt("non-existent-key", 8080)

        result mustBe 8080
      }
    }

    "getConfBool" - {

      "must return true when key exists" in {
        val result = appConfig.getConfBool("test-key")

        result mustBe true
      }

      "must return false when key does not exist" in {
        val result = appConfig.getConfBool("non-existent-key")

        result mustBe false
      }
    }

    "baseUrl" - {

      "must construct URL with protocol, host and port from config" in {
        val result = appConfig.baseUrl("test-service")

        result mustBe "https://example.com:9000"
      }

      "must handle different service names" in {
        val result = appConfig.baseUrl("another-service")

        result mustBe "https://different.com:8080"
      }

      "must throw RuntimeException when service config is missing" in {
        val exception = intercept[RuntimeException] {
          appConfig.baseUrl("non-existent-service")
        }

        exception.getMessage must include("Could not find config key")
      }

      "must throw RuntimeException when service port is missing" in {
        val fakeAppNoPort = new GuiceApplicationBuilder()
          .configure(
            "microservice.services.broken-service.protocol" -> "http",
            "microservice.services.broken-service.host" -> "example.com"
          )
          .build()

        val configNoPort = fakeAppNoPort.injector.instanceOf[FrontendAppConfig]
        val exception = intercept[RuntimeException] {
          configNoPort.baseUrl("broken-service")
        }

        exception.getMessage must include("Could not find config key 'broken-service.port'")
       }
    }

    "contactUrl" - {
      "must build the correct contact URL using the request URI" in {
        implicit val request: RequestHeader = FakeRequest(GET, "/test-path")

        val result = appConfig.contactUrl

        result mustBe "http://contact-frontend/contact/beta-feedback?service=sdlt-filing-frontend&backUrl=http://localhost:9000/test-path"
      }
    }

    "languageMap" - {

      "must return map with English and Welsh languages" in {
        val result = appConfig.languageMap

        result must contain("en" -> Lang("en"))
        result must contain("cy" -> Lang("cy"))
        result.size mustBe 2
      }
    }

    "address lookup" - {
      "must return stub URL when stubAddressLookup is true" in {
        val id = "12345"

        val result = appConfig.addressLookupRetrievalUrl(id)

        result mustBe s"${appConfig.sdltStubUrl}/stamp-duty-land-tax-stub/prelim-questions/address-lookup/confirmed?id=$id"
      }

      "must return stub URL when stubAddressLookup is false" in {
        val appAddressLookupFalse = new GuiceApplicationBuilder()
          .configure(
            "features.address-lookup-stub" -> false,
            "microservice.services.address-lookup-frontend.protocol" -> "http",
            "microservice.services.address-lookup-frontend.host" -> "localhost",
            "microservice.services.address-lookup-frontend.port" -> 9876
          ).build()

        val config = appAddressLookupFalse.injector.instanceOf[FrontendAppConfig]

        val id = "12345"
        val result = config.addressLookupRetrievalUrl(id)

        result mustBe "http://localhost:9876/api/v2/confirmed?id=12345"
      }

      "must return stub journey URL when stubAddressLookup is true" in {
        val result = appConfig.addressLookupJourneyUrl

        result mustBe s"${appConfig.sdltStubUrl}/stamp-duty-land-tax-stub/prelim-questions/address-lookup/init"
      }

      "must return stub journey URL when stubAddressLookup is false" in {
        val appAddressLookupFalse = new GuiceApplicationBuilder()
          .configure(
            "features.address-lookup-stub" -> false,
            "microservice.services.address-lookup-frontend.protocol" -> "http",
            "microservice.services.address-lookup-frontend.host" -> "localhost",
            "microservice.services.address-lookup-frontend.port" -> 9876
          ).build()

        val config = appAddressLookupFalse.injector.instanceOf[FrontendAppConfig]

        val result = config.addressLookupJourneyUrl

        result mustBe "http://localhost:9876/api/v2/init"
      }


    }
  }
}