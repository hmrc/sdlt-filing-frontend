/*
 * Copyright 2025 HM Revenue & Customs
 *
 */

package pages.scalabuild

import queries.{Gettable, Settable}

import scala.language.implicitConversions

trait Page

object Page {

  implicit def toString(page: Page): String =
    page.toString
}

trait QuestionPage[A] extends Page with Gettable[A] with Settable[A]
