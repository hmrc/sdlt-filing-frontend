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

package controllers

import models.{FullReturn, Land, Lease, Purchaser, Residency, Transaction, UserAnswers}
import models.taxCalculation.{CalculationResponse, SdltCalculationRequest}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.taxCalculation.SdltCalculationService
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import views.html.{TaxCalcResultView, TaxCalcTestView}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

case class TaxCalcTestFormData(
  propertyType: String,
  interestCode: String,
  effectiveDate: String,
  totalConsideration: BigDecimal,
  isLinked: Option[String],
  isNonUkResident: Option[String],
  purchaserIsCompany: Option[String],
  contractStartDate: Option[String],
  contractEndDate: Option[String],
  startingRent: Option[String],
  isAnnualRentOver1000: Option[String]
)

class TaxCalculationTestController @Inject()(
  val controllerComponents: MessagesControllerComponents,
  calculationService: SdltCalculationService,
  testView: TaxCalcTestView,
  resultView: TaxCalcResultView
)(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form: Form[TaxCalcTestFormData] = Form(
    mapping(
      "propertyType"         -> nonEmptyText,
      "interestCode"         -> nonEmptyText,
      "effectiveDate"        -> nonEmptyText,
      "totalConsideration"   -> bigDecimal,
      "isLinked"             -> optional(text),
      "isNonUkResident"      -> optional(text),
      "purchaserIsCompany"   -> optional(text),
      "contractStartDate"    -> optional(text),
      "contractEndDate"      -> optional(text),
      "startingRent"         -> optional(text),
      "isAnnualRentOver1000" -> optional(text)
    )(TaxCalcTestFormData.apply)(o => Some(Tuple.fromProductTyped(o)))
  )

  def onPageLoad: Action[AnyContent] = Action { implicit request =>
    Ok(testView(form))
  }

  def onSubmit: Action[AnyContent] = Action.async { implicit request =>
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    form.bindFromRequest().fold(
      formWithErrors => Future.successful(BadRequest(testView(formWithErrors))),
      data => {
        val userAnswers = buildUserAnswers(data)
        val requestJson = calculationService.buildCalculationRequest(userAnswers) match {
          case Right(req) => Json.prettyPrint(Json.toJson(req))
          case Left(err)  => s"Failed to build request: $err"
        }
        calculationService.calculateStampDutyLandTax(userAnswers).map { response =>
          val responseJson = Json.prettyPrint(Json.toJson(response))
          Ok(resultView(response, data, requestJson, responseJson))
        }.recover {
          case ex: Exception =>
            InternalServerError(testView(form.fill(data).withGlobalError(s"Calculation failed: ${ex.getMessage}")))
        }
      }
    )
  }

  private def buildUserAnswers(data: TaxCalcTestFormData): UserAnswers = {
    val land = Land(
      propertyType               = Some(data.propertyType),
      interestCreatedTransferred = Some(data.interestCode)
    )

    val transaction = Transaction(
      effectiveDate      = Some(data.effectiveDate),
      totalConsideration = Some(data.totalConsideration),
      isLinked           = data.isLinked
    )

    val residency = data.isNonUkResident.map(r => Residency(isNonUkResidents = Some(r)))

    val purchaser = data.purchaserIsCompany.map(c => Purchaser(isCompany = Some(c)))

    val lease = (data.contractStartDate, data.contractEndDate, data.startingRent) match {
      case (Some(start), Some(end), Some(rent)) =>
        Some(Lease(
          contractStartDate    = Some(start),
          contractEndDate      = Some(end),
          startingRent         = Some(rent),
          isAnnualRentOver1000 = data.isAnnualRentOver1000
        ))
      case _ => None
    }

    val fullReturn = FullReturn(
      stornId           = "test-storn",
      returnResourceRef = "test-ref",
      land              = Some(Seq(land)),
      transaction       = Some(transaction),
      residency         = residency,
      purchaser         = purchaser.map(Seq(_)),
      lease             = lease
    )

    UserAnswers(id = "test", storn = "test-storn", fullReturn = Some(fullReturn))
  }
}