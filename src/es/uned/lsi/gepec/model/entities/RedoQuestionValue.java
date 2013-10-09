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
package es.uned.lsi.gepec.model.entities;

//Generated 11-abr-2012 15:51:55 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * RedoQuestionValue generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@SequenceGenerator(name="redoquestionvalues_id_seq", sequenceName="redoquestionvalues_id_seq") 
@Table(name = "redoquestionvalues", schema = "public")
public class RedoQuestionValue implements java.io.Serializable {

	private long id;
	private String value;

	public RedoQuestionValue() {
	}

	public RedoQuestionValue(long id, String value) {
		this.id = id;
		this.value = value;
	}

	@Id
	@GeneratedValue(generator="redoquestionvalues_id_seq", strategy=GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "value", nullable = false, length = 30)
	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	/**
	 * Set the fields of this available value for property 'redoQuestion' with the values from fields from other 
	 * available value for property 'redoQuestion'.
	 * @param otherRedoQuestion Other available value for property 'redoQuestion'
	 */
	@Transient
	public void setFromOtherRedoQuestionValue(RedoQuestionValue otherRedoQuestion)
	{
		if (otherRedoQuestion!=null)
		{
			setId(otherRedoQuestion.getId());
			setValue(otherRedoQuestion.getValue());
		}
	}
	
	/**
	 * @return A copy of this available value for property 'redoQuestion'.
	 */
	@Transient
	public RedoQuestionValue getRedoQuestionValueCopy()
	{
		return new RedoQuestionValue(getId(),getValue());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof RedoQuestionValue && getId()==((RedoQuestionValue)obj).getId();
	}
}
