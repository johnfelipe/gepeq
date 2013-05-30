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
 * Clase que contiene un estado y un mensaje. Se utiliza como salida de muchas comprobaciones, 
 * avisando de si es correcta y de qué ha fallado en su caso.
 * 
 * @author David Domínguez
 *
 */
public class MensajeEstado {
	
	/**
	 * Mensaje a almacenar en caso de que haya error.
	 */
	protected String mensaje;
	/**
	 * Estado de la comprobación.
	 */
	protected boolean estado;
	
	/**
	 * Constructor que simplemente pone el estado correcto, inicializando el estado a true. 
	 * Si no lo fuera, deberá ponerse a falso posteriormente. El mensaje es un <code>String
	 * </code> que se inicializa a la cadena vacía para después ir añadiendo avisos sin 
	 * problemas de si es la primera vez o no que se añade una cadena, añadiendo las sucesivas 
	 * con el operador '+' para cadenas.
	 */
	public MensajeEstado() {
		super();
		estado = true;				// Estado correcto
		mensaje = "";				// Cadena vacía
	}
	
	/**
	 * Método para obtener el estado de la clase.
	 * 
	 * @return				boolean con el estado de la clase
	 */
	public boolean isEstado() {
		return estado;
	}
	
	/**
	 * Método "set" para establecer el estado de la clase.
	 * 
	 * @param	estado		el estado al que poner la clase
	 */
	public void setEstado(boolean estado) {
		this.estado = estado;
	}
	
	/**
	 * Método "get" para obtener el mensaje de la clase.
	 * 
	 * @return				el mensaje almacenado en la clase
	 * @see		#setMensaje
	 */
	public String getMensaje() {
		return mensaje;
	}
	
	/**
	 * Método "set" para establecer el mensaje.
	 * 
	 * @param	mensaje		string con el mensaje a añadir al mensaje actual
	 * @see		#getMensaje
	 */
	public void setMensaje(String mensaje) {
		this.mensaje += mensaje;
	}
}