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

package controllers.taxCalculation

import models.*
import viewmodels.taxCalculation.CalculationResultHelper
import play.api.data.Form
import play.api.data.Forms.*
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import _root_.views.html.testOnly.taxCalculation.TaxCalculationTestView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class TaxCalculationTestController @Inject()(
  override val messagesApi: MessagesApi,
  calculationService: SdltCalculationService,
  val controllerComponents: MessagesControllerComponents,
  view: TaxCalculationTestView
)(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport {

  val form: Form[String] = Form(single("json" -> text))

  private val defaultJson: String =
    Json.prettyPrint(
      Json.toJson(
        FullReturn(
          stornId = "TEST-STORN",
          returnResourceRef = "TEST-REF",
          land = Some(Seq(Land(
            propertyType               = Some("01"),
            interestCreatedTransferred = Some("LG")
          ))),
          transaction = Some(Transaction(
            transactionDescription = Some("L"),
            effectiveDate          = Some("2025-06-15"),
            totalConsideration     = Some(BigDecimal(250000)),
            isLinked               = Some("no"),
            claimingRelief         = Some("yes"),
            reliefReason           = Some("20"),
            reliefAmount           = None
          )),
          residency = Some(Residency(
            isNonUkResidents = Some("no")
          )),
          lease = Some(Lease(
            contractStartDate    = Some("2025-06-15"),
            contractEndDate      = Some("2030-06-14"),
            isAnnualRentOver1000 = Some("yes"),
            netPresentValue      = Some("100000")
          ))
        )
      )
    )

  def onPageLoad(): Action[AnyContent] = Action { implicit request =>
    Ok(view(form, defaultJson, None, None))
  }

  def onSubmit(): Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrier()

    form.bindFromRequest().fold(
      _ => Future.successful(BadRequest(view(form, "", None, Some("Invalid form submission")))),
      jsonStr => {
        Json.parse(jsonStr).validate[FullReturn].fold(
          errors => Future.successful(Ok(view(form, jsonStr, None, Some(s"JSON parse error: $errors")))),
          fullReturn => {
            val userAnswers = UserAnswers(id = "test-user", storn = fullReturn.stornId, fullReturn = Some(fullReturn))
            calculationService.calculateStampDutyLandTax(userAnswers).map { response =>
              Ok(view(form, jsonStr, Some(CalculationResultHelper.toViewModel(response)), None))
            }.recover {
              case ex: Exception => Ok(view(form, jsonStr, None, Some(ex.getMessage)))
            }
          }
        )
      }
    )
  }
}
