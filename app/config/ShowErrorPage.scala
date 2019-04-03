package config

import play.api.i18n.Messages.Implicits._
import play.api.Play._
import play.api._
import play.api.i18n.Messages
import play.api.mvc.Results._
import play.api.mvc.{Result, _}
import play.twirl.api.Html

import scala.concurrent.Future

trait ShowErrorPage extends GlobalSettings {

  private implicit def rhToRequest(rh: RequestHeader) : Request[_] = Request(rh, "")

  def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit request: Request[_]): Html

  def badRequestTemplate(implicit request: Request[_]): Html = standardErrorTemplate(
    Messages("calc.error.badRequest400.title"),
    Messages("calc.error.badRequest400.heading"),
    Messages("calc.error.badRequest400.message"))

  def notFoundTemplate(implicit request: Request[_]): Html = standardErrorTemplate(
    Messages("calc.error.pageNotFound404.title"),
    Messages("calc.error.pageNotFound404.heading"),
    Messages("calc.error.pageNotFound404.message"))

  def internalServerErrorTemplate(implicit request: Request[_]): Html = standardErrorTemplate(
    Messages("calc.error.InternalServerError500.title"),
    Messages("calc.error.InternalServerError500.heading"),
    Messages("calc.error.InternalServerError500.message"))

  final override def onBadRequest(rh: RequestHeader, error: String) =
    Future.successful(BadRequest(badRequestTemplate(rh)))

  final override def onError(request: RequestHeader, ex: Throwable): Future[Result] =
    Future.successful(resolveError(request, ex))

  final override def onHandlerNotFound(rh: RequestHeader) =
    Future.successful(NotFound(notFoundTemplate(rh)))

  def resolveError(rh: RequestHeader, ex: Throwable) = ex.getCause match {
    case ApplicationException(domain, result, _) => result
    case _ => InternalServerError(internalServerErrorTemplate(rh))
  }

}

case class ApplicationException(domain: String, result: Result, message: String) extends Exception(message)
