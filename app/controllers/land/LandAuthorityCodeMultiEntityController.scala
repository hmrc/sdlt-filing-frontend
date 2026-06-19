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

package controllers.land

import controllers.actions.*
import pages.land.LandOverviewRemovePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.crossflow.fields.CrossFlowValidationService
import services.land.LandService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.LandAuthorityCodeMultiEntityView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandAuthorityCodeMultiEntityController @Inject()(
                                                        override val messagesApi: MessagesApi,
                                                        identify: IdentifierAction,
                                                        getData: DataRetrievalAction,
                                                        requireData: DataRequiredAction,
                                                        sessionRepository: SessionRepository,
                                                        crossFlow: CrossFlowValidationService,
                                                        landService: LandService,
                                                        val controllerComponents: MessagesControllerComponents,
                                                        view: LandAuthorityCodeMultiEntityView
                                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val headingKey = "crossflow.land.Cf-7.heading"

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val offending = crossFlow.landFailuresExcluding(Set("Cf-6"), request.userAnswers)
      
      offending match {

        case Nil =>
          Future.successful(Redirect(controllers.routes.ReturnTaskListController.onPageLoad(None)))

        case Seq((singleLand, _)) =>
          singleLand.landID
            .map { landId =>
              Future.successful(Redirect(
                controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad(landId)
              ))
            }
            .getOrElse(Future.successful(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))

        case multiple =>
          val failingLandsCount = multiple.flatMap(_._2).size
          val summary           = landService.generateLandErrors(multiple)
          Future.successful(Ok(view(summary, headingKey, failingLandsCount)))
      }
  }

  def removeLand(landId: String): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.set(LandOverviewRemovePage, landId))
        _              <- sessionRepository.set(updatedAnswers)
      } yield Redirect(controllers.land.routes.RemoveLandController.onPageLoad())
    }
}