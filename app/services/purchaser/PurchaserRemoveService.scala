/*
 * Copyright 2026 HM Revenue & Customs
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

package services.purchaser

import connectors.StampDutyLandTaxConnector
import models.purchaser.*
import models.requests.DataRequest
import models.{Mode, Purchaser, ReturnInfo, ReturnInfoRequest, ReturnVersionUpdateRequest, UserAnswers}
import pages.purchaser.{PurchaserOverviewRemovePage, PurchaserRemovePage}
import play.api.Logging
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.HeaderCarrier
import utils.FullName
import views.html.purchaser.PurchaserRemoveView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class PurchaserRemoveService @Inject()(
                                        view: PurchaserRemoveView,
                                        backendConnector: StampDutyLandTaxConnector,
                                        purchaserService: PurchaserService
                                      ) extends Logging {

  def purchaserRemoveView(
                           form: Form[PurchaserRemove],
                           mode: Mode
                         )(implicit request: DataRequest[AnyContent], messages: Messages): Either[Result, Html] = {

    request.userAnswers.get(PurchaserOverviewRemovePage).map { purchaserRefs =>
      val allPurchasers: Seq[Purchaser] = purchaserService.allPurchasers(request.userAnswers)
      val purchaserToRemove: Option[Purchaser] = purchaserService.findById(allPurchasers, purchaserRefs.purchaserID)

      val purchaserToRemoveName: Option[String] = purchaserToRemove.flatMap { purchaser =>
        purchaser.companyName.orElse(
          FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
        )
      }

      val remainingPurchasers: Seq[Purchaser] = allPurchasers
        .filterNot(_.purchaserID.contains(purchaserRefs.purchaserID))

      val remainingPurchasersWithNames: Seq[(String, String)] = remainingPurchasers.flatMap { purchaser =>
        val name = purchaser.companyName.orElse(
          FullName.optionalFullName(purchaser.forename1, purchaser.forename2, purchaser.surname)
        )
        purchaser.purchaserID.flatMap(id => name.map(n => (id, n)))
      }

      val preparedForm = request.userAnswers.get(PurchaserRemovePage) match {
        case None => form
        case Some(value) => form.fill(value)
      }

      val mainPurchaserCheck: Boolean = purchaserService.isMainPurchaser(purchaserRefs.purchaserID, request.userAnswers)

      val effectiveCount = if (mainPurchaserCheck) {
        allPurchasers.length
      } else {
        0
      }

      val viewData = PurchaserRemoveData(
        purchaserId = purchaserRefs.purchaserID,
        companyDetailsId = purchaserRefs.companyDetailsID,
        purchaserCount = effectiveCount,
        purchaserToRemoveName = purchaserToRemoveName,
        remainingPurchasers = remainingPurchasersWithNames,
        isMainPurchaser = mainPurchaserCheck
      )

      Right(view(preparedForm, mode, viewData))
    }.getOrElse(Left(Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())))
  }

  private object PurchaserOps {
        
    def journeyRecoveryRedirect: Result =
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

    def purchaserOverviewRedirect(removedPurchaser: Option[Purchaser] = None): Result = {
      removedPurchaser match {
        case Some(purchaser) =>
          val maybePurchaserFullName: String = purchaserService.createPurchaserName(purchaser) match {
            case Some(name) => name.fullName
            case _ => ""
          }
          Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad()).flashing("purchaserDeleted" -> maybePurchaserFullName)
        case _ => Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      }
    }

    def withNewVersion(
                        userAnswers: UserAnswers,
                        version: Option[Int] = None
                      )(implicit hc: HeaderCarrier,
                        ec: ExecutionContext,
                        request: DataRequest[AnyContent]): Future[Int] =
      logger.info(s"[PurchaserRemoveService][withNewVersion] version passed in: $version")
      for {
        updateReq <- ReturnVersionUpdateRequest.from(userAnswers, version)
        versionResponse <- backendConnector.updateReturnVersion(updateReq)
        newVersion <- versionResponse.newVersion match {
          case Some(v) =>
            logger.info(s"[PurchaserRemoveService][withNewVersion] update return version response version: $v")
            Future.successful(v)
          case None => Future.failed(new IllegalStateException("Return version was not updated (newVersion missing)"))
        }
      } yield newVersion

    def deletePurchaser(
                         userAnswers: UserAnswers,
                         purchaserIdSession: String
                       )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      for {
        req <- DeletePurchaserRequest.from(userAnswers, purchaserIdSession)
        _ <- backendConnector.deletePurchaser(req)
      } yield ()

    def deleteCompanyDetailsIfPresent(
                                       userAnswers: UserAnswers,
                                       isMainPurchaser: Boolean,
                                       companyDetailsIdInSession: Option[String]
                                     )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      companyDetailsIdInSession match {
        case Some(companyId) if isMainPurchaser =>
          for {
            req <- DeleteCompanyDetailsRequest.from(userAnswers, companyId)
            _ <- backendConnector.deleteCompanyDetails(req)
          } yield ()
        case _ =>
          Future.successful(())
      }

    def updateReturnInfo(
                          userAnswers: UserAnswers,
                          returnInfo: ReturnInfo,
                        )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      for {
        req <- ReturnInfoRequest.from(userAnswers = userAnswers, returnInfo = returnInfo)
        _ <- backendConnector.updateReturnInfo(req)
      } yield ()
  }

  def handleRemoval(
                     removal: PurchaserRemove,
                     userAnswers: UserAnswers
                   )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {
    request.userAnswers.get(PurchaserOverviewRemovePage).map { purchaserRefs =>
      removal match {
        case PurchaserRemove.Remove(purchaserId) =>
          val mainPurchaserCheck = purchaserService.isMainPurchaser(purchaserRefs.purchaserID, request.userAnswers)
          handleRemovePurchaser(
            purchaserIdSession = purchaserRefs.purchaserID,
            companyDetailsIdInSession = purchaserRefs.companyDetailsID,
            isMainPurchaser = mainPurchaserCheck,
            userAnswers = userAnswers
          )
        case PurchaserRemove.SelectNewMain(newMainPurchaserID) =>
          handleMultiplePurchasersWithNewMain(
            chosenPurchaserId = newMainPurchaserID,
            purchaserIdSession = purchaserRefs.purchaserID,
            companyDetailsIdInSession = purchaserRefs.companyDetailsID,
            userAnswers = userAnswers
          )
        case _ =>
          handleNoRemoval()
      }
    }.getOrElse(Future.successful(PurchaserOps.purchaserOverviewRedirect()))
  }

  private def handleNoRemoval(): Future[Result] =
    Future.successful(PurchaserOps.purchaserOverviewRedirect())

  private def handleRemovePurchaser(
                                     purchaserIdSession: String,
                                     companyDetailsIdInSession: Option[String] = None,
                                     isMainPurchaser: Boolean,
                                     userAnswers: UserAnswers
                                   )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchasers = purchaserService.allPurchasers(userAnswers)
    val removedPurchaser = purchaserService.findById(purchasers, purchaserIdSession)
    val promotingPurchaser = if purchasers.length == 2 && isMainPurchaser then purchasers.find(!_.purchaserID.contains(purchaserIdSession)) else None
    val promotingPurchaserId = promotingPurchaser.flatMap(_.purchaserID)

    logger.info(s"[PurchaserRemoveService][handleRemovePurchaser] purchaser in session to remove: $removedPurchaser")
    logger.info(s"[PurchaserRemoveService][handleRemovePurchaser] chosen purchaser to promote: $promotingPurchaser")

      userAnswers.fullReturn.flatMap(_.returnInfo).map { returnInfo =>
        (for {
          version <- PurchaserOps.withNewVersion(userAnswers)
          _ <- PurchaserOps.deletePurchaser(userAnswers, purchaserIdSession)
          _ <- PurchaserOps.deleteCompanyDetailsIfPresent(userAnswers, isMainPurchaser, companyDetailsIdInSession)
          result <- if (isMainPurchaser) {
            for {
              newVersion <- PurchaserOps.withNewVersion(userAnswers, Some(version))
              _ <- PurchaserOps.updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = promotingPurchaserId))
            } yield PurchaserOps.purchaserOverviewRedirect(removedPurchaser)
          } else {
            Future.successful(PurchaserOps.purchaserOverviewRedirect(removedPurchaser))
          }
        } yield result)
          .recover {
            case _ =>
              logger.info(s"[PurchaserRemoveService][handleRemovePurchaser] failed to delete purchaser. Redirecting to journey recovery")
              PurchaserOps.journeyRecoveryRedirect
          }
      }.getOrElse(Future.successful(PurchaserOps.journeyRecoveryRedirect))
  }

  private def handleMultiplePurchasersWithNewMain(
                                                   chosenPurchaserId: String,
                                                   purchaserIdSession: String,
                                                   companyDetailsIdInSession: Option[String] = None,
                                                   userAnswers: UserAnswers
                                                 )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchasers = purchaserService.allPurchasers(userAnswers)
    val removedPurchaser = purchaserService.findById(purchasers, purchaserIdSession)

    logger.info(s"[PurchaserRemoveService][handleMultiplePurchasersWithNewMain] purchaser in session to remove: $removedPurchaser")
    logger.info(s"[PurchaserRemoveService][handleMultiplePurchasersWithNewMain] chosen purchaser ID to promote: $chosenPurchaserId")

    userAnswers.fullReturn.flatMap(_.returnInfo).map { returnInfo =>
      (for {
        version <- PurchaserOps.withNewVersion(userAnswers)
        _ <- PurchaserOps.deletePurchaser(userAnswers, purchaserIdSession)
        _ <- PurchaserOps.deleteCompanyDetailsIfPresent(userAnswers, true, companyDetailsIdInSession)
        newVersion <- PurchaserOps.withNewVersion(userAnswers, Some(version))
        _ <- PurchaserOps.updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = Some(chosenPurchaserId)))
      } yield {
        PurchaserOps.purchaserOverviewRedirect(removedPurchaser)
      })
        .recover {
          case _ =>
            logger.info(s"[PurchaserRemoveService][handleMultiplePurchasersWithNewMain] redirecting to journey recovery")
            PurchaserOps.journeyRecoveryRedirect
        }
      }.getOrElse(Future.successful(PurchaserOps.journeyRecoveryRedirect))
  }
}
