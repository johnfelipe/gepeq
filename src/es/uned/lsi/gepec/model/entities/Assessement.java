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

// Generated 21-nov-2012 14:22:32 by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Assessement generated by hbm2java
 */
@SuppressWarnings("serial")
@Entity
@SequenceGenerator(name="assessements_id_seq", sequenceName="assessements_id_seq") 
@Table(name = "assessements", schema = "public")
public class Assessement implements java.io.Serializable {

	private long id;
	private String type;

	public Assessement() {
	}

	public Assessement(long id) {
		this.id = id;
	}

	public Assessement(long id, String type) {
		this.id = id;
		this.type = type;
	}

	@Id
	@GeneratedValue(generator="assessements_id_seq", strategy=GenerationType.AUTO)
	@Column(name = "id", unique = true, nullable = false)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Column(name = "type", nullable = false, length = 30)
	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Set the fields of this assesement with the values from fields from other assessement.
	 * @param otherAssessement Other assessement
	 */
	@Transient
	public void setFromOtherAssessement(Assessement otherAssessement)
	{
		if (otherAssessement!=null)
		{
			setId(otherAssessement.getId());
			setType(otherAssessement.getType());
		}
	}
	
	/**
	 * @return A copy of this assessement.
	 */
	@Transient
	public Assessement getAssessementCopy()
	{
		return new Assessement(getId(),getType());
	}
	
	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof Assessement && getId()==((Assessement)obj).getId();
	}
}
