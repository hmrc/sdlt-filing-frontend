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

package services.purchaser

import models.{Mode, UserAnswers}
import models.purchaser.WhoIsMakingThePurchase
import navigation.Navigator
import pages.purchaser._
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserSessionService @Inject()(sessionRepository: SessionRepository,
                                        navigator: Navigator) {
  
  
  val listOfIndividualPages = Seq(
    NameOfPurchaserPage,
    PurchaserDateOfBirthPage,
    PurchaserFormOfIdIndividualPage,
    PurchaserNationalInsurancePage,
    DoesPurchaserHaveNIPage
  )

  val listOfCompanyPages = Seq(
    CompanyFormOfIdPage,
    PurchaserUTRPage,
    PurchaserTypeOfCompanyPage,
    RegistrationNumberPage,
    NameOfPurchaserPage
  )

  def companyOrIndividualPurchaserRemoveFromSession(userAnswers: UserAnswers,
                                      value: WhoIsMakingThePurchase,
                                      mode: Mode)(
                                     implicit ex: ExecutionContext,
                                     hc: HeaderCarrier
  ): Future[Result] = {
    val listToRemove =  if(value == WhoIsMakingThePurchase.Individual) listOfCompanyPages else listOfIndividualPages
    
    for {
      updatedAnswers <- Future.fromTry(userAnswers.set(WhoIsMakingThePurchasePage, value))
      removeDetails <- Future.fromTry(updatedAnswers.removeAll(listToRemove))
      _ <- sessionRepository.set(removeDetails)
    } yield Redirect(navigator.nextPage(WhoIsMakingThePurchasePage, mode, updatedAnswers))
  }
  
}
