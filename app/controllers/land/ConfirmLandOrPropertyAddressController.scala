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
import forms.land.ConfirmLandOrPropertyAddressFormProvider
import models.Mode
import navigation.Navigator
import pages.land.ConfirmLandOrPropertyAddressPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.land.ConfirmLandOrPropertyAddressView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmLandOrPropertyAddressController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       sessionRepository: SessionRepository,
                                       navigator: Navigator,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: ConfirmLandOrPropertyAddressFormProvider,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ConfirmLandOrPropertyAddressView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val address1 = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.address1.isDefined).flatMap(_.address1)))
      val address2 = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.address2.isDefined).flatMap(_.address2)))
      val postcode = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.postcode.isDefined).flatMap(_.postcode)))
      val willSendPlanByPost = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.willSendPlanByPost.isDefined)))
      val localAuthorityNumber = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.localAuthorityNumber.isDefined)))
      val interestCreatedTransferred = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.interestCreatedTransferred.isDefined)))
      val landList = request.userAnswers.fullReturn.flatMap(_.land).getOrElse(Seq.empty)
      val totalLand = landList.length == 1

      (address1, address2, postcode, willSendPlanByPost, localAuthorityNumber, interestCreatedTransferred) match {
        case (Some(address1), Some(address2) , Some(postcode), None, None, None) if totalLand =>
          val preparedForm = request.userAnswers.get(ConfirmLandOrPropertyAddressPage) match {
          case None => form
          case Some(value) => form.fill(value)
        }
          Ok(view(preparedForm, mode, address1, address2, postcode))

        case (Some(address1), None, Some(postcode), None, None, None) if totalLand =>
          val preparedForm = request.userAnswers.get(ConfirmLandOrPropertyAddressPage) match {
            case None => form
            case Some(value) => form.fill(value)
          }
          Ok(view(preparedForm, mode, address1, "", postcode))

        case _ => Redirect(controllers.land.routes.LandAddressController.redirectToAddressLookupLand())
      }

  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val address1 = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.address1.isDefined).flatMap(_.address1)))
      val address2 = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.address2.isDefined).flatMap(_.address2)))
      val postcode = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.postcode.isDefined).flatMap(_.postcode)))
      val willSendPlanByPost = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.willSendPlanByPost.isDefined)))
      val localAuthorityNumber = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.localAuthorityNumber.isDefined)))
      val interestCreatedTransferred = request.userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(_.interestCreatedTransferred.isDefined)))
      val landList = request.userAnswers.fullReturn.flatMap(_.land).getOrElse(Seq.empty)
      val totalLand = landList.length == 1

      (address1, address2, postcode, willSendPlanByPost, localAuthorityNumber, interestCreatedTransferred) match {
        case (Some(address1), Some(address2), Some(postcode), None, None, None) if totalLand =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, address1, address2, postcode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmLandOrPropertyAddressPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value.toString == "yes") {
                  Redirect(navigator.nextPage(ConfirmLandOrPropertyAddressPage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.land.routes.LandAddressController.redirectToAddressLookupLand())
                }
              }
          )

        case (Some(address1), None, Some(postcode), None, None, None) if totalLand =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, address1, "", postcode))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmLandOrPropertyAddressPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value.toString == "yes") {
                  Redirect(navigator.nextPage(ConfirmLandOrPropertyAddressPage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.land.routes.LandAddressController.redirectToAddressLookupLand())
                }
              }
          )

        case _ =>
          form.bindFromRequest().fold(
            formWithErrors =>
              Future.successful(BadRequest(view(formWithErrors, mode, "", "", ""))),

            value =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(ConfirmLandOrPropertyAddressPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield {
                if (value.toString == "yes") {
                  Redirect(navigator.nextPage(ConfirmLandOrPropertyAddressPage, mode, updatedAnswers))
                } else {
                  Redirect(controllers.land.routes.LandAddressController.redirectToAddressLookupLand())
                }
              }
          )
      }
  }
}