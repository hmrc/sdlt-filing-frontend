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
import models.{NormalMode, UserAnswers}
import pages.land.LandTypeOfPropertyPage
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.Future


class LandService {

  def propertyTypeCheck(userAnswers: UserAnswers, continueRoute: Result): Result = {

    userAnswers.get(LandTypeOfPropertyPage) match {
      case Some(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential) =>
        continueRoute

      case None =>
        Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))

      case _ => //TODO - DTR-2495 - SPRINT-10 - Update to land CYA
        Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))

    }
  }

  def propertyTypeCheckAsync(
                              userAnswers: UserAnswers,
                              continueRoute: => Future[Result]
                            ): Future[Result] =
    userAnswers.get(LandTypeOfPropertyPage) match {
      case Some(LandTypeOfProperty.Mixed | LandTypeOfProperty.NonResidential) =>
        continueRoute

      case None =>
        Future.successful(
          Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))
        )

      case _ => //TODO - DTR-2495 - SPRINT-10 - Update to land CYA
        Future.successful(
          Redirect(controllers.land.routes.LandTypeOfPropertyController.onPageLoad(NormalMode))
        )
    }

}