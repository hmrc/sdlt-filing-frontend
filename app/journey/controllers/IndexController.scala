/*
 * Copyright 2020 HM Revenue & Customs
 *
 */

package journey.controllers

import java.util.UUID
import javax.inject.{Inject, Singleton}
import config.FrontendAppConfig
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.Future
import scala.util.Random

@Singleton
class IndexController @Inject()(mcc: MessagesControllerComponents,
                                template: journey.views.html.index,
                                implicit val config: FrontendAppConfig) extends FrontendController(mcc) {

  val googleTagManagerId: String = config.googleTagManagerId
  val sessionId = UUID.randomUUID().toString
  val showIndex: Action[AnyContent] = Action.async { implicit request =>

    if(request.session.get(SessionKeys.sessionId).isEmpty) {
      Future.successful(Ok(template(setURPanelFlag(sessionId), googleTagManagerId))
        .withSession(request.session + (SessionKeys.sessionId -> s"session-$sessionId")))
    } else {
      Future.successful(Ok(template(setURPanelFlag(sessionId), googleTagManagerId)))
    }
  }

  private[controllers] def setURPanelFlag(implicit hc: HeaderCarrier): Boolean = {
    val session = hc.sessionId.map(_.value).getOrElse("0")
    val numericSessionValues = session.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    setBooleanFlag(numericSessionValues)
  }

  private[controllers] def setURPanelFlag(sessionID: String): Boolean = {
    val numericSessionValues = sessionID.replaceAll("[^0-9]", "") match {
      case "" => "0"
      case num => num
    }
    setBooleanFlag(numericSessionValues)
  }

  private [controllers] def setBooleanFlag(numericSessionValues: String): Boolean = {
    val random = new Random()
    val seed = numericSessionValues.takeRight(10).toLong
    random.setSeed(seed)
    random.nextInt(3) == 0
  }
}
