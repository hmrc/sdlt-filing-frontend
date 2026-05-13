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

package models.taxCalculation

sealed trait BuildRequestError {
  def message: String
}

sealed trait MissingDataError extends BuildRequestError

case object MissingFullReturnError extends MissingDataError {
  val message = "FullReturn not found in User answers"
}

case object MissingAboutTheLandError extends MissingDataError {
  val message = "Could not find the main land in User answers"
}

case object MissingMainLandIdError extends MissingDataError {
  val message = "ReturnInfo.mainLandID not set in User answers"
}

case object MissingAboutTheTransactionError extends MissingDataError {
  val message = "Could not extract 'About the Transaction' journey answers from Full return"
}

case class MissingLandAnswerError(value: String) extends MissingDataError {
  val message = s"Could not find user answer from 'About the Land' journey: $value"
}

case class MissingTransactionAnswerError(value: String) extends MissingDataError {
  val message = s"Could not find user answer from 'About the Transaction' journey: $value"
}

case class MissingLeaseAnswerError(value: String) extends MissingDataError {
  val message = s"Could not find user answer from 'About the Lease': $value"
}

case class MissingTaxCalculationAnswerError(value: String) extends MissingDataError {
  val message = s"Could not extract 'Tax Calculation' journey answers from Full return: $value"
}

case object MissingPremiumCalcError extends MissingDataError {
  val message = "TaxCalculationResult missing premium CalculationDetails"
}

case class InvalidDateError(value: String) extends BuildRequestError {
  val message = s"Invalid date: $value"
}

case class UnknownHoldingTypeError(value: String) extends BuildRequestError {
  val message = s"Unknown Holding Type found: $value"
}

case class UnknownPropertyTypeError(value: String) extends BuildRequestError {
  val message = s"Unknown Property Type found: $value"
}

case class InvalidReliefReasonError(value: String) extends BuildRequestError {
  val message = s"Invalid relief reason (not an integer): $value"
}

case class InvalidYesNoAnswerError(value: String) extends BuildRequestError {
  val message = s"Invalid yes/no answer: $value"
}
