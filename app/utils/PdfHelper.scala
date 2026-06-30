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

package utils

import models.{CompanyDetails, FullReturn, Transaction}

object PdfHelper {

  def hasSdlt4Answers(fullReturn: FullReturn, propertyType: String): Boolean = {
    hasAnotherFormOfIdAndPlaceOfRegistration(fullReturn) ||
      (Set("02", "03").contains(propertyType) && hasSaleOfBusiness(fullReturn.transaction)) ||
      dependsOnFutureEventOrAgreedToDefer(fullReturn.transaction) ||
      hasCompanyDetails(fullReturn.companyDetails)
  }

  private def hasAnotherFormOfIdAndPlaceOfRegistration(f: FullReturn): Boolean =
    f.purchaser
      .flatMap(_.headOption)
      .exists(p =>
        p.registrationNumber.isDefined &&
          p.placeOfRegistration.isDefined
      )

  private def hasSaleOfBusiness(t: Option[Transaction]): Boolean =
    anyYes(
      t.flatMap(_.includesStock),
      t.flatMap(_.includesGoodwill),
      t.flatMap(_.includesOther),
      t.flatMap(_.includesChattel)
    )

  private def dependsOnFutureEventOrAgreedToDefer(t: Option[Transaction]): Boolean =
    anyYes(
      t.flatMap(_.isDependantOnFutureEvent),
      t.flatMap(_.agreedToDeferPayment)
    )

  private def hasCompanyDetails(c: Option[CompanyDetails]): Boolean =
    anyYes(
      c.flatMap(_.companyTypeBank),
      c.flatMap(_.companyTypeBuilder),
      c.flatMap(_.companyTypeBuildsoc),
      c.flatMap(_.companyTypeCentgov),
      c.flatMap(_.companyTypeIndividual),
      c.flatMap(_.companyTypeInsurance),
      c.flatMap(_.companyTypeLocalauth),
      c.flatMap(_.companyTypeOthercharity),
      c.flatMap(_.companyTypeOthercompany),
      c.flatMap(_.companyTypeOtherfinancial),
      c.flatMap(_.companyTypePartnership),
      c.flatMap(_.companyTypeProperty),
      c.flatMap(_.companyTypePubliccorp),
      c.flatMap(_.companyTypeSoletrader),
      c.flatMap(_.companyTypePensionfund)
    )

  private def anyYes(values: Option[String]*): Boolean =
    values.exists(_.exists(_.equalsIgnoreCase("yes")))
}
