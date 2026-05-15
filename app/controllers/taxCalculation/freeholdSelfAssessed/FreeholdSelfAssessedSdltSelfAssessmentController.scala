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

package controllers.taxCalculation.freeholdSelfAssessed

import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.taxCalculation.SdltSelfAssessmentFormProvider
import models.Mode
import models.taxCalculation.TaxCalculationFlow.FreeholdSelfAssessed
import navigation.Navigator
import pages.taxCalculation.freeholdSelfAssessed.FreeholdSelfAssessedAmountPage
import play.api.Logging
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.taxCalculation.shared.SdltSelfAssessmentView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FreeholdSelfAssessedSdltSelfAssessmentController @Inject()(
                                                                 override val messagesApi: MessagesApi,
                                                                 identify: IdentifierAction,
                                                                 getData: DataRetrievalAction,
                                                                 requireData: DataRequiredAction,
                                                                 sdltCalculationService: SdltCalculationService,
                                                                 sessionRepository: SessionRepository,
                                                                 navigator: Navigator,
                                                                 formProvider: SdltSelfAssessmentFormProvider,
                                                                 val controllerComponents: MessagesControllerComponents,
                                                                 view: SdltSelfAssessmentView
                                                               )(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  private val form: Form[String] = formProvider()

  private val sectionKey: String = "site.taxCalculation.freeholdSelfAssessed.section"

  private def postAction(mode: Mode): Call =
    routes.FreeholdSelfAssessedSdltSelfAssessmentController.onSubmit(mode)

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      sdltCalculationService.whenInFlow(FreeholdSelfAssessed) {
        val prepared = request.userAnswers.get(FreeholdSelfAssessedAmountPage).fold(form)(form.fill)
        Ok(view(prepared, postAction(mode), sectionKey))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      sdltCalculationService.whenInFlowAsync(FreeholdSelfAssessed) {
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, postAction(mode), sectionKey))),
          value =>
            for {
              updated <- Future.fromTry(request.userAnswers.set(FreeholdSelfAssessedAmountPage, value))
              _       <- sessionRepository.set(updated)
            } yield Redirect(navigator.nextPage(FreeholdSelfAssessedAmountPage, mode, updated))
        )
      }
  }
}
