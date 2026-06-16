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

package controllers.purchaser

import controllers.actions.*
import forms.purchaser.PurchaserRemoveFormProvider
import models.{Mode, Purchaser}
import models.purchaser.PurchaserRemove
import pages.purchaser.PurchaserOverviewRemovePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.purchaser.{PurchaserRemoveService, PurchaserService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.FullName

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PurchaserRemoveController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       requireData: DataRequiredAction,
                                       formProvider: PurchaserRemoveFormProvider,
                                       purchaserRemoveService: PurchaserRemoveService,
                                       purchaserService: PurchaserService,
                                       val controllerComponents: MessagesControllerComponents
                                         )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


 

  def onPageLoad(mode: Mode): Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>

      val purchaserName: String = request.userAnswers.get(PurchaserOverviewRemovePage).flatMap { purchaserRefs =>
        val allPurchasers: Seq[Purchaser] = purchaserService.allPurchasers(request.userAnswers)
        val purchaserToRemove: Option[Purchaser] = purchaserService.findById(allPurchasers, purchaserRefs.purchaserID)

        purchaserToRemove.flatMap { purchaser =>
          purchaser.companyName.orElse(
            FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
          )
        }
      }.getOrElse("")

      val form: Form[PurchaserRemove] = formProvider(purchaserName)
      
      purchaserRemoveService.purchaserRemoveView(form, mode) match {
        case Right(html) => Ok(html)
        case Left(result) => result
      }
    }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      val purchaserName: String = request.userAnswers.get(PurchaserOverviewRemovePage).flatMap { purchaserRefs =>
        val allPurchasers: Seq[Purchaser] = purchaserService.allPurchasers(request.userAnswers)
        val purchaserToRemove: Option[Purchaser] = purchaserService.findById(allPurchasers, purchaserRefs.purchaserID)

        purchaserToRemove.flatMap { purchaser =>
          purchaser.companyName.orElse(
            FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
          )
        }
      }.getOrElse("")

      val form: Form[PurchaserRemove] = formProvider(purchaserName)

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(
            purchaserRemoveService.purchaserRemoveView(formWithErrors, mode).fold(identity, BadRequest(_))
          ),
        value =>
          purchaserRemoveService.handleRemoval(value, userAnswers = request.userAnswers)
      )
  }
}
