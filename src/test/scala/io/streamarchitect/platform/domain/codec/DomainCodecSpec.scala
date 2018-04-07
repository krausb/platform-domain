/*
 * Copyright (C) 2018  Bastian Kraus
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package io.streamarchitect.platform.domain.codec

import java.io._
import java.nio.file.{ Files, Paths }

import io.streamarchitect.platform.domain.telemetry.Position
import org.apache.logging.log4j.LogManager
import org.scalatest.{ MustMatchers, WordSpec }

import scala.util.Random

/**
  *
  */
class DomainCodecSpec extends WordSpec with MustMatchers {

  private val log = LogManager.getLogger(getClass)

  "A DomainCodec" should {

    "encode a list of platform domain entities" in {

      val position = getRandomPosition
      log.debug(position)

      val serializedPositions = DomainCodec.encode(position)
      log.debug(serializedPositions)

      val out = new BufferedOutputStream(new FileOutputStream("/tmp/payload.avro"))
      out.write(serializedPositions)
      out.close()
    }

    "decode a list of platform domain entities" in {

      val bytes = Files.readAllBytes(Paths.get(getClass().getResource("/payload.avro").getPath))
      log.debug(bytes)
      val position: Position = DomainCodec.decode(bytes, Position.SCHEMA$)
      log.debug(position)

    }

  }

  private def getRandomPosition: Position =
    Position(Random.nextDouble(), Random.nextDouble(), Random.nextDouble(), Random.nextInt())

}
