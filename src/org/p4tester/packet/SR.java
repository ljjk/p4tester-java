/**
*    Copyright 2011, Big Switch Networks, Inc.
*    Originally created by David Erickson, Stanford University
*
*    Licensed under the Apache License, Version 2.0 (the "License"); you may
*    not use this file except in compliance with the License. You may obtain
*    a copy of the License at
*
*         http://www.apache.org/licenses/LICENSE-2.0
*
*    Unless required by applicable law or agreed to in writing, software
*    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
*    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
*    License for the specific language governing permissions and limitations
*    under the License.
**/

package org.p4tester.packet;

import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.TransportPort;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class SR extends BasePacket {

    protected short port;
    protected short f;



    public SR setPort(short port) {
        this.port = port;
        return this;
    }


    public SR setF(short f) {
        this.f = f;
        return this;
    }

    public short getPort() {
        return port;
    }

    public short getF() {
        return f;
    }

    /**
     * Serializes the packet. Will compute and set the following fields if they
     * are set to specific values at the time serialize is called:
     *      -checksum : 0
     *      -length : 0
     */
    public byte[] serialize() {
        byte[] payloadData = null;
        if (payload != null) {
            payload.setParent(this);
            payloadData = payload.serialize();
        }

        if (payloadData != null) {
            byte[] data = new byte[2 + payloadData.length];
            ByteBuffer bb = ByteBuffer.wrap(data);

            short sr = (short) ((port << 1) | f);

            bb.putShort(sr);
            bb.put(payloadData);
            return data;
        } else {
            byte[] data = new byte[2];
            ByteBuffer bb = ByteBuffer.wrap(data);

            short sr = (short) ((port << 1) | f);

            bb.putShort(sr);

            return data;
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {

        return (port << 1) + 1;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof SR))
            return false;
        SR other = (SR) obj;
        if (port != other.port)
            return false;

        if (f != other.f)
            return false;

        return true;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length)
            throws PacketParsingException {
        ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        this.port = bb.getShort(); // short will be signed, pos or neg
        this.port >>= 1;
        this.f = (short) (port & 1); // convert range 0 to 65534, not -32768 to 32767


        if (length > 2) {
            this.payload = new SR();
        }

        this.payload = payload.deserialize(data, bb.position(), bb.limit()-bb.position());
        this.payload.setParent(this);
        return this;
    }
}
