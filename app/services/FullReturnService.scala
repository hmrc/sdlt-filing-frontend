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

package services

import connectors.StubConnector
import models.{FullReturn, PrelimReturn}
import org.slf4j.{Logger, LoggerFactory}
import play.api.mvc.Request
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FullReturnService @Inject()(stubConnector: StubConnector)(implicit ec: ExecutionContext) {
  
  val logger: Logger = LoggerFactory.getLogger(getClass)

  def getFullReturn(returnId: Option[String])(implicit hc: HeaderCarrier, request: Request[_]): Future[FullReturn] = {
    logger.info("[getFullReturn] Getting full return")
    returnId match {
      case Some(id) => {
        for {
          prelimReturn <- getPrelimQuestions(id)
        } yield new FullReturn(Some(prelimReturn))
      }
      case _ => Future(new FullReturn(None))
    }
  }


  def getPrelimQuestions(returnId: String)(implicit hc: HeaderCarrier, request: Request[_]): Future[PrelimReturn] = {
    logger.info("[getPrelimQuestions] Getting prelim questions")
    stubConnector.stubPremlimQuestions(returnId)
  }

  
}
