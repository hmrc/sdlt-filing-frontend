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
import models.UserAnswers
import org.scalatest.matchers.must.Matchers
import services.crossflow.{
  CrossFlowFailure,
  CrossFlowRule,
  CrossFlowTarget,
  Pages,
  PageId,
  ReturnSection
}

class CrossFlowValidationServiceSpec extends SpecBase with Matchers {

  private class AlwaysFireRule(
                                val id:     String,
                                val affects: ReturnSection = ReturnSection.Transaction,
                                targetPage: PageId         = Pages.ReliefReason
                              ) extends CrossFlowRule {
    val inputs:  Set[ReturnSection]   = Set(affects)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(targetPage, "value"))
    def validate(ua: UserAnswers): Option[CrossFlowFailure] =
      Some(CrossFlowFailure(id, affects, s"$id.message", targets))
  }

  private class NeverFireRule(val id: String) extends CrossFlowRule {
    val affects: ReturnSection        = ReturnSection.Transaction
    val inputs:  Set[ReturnSection]   = Set(ReturnSection.Transaction)
    val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))
    def validate(ua: UserAnswers): Option[CrossFlowFailure] = None
  }

  private def alwaysFire(
                          id:         String,
                          section:    ReturnSection = ReturnSection.Transaction,
                          targetPage: PageId        = Pages.ReliefReason
                        ): CrossFlowRule = new AlwaysFireRule(id, section, targetPage)

  private def neverFire(id: String): CrossFlowRule = new NeverFireRule(id)

  "validateAll" - {

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.validateAll(emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")))

      service.validateAll(emptyUserAnswers) mustBe Nil
    }

    "must return failures for every rule that fires" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1"), alwaysFire("R2")))

      val ids = service.validateAll(emptyUserAnswers).map(_.ruleId)
      ids must contain allOf ("R1", "R2")
    }

    "must return only the firing rules when some pass and some fail" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("FIRE"), neverFire("PASS")))

      service.validateAll(emptyUserAnswers).map(_.ruleId) mustBe Seq("FIRE")
    }
  }

  "failuresAffecting" - {

    "must return only failures that affect the given section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN",  section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ))

      service.failuresAffecting(ReturnSection.Transaction, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("TXN")

      service.failuresAffecting(ReturnSection.Land, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("LAND")
    }

    "must return an empty list for sections with no failing rules" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("TXN")))

      service.failuresAffecting(ReturnSection.Vendor, emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.failuresAffecting(ReturnSection.Transaction, emptyUserAnswers) mustBe Nil
    }
  }

  "failuresForPage" - {

    "must return only failures that target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("ON-RELIEF", targetPage = Pages.ReliefReason),
        alwaysFire("ON-DATE",   targetPage = Pages.EffectiveDate)
      ))

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
      ))

      service.failuresForPage(Pages.ReliefReason, emptyUserAnswers)
        .map(_.ruleId) mustBe Seq("A-rule", "M-rule", "Z-rule")
    }

    "must return an empty list when no rules target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason)
      ))

      service.failuresForPage(Pages.EffectiveDate, emptyUserAnswers) mustBe Nil
    }

    "must return an empty list when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.failuresForPage(Pages.ReliefReason, emptyUserAnswers) mustBe Nil
    }
  }

  "sectionStatuses" - {

    "must return an empty map when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1")))

      service.sectionStatuses(emptyUserAnswers) mustBe empty
    }

    "must return an empty map when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.sectionStatuses(emptyUserAnswers) mustBe empty
    }

    "must group failures by their affected section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN1", section = ReturnSection.Transaction),
        alwaysFire("TXN2", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ))

      val statuses = service.sectionStatuses(emptyUserAnswers)

      statuses(ReturnSection.Transaction).ruleIds must contain allOf ("TXN1", "TXN2")
      statuses(ReturnSection.Land).ruleIds        mustBe Seq("LAND")
    }

    "must set hasFailures = true for sections with failing rules" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1")))

      service.sectionStatuses(emptyUserAnswers)(ReturnSection.Transaction).hasFailures mustBe true
    }

    "must include the message keys of every failing rule for that section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", section = ReturnSection.Transaction),
        alwaysFire("R2", section = ReturnSection.Transaction)
      ))

      val keys = service.sectionStatuses(emptyUserAnswers)(ReturnSection.Transaction).messageKeys
      keys must contain allOf ("R1.message", "R2.message")
    }
  }

  // ---- isSectionValid --------------------------------------------------------------

  "isSectionValid" - {

    "must return true when no rules affect the section" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("LAND", section = ReturnSection.Land)))

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }

    "must return true when every rule for the section passes" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("R1"),
        neverFire("R2")
      ))

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }

    "must return false when any rule for the section fires" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("PASS"),
        alwaysFire("FAIL")
      ))

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe false
    }

    "must return true for sections with no registered rules at all" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.isSectionValid(ReturnSection.Transaction, emptyUserAnswers) mustBe true
    }
  }

  // ---- invalidSections -------------------------------------------------------------

  "invalidSections" - {

    "must return an empty set when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")))

      service.invalidSections(emptyUserAnswers) mustBe empty
    }

    "must return an empty set when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.invalidSections(emptyUserAnswers) mustBe empty
    }

    "must include each section that has at least one failing rule" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ))

      service.invalidSections(emptyUserAnswers) mustBe Set(ReturnSection.Transaction, ReturnSection.Land)
    }

    "must deduplicate when multiple rules affect the same section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN1", section = ReturnSection.Transaction),
        alwaysFire("TXN2", section = ReturnSection.Transaction)
      ))

      service.invalidSections(emptyUserAnswers) mustBe Set(ReturnSection.Transaction)
    }
  }

  // ---- hasFailures -----------------------------------------------------------------

  "hasFailures" - {

    "must return false when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.hasFailures(emptyUserAnswers) mustBe false
    }

    "must return false when no rules fire" in {
      val service = new CrossFlowValidationService(Set(neverFire("R1"), neverFire("R2")))

      service.hasFailures(emptyUserAnswers) mustBe false
    }

    "must return true when any rule fires" in {
      val service = new CrossFlowValidationService(Set(
        neverFire("PASS"),
        alwaysFire("FAIL")
      ))

      service.hasFailures(emptyUserAnswers) mustBe true
    }

    "must return true when every rule fires" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ))

      service.hasFailures(emptyUserAnswers) mustBe true
    }
  }

  // ---- sectionStatus ---------------------------------------------------------------

  "sectionStatus" - {

    "must return an empty status when no rules fire for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("LAND", section = ReturnSection.Land)
      ))

      val status = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers)

      status.section mustBe ReturnSection.Transaction
      status.hasFailures mustBe false
      status.ruleIds mustBe empty
      status.messageKeys mustBe empty
      status.targets mustBe empty
    }

    "must set hasFailures = true when at least one rule fires" in {
      val service = new CrossFlowValidationService(Set(alwaysFire("R1")))

      service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).hasFailures mustBe true
    }

    "must include the ruleId of every failing rule for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ))

      val ruleIds = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).ruleIds
      ruleIds must contain allOf("R1", "R2")
    }

    "must include the message key of every failing rule for the section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1"),
        alwaysFire("R2")
      ))

      val keys = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).messageKeys
      keys must contain allOf("R1.message", "R2.message")
    }

    "must deduplicate message keys when multiple rules share the same one" in {
      val sharedMessage = "shared.message"

      val sharedRuleA = new AlwaysFireRule("R1") {
        override val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))

        override def validate(ua: UserAnswers): Option[CrossFlowFailure] =
          Some(CrossFlowFailure("R1", ReturnSection.Transaction, sharedMessage, targets))
      }

      val sharedRuleB = new AlwaysFireRule("R2") {
        override val targets: Seq[CrossFlowTarget] = Seq(CrossFlowTarget(Pages.ReliefReason, "value"))

        override def validate(ua: UserAnswers): Option[CrossFlowFailure] =
          Some(CrossFlowFailure("R2", ReturnSection.Transaction, sharedMessage, targets))
      }

      val service = new CrossFlowValidationService(Set(sharedRuleA, sharedRuleB))

      service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).messageKeys mustBe Seq(sharedMessage)
    }

    "must deduplicate targets when multiple rules share the same one" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason),
        alwaysFire("R2", targetPage = Pages.ReliefReason)
      ))

      val targets = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).targets
      targets.count(_.page == Pages.ReliefReason) mustBe 1
    }

    "must include targets from every distinct page when rules target different pages" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason),
        alwaysFire("R2", targetPage = Pages.EffectiveDate)
      ))

      val pages = service.sectionStatus(ReturnSection.Transaction, emptyUserAnswers).targets.map(_.page)
      pages must contain allOf(Pages.ReliefReason, Pages.EffectiveDate)
    }
  }

  // ---- rulesForPage ----------------------------------------------------------------

  "rulesForPage" - {

    "must return every rule whose targets include the given page" in {
      val r1 = alwaysFire("R1", targetPage = Pages.ReliefReason)
      val r2 = alwaysFire("R2", targetPage = Pages.ReliefReason)
      val r3 = alwaysFire("R3", targetPage = Pages.EffectiveDate)
      val service = new CrossFlowValidationService(Set(r1, r2, r3))

      val ids = service.rulesForPage(Pages.ReliefReason).map(_.id)
      ids must contain allOf("R1", "R2")
      ids must not contain "R3"
    }

    "must return an empty seq when no rules target the given page" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("R1", targetPage = Pages.ReliefReason)
      ))

      service.rulesForPage(Pages.EffectiveDate) mustBe empty
    }

    "must return an empty seq when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.rulesForPage(Pages.ReliefReason) mustBe empty
    }
  }

  // ---- rulesTriggeredBy ------------------------------------------------------------

  "rulesTriggeredBy" - {

    "must return every rule whose inputs include the given section" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction),
        alwaysFire("LAND", section = ReturnSection.Land)
      ))

      service.rulesTriggeredBy(ReturnSection.Transaction).map(_.id) mustBe Seq("TXN")
      service.rulesTriggeredBy(ReturnSection.Land).map(_.id) mustBe Seq("LAND")
    }

    "must return an empty seq when no rule lists the given section as an input" in {
      val service = new CrossFlowValidationService(Set(
        alwaysFire("TXN", section = ReturnSection.Transaction)
      ))

      service.rulesTriggeredBy(ReturnSection.Vendor) mustBe empty
    }

    "must return an empty seq when no rules are registered" in {
      val service = new CrossFlowValidationService(Set.empty)

      service.rulesTriggeredBy(ReturnSection.Transaction) mustBe empty
    }

    "must return rules whose inputs cover multiple sections, on any matching section" in {
      val multiInputRule = new AlwaysFireRule("MULTI") {
        override val inputs: Set[ReturnSection] = Set(ReturnSection.Transaction, ReturnSection.Land)
      }
      val service = new CrossFlowValidationService(Set(multiInputRule))

      service.rulesTriggeredBy(ReturnSection.Transaction).map(_.id) mustBe Seq("MULTI")
      service.rulesTriggeredBy(ReturnSection.Land).map(_.id) mustBe Seq("MULTI")
    }
  }
}