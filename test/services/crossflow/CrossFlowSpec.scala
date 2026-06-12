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

package services.crossflow

import base.SpecBase
import models.UserAnswers

class CrossFlowSpec extends SpecBase {

  private val reliefTarget = CrossFlowTarget(Pages.ReliefReason,  "value")
  private val dateTarget   = CrossFlowTarget(Pages.EffectiveDate, "value")

  "PageId" - {

    "must be equal when values match" in {
      PageId("foo") mustBe PageId("foo")
    }

    "must not be equal when values differ" in {
      PageId("foo") must not be PageId("bar")
    }
  }

  "Pages" - {

    "must expose the relief reason page id" in {
      Pages.ReliefReason mustBe PageId("reliefReason")
    }

    "must expose the effective date page id" in {
      Pages.EffectiveDate mustBe PageId("effectiveDate")
    }

    "must expose the land property type page id" in {
      Pages.LandPropertyType mustBe PageId("landPropertyType")
    }

    "must expose the contract date page id" in {
      Pages.ContractDate mustBe PageId("contractDate")
    }
  }

  "Fields" - {

    "must use 'value' as the standard form field key" in {
      Fields.ReliefReason  mustBe "value"
      Fields.EffectiveDate mustBe "value"
      Fields.PropertyType  mustBe "value"
      Fields.ContractDate  mustBe "value"
    }
  }

  "CrossFlowFailure.targetsOn" - {

    "must return only the targets matching the given page" in {
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Seq(reliefTarget, dateTarget)
      )

      failure.targetsOn(Pages.ReliefReason) mustBe Seq(reliefTarget)
    }

    "must return an empty seq when no targets match the given page" in {
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Seq(reliefTarget)
      )

      failure.targetsOn(Pages.EffectiveDate) mustBe empty
    }

    "must return every matching target when the same page appears more than once" in {
      val anotherReliefTarget = CrossFlowTarget(Pages.ReliefReason, "altField")
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Seq(reliefTarget, anotherReliefTarget, dateTarget)
      )

      failure.targetsOn(Pages.ReliefReason) mustBe Seq(reliefTarget, anotherReliefTarget)
    }
  }

  "CrossFlowFailure.appearsOn" - {

    "must return true when at least one target matches the given page" in {
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Seq(reliefTarget, dateTarget)
      )

      failure.appearsOn(Pages.ReliefReason) mustBe true
    }

    "must return false when no target matches the given page" in {
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Seq(reliefTarget)
      )

      failure.appearsOn(Pages.EffectiveDate) mustBe false
    }

    "must return false when the failure has no targets" in {
      val failure = CrossFlowFailure(
        ruleId         = "R1",
        affects        = ReturnSection.Transaction,
        messageKey     = "key",
        inlineErrorKey = "key",
        targets        = Nil
      )

      failure.appearsOn(Pages.ReliefReason) mustBe false
    }
  }

  "SectionStatus.primaryUrl" - {

    val urlOf: PageId => String = {
      case Pages.ReliefReason  => "/relief-reason"
      case Pages.EffectiveDate => "/effective-date"
      case _                   => "/other"
    }

    "must return the URL of the first target's page" in {
      val status = SectionStatus(
        section     = ReturnSection.Transaction,
        hasFailures = true,
        ruleIds     = Seq("R1"),
        messageKeys = Seq("key"),
        targets     = Seq(reliefTarget, dateTarget)
      )

      status.primaryUrl("/default", urlOf) mustBe "/relief-reason"
    }

    "must return the default when there are no targets" in {
      val status = SectionStatus(
        section     = ReturnSection.Transaction,
        hasFailures = false,
        ruleIds     = Nil,
        messageKeys = Nil,
        targets     = Nil
      )

      status.primaryUrl("/default", urlOf) mustBe "/default"
    }

    "must use the first target even when later targets would map to different URLs" in {
      val status = SectionStatus(
        section     = ReturnSection.Transaction,
        hasFailures = true,
        ruleIds     = Seq("R1"),
        messageKeys = Seq("key"),
        targets     = Seq(dateTarget, reliefTarget)
      )

      status.primaryUrl("/default", urlOf) mustBe "/effective-date"
    }
  }

  "SectionStatus.empty" - {

    "must produce a status for the given section with no failures" in {
      val status = SectionStatus.empty(ReturnSection.Land)

      status.section     mustBe ReturnSection.Land
      status.hasFailures mustBe false
      status.ruleIds     mustBe empty
      status.messageKeys mustBe empty
      status.targets     mustBe empty
    }
  }

  "GuardRule.validate" - {

    abstract class TestRule extends GuardRule {
      val id      = "TEST"
      val affects = ReturnSection.Transaction
      val inputs  = Set(ReturnSection.Transaction)
      val targets = Seq(reliefTarget)
      protected def messageKey = "test.message"
    }

    "must produce no failure when appliesTo is false" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = false
        protected def isValid(ua: UserAnswers)   = true
      }

      rule.validate(emptyUserAnswers) mustBe None
    }

    "must produce no failure when appliesTo is false even if isValid is also false" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = false
        protected def isValid(ua: UserAnswers)   = false
      }

      rule.validate(emptyUserAnswers) mustBe None
    }

    "must produce no failure when appliesTo is true and isValid is true" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = true
        protected def isValid(ua: UserAnswers)   = true
      }

      rule.validate(emptyUserAnswers) mustBe None
    }

    "must produce a failure when appliesTo is true and isValid is false" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = true
        protected def isValid(ua: UserAnswers)   = false
      }

      rule.validate(emptyUserAnswers) mustBe Some(
        CrossFlowFailure(
          ruleId         = "TEST",
          affects        = ReturnSection.Transaction,
          messageKey     = "test.message",
          inlineErrorKey = "test.message",
          targets        = Seq(reliefTarget),
          args           = Nil
        )
      )
    }

    "must default inlineErrorKey to messageKey when not overridden" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = true
        protected def isValid(ua: UserAnswers)   = false
      }

      val failure = rule.validate(emptyUserAnswers).value

      failure.inlineErrorKey mustBe failure.messageKey
      failure.inlineErrorKey mustBe "test.message"
    }

    "must use the overridden inlineErrorKey when one is provided" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers)        = true
        protected def isValid(ua: UserAnswers)          = false
        protected override def inlineErrorKey: String   = "test.inline"
      }

      val failure = rule.validate(emptyUserAnswers).value

      failure.messageKey     mustBe "test.message"
      failure.inlineErrorKey mustBe "test.inline"
    }

    "must pass args through to the produced failure" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers)     = true
        protected def isValid(ua: UserAnswers)       = false
        override protected def args(ua: UserAnswers) = Seq("hello", 42)
      }

      rule.validate(emptyUserAnswers).map(_.args) mustBe Some(Seq("hello", 42))
    }
  }
}