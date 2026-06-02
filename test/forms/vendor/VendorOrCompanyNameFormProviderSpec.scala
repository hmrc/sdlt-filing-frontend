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

package forms.vendor

import forms.behaviours.StringFieldBehaviours
import play.api.data.FormError

class VendorOrCompanyNameFormProviderSpec extends StringFieldBehaviours {

  val firstNameLengthKeyIndividual = "vendor.individual.firstName.error.length"
  val middleNameLengthKeyIndividual = "vendor.individual.middleName.error.length"
  val nameLengthKeyIndividual = "vendor.individual.name.error.length"
  val firstNameInvalidKeyIndividual = "vendor.individual.firstName.regex.error"
  val middleNameInvalidKeyIndividual = "vendor.individual.middleName.regex.error"
  val nameInvalidKeyIndividual = "vendor.individual.name.regex.error"
  val nameRequiredKeyIndividual = "vendor.individual.name.error.required"

  val nameLengthKeyCompany = "vendor.company.name.error.length"
  val nameInvalidKeyCompany = "vendor.company.name.regex.error"
  val nameRequiredKeyCompany = "vendor.company.name.error.required"

  val maxLength = 56
  val firstNameMaxLength = 14
  val middleNameMaxLength = 14

  val formProvider = new VendorOrCompanyNameFormProvider()

  ".vendorOrCompanyName" - {

    val mandatoryFieldName = "name"
    val optionalFirstName = "forename1"
    val optionalMiddleName = "forename2"
    
    ".name" - {
      val nameCases = Table(
        ("vendorType", "requiredKey", "lengthKey", "invalidKey"),
        ("individual", nameRequiredKeyIndividual, nameLengthKeyIndividual, nameInvalidKeyIndividual),
        ("company", nameRequiredKeyCompany, nameLengthKeyCompany, nameInvalidKeyCompany)
      )

      forAll(nameCases) { (vendorType, requiredKey, lengthKey, invalidKey) =>
        val form = formProvider(vendorType)

        s"when vendor type is $vendorType" - {
          "must bind valid form data" in {
            val validNames = Seq(
              "Mr test",
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
            result.errors must contain(FormError(mandatoryFieldName, lengthKey, Seq(maxLength)))
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
              val result = form.bind(Map(mandatoryFieldName -> invalidName))
              result.errors must contain(
                FormError(mandatoryFieldName, invalidKey, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
              )
            }
          }
        }
      }
    }

    ".forename1" - {
      val form = formProvider("individual")

      "must bind valid form data" in {
        val validNames = Seq(
          "Mr test",
          "Company",
          "Pokemon",
          "Middle Name",
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(optionalFirstName -> validName, mandatoryFieldName -> "name"))
          result.errors.isEmpty must be(true)
        }
      }

      "must not bind strings longer than 14 characters" in {
        val longName = "a" * 15

        val result = form.bind(Map(optionalFirstName -> longName, mandatoryFieldName -> "name"))
        result.errors must contain(FormError(optionalFirstName, firstNameLengthKeyIndividual, Seq(firstNameMaxLength)))
      }

      behave like optionalField(
        form,
        optionalFirstName
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
          val result = form.bind(Map(optionalFirstName -> invalidName, mandatoryFieldName -> "name"))
          result.errors must contain(
            FormError(optionalFirstName, firstNameInvalidKeyIndividual, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }
    }

    ".forename2" - {
      val form = formProvider("individual")

      "must bind valid form data" in {
        val validNames = Seq(
          "Mr test",
          "Company",
          "Pokemon",
          "Middle Name",
        )

        validNames.foreach { validName =>
          val result = form.bind(Map(mandatoryFieldName -> "name", optionalMiddleName -> validName))
          result.errors.isEmpty must be(true)
        }
      }

      "must not bind strings longer than 14 characters" in {
        val longName = "a" * 15

        val result = form.bind(Map(mandatoryFieldName -> "name", optionalMiddleName -> longName))
        result.errors must contain(FormError(optionalMiddleName, middleNameLengthKeyIndividual, Seq(firstNameMaxLength)))
      }

      behave like optionalField(
        form,
        optionalMiddleName
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
          val result = form.bind(Map(mandatoryFieldName -> "name", optionalMiddleName -> invalidName))
          result.errors must contain(
            FormError(optionalMiddleName, middleNameInvalidKeyIndividual, Seq("[A-Za-z0-9 ~!@%&'()*+,\\-./:=?\\[\\]^_{}\\;]*"))
          )
        }
      }
    }
  }
}

