/* OpenMark Authoring Tool (GEPEQ)
 * Copyright (C) 2013 UNED
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package es.uned.lsi.gepec.util;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * Utility class to validate e-mail addresses
 */
public class EmailValidator
{
	/**
	 * @param address E-mail address to validate
	 * @return true if e-mail address is a valid not local email address, false otherwise
	 */
	public static boolean validate(String address)
	{
		boolean ok=false;
		if (address!=null)
		{
			try
			{
				new InternetAddress(address);
				String[] addressTokens=address.split("@");
				if (addressTokens.length==2)
				{
					String name=addressTokens[0].trim();
					String domain=addressTokens[1].trim();
					ok=!"".equals(name) && !"".equals(domain) && !"localhost".equals(domain);
				}
			}
			catch (AddressException ae)
			{
				ok=false;
			}
		}
		return ok;
	}
}
