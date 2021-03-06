/* Copyright 2014, 2015 Richard Wiedenhöft <richard@wiedenhoeft.xyz>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.wiedenhoeft.scalacrypt

import org.scalatest._
import scala.util.{ Try, Success, Failure }

class SymmetricBlockCipherSuiteSpec extends FlatSpec with Matchers {
  "AES128 with CBC and no padding" should "conform to the test vectors" in {
    val testvectors: Seq[(Seq[Byte], Seq[Byte], Seq[Byte])] = Seq(
      (
        Seq(0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F) map { _.toByte },
        Seq(0x6b, 0xc1, 0xbe, 0xe2, 0x2e, 0x40, 0x9f, 0x96, 0xe9, 0x3d, 0x7e, 0x11, 0x73, 0x93, 0x17, 0x2a) map { _.toByte },
        Seq(0x76, 0x49, 0xab, 0xac, 0x81, 0x19, 0xb2, 0x46, 0xce, 0xe9, 0x8e, 0x9b, 0x12, 0xe9, 0x19, 0x7d) map { _.toByte }
      ), (
        Seq(0x76, 0x49, 0xAB, 0xAC, 0x81, 0x19, 0xB2, 0x46, 0xCE, 0xE9, 0x8E, 0x9B, 0x12, 0xE9, 0x19, 0x7D) map { _.toByte },
        Seq(0xae, 0x2d, 0x8a, 0x57, 0x1e, 0x03, 0xac, 0x9c, 0x9e, 0xb7, 0x6f, 0xac, 0x45, 0xaf, 0x8e, 0x51) map { _.toByte },
        Seq(0x50, 0x86, 0xcb, 0x9b, 0x50, 0x72, 0x19, 0xee, 0x95, 0xdb, 0x11, 0x3a, 0x91, 0x76, 0x78, 0xb2) map { _.toByte }
      ), (
        Seq(0x50, 0x86, 0xCB, 0x9B, 0x50, 0x72, 0x19, 0xEE, 0x95, 0xDB, 0x11, 0x3A, 0x91, 0x76, 0x78, 0xB2) map { _.toByte },
        Seq(0x30, 0xc8, 0x1c, 0x46, 0xa3, 0x5c, 0xe4, 0x11, 0xe5, 0xfb, 0xc1, 0x19, 0x1a, 0x0a, 0x52, 0xef) map { _.toByte },
        Seq(0x73, 0xbe, 0xd6, 0xb8, 0xe3, 0xc1, 0x74, 0x3b, 0x71, 0x16, 0xe6, 0x9e, 0x22, 0x22, 0x95, 0x16) map { _.toByte }
      ), (
        Seq(0x73, 0xBE, 0xD6, 0xB8, 0xE3, 0xC1, 0x74, 0x3B, 0x71, 0x16, 0xE6, 0x9E, 0x22, 0x22, 0x95, 0x16) map { _.toByte },
        Seq(0xf6, 0x9f, 0x24, 0x45, 0xdf, 0x4f, 0x9b, 0x17, 0xad, 0x2b, 0x41, 0x7b, 0xe6, 0x6c, 0x37, 0x10) map { _.toByte },
        Seq(0x3f, 0xf1, 0xca, 0xa1, 0x68, 0x1f, 0xac, 0x09, 0x12, 0x0e, 0xca, 0x30, 0x75, 0x86, 0xe1, 0xa7) map { _.toByte }
      )
    )

    for (test <- testvectors) {
      val enc = suites.AES128_CBC_NoPadding(Seq(0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c).map({ _.toByte }).toKey[SymmetricKey128].get, Some(test._1)).get

      enc.encrypt(Iterator(test._2)).toSeq.map({ _.get }).flatten should be(test._3)
      enc.decrypt(Iterator(test._3)).toSeq.map({ _.get }).flatten should be(test._2)

      enc.encrypt(test._2).get should be(test._3)
      enc.decrypt(test._3).get should be(test._2)
    }
  }

  "AES with CBC and PKCS7Padding" should "operate on arbitrary iterators" in {

    val tests = Seq[Seq[Seq[Byte]]](
      Seq(
        Seq(1, 2, 3), Seq(2, 3, 4)
      ), Seq(
        Seq(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17),
        Seq(18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30),
        Seq(31, 32, 33, 34, 35)
      )
    )

    for (test <- tests) {
      val key = Key.generate[SymmetricKey128]
      val enc = suites.AES128_CBC_PKCS7Padding(key).get

      val crypt = enc.encrypt(test.toIterator).toSeq.map({ _.get }).flatten
      enc.decrypt(Iterator(crypt)).toSeq.map({ _.get }).flatten should be(test.flatten)
    }
  }
}
