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
                                      ) {

  def isMainPurchaser(purchaserId: String, userAnswers: UserAnswers): Boolean = {
    val mainPurchaserID: Option[String] = userAnswers.fullReturn
      .flatMap(_.returnInfo)
      .flatMap(_.mainPurchaserID)

    mainPurchaserID.contains(purchaserId)
  }

  def allPurchasersSeq(userAnswers: UserAnswers): Seq[Purchaser] = {
    userAnswers.fullReturn
      .flatMap(_.purchaser)
      .getOrElse(Seq.empty)
  }

  def purchaserRemoveView(
                           form: Form[PurchaserRemove],
                           mode: Mode
                         )(implicit request: DataRequest[AnyContent], messages: Messages): Either[Result, Html] = {

    request.userAnswers.get(PurchaserOverviewRemovePage).map { purchaserRefs =>
      val allPurchasers: Seq[Purchaser] = allPurchasersSeq(request.userAnswers)

      val purchaserToRemove: Option[Purchaser] = allPurchasers
        .find(_.purchaserID.contains(purchaserRefs.purchaserID))

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

      val mainPurchaserCheck: Boolean = isMainPurchaser(userAnswers = request.userAnswers, purchaserId = purchaserRefs.purchaserID)

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
    }.getOrElse(Left(Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())))
  }

  private object PurchaserOps {

    def allPurchasers(userAnswers: UserAnswers): Seq[Purchaser] =
      userAnswers.fullReturn.flatMap(_.purchaser).getOrElse(Seq.empty)

    def findById(purchasers: Seq[Purchaser], purchaserId: String): Option[Purchaser] =
      purchasers.find(_.purchaserID.contains(purchaserId))

    def findByNextId(purchasers: Seq[Purchaser], nextId: String): Option[Purchaser] =
      purchasers.find(_.nextPurchaserID.contains(nextId))

    def journeyRecoveryRedirect: Result =
      Redirect(controllers.routes.JourneyRecoveryController.onPageLoad())

    def purchaserOverviewRedirect(removedPurchaser: Option[Purchaser] = None): Result = {
      removedPurchaser match {
        case Some(purchaser) => {
          val maybePurchaserFullName: String = purchaserService.createPurchaserName(purchaser) match {
            case (Some(name)) => name.fullName
            case _ => ""
          }
          //TODO change this to purchaser overview
          Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad()).flashing("purchaserDeleted" -> maybePurchaserFullName)
        }
        case _ =>
          //TODO change this to purchaser overview
          Redirect(controllers.purchaser.routes.PurchaserOverviewController.onPageLoad())
      }
    }

    def withNewVersion[A](
                           userAnswers: UserAnswers
                         )(f: => Future[A])(implicit hc: HeaderCarrier,
                                            ec: ExecutionContext,
                                            request: DataRequest[AnyContent]): Future[A] =
      for {
        updateReq <- ReturnVersionUpdateRequest.from(userAnswers)
        version <- backendConnector.updateReturnVersion(updateReq)
        result <- version.newVersion match {
          case Some(_) => f
          case None => Future.failed(new IllegalStateException("Return version was not updated (newVersion missing)"))
        }
      } yield result

    def deletePurchaser(
                         userAnswers: UserAnswers,
                         purchaserIdSession: String
                       )(implicit  request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      for {
        req <- DeletePurchaserRequest.from(userAnswers, purchaserIdSession)
        _ <- backendConnector.deletePurchaser(req)
      } yield ()

    def deleteCompanyDetailsIfPresent(
                                       userAnswers: UserAnswers,
                                       companyDetailsIdInSession: Option[String]
                                     )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      companyDetailsIdInSession match {
        case Some(companyId) =>
          for {
            req <- DeleteCompanyDetailsRequest.from(userAnswers, companyId)
            _ <- backendConnector.deleteCompanyDetails(req)
          } yield ()
        case None =>
          Future.successful(())
      }

    def updatePurchaser(
                         userAnswers: UserAnswers,
                         purchaser: Purchaser
                       )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      for {
        req <- UpdatePurchaserRequest.from(userAnswers = userAnswers, purchaser = purchaser)
        _ <- backendConnector.updatePurchaser(req)
      } yield ()

    def updateReturnInfo(
                          userAnswers: UserAnswers,
                          returnInfo: ReturnInfo,
                        )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
      for {
        req <- ReturnInfoRequest.from(userAnswers = userAnswers, returnInfo = returnInfo)
        _ <- backendConnector.updateReturnInfo(req)
      } yield ()

    def withReturnInfo[A](userAnswers: UserAnswers)(f: ReturnInfo => Future[A]): Future[A] =
      userAnswers.fullReturn.flatMap(_.returnInfo) match {
        case Some(ri) => f(ri)
        case None => Future.failed(new IllegalStateException("Missing returnInfo"))
      }
  }

  def handleRemoval(
                     removal: PurchaserRemove,
                     userAnswers: UserAnswers
                   )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchaserLength = allPurchasersSeq(userAnswers).length

    request.userAnswers.get(PurchaserOverviewRemovePage).map { purchaserRefs =>
      removal match {

        case PurchaserRemove.Remove(purchaserId) =>
          val mainPurchaserCheck = isMainPurchaser(userAnswers = request.userAnswers, purchaserId = purchaserRefs.purchaserID)
          handleRemovePurchaser(
            chosenPurchaserId = purchaserId,
            purchaserIdSession = purchaserRefs.purchaserID,
            companyDetailsIdInSession = purchaserRefs.companyDetailsID,
            isMainPurchaser = mainPurchaserCheck,
            purchaserLength = purchaserLength,
            userAnswers = userAnswers
          )

        case PurchaserRemove.SelectNewMain(newMainPurchaserId) =>
          handleMultiplePurchasersWithNewMain(
            chosenPurchaserId = newMainPurchaserId,
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
                                     chosenPurchaserId: String,
                                     purchaserIdSession: String,
                                     companyDetailsIdInSession: Option[String] = None,
                                     isMainPurchaser: Boolean,
                                     purchaserLength: Int,
                                     userAnswers: UserAnswers
                                   )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchasers = PurchaserOps.allPurchasers(userAnswers)
    val chosenPurchaser = PurchaserOps.findById(purchasers, chosenPurchaserId)
    val removedPurchaser = PurchaserOps.findById(purchasers, purchaserIdSession)
    val previousPurchaserInList = PurchaserOps.findByNextId(purchasers, chosenPurchaserId)

    def changeNextIdOfPreviousPurchaser(): Future[Unit] = {
      (previousPurchaserInList, chosenPurchaser) match {
        case (Some(changeP), Some(chosenP)) if !isMainPurchaser && purchaserLength > 1 =>
          PurchaserOps.updatePurchaser(userAnswers, changeP.copy(nextPurchaserID = chosenP.nextPurchaserID))
        case _ =>
          Future.successful(())
      }
    }

    def updateChosenPurchaserIfNeeded(): Future[Option[Result]] =
      chosenPurchaser match {
        case Some(p) if isMainPurchaser && purchaserLength == 2 =>
          updateChosenPurchaserDouble(chosenPurchaserId = chosenPurchaserId,
            purchaser = p,
            userAnswers = userAnswers,
            removedPurchaser = removedPurchaser)
            .map(Some(_))
        case _ =>
          Future.successful(None)
      }

    PurchaserOps
      .withNewVersion(userAnswers) {
        for {
          _ <- PurchaserOps.deletePurchaser(userAnswers, purchaserIdSession)
          _ <- PurchaserOps.deleteCompanyDetailsIfPresent(userAnswers, companyDetailsIdInSession)
          _ <- changeNextIdOfPreviousPurchaser()
          maybeResult <- updateChosenPurchaserIfNeeded()
        } yield maybeResult.getOrElse(PurchaserOps.purchaserOverviewRedirect(removedPurchaser))
      }
      .recover { case _ => PurchaserOps.journeyRecoveryRedirect }
  }

  private def handleMultiplePurchasersWithNewMain(
                                   chosenPurchaserId: String,
                                   purchaserIdSession: String,
                                   companyDetailsIdInSession: Option[String] = None,
                                   userAnswers: UserAnswers
                                 )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchasers = PurchaserOps.allPurchasers(userAnswers)
    val chosenPurchaser = PurchaserOps.findById(purchasers, chosenPurchaserId)

    def doUpdate(): Future[Result] =
      updateChosenPurchaserMulti(
        chosenPurchaserId = chosenPurchaserId,
        purchaserIdSession = purchaserIdSession,
        userAnswers = userAnswers
      )

    PurchaserOps
      .withNewVersion(userAnswers) {
        for {
          _ <- PurchaserOps.deletePurchaser(userAnswers, purchaserIdSession)
          _ <- PurchaserOps.deleteCompanyDetailsIfPresent(userAnswers, companyDetailsIdInSession)
          r <- chosenPurchaser match {
            case Some(_) => doUpdate()
            case None => Future.successful(PurchaserOps.journeyRecoveryRedirect)
          }
        } yield r
      }
      .recover { case _ => PurchaserOps.journeyRecoveryRedirect }
  }

  private def updateChosenPurchaserDouble(
                                           chosenPurchaserId: String,
                                           removedPurchaser: Option[Purchaser],
                                           purchaser: Purchaser,
                                           userAnswers: UserAnswers
                                         )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    PurchaserOps
      .withReturnInfo(userAnswers) { returnInfo =>
        PurchaserOps.withNewVersion(userAnswers) {
          for {
            _ <- PurchaserOps.updatePurchaser(userAnswers, purchaser.copy(nextPurchaserID = None))
            _ <- PurchaserOps.updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = Some(chosenPurchaserId)))
          } yield PurchaserOps.purchaserOverviewRedirect(removedPurchaser)
        }
      }
      .recover { case _ => PurchaserOps.journeyRecoveryRedirect }
  }

  private def updateChosenPurchaserMulti(
                                          chosenPurchaserId: String,
                                          purchaserIdSession: String,
                                          userAnswers: UserAnswers
                                        )(implicit request: DataRequest[AnyContent], hc: HeaderCarrier, ec: ExecutionContext): Future[Result] = {

    val purchasers = allPurchasersSeq(userAnswers)
    val newMainPurchaser = purchasers.find(_.purchaserID.contains(chosenPurchaserId))
    val originalMainPurchaser = purchasers.find(_.purchaserID.contains(purchaserIdSession))
    val previousPurchaserInList = purchasers.find(_.nextPurchaserID.contains(chosenPurchaserId))

    PurchaserOps
      .withReturnInfo(userAnswers) { returnInfo =>
        (newMainPurchaser, originalMainPurchaser, previousPurchaserInList) match {
          case (Some(newMainP), Some(mainP), Some(nextIdP)) =>
            (newMainP.purchaserID, mainP.nextPurchaserID) match {

              case (Some(newMainPurchaserID), Some(previousMainNextID)) if newMainPurchaserID == previousMainNextID =>
                PurchaserOps
                  .withNewVersion(userAnswers) {
                    for {
                      _ <- PurchaserOps.updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = Some(newMainPurchaserID)))
                    } yield PurchaserOps.purchaserOverviewRedirect(originalMainPurchaser)
                  }
                  .recover { case _ => PurchaserOps.journeyRecoveryRedirect }

              case (Some(nextPId), Some(mainPId)) =>
                PurchaserOps
                  .withNewVersion(userAnswers) {
                    for {
                      _ <- PurchaserOps.updatePurchaser(userAnswers, newMainP.copy(nextPurchaserID = Some(nextPId)))
                      _ <- PurchaserOps.updatePurchaser(userAnswers, nextIdP.copy(nextPurchaserID = newMainP.nextPurchaserID))
                      _ <- PurchaserOps.updateReturnInfo(userAnswers, returnInfo.copy(mainPurchaserID = Some(mainPId)))
                    } yield PurchaserOps.purchaserOverviewRedirect(originalMainPurchaser)
                  }
                  .recover { case _ => PurchaserOps.journeyRecoveryRedirect }

              case _ =>
                Future.successful(PurchaserOps.purchaserOverviewRedirect())
            }

          case _ =>
            Future.successful(PurchaserOps.journeyRecoveryRedirect)
        }
      }
      .recover { case _ => PurchaserOps.journeyRecoveryRedirect }
  }


}