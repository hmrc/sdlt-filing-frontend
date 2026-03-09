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

import models._

import javax.inject.{Inject, Singleton}

/**
 * Concrete @Singleton classes for each supplementary SDLT form filler.
 *
 * Because these are concrete classes annotated with @Singleton and @Inject,
 * Guice will self-bind them — no explicit module bindings needed.
 *
 * Each currently throws NotImplementedError. Implement the fillPdf body
 * following the same pattern as SdltReturnPdf1a when the template PDFs
 * are available.
 */

@Singleton
class SdltReturnPdf2Purchaser @Inject()() {
  def fillPdf(purchaser: Purchaser, fullReturn: FullReturn): Array[Byte] =
    throw new NotImplementedError("SDLT2 Purchaser PDF not yet implemented")
}

@Singleton
class SdltReturnPdf2Vendor @Inject()() {
  def fillPdf(vendor: Vendor, fullReturn: FullReturn): Array[Byte] =
    throw new NotImplementedError("SDLT2 Vendor PDF not yet implemented")
}

@Singleton
class SdltReturnPdf3 @Inject()() {
  def fillPdf(land: Land, fullReturn: FullReturn): Array[Byte] =
    throw new NotImplementedError("SDLT3 PDF not yet implemented")
}

@Singleton
class SdltReturnPdf4 @Inject()() {
  def fillPdf(land: Land, fullReturn: FullReturn, firstTimeThrough: Boolean): Array[Byte] =
    throw new NotImplementedError("SDLT4 PDF not yet implemented")
}

@Singleton
class SdltReturnPdf4a @Inject()() {
  def fillPdf(fullReturn: FullReturn): Array[Byte] =
    throw new NotImplementedError("SDLT4a PDF not yet implemented")
}

/**
 * Loads a named PDF template from the Play classpath (conf/pdf/).
 *
 * Bind the implementation in your Module:
 *   bind(classOf[PdfTemplateLoader]).to(classOf[ClasspathPdfTemplateLoader])
 */
trait PdfTemplateLoader {
  def load(filename: String): Array[Byte]
}