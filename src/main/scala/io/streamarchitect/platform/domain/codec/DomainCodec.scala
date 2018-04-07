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

import java.io.ByteArrayOutputStream

import org.apache.avro.Schema
import org.apache.avro.io._
import org.apache.avro.specific.{ SpecificDatumReader, SpecificDatumWriter, SpecificRecordBase }

/**
  * Codec to en- and decode Avro domain entities
  */
object DomainCodec {

  /**
    * Encode single given [[SpecificRecordBase]] based entity to an [[Array]] of [[Byte]]
    * @param entity
    * @tparam S
    * @return
    */
  def encode[S <: SpecificRecordBase](entity: S): Array[Byte] = {
    val datumWriter: DatumWriter[S] = new SpecificDatumWriter[S](entity.getSchema())
    val out                         = new ByteArrayOutputStream()
    val encoder                     = EncoderFactory.get().binaryEncoder(out, null)

    datumWriter.write(entity, encoder)

    encoder.flush()
    out.close()
    out.toByteArray
  }

  /**
    * Decode a given [[Array]] of [[Byte]] to a typed [[SpecificRecordBase]] based entity
    *
    * @param bytes
    * @tparam D
    * @return
    */
  def decode[D <: SpecificRecordBase](bytes: Array[Byte], schema: Schema): D = {
    val datumReader: DatumReader[D] = new SpecificDatumReader[D](schema)
    val decoder                     = DecoderFactory.get.binaryDecoder(bytes, null)

    datumReader.read(null.asInstanceOf[D], decoder)
  }

}
