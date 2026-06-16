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

package forms.purchaser

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class NameOfPurchaserFormProviderSpec extends StringFieldBehaviours {

  val formProvider = new NameOfPurchaserFormProvider()

  "NameOfPurchaserFormProvider - Individual Branch" - {

    val requiredKey = "purchaser.individual.error.required"
    val lastNameLengthKey = "purchaser.individual.error.length.lastName"
    val firstNameLengthKey = "purchaser.individual.error.length.firstName"
    val middleNameLengthKey = "purchaser.individual.error.length.middleName"
    val lastNameInvalidKey = "purchaser.name.form.regex.error.lastName"
    val firstNameInvalidKey = "purchaser.name.form.regex.error.firstName"
    val middleNameInvalidKey = "purchaser.name.form.regex.error.middleName"
    val maxLength = 56
    val firstNameMaxLength = 14
    val middleNameMaxLength = 14

    val form = formProvider("Individual")

    val mandatoryFieldName = "name"
    val optionalFirstName = "forename1"
    val optionalMiddleName = "forename2"

    ".name" - {
      "must bind valid form data" in {
        val validNames = Seq(
          "Mr test",
          "Company test name",
          "Company are us",
          "Company@company.com",
          "(555) 123-4567"
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(
            mandatoryFieldName -> validName,
            optionalFirstName -> "",
            optionalMiddleName -> ""
          ))
          result.errors must be(empty)
        }
      }

      "must not bind strings longer than 56 characters" in {
        val longName = "a" * 57
        val result = form.bind(Map(
          mandatoryFieldName -> longName,
          optionalFirstName -> "",
          optionalMiddleName -> ""
        ))
        result.errors must contain(FormError(mandatoryFieldName, lastNameLengthKey, Seq(maxLength)))
      }

      behave like mandatoryField(
        form,
        mandatoryFieldName,
        requiredError = FormError(mandatoryFieldName, requiredKey)
      )

      "must reject invalid name formats" in {
        val invalidNames = Seq(
          "Hello #world",
          "Price: $50",
          "A < B",
          "File \\ path",
          "José",
          "\"Line1\\nLine2\""
        )

        invalidNames.foreach { invalidName =>
          val result = form.bind(Map(
            mandatoryFieldName -> invalidName,
            optionalFirstName -> "",
            optionalMiddleName -> ""
          ))
          result.errors must contain(
            FormError(mandatoryFieldName, lastNameInvalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }

      "must bind valid form data with max length surname" in {
        val maxLengthName = "a" * 56
        val result = form.bind(Map(
          mandatoryFieldName -> maxLengthName,
          optionalFirstName -> "",
          optionalMiddleName -> ""
        ))
        result.errors must be(empty)
      }

      "must accept valid special characters in surname" in {
        val result = form.bind(Map(
          mandatoryFieldName -> "Smith-Jones~!@%&'()*+,-./:=?[]^_{};",
          optionalFirstName -> "",
          optionalMiddleName -> ""
        ))
        result.errors must be(empty)
      }
    }

    ".forename1" - {
      "must bind valid form data" in {
        val validNames = Seq(
          "Mr test",
          "Company",
          "Pokemon",
          "Middle Name"
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(
            optionalFirstName -> validName,
            mandatoryFieldName -> "name",
            optionalMiddleName -> ""
          ))
          result.errors.isEmpty must be(true)
        }
      }

      "must not bind strings longer than 14 characters" in {
        val longName = "a" * 15

        val result = form.bind(Map(
          optionalFirstName -> longName,
          mandatoryFieldName -> "name",
          optionalMiddleName -> ""
        ))
        result.errors must contain(FormError(optionalFirstName, firstNameLengthKey, Seq(firstNameMaxLength)))
      }

      behave like optionalField(
        form,
        optionalFirstName
      )

      "must reject invalid forename1 formats" in {
        val invalidNames = Seq(
          "Hello #world",
          "Price: $50",
          "A < B",
          "File \\ path",
          "José",
          "\"Line1\\nLine2\""
        )

        invalidNames.foreach { invalidName =>
          val result = form.bind(Map(
            optionalFirstName -> invalidName,
            mandatoryFieldName -> "name",
            optionalMiddleName -> ""
          ))
          result.errors must contain(
            FormError(optionalFirstName, firstNameInvalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }
    }

    ".forename2" - {
      "must bind valid form data" in {
        val validNames = Seq(
          "Mr test",
          "Company",
          "Pokemon",
          "Middle Name"
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(
            mandatoryFieldName -> "name",
            optionalMiddleName -> validName,
            optionalFirstName -> ""
          ))
          result.errors.isEmpty must be(true)
        }
      }

      "must not bind strings longer than 14 characters" in {
        val longName = "a" * 15

        val result = form.bind(Map(
          mandatoryFieldName -> "name",
          optionalMiddleName -> longName,
          optionalFirstName -> ""
        ))
        result.errors must contain(FormError(optionalMiddleName, middleNameLengthKey, Seq(middleNameMaxLength)))
      }

      behave like optionalField(
        form,
        optionalMiddleName
      )

      "must reject invalid forename2 formats" in {
        val invalidNames = Seq(
          "Hello #world",
          "Price: $50",
          "A < B",
          "File \\ path",
          "José",
          "\"Line1\\nLine2\""
        )

        invalidNames.foreach { invalidName =>
          val result = form.bind(Map(
            mandatoryFieldName -> "name",
            optionalMiddleName -> invalidName,
            optionalFirstName -> ""
          ))
          result.errors must contain(
            FormError(optionalMiddleName, middleNameInvalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }

      "must bind valid form data with max length forename2" in {
        val maxLengthName = "a" * 14
        val result = form.bind(Map(
          mandatoryFieldName -> "name",
          optionalMiddleName -> maxLengthName,
          optionalFirstName -> ""
        ))
        result.errors must be(empty)
      }
    }

    "combined fields" - {
      "must bind valid form data with all fields" in {
        val result = form.bind(Map(
          optionalFirstName -> "John",
          optionalMiddleName -> "Michael",
          mandatoryFieldName -> "Smith"
        ))
        result.errors must be(empty)
        result.value mustBe Some(models.purchaser.NameOfPurchaser(Some("John"), Some("Michael"), "Smith"))
      }

      "must bind valid form data with only surname" in {
        val result = form.bind(Map(
          optionalFirstName -> "",
          optionalMiddleName -> "",
          mandatoryFieldName -> "Smith"
        ))
        result.errors must be(empty)
        result.value mustBe Some(models.purchaser.NameOfPurchaser(None, None, "Smith"))
      }
    }
  }

  "NameOfPurchaserFormProvider - Company Branch" - {

    val requiredKey = "purchaser.company.error.required"
    val nameLengthKey = "purchaser.company.error.length.name"
    val invalidKey = "purchaser.name.form.regex.error.company"
    val maxLength = 56

    val form = formProvider("Company")

    val mandatoryFieldName = "name"
    val optionalFirstName = "forename1"
    val optionalMiddleName = "forename2"

    ".name" - {
      "must bind valid form data" in {
        val validNames = Seq(
          "ACME Corporation",
          "Company test name",
          "Company are us",
          "Company@company.com",
          "(555) 123-4567"
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(mandatoryFieldName -> validName))
          result.errors must be(empty)
        }
      }

      "must not bind strings longer than 56 characters" in {
        val longName = "a" * 57
        val result = form.bind(Map(mandatoryFieldName -> longName))
        result.errors must contain(FormError(mandatoryFieldName, nameLengthKey, Seq(maxLength)))
      }

      behave like mandatoryField(
        form,
        mandatoryFieldName,
        requiredError = FormError(mandatoryFieldName, requiredKey)
      )

      "must reject invalid company name formats" in {
        val invalidNames = Seq(
          "Hello #world",
          "Price: $50",
          "A < B",
          "File \\ path",
          "José",
          "\"Line1\\nLine2\""
        )

        invalidNames.foreach { invalidName =>
          val result = form.bind(Map(mandatoryFieldName -> invalidName))
          result.errors must contain(
            FormError(mandatoryFieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }

      "must bind valid form data with max length company name" in {
        val maxLengthName = "a" * 56
        val result = form.bind(Map(mandatoryFieldName -> maxLengthName))
        result.errors must be(empty)
      }

      "must accept valid special characters in company name" in {
        val result = form.bind(Map(
          mandatoryFieldName -> "Smith & Jones Ltd~!@%&'()*+,-./:=?[]^_{};."
        ))
        result.errors must be(empty)
      }

      "must ignore forename1 and forename2 for company" in {
        val result = form.bind(Map(
          optionalFirstName -> "This should be ignored",
          optionalMiddleName -> "This should also be ignored",
          mandatoryFieldName -> "ACME Corporation"
        ))
        result.errors must be(empty)
        result.value mustBe Some(models.purchaser.NameOfPurchaser(None, None, "ACME Corporation"))
      }
    }
  }

  "NameOfPurchaserFormProvider - Other Purchaser Types" - {

    "must default to Company branch for unknown purchaser type" in {
      val form = formProvider("Unknown")
      val result = form.bind(Map("name" -> "ACME Corporation"))
      result.hasErrors mustBe false
      result.value mustBe Some(models.purchaser.NameOfPurchaser(None, None, "ACME Corporation"))
    }

    "must default to Company branch for empty string" in {
      val form = formProvider("")
      val result = form.bind(Map("name" -> "ACME Corporation"))
      result.hasErrors mustBe false
      result.value mustBe Some(models.purchaser.NameOfPurchaser(None, None, "ACME Corporation"))
    }
  }

  "NameOfPurchaserFormProvider - Form Filling" - {

    "must fill and unbind Individual form correctly" in {
      val form = formProvider("Individual")
      val data = models.purchaser.NameOfPurchaser(Some("John"), Some("Michael"), "Smith")
      val filledForm = form.fill(data)

      filledForm("forename1").value mustBe Some("John")
      filledForm("forename2").value mustBe Some("Michael")
      filledForm("name").value mustBe Some("Smith")
    }

    "must fill and unbind Company form correctly" in {
      val form = formProvider("Company")
      val data = models.purchaser.NameOfPurchaser(None, None, "ACME Corporation")
      val filledForm = form.fill(data)

      filledForm("forename1").value mustBe None
      filledForm("forename2").value mustBe None
      filledForm("name").value mustBe Some("ACME Corporation")
    }
  }
}