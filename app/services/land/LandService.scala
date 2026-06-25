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

package services.land

import models.land.LandTypeOfProperty
import models.{Land, NormalMode, UserAnswers}
import pages.land.LandTypeOfPropertyPage
import play.api.i18n.Messages
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import services.crossflow.CrossFlowFailure
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.*
import viewmodels.land.LandPropertyTypeRow

class LandService {

  def generateLandPropertyTypeRows(
                                    allLands: Seq[Land],
                                    mismatchLands: Seq[Land]
                                  ): Seq[LandPropertyTypeRow] = {
    val mismatchIds = mismatchLands.flatMap(_.landID).toSet

    allLands.zipWithIndex.map { case (land, idx) =>
      val landId = land.landID.getOrElse("")
      val label = land.address1
        .map(addr => land.postcode.fold(addr)(pc => s"$addr, $pc"))
        .getOrElse(s"Land ${idx + 1}")

      val propertyTypeDisplay = land.propertyType match {
        case Some("01") => "01 - Residential"
        case Some("02") => "02 - Mixed"
        case Some("03") => "03 - Non-residential"
        case Some("04") => "04 - Additional residential property liable to higher rate"
        case Some(other) => other
        case None => ""
      }

      LandPropertyTypeRow(
        landId = landId,
        label = label,
        propertyTypeDisplay = propertyTypeDisplay,
        updateUrl = controllers.land.routes.LandPropertyTypeMultiEntityController.updateLand(landId).url,
        removeUrl = controllers.land.routes.LandPropertyTypeMultiEntityController.removeLand(landId).url,
        canRemove = allLands.size > 1,
        isMismatch = mismatchIds.contains(landId)
      )
    }
  }

  def propertyTypeCheck(userAnswers: UserAnswers, continueRoute: Result): Result = {

    userAnswers.get(LandTypeOfPropertyPage) match {
      case Some(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential) =>
        continueRoute

      case None =>
        Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))

      case _ =>
        Redirect(controllers.land.routes.LandCheckYourAnswersController.onPageLoad())
    }
  }

  def getMainLand(userAnswers: UserAnswers): Option[Land] = {
    val mainLandId: Option[String] = userAnswers.fullReturn
      .flatMap(_.returnInfo)
      .flatMap(_.mainLandID)

    userAnswers.fullReturn.flatMap(_.land.flatMap(_.find(land => mainLandId.equals(land.landID))))
  }

  def isMainLand(userAnswers: UserAnswers, landId: String): Boolean = {
    val mainLandId: Option[String] = userAnswers.fullReturn
      .flatMap(_.returnInfo)
      .flatMap(_.mainLandID)

    mainLandId.contains(landId)
  }
  
  def generateLandErrors(failures: Seq[(Land, Seq[CrossFlowFailure])])
                        (implicit messages: Messages): SummaryList =
    SummaryList(rows = failures.flatMap { case (land, _) =>
      for {
        landId  <- land.landID
        address  = land.address1.getOrElse(landId)
      } yield SummaryListRow(
        key = Key(
          content = Text(address),
          classes = "govuk-!-width-one-third govuk-!-font-weight-regular hmrc-summary-list__key"
        ),
        actions = Some(Actions(
          items = Seq(
            ActionItem(
              href               = controllers.land.routes.LandAuthorityCodeSingleEntityController.onPageLoad(landId).url,
              content            = Text(messages("site.update")),
              visuallyHiddenText = Some(address)
            ),
            ActionItem(
              href               = controllers.land.routes.LandAuthorityCodeMultiEntityController.removeLand(landId).url,
              content            = Text(messages("site.remove")),
              visuallyHiddenText = Some(address)
            )
          ),
          classes = "govuk-!-width-one-third"
        ))
      )
    })
}