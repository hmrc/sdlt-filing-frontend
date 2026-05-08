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

package services.lease

import models.lease.TypeOfLease
import models.UserAnswers
import services.lease.LeaseService._

class LeaseService {

  def leasePropertyLandPropertyValidation(userAnswers: UserAnswers, leaseType: TypeOfLease): Result = {
    val lands =
      userAnswers.fullReturn
        .flatMap(_.land)
        .getOrElse(Seq.empty)

    val propertyTypes =
      lands.flatMap(_.propertyType)

    if(propertyTypes.isEmpty) {
      Valid
    }
    else {
      val propertyType = propertyTypes.head

      (leaseType, propertyType) match {
        case (TypeOfLease.R, "01") | (TypeOfLease.R, "04") => Valid
        case (TypeOfLease.R, _) => InvalidResidentialRule
        case (TypeOfLease.M, "02") => Valid
        case (TypeOfLease.M, _) => InvalidMixedRule
        case (TypeOfLease.N, "03") => Valid
        case (TypeOfLease.N, _) => InvalidNonResidentialRule
      }
    }
  }
}

object LeaseService {

  sealed trait Result

  case object Valid extends Result

  case object InvalidResidentialRule extends Result

  case object InvalidNonResidentialRule extends Result

  case object InvalidMixedRule extends Result
}