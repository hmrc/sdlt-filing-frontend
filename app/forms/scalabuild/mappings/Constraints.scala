/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild.mappings

import models.scalabuild.PropertyType
import models.scalabuild.PropertyType.NonResidential
import play.api.data.Mapping
import play.api.data.validation.{Constraint, Invalid, Valid}
import uk.gov.voa.play.form.{ConditionalMapping, MandatoryOptionalMapping}

trait Constraints {
  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum && input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  protected def earliestDateIfFreehold[A](dateCutoff: A, propertyType: PropertyType, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (propertyType == NonResidential) {
        Valid
      } else if (input >= dateCutoff){
        Valid
      } else {
        Invalid(errorKey, dateCutoff)
      }
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input >= minimum) {
        Valid
      } else {
        Invalid(errorKey, minimum)
      }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint { input =>
      import ev._

      if (input <= maximum) {
        Valid
      } else {
        Invalid(errorKey, maximum)
      }
    }

  def mandatoryIfExists[T](fieldName: String, mapping: Mapping[T]): Mapping[Option[T]] =
    ConditionalMapping(_.keys.toSeq.contains(fieldName), MandatoryOptionalMapping(mapping, Nil), None, Seq.empty)

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

}
