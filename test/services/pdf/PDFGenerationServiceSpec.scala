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

package services.pdf

import base.SpecBase
import constants.FullReturnConstants.*
import models.*
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatestplus.mockito.MockitoSugar
import play.api.Environment

import java.io.ByteArrayInputStream
import scala.concurrent.ExecutionContext

class PDFGenerationServiceSpec extends SpecBase with MockitoSugar {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  private val blankPdfBytes: Array[Byte] = {
    val content =
      """%PDF-1.4
        |1 0 obj<</Type /Catalog /Pages 2 0 R>>endobj
        |2 0 obj<</Type /Pages /Kids [3 0 R] /Count 1>>endobj
        |3 0 obj<</Type /Page /Parent 2 0 R /MediaBox [0 0 595 842]>>endobj
        |xref
        |0 4
        |0000000000 65535 f
        |0000000009 00000 n
        |0000000058 00000 n
        |0000000115 00000 n
        |trailer<</Size 4 /Root 1 0 R>>
        |startxref
        |190
        |%%EOF""".stripMargin
    content.getBytes("UTF-8")
  }

  private def mockPdf1a(bytes: Array[Byte] = blankPdfBytes): SdltReturnPdf1a = {
    val m = mock[SdltReturnPdf1a]
    when(m.fillPdf(any[FullReturn](), any[Boolean]())).thenReturn(bytes)
    m
  }

  private def buildService(pdf1a: SdltReturnPdf1a = mockPdf1a()): PDFGenerationService =
    new PDFGenerationService(pdf1a)
  
  "PDFGenerationService" - {

    "generatePdf" - {

      "must return a non-empty byte array for a minimal return" in {
        val result = buildService().generatePdf(minimalFullReturn).futureValue
        result must not be empty
      }

      "must return a non-empty byte array for a complete return" in {
        val result = buildService().generatePdf(completeFullReturn).futureValue
        result must not be empty
      }

      "must return a valid PDF byte array (starts with %PDF header)" in {
        val result = buildService().generatePdf(minimalFullReturn).futureValue
        new String(result.take(4), "UTF-8") mustBe "%PDF"
      }

      "must call pdf1aFiller exactly once regardless of return content" in {
        val pdf1a   = mockPdf1a()
        val service = buildService(pdf1a)
        service.generatePdf(minimalFullReturn).futureValue
        verify(pdf1a, times(1)).fillPdf(any[FullReturn](), any[Boolean]())
      }

      "must call pdf1aFiller with the full return passed in" in {
        val pdf1a   = mockPdf1a()
        val service = buildService(pdf1a)
        service.generatePdf(completeFullReturn).futureValue
        verify(pdf1a, times(1)).fillPdf(eqTo(completeFullReturn), any[Boolean]())
      }

      "must call pdf1aFiller exactly once per invocation when called multiple times" in {
        val pdf1a   = mockPdf1a()
        val service = buildService(pdf1a)
        service.generatePdf(minimalFullReturn).futureValue
        service.generatePdf(completeFullReturn).futureValue
        verify(pdf1a, times(2)).fillPdf(any[FullReturn](), any[Boolean]())
      }

      "must propagate failures from pdf1aFiller" in {
        val pdf1a = mock[SdltReturnPdf1a]
        when(pdf1a.fillPdf(any[FullReturn](), any[Boolean]())).thenThrow(new SdltPdfFillException("template missing", null))
        whenReady(buildService(pdf1a).generatePdf(minimalFullReturn).failed) { ex =>
          ex mustBe a[SdltPdfFillException]
        }
      }

      "must handle a return with no optional fields set" in {
        val result = buildService().generatePdf(emptyFullReturn).futureValue
        result must not be empty
      }
    }
  }
  
  "ClasspathPdfTemplateLoader" - {

    val testBytes = "fake-pdf-content".getBytes("UTF-8")

    def mockEnvWith(filename: String, bytes: Array[Byte]): Environment = {
      val env = mock[Environment]
      when(env.resourceAsStream(s"pdf/$filename"))
        .thenReturn(Some(new ByteArrayInputStream(bytes)))
      env
    }

    def mockEnvMissing(filename: String): Environment = {
      val env = mock[Environment]
      when(env.resourceAsStream(s"pdf/$filename"))
        .thenReturn(None)
      env
    }

    "load" - {

      "must return the bytes from the resource stream when the file exists" in {
        val loader = new ClasspathPdfTemplateLoader(mockEnvWith("SDLT1a.pdf", testBytes))
        loader.load("SDLT1a.pdf") mustBe testBytes
      }

      "must look up the resource under the pdf/ prefix" in {
        val env = mockEnvWith("SDLT1a.pdf", testBytes)
        new ClasspathPdfTemplateLoader(env).load("SDLT1a.pdf")
        verify(env, times(1)).resourceAsStream("pdf/SDLT1a.pdf")
      }

      "must throw IllegalStateException when the file is not found" in {
        val loader = new ClasspathPdfTemplateLoader(mockEnvMissing("SDLT1a.pdf"))
        val ex = intercept[IllegalStateException] {
          loader.load("SDLT1a.pdf")
        }
        ex.getMessage must include("SDLT1a.pdf")
      }

      "must include the filename in the exception message when not found" in {
        val loader = new ClasspathPdfTemplateLoader(mockEnvMissing("SDLT2.pdf"))
        val ex = intercept[IllegalStateException] {
          loader.load("SDLT2.pdf")
        }
        ex.getMessage must include("SDLT2.pdf")
      }

      "must return the exact bytes from the stream without modification" in {
        val specificBytes = Array[Byte](0x25, 0x50, 0x44, 0x46) // %PDF
        val loader        = new ClasspathPdfTemplateLoader(mockEnvWith("SDLT1a.pdf", specificBytes))
        loader.load("SDLT1a.pdf") mustBe specificBytes
      }

      "must use the correct pdf/ path for different filenames" in {
        val env = mock[Environment]
        when(env.resourceAsStream("pdf/SDLT2.pdf"))
          .thenReturn(Some(new ByteArrayInputStream(testBytes)))
        new ClasspathPdfTemplateLoader(env).load("SDLT2.pdf")
        verify(env, times(1)).resourceAsStream("pdf/SDLT2.pdf")
        verify(env, never()).resourceAsStream("pdf/SDLT1a.pdf")
      }

      "must not call resourceAsStream more than once per load call" in {
        val env = mockEnvWith("SDLT1a.pdf", testBytes)
        new ClasspathPdfTemplateLoader(env).load("SDLT1a.pdf")
        verify(env, times(1)).resourceAsStream(any[String]())
      }
    }
  }
}