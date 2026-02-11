/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package forms.scalabuild.mappings

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
}
