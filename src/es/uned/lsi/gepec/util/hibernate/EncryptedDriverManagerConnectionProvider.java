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
package es.uned.lsi.gepec.util.hibernate;

import java.util.Properties;

import org.hibernate.HibernateException;
import org.hibernate.cfg.Environment;
import org.hibernate.connection.DriverManagerConnectionProvider;

import es.uned.lsi.gepec.util.Decryptor;

public class EncryptedDriverManagerConnectionProvider extends DriverManagerConnectionProvider
{
	public EncryptedDriverManagerConnectionProvider()
	{
		super();
	}
	
	@Override
	public void configure(Properties properties) throws HibernateException
	{
		// Decrypt some properties if needed
		properties.setProperty(
			Environment.DIALECT,Decryptor.decrypt(properties.getProperty(Environment.DIALECT)));
		properties.setProperty(
			Environment.DRIVER,Decryptor.decrypt(properties.getProperty(Environment.DRIVER)));
		properties.setProperty(Environment.URL,Decryptor.decrypt(properties.getProperty(Environment.URL)));
		properties.setProperty(Environment.USER,Decryptor.decrypt(properties.getProperty(Environment.USER)));
		properties.setProperty(Environment.PASS,Decryptor.decrypt(properties.getProperty(Environment.PASS)));
		
		// Let Hibernate process
		super.configure(properties);
	}
}
