/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild.mappings

import models.scalabuild.Enumerable
import play.api.data.FormError
import play.api.data.format.Formatter

import scala.util.control.Exception.nonFatalCatch

trait Formatters {

  private[mappings] def stringFormatter(
      errorKey: String,
      args: Seq[String] = Seq.empty
  ): Formatter[String] =
    new Formatter[String] {

      override def bind(
          key: String,
          data: Map[String, String]
      ): Either[Seq[FormError], String] =
        data.get(key) match {
          case None => Left(Seq(FormError(key, errorKey, args)))
          case Some(s) if s.trim.isEmpty =>
            Left(Seq(FormError(key, errorKey, args)))
          case Some(s) => Right(s)
        }

      override def unbind(key: String, value: String): Map[String, String] =
        Map(key -> value)
    }

  private[mappings] def booleanFormatter(
                                          requiredKey: String,
                                          invalidKey: String,
                                          args: Seq[String] = Seq.empty
                                        ): Formatter[Boolean] =
    new Formatter[Boolean] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .flatMap {
            case "true"  => Right(true)
            case "false" => Right(false)
            case _       => Left(Seq(FormError(key, invalidKey, args)))
          }

      def unbind(key: String, value: Boolean) = Map(key -> value.toString)
    }

  private[mappings] def intFormatter(
      requiredKey: String,
      wholeNumberKey: String,
      nonNumericKey: String,
      args: Seq[String] = Seq.empty
  ): Formatter[Int] =
    new Formatter[Int] {

      val decimalRegexp = """^-?(\d*\.\d*)$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            case s if s.matches(decimalRegexp) =>
              Left(Seq(FormError(key, wholeNumberKey, args)))
            case s =>
              nonFatalCatch
                .either(s.toInt)
                .left
                .map(_ => Seq(FormError(key, nonNumericKey, args)))
          }

      override def unbind(key: String, value: Int) =
        baseFormatter.unbind(key, value.toString)
    }
  private[mappings] def currencyFormatter(
                                           requiredKey: String,
                                           twoDecimalPlacesKey: String,
                                           nonNumericKey: String,
                                           args: Seq[String] = Seq.empty
                                         ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {

      val decimalRegexp = """^[+-]?[0-9]*(\.[0-9]{0,2})?$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]) =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", ""))
          .flatMap {
            s =>
              nonFatalCatch
                .either(BigDecimal(s))
                .left.map(_ =>
                  Seq(FormError(key, nonNumericKey, args))).flatMap { res =>
                  if (res.toString().matches(decimalRegexp)) {
                    Right(res)
                  } else {
                    Left(Seq(FormError(key, twoDecimalPlacesKey, args)))
                  }
                }
          }

      override def unbind(key: String, value: BigDecimal) =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def bigDecimalFormatter(
                                             decimalPlaces: Int,
                                             requiredKey: String,
                                             nonNumericKey: String,
                                             decimalPlacesKey: String,
                                             args: Seq[String] = Seq.empty
                                           ): Formatter[BigDecimal] =
    new Formatter[BigDecimal] {
      private val decimalRegexp = s"""^[+-]?[0-9]*(\\.[0-9]{0,$decimalPlaces})?$$"""

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], BigDecimal] =
        baseFormatter
          .bind(key, data)
          .map(_.replace(",", "").replaceAll("\\s", ""))
          .flatMap { s =>
            nonFatalCatch
              .either(BigDecimal(s))
              .left
              .map(_ => Seq(FormError(key, nonNumericKey, args)))
              .flatMap { res =>
                if (res.toString().matches(decimalRegexp)) {
                  Right(res)
                } else {
                  Left(Seq(FormError(key, decimalPlacesKey, args)))
                }
              }
          }

      override def unbind(key: String, value: BigDecimal): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

  private[mappings] def enumerableFormatter[A](
      requiredKey: String,
      invalidKey: String,
      args: Seq[String] = Seq.empty
  )(implicit
      ev: Enumerable[A]
  ): Formatter[A] =
    new Formatter[A] {

      private val baseFormatter = stringFormatter(requiredKey, args)

      override def bind(
          key: String,
          data: Map[String, String]
      ): Either[Seq[FormError], A] =
        baseFormatter.bind(key, data).flatMap { str =>
          ev.withName(str)
            .map(Right.apply)
            .getOrElse(Left(Seq(FormError(key, invalidKey, args))))
        }

      override def unbind(key: String, value: A): Map[String, String] =
        baseFormatter.unbind(key, value.toString)
    }

}
