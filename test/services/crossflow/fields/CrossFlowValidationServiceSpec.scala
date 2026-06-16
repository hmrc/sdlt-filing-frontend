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

package services.crossflow.fields

import base.SpecBase
import constants.FullReturnConstants.emptyFullReturn
import models.{Land, UserAnswers}
import org.scalatest.matchers.must.Matchers
import services.crossflow._
import play.api.libs.json.{Json, JsNull}

class CrossFlowValidationServiceSpec extends SpecBase with Matchers {

  /** Helper that builds a CrossFlowFailure with sensible defaults for body and headingKey. */
  private def fixtureFailure(
                              ruleId:     String,
                              affects:    ReturnSection,
                              targetPage: PageId,
                              messageKey: String = ""
                            ): CrossFlowFailure = {
    val key = if (messageKey.nonEmpty) messageKey else s"$ruleId.message"
    CrossFlowFailure(
      ruleId         = ruleId,
      affects        = affects,
      messageKey     = key,
      inlineErrorKey = key,
      body           = CrossFlowBody.Single(key),
      targets        = Seq(CrossFlowTarget(targetPage, "value")),
      headingKey     = "crossflow.relief.heading"
    )
  }

  private class AlwaysFireRule(
                                val id:     String,
                                val affects: ReturnSection = ReturnSection.Transaction,
                                targetPage: PageId         = Pages.ReliefReason
                              ) extends CrossFlowRule {
    val inputs:  Set[ReturnSection]   = Set(affects)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(targetPage, "value"))
    def validate(ua: UserAnswers): Option[CrossFlowFailure] =
      Some(fixtureFailure(id, affects, targetPage))
  }

  private class NeverFireRule(val id: String) extends CrossFlowRule {
    val affects: ReturnSection        = ReturnSection.Transaction
    val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))
    def validate(ua: UserAnswers): Option[CrossFlowFailure] = None
  }

  private class AlwaysFireLandRule(
                                    val id:         String,
                                    val affects:    ReturnSection = ReturnSection.Land,
                                    targetPage:     PageId        = Pages.LandAuthorityCode
                                  ) extends LandRule {
    val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land, ReturnSection.Transaction)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(targetPage, "value"))
    def validate(land: Land, ua: UserAnswers): Option[CrossFlowFailure] =
      Some(fixtureFailure(id, affects, targetPage))
  }

  private class NeverFireLandRule(val id: String) extends LandRule {
    val affects: ReturnSection        = ReturnSection.Land
    val inputs:  Set[ReturnSection]   = Set(ReturnSection.Land)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value"))
    def validate(land: Land, ua: UserAnswers): Option[CrossFlowFailure] = None
  }

  private def alwaysFire(
                          id:         String,
                          section:    ReturnSection = ReturnSection.Transaction,
                          targetPage: PageId        = Pages.ReliefReason
                        ): CrossFlowRule = new AlwaysFireRule(id, section, targetPage)

  private def neverFire(id: String): CrossFlowRule = new NeverFireRule(id)

  private def alwaysFireLand(
                              id:         String,
                              targetPage: PageId = Pages.LandAuthorityCode
                            ): LandRule = new AlwaysFireLandRule(id, targetPage = targetPage)

  private def neverFireLand(id: String): LandRule = new NeverFireLandRule(id)

  "validateAll" - {

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.validateAll(emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")), Set.empty)

      service.validateAll(emptyUserAnswers) mustBe Nil
    }

    "must return failures for every rule that fires" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1"), alwaysFire("R2")), Set.empty)

      val ids = service.validateAll(emptyUserAnswers).map(_.ruleId)
      ids must contain allOf ("R1", "R2")
    }

    "must return only the firing rules when some pass and some fail" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("FIRE"), neverFire("PASS")), Set.empty)

      service.validateAll(emptyUserAnswers).map(_.ruleId) mustBe Seq("FIRE")
    }
  }

  "failuresAffecting" - {

    "must return only failures that affect the given section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN",  section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ), Set.empty)

      service.failuresAffecting(ReturnSection.Transaction, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("TXN")

      service.failuresAffecting(ReturnSection.Land, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("LAND")
    }

    "must return an empty list for sections with no failing rules" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("TXN")), Set.empty)

      service.failuresAffecting(ReturnSection.Vendor, emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.failuresAffecting(ReturnSection.Transaction, emptyUserAnswers) mustBe Nil
    }
  }

  "failuresForPage" - {

    "must return only failures that target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("ON-RELIEF", targetPage = Pages.ReliefReason),
        alwaysFire("ON-DATE",   targetPage = Pages.EffectiveDate)
      ), Set.empty)

      service.failuresForPage(Pages.ReliefReason, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("ON-RELIEF")

      service.failuresForPage(Pages.EffectiveDate, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("ON-DATE")
    }

    "must return failures sorted by ruleId for deterministic ordering" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("Z-rule"),
        alwaysFire("A-rule"),
        alwaysFire("M-rule")
      ), Set.empty)

      service.failuresForPage(Pages.ReliefReason, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("A-rule", "M-rule", "Z-rule")
    }

    "must return an empty list when no rules target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason)
      ), Set.empty)

      service.failuresForPage(Pages.EffectiveDate, emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.failuresForPage(Pages.ReliefReason, emptyUserAnswers) mustBe Nil
    }
  }

  "sectionStatuses" - {

    "must return an empty map when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1")), Set.empty)

      service.sectionStatuses(emptyUserAnswers) mustBe empty
    }

    "must return an empty map when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.sectionStatuses(emptyUserAnswers) mustBe empty
    }

    "must group failures by their affected section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN1", section = ReturnSection.Transaction),
        alwaysFire("TXN2", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ), Set.empty)

      val statuses = service.sectionStatuses(emptyUserAnswers)

      statuses(ReturnSection.Transaction).ruleIds must contain allOf ("TXN1", "TXN2")
      statuses(ReturnSection.Land).ruleIds        mustBe Seq("LAND")
    }

    "must set hasFailures = true for sections with failing rules" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1")), Set.empty)

      service.sectionStatuses(emptyUserAnswers)(ReturnSection.Transaction).hasFailures mustBe true
    }

    "must include the message keys of every failing rule for that section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", section = ReturnSection.Transaction),
        alwaysFire("R2", section = ReturnSection.Transaction)
      ), Set.empty)

      val keys = service.sectionStatuses(emptyUserAnswers)(ReturnSection.Transaction).messageKeys
      keys must contain allOf ("R1.message", "R2.message")
    }
  }

  "isSectionValid" - {

    "must return true when no rules affect the section" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("LAND", section = ReturnSection.Land)), Set.empty)

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }

    "must return true when every rule for the section passes" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("R1"),
        neverFire("R2")
      ), Set.empty)

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }

    "must return false when any rule for the section fires" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("PASS"),
        alwaysFire("FAIL")
      ), Set.empty)

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe false
    }

    "must return true for sections with no registered rules at all" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }
  }

  "invalidSections" - {

    "must return an empty set when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")), Set.empty)

      service.invalidSections(emptyUserAnswers) mustBe empty
    }

    "must return an empty set when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.invalidSections(emptyUserAnswers) mustBe empty
    }

    "must include each section that has at least one failing rule" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ), Set.empty)

      service.invalidSections(emptyUserAnswers) mustBe Set(ReturnSection.Transaction, ReturnSection.Land)
    }

    "must deduplicate when multiple rules affect the same section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN1", section = ReturnSection.Transaction),
        alwaysFire("TXN2", section = ReturnSection.Transaction)
      ), Set.empty)

      service.invalidSections(emptyUserAnswers) mustBe Set(ReturnSection.Transaction)
    }
  }

  "hasFailures" - {

    "must return false when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.hasFailures(emptyUserAnswers) mustBe false
    }

    "must return false when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")), Set.empty)

      service.hasFailures(emptyUserAnswers) mustBe false
    }

    "must return true when any rule fires" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("PASS"),
        alwaysFire("FAIL")
      ), Set.empty)

      service.hasFailures(emptyUserAnswers) mustBe true
    }

    "must return true when every rule fires" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ), Set.empty)

      service.hasFailures(emptyUserAnswers) mustBe true
    }
  }

  "sectionStatus" - {

    "must return an empty status when no rules fire for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("LAND", section = ReturnSection.Land)
      ), Set.empty)

      val status = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers)

      status.section     mustBe ReturnSection.Transaction
      status.hasFailures mustBe false
      status.ruleIds     mustBe empty
      status.messageKeys mustBe empty
      status.targets     mustBe empty
    }

    "must set hasFailures = true when at least one rule fires" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1")), Set.empty)

      service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).hasFailures mustBe true
    }

    "must include the ruleId of every failing rule for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ), Set.empty)

      val ruleIds = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).ruleIds
      ruleIds must contain allOf("R1", "R2")
    }

    "must include the message key of every failing rule for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ), Set.empty)

      val keys = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).messageKeys
      keys must contain allOf("R1.message", "R2.message")
    }

    "must deduplicate message keys when multiple rules share the same one" in {
      val sharedMessage = "shared.message"

      val sharedRuleA = new AlwaysFireRule("R1") {
        override val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))

        override def validate(ua: UserAnswers): Option[CrossFlowFailure] =
          Some(fixtureFailure(
            ruleId     = "R1",
            affects    = ReturnSection.Transaction,
            targetPage = Pages.ReliefReason,
            messageKey = sharedMessage
          ))
      }

      val sharedRuleB = new AlwaysFireRule("R2") {
        override val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))

        override def validate(ua: UserAnswers): Option[CrossFlowFailure] =
          Some(fixtureFailure(
            ruleId     = "R2",
            affects    = ReturnSection.Transaction,
            targetPage = Pages.ReliefReason,
            messageKey = sharedMessage
          ))
      }

      val service = new CrossFlowValidationService(Set(sharedRuleA, sharedRuleB), Set.empty)

      service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).messageKeys mustBe Seq(sharedMessage)
    }

    "must deduplicate targets when multiple rules share the same one" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason),
        alwaysFire("R2", targetPage = Pages.ReliefReason)
      ), Set.empty)

      val targets = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).targets
      targets.count(_.page == Pages.ReliefReason) mustBe 1
    }

    "must include targets from every distinct page when rules target different pages" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason),
        alwaysFire("R2", targetPage = Pages.EffectiveDate)
      ), Set.empty)

      val pages = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).targets.map(_.page)
      pages must contain allOf(Pages.ReliefReason, Pages.EffectiveDate)
    }
  }

  "rulesForPage" - {

    "must return every rule whose targets include the given page" in {
      val r1 = alwaysFire("R1", targetPage = Pages.ReliefReason)
      val r2 = alwaysFire("R2", targetPage = Pages.ReliefReason)
      val r3 = alwaysFire("R3", targetPage = Pages.EffectiveDate)
      val service = new CrossFlowValidationService(Set(r1, r2, r3), Set.empty)

      val ids = service.rulesForPage(Pages.ReliefReason).map(_.id)
      ids must contain allOf("R1", "R2")
      ids must not contain "R3"
    }

    "must return an empty seq when no rules target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason)
      ), Set.empty)

      service.rulesForPage(Pages.EffectiveDate) mustBe empty
    }

    "must return an empty seq when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.rulesForPage(Pages.ReliefReason) mustBe empty
    }
  }

  "rulesTriggeredBy" - {

    "must return every rule whose inputs include the given section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ), Set.empty)

      service.rulesTriggeredBy(ReturnSection.Transaction).map(_.id) mustBe Seq("TXN")
      service.rulesTriggeredBy(ReturnSection.Land).map(_.id)        mustBe Seq("LAND")
    }

    "must return an empty seq when no rule lists the given section as an input" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction)
      ), Set.empty)

      service.rulesTriggeredBy(ReturnSection.Vendor) mustBe empty
    }

    "must return an empty seq when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.rulesTriggeredBy(ReturnSection.Transaction) mustBe empty
    }

    "must return rules whose inputs cover multiple sections, on any matching section" in {
      val multiInputRule = new AlwaysFireRule("MULTI") {
        override val inputs: Set[ReturnSection] = Set(ReturnSection.Transaction, ReturnSection.Land)
      }
      val service = new CrossFlowValidationService(Set(multiInputRule), Set.empty)

      service.rulesTriggeredBy(ReturnSection.Transaction).map(_.id) mustBe Seq("MULTI")
      service.rulesTriggeredBy(ReturnSection.Land).map(_.id)        mustBe Seq("MULTI")
    }
  }

  "landFailures" - {

    "must return an empty list when no land rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.landFailures(emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no land rules fire" in {
      val service = new CrossFlowValidationService(Set.empty, Set(neverFireLand("R1")))

      service.landFailures(emptyUserAnswers) mustBe Nil
    }

    "must include session land alongside committed lands when a session land exists" in {
      val committedLand = Land(landID = Some("LND001"))

      val sessionLand = Json.obj(
        "landCurrent" -> Json.obj(
          "landId"                           -> "SESSION-LAND",
          "propertyType"                     -> "03",
          "landInterestTransferredOrCreated" -> "FGS",
          "landAddress" -> Json.obj(
            "houseNumber"      -> JsNull,
            "line1"            -> "1 Test Street",
            "line2"            -> "Test Town",
            "line3"            -> JsNull,
            "line4"            -> JsNull,
            "line5"            -> JsNull,
            "postcode"         -> "AB1 2CD",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "UK"
            ),
            "addressValidated" -> true
          ),
          "localAuthorityCode"              -> "0220",
          "landRegisteredHmRegistry"        -> false,
          "landAddNlpgUprn"                 -> false,
          "landSendingPlanByPost"           -> false,
          "landMineralsOrMineralRights"     -> false,
          "agriculturalOrDevelopmentalLand" -> false
        )
      )

      val ua = emptyUserAnswers.copy(
        fullReturn = Some(emptyFullReturn.copy(land = Some(Seq(committedLand)))),
        data       = sessionLand
      )

      val service = new CrossFlowValidationService(Set.empty, Set(alwaysFireLand("L1")))

      val resultIds = service.landFailures(ua).map(_._1.landID.getOrElse(""))

      resultIds      must contain ("LND001")
      resultIds      must contain ("SESSION-LAND")
      resultIds.size mustBe 2
    }
  }

  "landFailuresGrouped" - {

    "must return an empty list when no land rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty, Set.empty)

      service.landFailuresGrouped(emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no land rules fire" in {
      val service = new CrossFlowValidationService(Set.empty, Set(neverFireLand("R1")))

      service.landFailuresGrouped(emptyUserAnswers) mustBe Nil
    }

    "must group failures by land" in {
      val landA = Land(landID = Some("LND001"))
      val landB = Land(landID = Some("LND002"))
      val ua    = emptyUserAnswers.copy(
        fullReturn = Some(emptyFullReturn.copy(land = Some(Seq(landA, landB))))
      )

      val service = new CrossFlowValidationService(Set.empty, Set(
        alwaysFireLand("L1"),
        alwaysFireLand("L2")
      ))

      val grouped = service.landFailuresGrouped(ua)
      grouped.map(_._1.landID.getOrElse("")) must contain allOf ("LND001", "LND002")
      grouped.foreach { case (_, fs) =>
        fs.map(_.ruleId) must contain allOf ("L1", "L2")
      }
    }

    "must read committed lands only, not session land" in {
      val committedLand = Land(landID = Some("LND001"))

      // Session land has a different landId so we can prove it's not in the result.
      val sessionLand = Json.obj(
        "landCurrent" -> Json.obj(
          "landId"                           -> "SESSION-ONLY",
          "propertyType"                     -> "03",
          "landInterestTransferredOrCreated" -> "FGS",
          "landAddress" -> Json.obj(
            "houseNumber"      -> JsNull,
            "line1"            -> "1 Test Street",
            "line2"            -> "Test Town",
            "line3"            -> JsNull,
            "line4"            -> JsNull,
            "line5"            -> JsNull,
            "postcode"         -> "AB1 2CD",
            "country" -> Json.obj(
              "code" -> "GB",
              "name" -> "UK"
            ),
            "addressValidated" -> true
          ),
          "localAuthorityCode"              -> "0220",
          "landRegisteredHmRegistry"        -> false,
          "landAddNlpgUprn"                 -> false,
          "landSendingPlanByPost"           -> false,
          "landMineralsOrMineralRights"     -> false,
          "agriculturalOrDevelopmentalLand" -> false
        )
      )

      val ua = emptyUserAnswers.copy(
        fullReturn = Some(emptyFullReturn.copy(land = Some(Seq(committedLand)))),
        data       = sessionLand
      )

      val service = new CrossFlowValidationService(Set.empty, Set(alwaysFireLand("L1")))

      val grouped   = service.landFailuresGrouped(ua)
      val resultIds = grouped.map(_._1.landID.getOrElse(""))

      resultIds    must contain ("LND001")
      resultIds    must not contain "SESSION-ONLY"
      grouped.size mustBe 1
    }
  }

  "sectionStatuses with land rules" - {

    "must include the Land section when a land rule fires against a committed land" in {
      val landA = Land(landID = Some("LND001"))
      val ua    = emptyUserAnswers.copy(
        fullReturn = Some(emptyFullReturn.copy(land = Some(Seq(landA))))
      )

      val service = new CrossFlowValidationService(Set.empty, Set(alwaysFireLand("L1")))

      val statuses = service.sectionStatuses(ua)
      statuses.contains(ReturnSection.Land)    mustBe true
      statuses(ReturnSection.Land).hasFailures mustBe true
      statuses(ReturnSection.Land).ruleIds     must contain ("L1")
    }

    "must merge land rule failures with cross-flow rule failures on the Land section" in {
      val landA = Land(landID = Some("LND001"))
      val ua    = emptyUserAnswers.copy(
        fullReturn = Some(emptyFullReturn.copy(land = Some(Seq(landA))))
      )

      val service = new CrossFlowValidationService(
        Set(alwaysFire("XF-LAND", section = ReturnSection.Land)),
        Set(alwaysFireLand("L1"))
      )

      val landStatus = service.sectionStatuses(ua)(ReturnSection.Land)
      landStatus.ruleIds must contain allOf ("XF-LAND", "L1")
    }
  }
}