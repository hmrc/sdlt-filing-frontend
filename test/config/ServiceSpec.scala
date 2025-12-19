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

class ServiceSpec extends AnyFreeSpec with Matchers {

  private val service = Service("localhost", "9000", "http")

  "Service" - {

    "base URL must return correct URL" in {
      service.baseUrl mustBe "http://localhost:9000"
    }

    "toString must return baseURL" in {
      service.toString mustBe "http://localhost:9000"
    }

    "implicit conversion to String must .." in {
      import Service.convertToString
      val str: String = service
      str mustBe "http://localhost:9000"
    }
  }
}
