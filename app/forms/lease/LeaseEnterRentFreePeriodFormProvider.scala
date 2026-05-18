/*
 * Copyright 2026 HM Revenue & Customs
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

package forms.lease

import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.validation.{Constraint, Invalid, Valid}

import javax.inject.Inject
import scala.util.Try

class LeaseEnterRentFreePeriodFormProvider @Inject() extends Mappings {

  private val maxValueInt = 99
  private val minValueInt = 1
  private val invalidKey  = "lease.enterRentFreePeriod.error.invalid"

  private val numericOnly: Constraint[String] =
    Constraint { value =>
      if (value.matches("""^-?\d+$""")) Valid
      else Invalid(invalidKey)
    }
  
  private def safeToInt(s: String): Int =
    Try(s.toInt).getOrElse {
      if (s.startsWith("-")) Int.MinValue else Int.MaxValue
    }

  def apply(): Form[Int] =
    Form(
      "value" -> text("lease.enterRentFreePeriod.error.required")
        .transform[String](_.replace(" ", "").replace(",", ""), identity)
        .verifying(numericOnly)
        .transform[Int](safeToInt, _.toString)
        .verifying(firstError(
          maximumValue(maxValueInt, "lease.enterRentFreePeriod.error.maxValue"),
          minimumValue(minValueInt, "lease.enterRentFreePeriod.error.minValue")
        ))
    )
}