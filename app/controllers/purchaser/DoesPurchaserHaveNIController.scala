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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.DoesPurchaserHaveNIFormProvider
import models.purchaser.DoesPurchaserHaveNI
import models.{Mode, NormalMode}
import navigation.Navigator
import pages.purchaser.{DoesPurchaserHaveNIPage, NameOfPurchaserPage, PurchaserDateOfBirthPage, PurchaserFormOfIdIndividualPage, PurchaserNationalInsurancePage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.purchaser.DoesPurchaserHaveNIView
import services.purchaser.PurchaserService
import models.purchaser.WhoIsMakingThePurchase
import scala.util.Success

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoesPurchaserHaveNIController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: DoesPurchaserHaveNIFormProvider,
                                       purchaserService: PurchaserService,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: DoesPurchaserHaveNIView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode))

        case Some(purchaserName) =>
          val preparedForm = request.userAnswers.get(DoesPurchaserHaveNIPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }

          purchaserService.continueIfAddingMainPurchaserWithPurchaserTypeCheck(
            purchaserType = WhoIsMakingThePurchase.Individual,
            userAnswers = request.userAnswers,
            continueRoute =  Ok(view(preparedForm, mode, purchaserName.fullName)),
            mode = mode)
        }
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      request.userAnswers.get(NameOfPurchaserPage) match {
        case None =>
          Future.successful(Redirect(controllers.purchaser.routes.NameOfPurchaserController.onPageLoad(NormalMode)))
        case Some(purchaserName) =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, purchaserName.fullName))),

            value =>
              val afterRemovingNino = if (value == DoesPurchaserHaveNI.No) {
                request.userAnswers.remove(PurchaserNationalInsurancePage)
              } else {
                Success(request.userAnswers)
              }

              val afterRemovingDOB = afterRemovingNino.flatMap { answers =>
                if (value == DoesPurchaserHaveNI.No) {
                  answers.remove(PurchaserDateOfBirthPage)
                } else {
                  Success(answers)
                }
              }

              val afterRemovingFormId = afterRemovingDOB.flatMap { answers =>
                if (value == DoesPurchaserHaveNI.Yes) {
                  answers.remove(PurchaserFormOfIdIndividualPage)
                } else {
                  Success(answers)
                }
              }

              for {
                cleaned <- Future.fromTry(afterRemovingFormId)
                updatedAnswers <- Future.fromTry(cleaned.set(DoesPurchaserHaveNIPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value.toString.equals("yes")) {
                  Redirect(navigator.nextPage(DoesPurchaserHaveNIPage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.purchaser.routes.PurchaserFormOfIdIndividualController.onPageLoad(mode))
                }
              }
          )
      }
  }
}
