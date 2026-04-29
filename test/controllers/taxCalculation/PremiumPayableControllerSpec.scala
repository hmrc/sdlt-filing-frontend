/*
 * Copyright 2026 HM Revenue & Customs
 *
 */

package controllers.taxCalculation

import base.ScalaSpecBase
import forms.scalabuild.taxCalculation.PremiumPayableFormProvider
import org.scalatest.freespec.AnyFreeSpec
class PremiumPayableControllerSpec extends AnyFreeSpec with ScalaSpecBase {

  val formProvider = new PremiumPayableFormProvider()
  val form = formProvider()
  lazy val premiumPayableRoute = controllers.taxCalculation.routes.PremiumPayableController.onPageLoad().url

  "PremiumPayable Controller" - {
    "must return OK and the correct view for a GET" in {

    }
}
