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
 * Clase que contiene la representación de un número entero positivo. Se encarga de recibirlo 
 * como un <code>String</code> y de comprobar que sea efectivamente un número entero positivo. 
 * Contiene métodos para almacenar y recoger el número, e informa de cualquier anomalía en él.
 * 
 * @author David Domínguez
 *
 */
public class EnteroPositivo {
	/**
	 * Variable que contiene el número en un String.
	 */
	private String stringNumero = null;
	/**
	 * Variable con el número ya en un entero.
	 */
	private int enteroPositivo;

	/**
	 * Constructor que recibe la representación del número en un <code>String</code>. Lo 
	 * almacena en la variable de clase dedicada a tal efecto.
	 * 
	 * @param 	stringNumero	número a almacenar en la clase como un String.
	 */
	public EnteroPositivo(String stringNumero) {
		super();
		this.stringNumero = stringNumero;
	}

	/**
	 * Método "get" para el número entero positivo como un entero. 
	 * ATENCIÓN: Antes de usarlo se debe haber comprobado que el número es un entero realmente 
	 * mediante el método <code>esPositivo</code>, y en caso de que devuelva un <code>
	 * MensajeEstado</code> que indique validez, podrá usarse este método.
	 * 
	 * @return		entero representando el valor que almacena la clase
	 */
	public int getEnteroPositivo() {
		return enteroPositivo;
	}

	/**
	 * Método "get" para el número entero positivo almacenado como un <code>String</code>. 
	 * Se puede usar siempre, pero sin usar el método <code>esPositivo</code> no se tendrá 
	 * seguridad de que el <code>String</code> represente un número entero positivo válido.
	 * 
	 * @return		String con el valor almacenado en la clase
	 */
	public String getStringNumero() {
		return stringNumero;
	}

	/**
	 * Comprueba que el número <code>String</code> almacenado sea un número entero positivo. 
	 * Comprueba que no sea una cadena vacía, que sea un número entero, y que sea cero o mayor 
	 * que cero, es decir, un número entero positivo válido.
	 * 
	 * @return		mensajeEstado indicando si ha ido bien la comprobación, en cuyo caso no 
	 * 				incluye	mensajes, o, si ha ido mal, indicando en el mensaje el error 
	 * 				ocurrido en la comprobación
	 * @see			utilidades.MensajeEstado
	 * @see			Integer#parseInt(java.lang.String)
	 */
	public MensajeEstado esPositivo(){
		MensajeEstado msg = new MensajeEstado();	// Mensaje de salida con el estado final
		
		if (stringNumero != null && !stringNumero.equals("")){
			// Si la cadena no está vacía
			try {
				enteroPositivo = Integer.parseInt(stringNumero);
				if (enteroPositivo < 0){
					// Si el número es menor que cero (negativo)
					msg.setMensaje("es un número menor que cero");
					msg.setEstado(false);
				}
			} catch (NumberFormatException e){
				// Si la cadena no contiene un número entero válido captura la excepción
				msg.setMensaje("es un entero positivo no válido");
				msg.setEstado(false);
			}
		} else {
			// Si la cadena está vacía
			msg.setMensaje("está vacío");
			msg.setEstado(false);
		}
		
		// Devuelve el mensaje con el estado de la comprobación
		return msg;
	}
}