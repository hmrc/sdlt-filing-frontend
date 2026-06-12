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

package services.crossflow.fields

import models.UserAnswers
import play.api.data.Form
import play.api.data.FormBinding.Implicits.formBinding
import play.api.data.FormError
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import play.api.mvc.Request
import services.crossflow.*

object CrossFlowFormSupport:

  def withCrossFlowErrors[A](form: Form[A], failures: Seq[CrossFlowFailure], page: PageId): Form[A] =
    failures.headOption.fold(form) { failure =>
      failure.targetsOn(page).foldLeft(form) { (acc, target) =>
        acc.withError(FormError(target.field, failure.inlineErrorKey, failure.args))
      }
    }

  def bindFromRequestWithCrossFlow[A](
                                       form:    Form[A],
                                       page:    PageId,
                                       service: CrossFlowValidationService
                                     )(candidateAnswers: A => UserAnswers)(using request: Request[?]): Either[Form[A], (A, UserAnswers)] =
    val bound = form.bindFromRequest()
    bound.fold(
      formWithErrors => Left(formWithErrors),
      value =>
        val candidate = candidateAnswers(value)
        val failures  = service.failuresForPage(page, candidate)
        if failures.isEmpty then Right((value, candidate))
        else Left(withCrossFlowErrors(bound, failures, page))
    )

object CrossFlowConstraints:

  def forPage[A](
                  page:             PageId,
                  rules:            Seq[CrossFlowRule],
                  candidateAnswers: A => UserAnswers
                ): Constraint[A] =
    Constraint("constraint.crossflow") { value =>
      val ua = candidateAnswers(value)
      val errors = rules
        .flatMap(_.validate(ua))
        .filter(_.appearsOn(page))
        .map(failure => ValidationError(failure.inlineErrorKey, failure.args*))
      if errors.isEmpty then Valid else Invalid(errors)
    }

object CrossFlowErrors:

  final case class CrossFlowError(messageKey: String, args: Seq[Any], anchor: Option[String])

  def fromFailures(failures: Seq[CrossFlowFailure]): Seq[CrossFlowError] =
    failures.map(f => CrossFlowError(f.messageKey, f.args, f.targets.headOption.map(_.field)))

  def forSection(section: ReturnSection, service: CrossFlowValidationService, ua: UserAnswers): Seq[CrossFlowError] =
    fromFailures(service.failuresAffecting(section, ua))