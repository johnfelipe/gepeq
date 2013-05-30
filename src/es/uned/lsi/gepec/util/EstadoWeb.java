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
 * Clase que representa el estado de comprobaci�n de una Web. La clase hereda de <code>
 * MensajeEstado</code>, a�adi�ndole la funcionalidad de almacenar adem�s en qu� componente del 
 * formulario de la p�gina web se debe localizar el foco en caso de error. Se encarga de 
 * almacenar el estado de la comprobaci�n de los datos en cualquier toma de datos de creaci�n de 
 * un �tem o un test, y, en caso de error, contiene los mensajes de aviso a mostrar al usuario y 
 * d�nde posicionar el foco en la p�gina.
 * 
 * @author	David Dom�nguez
 * @see		utilidades.MensajeEstado
*
 */
public class EstadoWeb extends MensajeEstado {

	/**
	 * Nombre del campo err�neo sobre el que ir� el foco en la p�gina.
	 */
	protected String focusON;

	/**
	 * El constructor llama al constructor de la superclase e inicializa la posici�n del foco 
	 * a null.
	 * 
	 * @see		utilidades.MensajeEstado
	 */
	public EstadoWeb() {
		super();
		focusON = null;
	}

	/**
	 * M�todo "get" para obtener la posici�n del foco. Obtiene el <code>String</code> que lo 
	 * almacena.
	 * 
	 * @return	string con el nombre del campo del formulario en el que localizar el foco en 
	 * 			caso de	error
	 */
	public String getFocusON() {
		return focusON;
	}

	/**
	 * A�ade un error al estado de la comprobaci�n. Al a�adir el error se a�ade el nuevo aviso 
	 * al ya existente, se pone el estado en falso, ya que a�ade un error, y, si el foco a�n 
	 * no se ha inicializado a ning�n valor, lo inicializa al que recibe como par�metro. Con 
	 * esto logra que foco se posicione en el primer error encontrado y no var�e al a�adir 
	 * m�s errores.
	 *  
	 * @param 	aviso	mensaje de aviso a a�adir a los existentes
	 * @param 	focus	campo err�neo en el que posicionar el foco en la p�gina
	 * @see		utilidades.MensajeEstado
	 */
	public void a�adeError(String aviso, String focus){
		this.mensaje += aviso;			// A�ade el nuevo aviso a los ya existentes
		this.estado = false;			// El estado es incorrecto ya que se a�ade un error
		if (this.focusON == null)		// Si el foco a�n no se ha inicializado se inicializa 
			this.focusON = focus;		// al valor recibido en el par�metro
	}
}