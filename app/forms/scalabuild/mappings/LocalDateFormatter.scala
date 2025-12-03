/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package forms.scalabuild.mappings

import play.api.data.FormError
import play.api.data.format.Formatter

import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
    invalidKey: String,
    allRequiredKey: String,
    twoRequiredKey: String,
    requiredKey: String,
    yearMinDigitKey: String,
    args: Seq[String] = Seq.empty
) extends Formatter[LocalDate]
    with Formatters {

  private val fieldKeys: List[String] = List("Day", "Month", "Year")

  private def toDate(
      key: String,
      day: Int,
      month: Int,
      year: Int
  ): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_) =>
        Left(Seq(FormError(key, invalidKey, args)))
    }

  private def formatDate(
      key: String,
      data: Map[String, String]
  ): Either[Seq[FormError], LocalDate] = {

    val int = intFormatter(
      requiredKey = invalidKey,
      wholeNumberKey = invalidKey,
      nonNumericKey = invalidKey,
      args
    )

    for {
      day <- int.bind(s"${key}Day", data)
      month <- int.bind(s"${key}Month", data)
      year <- int.bind(s"${key}Year", data)
      year <- yearMinFourDigits(year)
      date <- toDate(key, day, month, year)
    } yield date
  }

  private def yearMinFourDigits(year: Int) = {
    year.toString match {
      case s if s.length < 4 =>
        Left(Seq(FormError("year", yearMinDigitKey, args)))
      case _ => Right(year)
    }
  }

  override def bind(
      key: String,
      data: Map[String, String]
  ): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map { field =>
      field -> data.get(s"$key$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 3 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 2 =>
        Left(List(FormError(key, requiredKey, missingFields ++ args)))
      case 1 =>
        Left(List(FormError(key, twoRequiredKey, missingFields ++ args)))
      case _ =>
        Left(List(FormError(key, allRequiredKey, args)))
    }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"${key}Day" -> value.getDayOfMonth.toString,
      s"${key}Month" -> value.getMonthValue.toString,
      s"${key}Year" -> value.getYear.toString
    )
}
