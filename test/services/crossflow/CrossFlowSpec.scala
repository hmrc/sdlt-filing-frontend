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

  private def failureWith(
                           targets: Seq[CrossFlowTarget],
                           ruleId: String = "R1",
                           affects: ReturnSection = ReturnSection.Transaction,
                           messageKey: String = "key",
                           inlineErrorKey: String = "key",
                           headingKey: String = "crossflow.relief.heading"
                         ): CrossFlowFailure =
    CrossFlowFailure(
      ruleId         = ruleId,
      affects        = affects,
      messageKey     = messageKey,
      inlineErrorKey = inlineErrorKey,
      body           = CrossFlowBody.Single(messageKey),
      targets        = targets,
      headingKey     = headingKey
    )

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

    "must expose the land authority code page id" in {
      Pages.LandAuthorityCode mustBe PageId("landAuthorityCode")
    }

    "must expose the land postcode page id" in {
      Pages.LandPostcode mustBe PageId("landPostcode")
    }
  }

  "Fields" - {

    "must use 'value' as the standard form field key" in {
      Fields.ReliefReason       mustBe "value"
      Fields.EffectiveDate      mustBe "value"
      Fields.PropertyType       mustBe "value"
      Fields.ContractDate       mustBe "value"
      Fields.LandAuthorityCode  mustBe "value"
      Fields.LandPostcode       mustBe "value"
    }
  }

  "CrossFlowFailure.targetsOn" - {

    "must return only the targets matching the given page" in {
      val failure = failureWith(targets = Seq(reliefTarget, dateTarget))

      failure.targetsOn(Pages.ReliefReason) mustBe Seq(reliefTarget)
    }

    "must return an empty seq when no targets match the given page" in {
      val failure = failureWith(targets = Seq(reliefTarget))

      failure.targetsOn(Pages.EffectiveDate) mustBe empty
    }

    "must return every matching target when the same page appears more than once" in {
      val anotherReliefTarget = CrossFlowTarget(Pages.ReliefReason, "altField")
      val failure = failureWith(targets = Seq(reliefTarget, anotherReliefTarget, dateTarget))

      failure.targetsOn(Pages.ReliefReason) mustBe Seq(reliefTarget, anotherReliefTarget)
    }
  }

  "CrossFlowFailure.appearsOn" - {

    "must return true when at least one target matches the given page" in {
      val failure = failureWith(targets = Seq(reliefTarget, dateTarget))

      failure.appearsOn(Pages.ReliefReason) mustBe true
    }

    "must return false when no target matches the given page" in {
      val failure = failureWith(targets = Seq(reliefTarget))

      failure.appearsOn(Pages.EffectiveDate) mustBe false
    }

    "must return false when the failure has no targets" in {
      val failure = failureWith(targets = Nil)

      failure.appearsOn(Pages.ReliefReason) mustBe false
    }
  }

  "CrossFlowBody" - {

    "Single must carry a message key" in {
      val body = CrossFlowBody.Single("some.key")

      body mustBe CrossFlowBody.Single("some.key")
    }

    "WithBullets must carry a lead key and a list of bullet keys" in {
      val body = CrossFlowBody.WithBullets(
        leadKey    = "lead.key",
        bulletKeys = Seq("opt1", "opt2")
      )

      body mustBe CrossFlowBody.WithBullets("lead.key", Seq("opt1", "opt2"))
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

      val failure = rule.validate(emptyUserAnswers).value

      failure.ruleId         mustBe "TEST"
      failure.affects        mustBe ReturnSection.Transaction
      failure.messageKey     mustBe "test.message"
      failure.inlineErrorKey mustBe "test.message"
      failure.targets        mustBe Seq(reliefTarget)
      failure.args           mustBe Nil
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

    "must default headingKey to the shared relief heading when not overridden" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = true
        protected def isValid(ua: UserAnswers)   = false
      }

      rule.validate(emptyUserAnswers).value.headingKey mustBe "crossflow.relief.heading"
    }

    "must use the overridden headingKey when one is provided" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers)     = true
        protected def isValid(ua: UserAnswers)       = false
        protected override def headingKey: String    = "custom.heading.key"
      }

      rule.validate(emptyUserAnswers).value.headingKey mustBe "custom.heading.key"
    }

    "must default body to a Single body referencing the messageKey" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers) = true
        protected def isValid(ua: UserAnswers)   = false
      }

      rule.validate(emptyUserAnswers).value.body mustBe CrossFlowBody.Single("test.message")
    }

    "must use the overridden body when one is provided" in {
      val rule = new TestRule {
        protected def appliesTo(ua: UserAnswers)     = true
        protected def isValid(ua: UserAnswers)       = false
        protected override def body: CrossFlowBody   = CrossFlowBody.WithBullets("intro", Seq("opt1", "opt2"))
      }

      rule.validate(emptyUserAnswers).value.body mustBe CrossFlowBody.WithBullets("intro", Seq("opt1", "opt2"))
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

  "LandGuardRule.validate" - {

    val testLand = models.Land(landID = Some("LND001"))

    abstract class TestLandRule extends LandGuardRule {
      val id      = "TEST-LAND"
      val affects = ReturnSection.Land
      val inputs  = Set(ReturnSection.Land)
      val targets = Seq(CrossFlowTarget(Pages.LandPropertyType, "value"))
      protected def messageKey = "test.land.message"
    }

    "must produce no failure when appliesTo is false" in {
      val rule = new TestLandRule {
        protected def appliesTo(land: models.Land, ua: UserAnswers) = false
        protected def isValid(land: models.Land, ua: UserAnswers)   = true
      }

      rule.validate(testLand, emptyUserAnswers) mustBe None
    }

    "must produce a failure when appliesTo is true and isValid is false" in {
      val rule = new TestLandRule {
        protected def appliesTo(land: models.Land, ua: UserAnswers) = true
        protected def isValid(land: models.Land, ua: UserAnswers)   = false
      }

      val failure = rule.validate(testLand, emptyUserAnswers).value

      failure.ruleId         mustBe "TEST-LAND"
      failure.affects        mustBe ReturnSection.Land
      failure.messageKey     mustBe "test.land.message"
      failure.inlineErrorKey mustBe "test.land.message"
    }

    "must default headingKey to the shared land heading when not overridden" in {
      val rule = new TestLandRule {
        protected def appliesTo(land: models.Land, ua: UserAnswers) = true
        protected def isValid(land: models.Land, ua: UserAnswers)   = false
      }

      rule.validate(testLand, emptyUserAnswers).value.headingKey mustBe "crossflow.land.heading"
    }

    "must use the overridden headingKey when one is provided" in {
      val rule = new TestLandRule {
        protected def appliesTo(land: models.Land, ua: UserAnswers) = true
        protected def isValid(land: models.Land, ua: UserAnswers)   = false
        protected override def headingKey: String                   = "custom.land.heading"
      }

      rule.validate(testLand, emptyUserAnswers).value.headingKey mustBe "custom.land.heading"
    }
  }
}