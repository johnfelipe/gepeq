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
package es.uned.lsi.gepec.qti;

import es.uned.lsi.gepec.util.Identificador;

/**
 * Clase que representa a la clase abstracta del mismo nombre seg�n la norma QTI del IMS. Es la 
 * superclase de otras clases que componen un Test. Contiene los atributos que se corresponden 
 * con los definidos en la norma.
 * 
 * @author Blas Medina Alcudia
 *
 */
public abstract class SectionPart {

	/**
	 * Identificador del elemento.
	 */
	protected Identificador identificador;
	/**
	 * boolean indicando si la subclase est� requerida obligatoriamente en caso de que se use 
	 * alg�n criterio de selecci�n.
	 */
	protected boolean requerido;
	/**
	 * boolean indicando si la subclase est� fija en caso de que se use alg�n criterio de 
	 * ordenaci�n.
	 */
	protected boolean fijo;
		
	/**
	 * Constructor de la clase que simplemente le da valores iniciales a las variables de clase.
	 * 
	 * @param 	identificador	identificador que contiene un identificador v�lido para la clase
	 * @param 	requerido		boolean indicando que este elemento es requerido
	 * @param 	fijo			boolean indicando que este elemento est� en posici�n fija
	 */
	public SectionPart(Identificador identificador, boolean requerido, boolean fijo) {
		this.identificador = identificador;
		this.requerido = requerido;
		this.fijo = fijo;
	}

	/**
	 * M�todo "get" para obtener el identificador de la clase.
	 * 
	 * @return		identificador que contiene el identificador v�lido de la clase
	 */
	public Identificador getIdentificador() {
		return identificador;
	}
}