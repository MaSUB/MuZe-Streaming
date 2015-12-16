/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RTSPtest;

/*
 * 02/12/99 : Java Conversion by E.B , ebsp@iname.com, JavaLayer
 *
 *-----------------------------------------------------------------------
 *  @(#) crc.h 1.5, last edit: 6/15/94 16:55:32
 *  @(#) Copyright (C) 1993, 1994 Tobias Bading (bading@cs.tu-berlin.de)
 *  @(#) Berlin University of Technology
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *-----------------------------------------------------------------------
 */

/**
 * 16-Bit CRC checksum
 */
public final class CRC{
	private static short polynomial=(short)0x8005;
	private short crc;

	/**
	 * Dummy Constructor
	 */
	public CRC(){ 
		crc = (short) 0xFFFF;
	}

	/**
	 * Feed a bitstring to the crc calculation (0 < length <= 32).
	 * @param bitstring <code>int</code> a bitstring to the crc calculation
	 * @param length <code>int</code> with length
	 */
	public void addBits(int bitstring, int length){
		int bitmask = 1 << (length - 1);
		do{
			if (((crc & 0x8000) == 0) ^ ((bitstring & bitmask) == 0 )){
				crc <<= 1;
				crc ^= polynomial;
			}
			else{
				crc <<= 1;
			}
		}while ((bitmask >>>= 1) != 0);
	}

	/**
	 * Return the calculated checksum.
	 * Erase it for next calls to add_bits().
	 * @return <code>short</code> with the calculated checksum
	 */
	public short checksum(){
		short sum = crc;
		crc = (short) 0xFFFF;
		return sum;
	}
}