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

package controllers.taxCalculation.freeholdTaxCalculated

import com.google.inject.Singleton
import connectors.errorLog
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import controllers.routes.ReturnTaskListController
import controllers.taxCalculation.PenaltiesAndInterestExtension
import forms.taxCalculation.PenaltiesAndInterestFormProvider
import models.taxCalculation.TaxCalculationFlow.{FreeholdTaxCalculated}
import models.{Mode, PenaltiesAndInterest}
import navigation.Navigator
import pages.taxCalculation.freeholdTaxCalculated.FreeholdTaxCalculatedPenaltiesAndInterestPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.LoggingUtil
import views.html.taxCalculation.AmountWithPenaltiesView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class FreeholdSdltCalculatedPenaltiesAndInterestController @Inject()(
                                                                      override val messagesApi: MessagesApi,
                                                                      identify: IdentifierAction,
                                                                      getData: DataRetrievalAction,
                                                                      requireData: DataRequiredAction,
                                                                      formProvider: PenaltiesAndInterestFormProvider,
                                                                      val controllerComponents: MessagesControllerComponents,
                                                                      sdltCalculationService: SdltCalculationService,
                                                                      navigator: Navigator,
                                                                      view: AmountWithPenaltiesView
                                                                    )(implicit ec: ExecutionContext) extends FrontendBaseController
  with I18nSupport with PenaltiesAndInterestExtension with LoggingUtil {

  private val form: Form[PenaltiesAndInterest] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      validateFlow(request.userAnswers)(FreeholdTaxCalculated) match {
        case None =>
          Ok(view(form, getPageTitle(flow = FreeholdTaxCalculated), postAction(FreeholdTaxCalculated, mode)))
        case Some(firstErrorFound) =>
          errorLog(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onPageLoad] invalid flow state: $firstErrorFound")
          Redirect(ReturnTaskListController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      validateFlow(request.userAnswers)(FreeholdTaxCalculated) match {
        case None =>
          form
            .bindFromRequest()
            .fold(
              formWithErrors =>
                Future.successful(BadRequest(view(formWithErrors, getPageTitle(flow = FreeholdTaxCalculated), postAction(FreeholdTaxCalculated, mode)))),
              {
                yesOrNoSelected =>
                  sdltCalculationService
                    .savePenaltiesAndInterestYesNoAnswer(
                      key = FreeholdTaxCalculatedPenaltiesAndInterestPage,
                      value = yesOrNoSelected)
                    .map { _ =>
                      infoLog(s"[FreeholdSdltCalculatedPenaltiesAndInterestController][onSubmit] userAnswer saved :: redirecting")
                      Redirect(navigator.nextPage(FreeholdTaxCalculatedPenaltiesAndInterestPage, mode, userAnswers = request.userAnswers))
                    }
              }
            )
        case Some(firstErrorFound) =>
          Future.successful(Redirect(ReturnTaskListController.onPageLoad()))
      }
  }

}