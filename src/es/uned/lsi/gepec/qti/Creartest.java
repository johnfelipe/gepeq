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

import java.io.IOException;

import es.uned.lsi.gepec.qti.*;

import es.uned.lsi.gepec.util.*;

/**
 * Componente de control impl�cito asociado a la p�gina JSP "creartest.jsp". Es la l�gica 
 * asociada a esa p�gina JSP. Se ejecutar� su m�todo <code>doLogic</code> siempre que sea 
 * llamada la p�gina. Su funci�n es comprobar que todos los par�metros iniciales de la toma de 
 * datos para crear un nuevo test sean correctos, en cuyo caso redirecciona la respuesta 
 * a la p�gina de seleccionar los �tems a incluir en el test. La clase implementa la interfaz 
 * <code>Control</code>, implementando su m�todo <code>doLogic</code>, que es el �nico que 
 * contiene, y que es el encargado de realizar la l�gica asociada a la p�gina "creartest.jsp".
 * 
 * @author 	Blas Medina Alcudia
 * @see		control.Control
 * 
 */
public class Creartest  {

	/**
	 * Realiza la comprobaci�n de los par�metros que se recogen en la p�gina "creartest.jsp". 
	 * Ser� llamado por el filtro <code>ControlFilter</code> en el caso de que detecte que la 
	 * p�gina requerida es "creartest.jsp". Ya que es llamado desde un filtro, ser� ejecutado 
	 * siempre que llegue una petici�n de un cliente y antes de que la petici�n llegue al 
	 * Servlet (p�gina JSP compilada a Servlet) que sirve la respuesta al cliente. Comprueba 
	 * uno por uno todos los par�metros, y, en caso de que sean correctos, crea un objeto <code>
	 * AssessmentTest</code> para guardar los datos asociados al test y lo guarda en el �mbito 
	 * de sesi�n para utilizarlos m�s adelante para terminar de crear el test.
	 * 
	 * @param	request				la petici�n http enviada por el cliente
	 * @param	response			la respuesta http a enviar desde el servidor
	 * @return						true para que se sigan ejecutando los dem�s filtros del 
	 * 								servidor y se env�e la respuesta correctamente
	 * @throws	IOException			no debe de producirse
	 * @throws	ServletException	si hay alg�n problema accediendo a request o response
	 * @see control.Control#doLogic(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 * @see 	control.ControlFilter
	 * @see		javax.servlet.http.HttpServletRequest
	 * @see		javax.servlet.http.HttpServletResponse
	 * @see		utilidades.EstadoWeb
	 * @see		utilidades.MensajeEstado
	 * @see		utilidades.EnteroPositivo
	 * @see		xml.test.AssessmentTest
	 */
	public boolean doLogic() throws IOException {
		
			// Caracteres reservados no permitidos
			final String reservados = ";/?:@&=+$,\\*\"<>|";

			String identificador = "Identif";	// Identificador del test 
			String titulo = "Tit";			// T�tulo dele test
			String instrucciones = "Instruc";	// Instrucciones del test
			// Permitir revisi�n del test al finalizarlo
			String rev = "allowReview";
			String selec = "Select";	// N�mero de �tems a mostrar
			String Shuff = "Shuffle";		// Mezclar los �tems o no
			
			// Estado de la petici�n web. Almacena si son correctos los par�metros, los 
			// mensajes de aviso al usuario y d�nde colocar el foco en la p�gina
			EstadoWeb estado = new EstadoWeb();		
			
			boolean shuffle = false;		// Valor por defecto del par�metro
			if ( Shuff != null){			// Interpreta el valor marcado en la p�gina 
				if (Shuff.equalsIgnoreCase("ON"))
					shuffle = true;
			}

			// Comprueba que est�n introducidos todos los atributos obligatorios
			if (identificador != null && !identificador.equals("") &&
					titulo != null && !titulo.equals("") &&
					rev != null && !rev.equals("")){

				// Comprueba que los caracteres del identificador est�n permitidos
		        for(int i = 0; i < identificador.length() && estado.isEstado() == true; i++) {
		        	char c = identificador.charAt(i);	// Lee el siguiente car�cter
		        	int marca = reservados.indexOf(c);
		        	if (marca >= 0 ){
						estado.a�adeError("Error: El car�cter '" + reservados.charAt(marca) + 
								"' en la posici�n " + (i+1) + " del Identificador no est� " +
								"permitido<br />", "Identif");
		        	}
		        }

		        // Permitir o no la revisi�n del documento
				boolean revision = Boolean.parseBoolean(rev);
				
				// Comprueba que el n�mero de �tems a seleccionar est� vac�o o sea un valor
				// v�lido
				EnteroPositivo seleccion;
				if (selec == null || selec.equals("")){
					seleccion = new EnteroPositivo("0");
					seleccion.esPositivo();			// Para actualizar el valor entero
				}
				else{
					seleccion = new EnteroPositivo(selec);
					MensajeEstado msg = seleccion.esPositivo();
					if(!msg.isEstado()){  // Si no es un n�mero v�lido y mayor o igual que cero
						estado.a�adeError("Error: el n�mero de �tems a seleccionar " + 
								msg.getMensaje() + "<br />", "Select");
					}
				}
				
				// Comprueba que las instrucciones sean un c�digo XHTML v�lido
				if (instrucciones != null && !instrucciones.equals("")){
					MensajeEstado msg = ParserNeutro.compruebaInstruccionesXHTML(instrucciones);
					if (msg.isEstado() == false)
						estado.a�adeError(msg.getMensaje(), "Instruc");
				}

				// Si el estado es todo correcto pasa a generar el objeto contenedor del test 
				if ( estado.isEstado()){
					// M�todo de ordenaci�n
					Ordering orden = null;
					if (shuffle == true)
						orden = new Ordering (shuffle);
					// M�todo de seleccionar �tems
					Selection select = null;
					// Si se selecciona alg�n n�mero de �tems del total, es sin reemplazamiento, 
					// ya que los �tems son constantes, no contienen variables, y no tiene 
					// sentido repetirlos en una secci�n
					if (seleccion.getEnteroPositivo() != 0)
						select = new Selection(seleccion.getEnteroPositivo(), false);
					// Se inicializa el objeto Secci�n del test excepto las referencias a los
					// �tems, con un identificador v�lido que es v�lido, por eso no se comprueba
					AssessmentSection seccion = 
						new AssessmentSection(new Identificador("seccion1"), titulo, true, 
								select, orden, instrucciones);
					// Objeto de control para permitir la revisi�n o no
					ItemSessionControl control = new ItemSessionControl(revision);
					// Se inicializa el objeto Parte de test con la Secci�n anterior y con un 
					// identificador v�lido, que como s�lo hay una parte, no se realiza ninguna 
					// comprobaci�n, el objeto control anterior, y estableciendo la navegaci�n y 
					// la presentaci�n a esos valores que son los �nicos que soporta esta 
					// implementaci�n
					TestPart parte = new TestPart(new Identificador("parte1"), "nonlinear", 
							"simultaneous", control, seccion);
					// Objeto Test inicializado con los datos proporcionados por el usuario 
					// y los objetos creados m�s arriba
					AssessmentTest test = new AssessmentTest(identificador, titulo, parte);
					// Guarda el objeto AsessmentTest en sesi�n para seguir utiliz�ndolo luego
				}
			}
			// Si no est�n todos los atributos obligatorios comprueba los que fallan
			// para informar al usuario
			else{
				if (identificador.equals(""))
					estado.a�adeError("Debe introducir un Identificador<br />", "Identif");
				if (titulo.equals(""))
					estado.a�adeError("Debe introducir un T�tulo<br />", "Tit");
				if (rev == null || rev.equals(""))
					estado.a�adeError("Debe marcar si desea permitir la revisi�n del test " +
							"al finalizarlo<br />", "Identif");
			}
			
		return true;
	}
}