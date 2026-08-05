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

package controllers.submission

import connectors.StampDutyLandTaxConnector
import controllers.actions.*
import forms.submission.Sdlt5CertificateForEachLandOrPropertyFormProvider
import models.{Mode, ReturnInfoRequest, UserAnswers}
import navigation.Navigator
import pages.submission.Sdlt5CertificateForEachLandOrPropertyPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.submission.Sdlt5CertificateForEachLandOrPropertyView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class Sdlt5CertificateForEachLandOrPropertyController @Inject()(
                                                                   override val messagesApi: MessagesApi,
                                                                   sessionRepository: SessionRepository,
                                                                   navigator: Navigator,
                                                                   activatedIdentify: ActivatedIdentifierAction,
                                                                   getData: DataRetrievalAction,
                                                                   requireData: DataRequiredAction,
                                                                   resubmissionCheck: ResubmissionCheckAction,
                                                                   formProvider: Sdlt5CertificateForEachLandOrPropertyFormProvider,
                                                                   backendConnector: StampDutyLandTaxConnector,
                                                                   val controllerComponents: MessagesControllerComponents,
                                                                   view: Sdlt5CertificateForEachLandOrPropertyView
                                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (activatedIdentify andThen getData andThen requireData andThen resubmissionCheck ) {
    implicit request =>

      val landList = request.userAnswers.fullReturn.flatMap(_.land).getOrElse(Seq.empty)

      if (landList.length > 1) {
        val preparedForm = request.userAnswers.get(Sdlt5CertificateForEachLandOrPropertyPage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }
        Ok(view(preparedForm, mode))
      } else {
        Redirect(controllers.submission.routes.WhoAreYouSubmittingForController.onPageLoad())
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (activatedIdentify andThen getData andThen requireData).async {
    implicit request =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

      val landList = request.userAnswers.fullReturn.flatMap(_.land).getOrElse(Seq.empty)

      if (landList.length > 1) {
        form.bindFromRequest().fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, mode))),

          value =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(Sdlt5CertificateForEachLandOrPropertyPage, value))
              answersWithCert = withLandCertForEachProp(updatedAnswers, if (value) "YES" else "NO")
              _              <- updateReturnInfo(answersWithCert)
              _              <- sessionRepository.set(answersWithCert)
            } yield Redirect(navigator.nextPage(Sdlt5CertificateForEachLandOrPropertyPage, mode, answersWithCert))
        )
      } else {
        Future.successful(Redirect(controllers.submission.routes.WhoAreYouSubmittingForController.onPageLoad()))
      }
  }

  private def withLandCertForEachProp(userAnswers: UserAnswers, value: String): UserAnswers =
    userAnswers.copy(fullReturn = userAnswers.fullReturn.map { fullReturn =>
      fullReturn.copy(returnInfo = fullReturn.returnInfo.map(_.copy(landCertForEachProp = Some(value))))
    })

  private def updateReturnInfo(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] =
    userAnswers.fullReturn.flatMap(_.returnInfo) match {
      case Some(returnInfo) =>
        for {
          req <- ReturnInfoRequest.from(userAnswers = userAnswers, returnInfo = returnInfo)
          _   <- backendConnector.updateReturnInfo(req)
        } yield ()

      case None =>
        Future.unit
    }
}
