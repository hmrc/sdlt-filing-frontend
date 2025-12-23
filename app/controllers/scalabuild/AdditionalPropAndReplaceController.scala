/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package controllers.scalabuild
import controllers.scalabuild.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import forms.scalabuild.{IsAdditionalPropertyFormProvider, ReplaceMainResidenceFormProvider}
import pages.scalabuild.{IsAdditionalPropertyPage, ReplaceMainResidencePage}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.scalabuild.AdditionalPropAndReplaceView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdditionalPropAndReplaceController @Inject() (
    val controllerComponents: MessagesControllerComponents,
    view: AdditionalPropAndReplaceView,
    addPropFormProvider: IsAdditionalPropertyFormProvider,
    replaceMainFormProvider: ReplaceMainResidenceFormProvider,
    sessionRepository: SessionRepository,
    getData: DataRetrievalAction,
    requireData: DataRequiredAction,
    identify: IdentifierAction
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad: Action[AnyContent] =
    (identify andThen getData andThen requireData) { implicit request =>
      val addPropForm: Form[_] = addPropFormProvider()
      val replaceMainForm: Form[_] = replaceMainFormProvider()
      Ok(view(addPropForm, replaceMainForm))
    }

  def onSubmit(): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>
      val addPropForm: Form[Boolean] = addPropFormProvider()
      val replaceMainForm: Form[Boolean] = replaceMainFormProvider()
      addPropForm
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, formWithErrors))),
          {
            case addProperty @ true =>
              replaceMainForm
                .bindFromRequest()
                .fold(
                  formWithErrors =>
                    Future.successful(
                      BadRequest(view(formWithErrors, formWithErrors))
                    ),
                  replaceMainProperty =>
                    for {
                      updatedAnswers <- Future.fromTry(
                        request.userAnswers
                          .setTwo(IsAdditionalPropertyPage, addProperty, ReplaceMainResidencePage, replaceMainProperty)
                      )
                      _ <- sessionRepository.set(updatedAnswers)
                    } yield ()
                )
            case addProperty @ false =>
              for {
                uaAdditionalProp <- Future.fromTry(
                  request.userAnswers.set(IsAdditionalPropertyPage, addProperty)
                )
                _ <- sessionRepository.set(uaAdditionalProp)
              } yield ()
          }
        )
      Future(Redirect(controllers.scalabuild.routes.PurchasePriceController.onPageLoad().url))
    }
}
