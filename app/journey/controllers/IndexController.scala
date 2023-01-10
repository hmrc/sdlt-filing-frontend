/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package journey.controllers

import config.FrontendAppConfig
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future
import scala.util.Random

@Singleton
class IndexController @Inject()(mcc: MessagesControllerComponents,
                                template: journey.views.html.index,
                                implicit val config: FrontendAppConfig) extends FrontendController(mcc) {

  val sessionId = UUID.randomUUID().toString
  val showIndex: Action[AnyContent] = Action.async { implicit request =>

    if(request.session.get(SessionKeys.sessionId).isEmpty) {
      Future.successful(Ok(template())
        .withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    } else {
      Future.successful(Ok(template()))
    }
  }

  private [controllers] def setBooleanFlag(numericSessionValues: String): Boolean = {
    val random = new Random()
    val seed = numericSessionValues.takeRight(10).toLong
    random.setSeed(seed)
    random.nextInt(3) == 0
  }
}
