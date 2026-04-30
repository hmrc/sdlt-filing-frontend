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
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

class LandService {

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
    val mainLandId:Option[String] = userAnswers.fullReturn
      .flatMap(_.returnInfo)
      .flatMap(_.mainLandID)
    
    mainLandId.contains(landId)
  }
}