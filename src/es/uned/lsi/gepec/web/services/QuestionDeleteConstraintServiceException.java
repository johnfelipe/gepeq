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
package es.uned.lsi.gepec.web.services;

/**
 * Class that represents that a <i>ConstraintViolationException</i> has been produced when trying to delete a
 * question.<br/><br/>
 * The cause is that there is one or more tests using that question. 
 */
@SuppressWarnings("serial")
public class QuestionDeleteConstraintServiceException extends ServiceException
{
	public QuestionDeleteConstraintServiceException()
	{
		super();
	}
	
	public QuestionDeleteConstraintServiceException(String message,Throwable cause)
	{
		super(message, cause);
	}
	
	public QuestionDeleteConstraintServiceException(String message)
	{
		super(message);
	}
	
	public QuestionDeleteConstraintServiceException(Throwable cause)
	{
		super(cause);
	}
}
