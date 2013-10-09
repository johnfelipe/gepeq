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

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.proxy.HibernateProxy;

import es.uned.lsi.gepec.model.dao.DaoException;

/**
 * Utility class to use Hibernate within GEPEQ. 
 */
public class HibernateUtil
{
	/**
	 * Utility class that represents an operation against DBMS.<br/><br/>
	 * It is used internally by service methods to execute several DAO methods (executing queries/updates against 
	 * DBMS) within the same transaction and commiting changes if needed.<br/><br/>
	 * You can even create your own operation and pass it to service classes to execute several service methods 
	 * within the same transaction.<br/><br/>
	 * However be careful that it is only supported a commit per operation (and a rollback before commit).
	 */
	@SuppressWarnings("serial")
	public static class Operation implements Serializable
	{
		public Operation()
		{
			session=null;
			tx=null;
			commitDone=false;
		}
		
		public Session session;
		
		private Transaction tx;
		private boolean commitDone;
		
		public void commit() throws DaoException
		{
			if (!commitDone)
			{
				try
				{
					tx.commit();
					commitDone=true;
				}
				catch (HibernateException he)
				{
					throw new DaoException(he.getMessage(),he);
				}
			}
		}
		
		public void rollback() throws DaoException
		{
			if (!commitDone)
			{
				try
				{
					tx.rollback();
				}
				catch (HibernateException he)
				{
					throw new DaoException(he.getMessage(),he);
				}
			}
		}
	}
	
	private static final SessionFactory SESSION_FACTORY;
	static
	{
		try
		{
			SESSION_FACTORY=new Configuration().configure().buildSessionFactory();
		}
		catch (Throwable ex)
		{
			StringBuffer error=new StringBuffer("Initial SessionFactory creation failed.");
			error.append(ex.toString());
			System.err.println(error.toString());;
			throw new ExceptionInInitializerError(ex);
		}
	}
	
	/**
	 * Starts a session and transaction against DBMS.
	 * @return Operation with the created session and transaction
	 * @throws HibernateException
	 */
	public static Operation startOperation() throws HibernateException
	{
		Operation operation=new Operation();
		try
		{
			operation.session=SESSION_FACTORY.openSession();
			operation.tx=operation.session.beginTransaction();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return operation;
	}
	
	/**
	 * Ends a session and transaction against DBMS.
	 * @param operation Operation with session and transaction
	 * @throws HibernateException
	 */
	public static void endOperation(Operation operation) throws HibernateException
	{
		if (operation!=null)
		{
			operation.session.close();
		}
	}
	
	/**
	 * Transforms a hibernate proxy to the entity's class.
	 * @param entity
	 * @return entity
	 */
	@SuppressWarnings("unchecked")
	public static <T> T unProxy(T entity)
	{
		if (entity instanceof HibernateProxy)
		{
			entity=(T)((HibernateProxy)entity).getHibernateLazyInitializer().getImplementation();
		}
		return entity;
	}
	
	/**
	 * Transforms a hibernate proxy object to the desired clazz, if possible.<br/><br/>
	 * For when the entity is an instance of clazz.
	 * @param entity
	 * @param clazz
	 * @return entity cast to clazz
	 */
	@SuppressWarnings("unchecked")
	public static <T,Y> Y unProxyToClass(T entity,Class<Y> clazz)
	{
		return (Y)unProxy(entity);
	}
}