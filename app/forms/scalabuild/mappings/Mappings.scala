/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package forms.scalabuild.mappings

import models.scalabuild.Enumerable
import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait Mappings extends Formatters  {

  protected def text(
      errorKey: String = "error.required",
      args: Seq[String] = Seq.empty
  ): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(
      requiredKey: String = "error.required",
      wholeNumberKey: String = "error.wholeNumber",
      nonNumericKey: String = "error.nonNumeric",
      args: Seq[String] = Seq.empty
  ): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))



  protected def enumerable[A](
      requiredKey: String = "error.required",
      invalidKey: String = "error.invalid",
      args: Seq[String] = Seq.empty
  )(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))


  protected def boolean(
                         requiredKey: String = "error.required",
                         invalidKey: String = "error.boolean",
                         args: Seq[String] = Seq.empty
                       ): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))

  protected def localDate(
      invalidKey: String,
      allRequiredKey: String,
      twoRequiredKey: String,
      requiredKey: String,
      yearMinDigitKey: String = "error.year.invalid",
      args: Seq[String] = Seq.empty
  ): FieldMapping[LocalDate] =
    of(
      new LocalDateFormatter(
        invalidKey,
        allRequiredKey,
        twoRequiredKey,
        requiredKey,
        yearMinDigitKey,
        args
      )
    )
}
