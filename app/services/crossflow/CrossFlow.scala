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

package services.crossflow

import models.UserAnswers


enum ReturnSection:
  case Vendor, VendorAgent, Purchaser, PurchaserAgent, Land,
  UkResidency, Transaction, Lease, TaxCalculation

final case class PageId(value: String)

object Pages:
  val ReliefReason: PageId = PageId("reliefReason")
  val EffectiveDate: PageId = PageId("effectiveDate")
  val LandPropertyType: PageId = PageId("landPropertyType")
  val ContractDate: PageId = PageId("contractDate")

object Fields:
  val ReliefReason  = "value"
  val EffectiveDate = "value"
  val PropertyType  = "value"
  val ContractDate  = "value"

final case class CrossFlowTarget(page: PageId, field: String)

final case class CrossFlowFailure(
                                   ruleId:     String,
                                   affects:    ReturnSection,
                                   messageKey: String,
                                   targets:    Seq[CrossFlowTarget],
                                   args:       Seq[Any] = Nil
                                 ):
  def targetsOn(page: PageId): Seq[CrossFlowTarget] = targets.filter(_.page == page)
  def appearsOn(page: PageId): Boolean              = targets.exists(_.page == page)


final case class SectionStatus(
                                section:     ReturnSection,
                                hasFailures: Boolean,
                                ruleIds:     Seq[String],
                                messageKeys: Seq[String],
                                targets:     Seq[CrossFlowTarget]
                              ):

  def primaryUrl(default: String, urlOf: PageId => String): String =
    targets.headOption.fold(default)(t => urlOf(t.page))

object SectionStatus:

  def empty(section: ReturnSection): SectionStatus =
    SectionStatus(section, hasFailures = false, ruleIds = Nil, messageKeys = Nil, targets = Nil)

trait CrossFlowRule:
  def id: String
  def affects: ReturnSection
  def inputs: Set[ReturnSection]
  def targets: Seq[CrossFlowTarget]
  def validate(userAnswers: UserAnswers): Option[CrossFlowFailure]

abstract class GuardRule extends CrossFlowRule:
  protected def appliesTo(ua: UserAnswers): Boolean
  protected def isValid(ua: UserAnswers): Boolean
  protected def messageKey: String
  protected def args(ua: UserAnswers): Seq[Any] = Nil

  final def validate(ua: UserAnswers): Option[CrossFlowFailure] =
    Option.when(appliesTo(ua) && !isValid(ua))(
      CrossFlowFailure(id, affects, messageKey, targets, args(ua))
    )