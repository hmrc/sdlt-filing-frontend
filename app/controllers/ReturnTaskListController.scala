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

package controllers

import config.FrontendAppConfig
import controllers.actions.*

import javax.inject.Inject
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.FullReturnService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.tasklist.{PrelimTaskList, VendorTaskList}
import views.html.ReturnTaskListView

import scala.concurrent.ExecutionContext

class ReturnTaskListController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       fullReturnService: FullReturnService,
                                       getData: DataRetrievalAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       view: ReturnTaskListView
                                     ) (implicit ec: ExecutionContext, frontendAppConfig: FrontendAppConfig) extends FrontendBaseController with I18nSupport {

  //See sdlt-team-1 Slack channel canvas page for breakdown on the role of the controller and its functionality

  def onPageLoad(returnId: Option[String] = None): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      for {
        fullReturn <- fullReturnService.getFullReturn(returnId)
        
        /*
        Controller needs to place every section (prelim, vendor, etc) within each relevant part after pulling the full return in from the stub/backend
        Functions and heavy lifting to be done in ReturnTaskListService
        Take from full return, push into mongo session

        Call getFullReturn, full return then pulled from stub, which will have all data so far that's same as getReturn endpoint
        Make sure to update mongo session with all data pulled back from get full return
        */

      } yield {
        val sections = List(Some(PrelimTaskList.build(fullReturn)), Some(VendorTaskList.build(fullReturn))).flatten
        Ok(view(sections: _*))
      }
  }
}