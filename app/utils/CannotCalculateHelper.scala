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

package utils

import models.UserAnswers
import utils.SelfAssessedHelper.{isBetweenDates, isResidentialBeforeMarch2012Date}

import java.time.LocalDate

case class DateRange(start: LocalDate, end: LocalDate)

object CannotCalculateHelper {

  val predatesCalcOneRange = DateRange(LocalDate.of(2010, 3, 25), LocalDate.of(2012, 3, 25))
  val predatesCalcTwoRange = DateRange(LocalDate.of(2008, 3, 12), LocalDate.of(2016, 3, 17))

  private def reasonWhen(condition: Boolean, reason: String): Option[String] =
    if (condition) Some(reason) else None

  private def reasonWhenAll(conditions: Seq[Boolean], reason: String): Option[String] =
    if (conditions.forall(identity)) Some(reason) else None

  def getCannotCalculateReason(answers: UserAnswers): List[String] = {
    val mainLandId = answers.fullReturn.flatMap(_.returnInfo.flatMap(_.mainLandID))
    
    val isLinked = answers.fullReturn.flatMap(_.transaction.flatMap(_.isLinked))
    val partialRelief = answers.fullReturn.flatMap(_.transaction.flatMap(_.reliefAmount))
    val interestTransferred =
      answers.fullReturn.flatMap(_.land.flatMap(_.find(_.landID == mainLandId)).flatMap(_.interestCreatedTransferred))
    val reliefReason = answers.fullReturn.flatMap(_.transaction.flatMap(_.reliefReason))
    val transactionType = answers.fullReturn.flatMap(_.transaction.flatMap(_.transactionDescription))
    val propertyType =
      answers.fullReturn.flatMap(_.land.flatMap(_.find(_.landID == mainLandId)).flatMap(_.propertyType))
    val isAnnualRentOver1000 = answers.fullReturn.flatMap(_.lease.flatMap(_.isAnnualRentOver1000))

    List(
      reasonWhen(isLinked.exists(_.equalsIgnoreCase("yes")), "reason1"),
      reasonWhen(partialRelief.nonEmpty, "reason2"),
      reasonWhen(interestTransferred.contains("OT"), "reason3"),
      reasonWhen(reliefReason.contains("33"), "reason4"),
      reasonWhen(isResidentialBeforeMarch2012Date(answers), "reason5"),
      reasonWhen(reliefReason.contains("25"), "reason6"),
      reasonWhenAll(Seq(
        isBetweenDates(answers, predatesCalcOneRange),
        transactionType.contains("L"),
        reliefReason.contains("32")),
        "reason7"),
      reasonWhenAll(Seq(
        isBetweenDates(answers, predatesCalcTwoRange),
        propertyType.contains("02"),
        isAnnualRentOver1000.exists(_.equalsIgnoreCase("yes"))),
        "reason8")
    ).flatten
  }

}
