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
 * Clase que contiene la representaci�n de un n�mero entero positivo. Se encarga de recibirlo 
 * como un <code>String</code> y de comprobar que sea efectivamente un n�mero entero positivo. 
 * Contiene m�todos para almacenar y recoger el n�mero, e informa de cualquier anomal�a en �l.
 * 
 * @author David Dom�nguez
 *
 */
public class EnteroPositivo {
	/**
	 * Variable que contiene el n�mero en un String.
	 */
	private String stringNumero = null;
	/**
	 * Variable con el n�mero ya en un entero.
	 */
	private int enteroPositivo;

	/**
	 * Constructor que recibe la representaci�n del n�mero en un <code>String</code>. Lo 
	 * almacena en la variable de clase dedicada a tal efecto.
	 * 
	 * @param 	stringNumero	n�mero a almacenar en la clase como un String.
	 */
	public EnteroPositivo(String stringNumero) {
		super();
		this.stringNumero = stringNumero;
	}

	/**
	 * M�todo "get" para el n�mero entero positivo como un entero. 
	 * ATENCI�N: Antes de usarlo se debe haber comprobado que el n�mero es un entero realmente 
	 * mediante el m�todo <code>esPositivo</code>, y en caso de que devuelva un <code>
	 * MensajeEstado</code> que indique validez, podr� usarse este m�todo.
	 * 
	 * @return		entero representando el valor que almacena la clase
	 */
	public int getEnteroPositivo() {
		return enteroPositivo;
	}

	/**
	 * M�todo "get" para el n�mero entero positivo almacenado como un <code>String</code>. 
	 * Se puede usar siempre, pero sin usar el m�todo <code>esPositivo</code> no se tendr� 
	 * seguridad de que el <code>String</code> represente un n�mero entero positivo v�lido.
	 * 
	 * @return		String con el valor almacenado en la clase
	 */
	public String getStringNumero() {
		return stringNumero;
	}

	/**
	 * Comprueba que el n�mero <code>String</code> almacenado sea un n�mero entero positivo. 
	 * Comprueba que no sea una cadena vac�a, que sea un n�mero entero, y que sea cero o mayor 
	 * que cero, es decir, un n�mero entero positivo v�lido.
	 * 
	 * @return		mensajeEstado indicando si ha ido bien la comprobaci�n, en cuyo caso no 
	 * 				incluye	mensajes, o, si ha ido mal, indicando en el mensaje el error 
	 * 				ocurrido en la comprobaci�n
	 * @see			utilidades.MensajeEstado
	 * @see			Integer#parseInt(java.lang.String)
	 */
	public MensajeEstado esPositivo(){
		MensajeEstado msg = new MensajeEstado();	// Mensaje de salida con el estado final
		
		if (stringNumero != null && !stringNumero.equals("")){
			// Si la cadena no est� vac�a
			try {
				enteroPositivo = Integer.parseInt(stringNumero);
				if (enteroPositivo < 0){
					// Si el n�mero es menor que cero (negativo)
					msg.setMensaje("es un n�mero menor que cero");
					msg.setEstado(false);
				}
			} catch (NumberFormatException e){
				// Si la cadena no contiene un n�mero entero v�lido captura la excepci�n
				msg.setMensaje("es un entero positivo no v�lido");
				msg.setEstado(false);
			}
		} else {
			// Si la cadena est� vac�a
			msg.setMensaje("est� vac�o");
			msg.setEstado(false);
		}
		
		// Devuelve el mensaje con el estado de la comprobaci�n
		return msg;
	}
}