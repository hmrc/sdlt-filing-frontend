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

package services.submission

import connectors.StampDutyLandTaxConnector
import models.{FullReturn, Purchaser, UserAnswers, Vendor}
import models.purchaser.UpdatePurchaserRequest
import models.vendor.UpdateVendorRequest
import models.submission.{SubmissionResponse, SubmitRequest}
import pages.submission.SubmissionFailedPage
import play.api.Logging
import play.api.mvc.Request
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

@Singleton
class ChrisSubmissionService @Inject()(connector: StampDutyLandTaxConnector,
                                       sessionRepository: SessionRepository)
                                      (implicit ec: ExecutionContext) extends Logging {
  
  def submit(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Future[SubmissionResponse] =
    userAnswers.fullReturn match {
      case None =>
        logger.error("[ChrisSubmissionService][submit] no fullReturn in userAnswers")
        Future.failed(new NoSuchElementException("No fullReturn present for submission"))

      case Some(fullReturn) =>
        val agentTypes: Set[String] =
          fullReturn.returnAgent.getOrElse(Nil).flatMap(_.agentType).map(_.trim.toUpperCase).toSet

        val vendorRepresented    = agentTypes.contains("VENDOR")
        val purchaserRepresented = agentTypes.contains("PURCHASER")

        val mainVendorId    = fullReturn.returnInfo.flatMap(_.mainVendorID)
        val mainPurchaserId = fullReturn.returnInfo.flatMap(_.mainPurchaserID)

        val persistFlags: Future[Unit] =
          for {
            _ <- if (vendorRepresented) updateMainVendor(userAnswers, fullReturn, mainVendorId) else Future.unit
            _ <- if (purchaserRepresented) updateMainPurchaser(userAnswers, fullReturn, mainPurchaserId) else Future.unit
          } yield ()

        persistFlags.flatMap { _ =>
          val withFlags     = applyAgentRepresentation(fullReturn, vendorRepresented, purchaserRepresented, mainVendorId, mainPurchaserId)
          val submitRequest = SubmitRequest(email = None, fullReturn = withFlags)
          connector.submit(submitRequest)
        }
    }

  def submitInBackground(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, request: Request[_]): Unit =
    submit(userAnswers).onComplete {
      case Success(response) =>
        logger.info(s"[ChrisSubmissionService][submitInBackground] completed: $response")
      case Failure(e) =>
        logger.error("[ChrisSubmissionService][submitInBackground] submit failed", e)
        val updated = userAnswers.set(SubmissionFailedPage, true)
        updated.fold(
          errs => logger.error(s"[ChrisSubmissionService] could not set SubmissionFailedPage: $errs"),
          ua   => sessionRepository.set(ua).recover {
            case re => logger.error("[ChrisSubmissionService] failed to persist SubmissionFailedPage", re)
          }
        )
    }

  private def updateMainVendor(userAnswers: UserAnswers, fullReturn: FullReturn, mainVendorId: Option[String])
                              (implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] =
    fullReturn.vendor.getOrElse(Nil).find(_.vendorID == mainVendorId) match {
      case Some(v) =>
        val flagged = v.copy(isRepresentedByAgent = Some("YES"))
        UpdateVendorRequest.from(userAnswers, flagged).flatMap { req =>
          connector.updateVendor(req).map(_ => ())
        }
      case None =>
        logger.warn("[ChrisSubmissionService] vendor agent present but no main vendor found")
        Future.unit
    }

  private def updateMainPurchaser(userAnswers: UserAnswers, fullReturn: FullReturn, mainPurchaserId: Option[String])
                                 (implicit hc: HeaderCarrier, request: Request[_]): Future[Unit] =
    fullReturn.purchaser.getOrElse(Nil).find(_.purchaserID == mainPurchaserId) match {
      case Some(p) =>
        val flagged = p.copy(isRepresentedByAgent = Some("YES"))
        UpdatePurchaserRequest.from(userAnswers, flagged).flatMap { req =>
          connector.updatePurchaser(req).map(_ => ())
        }
      case None =>
        logger.warn("[ChrisSubmissionService] purchaser agent present but no main purchaser found")
        Future.unit
    }

  private def applyAgentRepresentation(fullReturn: FullReturn,
                                       vendorRepresented: Boolean,
                                       purchaserRepresented: Boolean,
                                       mainVendorId: Option[String],
                                       mainPurchaserId: Option[String]): FullReturn = {
    val updatedVendors: Option[Seq[Vendor]] =
      if (vendorRepresented)
        fullReturn.vendor.map(_.map(v => if (v.vendorID == mainVendorId) v.copy(isRepresentedByAgent = Some("YES")) else v))
      else fullReturn.vendor

    val updatedPurchasers: Option[Seq[Purchaser]] =
      if (purchaserRepresented)
        fullReturn.purchaser.map(_.map(p => if (p.purchaserID == mainPurchaserId) p.copy(isRepresentedByAgent = Some("YES")) else p))
      else fullReturn.purchaser

    fullReturn.copy(vendor = updatedVendors, purchaser = updatedPurchasers)
  }
}