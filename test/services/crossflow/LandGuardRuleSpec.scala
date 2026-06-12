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
import models.{Land, UserAnswers}
import org.scalatest.matchers.must.Matchers

class LandGuardRuleSpec extends SpecBase with Matchers {


  private val testLand = Land(landID = Some("LND001"))
  
  private class StubLandGuardRule(
                                   appliesToResult: Boolean,
                                   isValidResult:   Boolean,
                                   override val id: String = "STUB-LAND-RULE",
                                   override val affects: ReturnSection = ReturnSection.Land,
                                   override val inputs:  Set[ReturnSection] = Set(ReturnSection.Land),
                                   override val targets: Seq[CrossFlowTarget] =
                                   Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value")),
                                   protected val messageKey: String = "stub.message",
                                   argsResult: Seq[Any] = Nil
                                 ) extends LandGuardRule {

    protected def appliesTo(land: Land, ua: UserAnswers): Boolean = appliesToResult
    protected def isValid(land: Land, ua: UserAnswers): Boolean   = isValidResult
    override protected def args(land: Land, ua: UserAnswers): Seq[Any] = argsResult
  }
  
  private class RecordingLandGuardRule(
                                        appliesToResult: Boolean = true,
                                        isValidResult:   Boolean = false
                                      ) extends LandGuardRule {

    var appliesToCalls: Seq[(Land, UserAnswers)] = Seq.empty
    var isValidCalls:   Seq[(Land, UserAnswers)] = Seq.empty
    var argsCalls:      Seq[(Land, UserAnswers)] = Seq.empty

    val id: String                          = "RECORDING-RULE"
    val affects: ReturnSection              = ReturnSection.Land
    val inputs:  Set[ReturnSection]         = Set(ReturnSection.Land)
    val targets: Seq[CrossFlowTarget]       = Seq(CrossFlowTarget(Pages.LandAuthorityCode, "value"))
    protected val messageKey: String        = "recording.message"

    protected def appliesTo(land: Land, ua: UserAnswers): Boolean = {
      appliesToCalls = appliesToCalls :+ (land -> ua)
      appliesToResult
    }
    protected def isValid(land: Land, ua: UserAnswers): Boolean = {
      isValidCalls = isValidCalls :+ (land -> ua)
      isValidResult
    }
    override protected def args(land: Land, ua: UserAnswers): Seq[Any] = {
      argsCalls = argsCalls :+ (land -> ua)
      Nil
    }
  }

  "LandGuardRule.validate" - {

    "must return None when appliesTo is false" in {
      val rule = new StubLandGuardRule(appliesToResult = false, isValidResult = true)

      rule.validate(testLand, emptyUserAnswers) mustBe None
    }

    "must return None when appliesTo is false even if isValid is also false" in {
      val rule = new StubLandGuardRule(appliesToResult = false, isValidResult = false)

      rule.validate(testLand, emptyUserAnswers) mustBe None
    }

    "must return None when appliesTo is true and isValid is also true" in {
      val rule = new StubLandGuardRule(appliesToResult = true, isValidResult = true)

      rule.validate(testLand, emptyUserAnswers) mustBe None
    }

    "must return a CrossFlowFailure when appliesTo is true and isValid is false" in {
      val rule = new StubLandGuardRule(appliesToResult = true, isValidResult = false)

      val result = rule.validate(testLand, emptyUserAnswers)

      result mustBe defined
    }

    "must include the rule's id on the returned failure" in {
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false,
        id              = "MY-RULE-ID"
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.ruleId mustBe "MY-RULE-ID"
    }

    "must include the rule's affects section on the returned failure" in {
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false,
        affects         = ReturnSection.Land
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.affects mustBe ReturnSection.Land
    }

    "must include the rule's messageKey on the returned failure" in {
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false,
        messageKey      = "custom.failure.message"
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.messageKey mustBe "custom.failure.message"
    }

    "must include the rule's targets on the returned failure" in {
      val customTargets = Seq(
        CrossFlowTarget(Pages.LandAuthorityCode, "value"),
        CrossFlowTarget(Pages.EffectiveDate, "value")
      )
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false,
        targets         = customTargets
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.targets mustBe customTargets
    }

    "must include the rule's args on the returned failure" in {
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false,
        argsResult      = Seq("arg1", 42, "third")
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.args mustBe Seq("arg1", 42, "third")
    }

    "must default args to Nil when not overridden" in {
      val rule = new StubLandGuardRule(
        appliesToResult = true,
        isValidResult   = false
      )

      val result = rule.validate(testLand, emptyUserAnswers).value

      result.args mustBe Nil
    }
  }

  "LandGuardRule" - {

    "must pass the supplied land to appliesTo" in {
      val rule = new RecordingLandGuardRule()
      val land = Land(landID = Some("LND-XYZ"))

      rule.validate(land, emptyUserAnswers)

      rule.appliesToCalls.map(_._1) must contain (land)
    }

    "must pass the supplied land to isValid when appliesTo returns true" in {
      val rule = new RecordingLandGuardRule(appliesToResult = true)
      val land = Land(landID = Some("LND-XYZ"))

      rule.validate(land, emptyUserAnswers)

      rule.isValidCalls.map(_._1) must contain (land)
    }

    "must not invoke isValid when appliesTo returns false" in {
      val rule = new RecordingLandGuardRule(appliesToResult = false)

      rule.validate(testLand, emptyUserAnswers)

      rule.isValidCalls mustBe empty
    }

    "must not invoke args when isValid returns true" in {
      val rule = new RecordingLandGuardRule(appliesToResult = true, isValidResult = true)

      rule.validate(testLand, emptyUserAnswers)

      rule.argsCalls mustBe empty
    }

    "must invoke args once when appliesTo true and isValid false" in {
      val rule = new RecordingLandGuardRule(appliesToResult = true, isValidResult = false)

      rule.validate(testLand, emptyUserAnswers)

      rule.argsCalls.size mustBe 1
    }

    "must pass the supplied UserAnswers to appliesTo and isValid" in {
      val rule = new RecordingLandGuardRule(appliesToResult = true, isValidResult = false)
      val ua   = emptyUserAnswers

      rule.validate(testLand, ua)

      rule.appliesToCalls.map(_._2) must contain (ua)
      rule.isValidCalls.map(_._2)   must contain (ua)
    }
  }
}