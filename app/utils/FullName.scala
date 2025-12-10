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

package utils

trait FullName {
  def forename1: Option[String]

  def forename2: Option[String]

  def name: String

  def fullName: String = (forename1, forename2, name) match {
    case (Some(f1), Some(f2), sn) => s"$f1 $f2 $sn"
    case (Some(f1), None, sn) => s"$f1 $sn"
    case (None, Some(f2), sn) => s"$f2 $sn"
    case (None, _, sn) => sn
  }
}