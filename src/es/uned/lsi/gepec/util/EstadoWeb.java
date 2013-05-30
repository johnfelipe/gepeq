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

/**
 * Clase que representa el estado de comprobación de una Web. La clase hereda de <code>
 * MensajeEstado</code>, añadiéndole la funcionalidad de almacenar además en qué componente del 
 * formulario de la página web se debe localizar el foco en caso de error. Se encarga de 
 * almacenar el estado de la comprobación de los datos en cualquier toma de datos de creación de 
 * un ítem o un test, y, en caso de error, contiene los mensajes de aviso a mostrar al usuario y 
 * dónde posicionar el foco en la página.
 * 
 * @author	David Domínguez
 * @see		utilidades.MensajeEstado
*
 */
public class EstadoWeb extends MensajeEstado {

	/**
	 * Nombre del campo erróneo sobre el que irá el foco en la página.
	 */
	protected String focusON;

	/**
	 * El constructor llama al constructor de la superclase e inicializa la posición del foco 
	 * a null.
	 * 
	 * @see		utilidades.MensajeEstado
	 */
	public EstadoWeb() {
		super();
		focusON = null;
	}

	/**
	 * Método "get" para obtener la posición del foco. Obtiene el <code>String</code> que lo 
	 * almacena.
	 * 
	 * @return	string con el nombre del campo del formulario en el que localizar el foco en 
	 * 			caso de	error
	 */
	public String getFocusON() {
		return focusON;
	}

	/**
	 * Añade un error al estado de la comprobación. Al añadir el error se añade el nuevo aviso 
	 * al ya existente, se pone el estado en falso, ya que añade un error, y, si el foco aún 
	 * no se ha inicializado a ningún valor, lo inicializa al que recibe como parámetro. Con 
	 * esto logra que foco se posicione en el primer error encontrado y no varíe al añadir 
	 * más errores.
	 *  
	 * @param 	aviso	mensaje de aviso a añadir a los existentes
	 * @param 	focus	campo erróneo en el que posicionar el foco en la página
	 * @see		utilidades.MensajeEstado
	 */
	public void añadeError(String aviso, String focus){
		this.mensaje += aviso;			// Añade el nuevo aviso a los ya existentes
		this.estado = false;			// El estado es incorrecto ya que se añade un error
		if (this.focusON == null)		// Si el foco aún no se ha inicializado se inicializa 
			this.focusON = focus;		// al valor recibido en el parámetro
	}
}