package controllers.vendorAgent

import controllers.actions.*
import forms.vendorAgent.VendorAgentBeforeYouStartFormProvider
import models.NormalMode
import navigation.Navigator
import pages.vendorAgent.VendorAgentBeforeYouStartPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.VendorAgentBeforeYouStartView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class VendorAgentBeforeYouStartController @Inject()(
                                                     override val messagesApi: MessagesApi,
                                                     sessionRepository: SessionRepository,
                                                     navigator: Navigator,
                                                     identify: IdentifierAction,
                                                     getData: DataRetrievalAction,
                                                     requireData: DataRequiredAction,
                                                     formProvider: VendorAgentBeforeYouStartFormProvider,
                                                     val controllerComponents: MessagesControllerComponents,
                                                     view: VendorAgentBeforeYouStartView
                                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  val form = formProvider()

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm = request.userAnswers.get(VendorAgentBeforeYouStartPage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      form.bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors))),

        value =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(VendorAgentBeforeYouStartPage, value))
            _ <- sessionRepository.set(updatedAnswers)
          } yield {
            if (value) {
              Redirect(navigator.nextPage(VendorAgentBeforeYouStartPage, NormalMode, updatedAnswers))
            } else {
              Redirect(controllers.routes.ReturnTaskListController.onPageLoad())
            }
          }
      )
  }
}