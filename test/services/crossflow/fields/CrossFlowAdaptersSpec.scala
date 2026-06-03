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
import play.api.data.Form
import play.api.data.Forms.*
import play.api.data.validation.{Invalid, Valid}
import play.api.mvc.Request
import play.api.test.FakeRequest
import services.crossflow.*

class CrossFlowAdaptersSpec extends SpecBase with Matchers {

  private val reliefTarget = CrossFlowTarget(Pages.ReliefReason,  "value")
  private val dateTarget   = CrossFlowTarget(Pages.EffectiveDate, "value")

  private def failure(
                       ruleId:  String,
                       msgKey:  String,
                       targets: Seq[CrossFlowTarget] = Seq(reliefTarget),
                       args:    Seq[Any]             = Nil
                     ): CrossFlowFailure =
    CrossFlowFailure(ruleId, ReturnSection.Transaction, msgKey, targets, args)

  private val testForm: Form[String] = Form("value" -> text)

  private class StubService(toReturn: Seq[CrossFlowFailure]) extends CrossFlowValidationService(Set.empty) {
    override def failuresForPage(page: PageId, ua: UserAnswers): Seq[CrossFlowFailure] = toReturn
    override def failuresAffecting(section: ReturnSection, ua: UserAnswers): Seq[CrossFlowFailure] = toReturn
  }

  "withCrossFlowErrors" - {

    "must return the form unchanged when there are no failures" in {
      val result = CrossFlowFormSupport.withCrossFlowErrors(testForm, Nil, Pages.ReliefReason)

      result.errors mustBe empty
    }

    "must attach a FormError for the first failure's matching target" in {
      val result = CrossFlowFormSupport.withCrossFlowErrors(
        testForm,
        Seq(failure("R1", "msg.one")),
        Pages.ReliefReason
      )

      result.errors.map(_.message) mustBe Seq("msg.one")
      result.errors.map(_.key) mustBe Seq("value")
    }

    "must show only the first failure when there are multiple" in {
      val result = CrossFlowFormSupport.withCrossFlowErrors(
        testForm,
        Seq(failure("R1", "msg.one"), failure("R2", "msg.two")),
        Pages.ReliefReason
      )

      result.errors.map(_.message) mustBe Seq("msg.one")
    }

    "must not attach errors for failures that don't target the given page" in {
      val result = CrossFlowFormSupport.withCrossFlowErrors(
        testForm,
        Seq(failure("R1", "msg.one", targets = Seq(dateTarget))),
        Pages.ReliefReason
      )

      result.errors mustBe empty
    }

    "must attach one error per matching target on the first failure" in {
      val extraReliefTarget = CrossFlowTarget(Pages.ReliefReason, "otherField")
      val result = CrossFlowFormSupport.withCrossFlowErrors(
        testForm,
        Seq(failure("R1", "msg.one", targets = Seq(reliefTarget, extraReliefTarget))),
        Pages.ReliefReason
      )

      result.errors.map(_.key) mustBe Seq("value", "otherField")
    }

    "must pass args through to the FormError" in {
      val result = CrossFlowFormSupport.withCrossFlowErrors(
        testForm,
        Seq(failure("R1", "msg.one", args = Seq("alpha", 42))),
        Pages.ReliefReason
      )

      result.errors.map(_.args) mustBe Seq(Seq("alpha", 42))
    }
  }

  "bindFromRequestWithCrossFlow" - {

    "must return Left with form errors when the form fails to bind" in {
      val numericForm = Form("value" -> number)
      given Request[?] = FakeRequest().withFormUrlEncodedBody("value" -> "not-a-number")

      val result = CrossFlowFormSupport.bindFromRequestWithCrossFlow(
        numericForm,
        Pages.ReliefReason,
        new StubService(Nil)
      )(_ => emptyUserAnswers)

      result.isLeft mustBe true
      result.left.toOption.get.errors must not be empty
    }

    "must return Right with the value and candidate answers when bind succeeds and no failures exist" in {
      given Request[?] = FakeRequest().withFormUrlEncodedBody("value" -> "hello")

      val result = CrossFlowFormSupport.bindFromRequestWithCrossFlow(
        testForm,
        Pages.ReliefReason,
        new StubService(Nil)
      )(_ => emptyUserAnswers)

      result.isRight mustBe true
      result.toOption.get._1 mustBe "hello"
    }

    "must return Left with cross-flow error when bind succeeds but failures exist" in {
      given Request[?] = FakeRequest().withFormUrlEncodedBody("value" -> "hello")
      val service = new StubService(Seq(failure("R1", "msg.one")))

      val result = CrossFlowFormSupport.bindFromRequestWithCrossFlow(
        testForm,
        Pages.ReliefReason,
        service
      )(_ => emptyUserAnswers)

      result.isLeft mustBe true
      result.left.toOption.get.errors.map(_.message) mustBe Seq("msg.one")
    }

    "must build the candidate UserAnswers from the bound value" in {
      given Request[?] = FakeRequest().withFormUrlEncodedBody("value" -> "hello")
      var capturedValue: Option[String] = None

      CrossFlowFormSupport.bindFromRequestWithCrossFlow(
        testForm,
        Pages.ReliefReason,
        new StubService(Nil)
      ) { v =>
        capturedValue = Some(v)
        emptyUserAnswers
      }

      capturedValue mustBe Some("hello")
    }
  }

  "forPage constraint" - {

    val firingRule: CrossFlowRule = new CrossFlowRule {
      val id      = "R1"
      val affects = ReturnSection.Transaction
      val inputs  = Set(ReturnSection.Transaction)
      val targets = Seq(reliefTarget)
      def validate(ua: UserAnswers): Option[CrossFlowFailure] =
        Some(failure("R1", "msg.one"))
    }

    val passingRule: CrossFlowRule = new CrossFlowRule {
      val id      = "R2"
      val affects = ReturnSection.Transaction
      val inputs  = Set(ReturnSection.Transaction)
      val targets = Seq(reliefTarget)
      def validate(ua: UserAnswers): Option[CrossFlowFailure] = None
    }

    val offPageRule: CrossFlowRule = new CrossFlowRule {
      val id      = "R3"
      val affects = ReturnSection.Transaction
      val inputs  = Set(ReturnSection.Transaction)
      val targets = Seq(dateTarget)
      def validate(ua: UserAnswers): Option[CrossFlowFailure] =
        Some(failure("R3", "msg.three", targets = Seq(dateTarget)))
    }

    "must produce Valid when no rules fire" in {
      val constraint = CrossFlowConstraints.forPage[String](
        Pages.ReliefReason,
        Seq(passingRule),
        _ => emptyUserAnswers
      )

      constraint("anything") mustBe Valid
    }

    "must produce Invalid when a rule fires on the given page" in {
      val constraint = CrossFlowConstraints.forPage[String](
        Pages.ReliefReason,
        Seq(firingRule),
        _ => emptyUserAnswers
      )

      constraint("anything") mustBe a[Invalid]
    }

    "must filter out failures that don't appear on the given page" in {
      val constraint = CrossFlowConstraints.forPage[String](
        Pages.ReliefReason,
        Seq(offPageRule),
        _ => emptyUserAnswers
      )

      constraint("anything") mustBe Valid
    }

    "must include the failure's message key on the resulting ValidationError" in {
      val constraint = CrossFlowConstraints.forPage[String](
        Pages.ReliefReason,
        Seq(firingRule),
        _ => emptyUserAnswers
      )

      val result = constraint("anything").asInstanceOf[Invalid]
      result.errors.map(_.message) mustBe Seq("msg.one")
    }
  }

  "fromFailures" - {

    "must return an empty seq when given no failures" in {
      CrossFlowErrors.fromFailures(Nil) mustBe empty
    }

    "must convert each failure to a CrossFlowError with its message key and args" in {
      val failures = Seq(
        failure("R1", "msg.one", args = Seq("a", 1)),
        failure("R2", "msg.two")
      )

      val result = CrossFlowErrors.fromFailures(failures)

      result.map(_.messageKey) mustBe Seq("msg.one", "msg.two")
      result.map(_.args) mustBe Seq(Seq("a", 1), Nil)
    }

    "must use the first target's field as the anchor" in {
      val targets = Seq(
        CrossFlowTarget(Pages.ReliefReason,  "first"),
        CrossFlowTarget(Pages.EffectiveDate, "second")
      )

      val result = CrossFlowErrors.fromFailures(Seq(failure("R1", "msg.one", targets = targets)))

      result.map(_.anchor) mustBe Seq(Some("first"))
    }

    "must produce a None anchor when the failure has no targets" in {
      val result = CrossFlowErrors.fromFailures(Seq(failure("R1", "msg.one", targets = Nil)))

      result.map(_.anchor) mustBe Seq(None)
    }
  }

  "forSection" - {

    "must convert the service's failures for the given section into CrossFlowErrors" in {
      val service = new StubService(Seq(failure("R1", "msg.one"), failure("R2", "msg.two")))

      val result = CrossFlowErrors.forSection(ReturnSection.Transaction, service, emptyUserAnswers)

      result.map(_.messageKey) mustBe Seq("msg.one", "msg.two")
    }

    "must return an empty seq when the service reports no failures" in {
      val service = new StubService(Nil)

      val result = CrossFlowErrors.forSection(ReturnSection.Transaction, service, emptyUserAnswers)

      result mustBe empty
    }
  }
}