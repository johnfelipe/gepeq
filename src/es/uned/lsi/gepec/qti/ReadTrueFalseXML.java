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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import es.uned.lsi.gepec.model.entities.*;

public class ReadTrueFalseXML {

	static final String TITLE = "title";
	static final String ASSESSMENTITEM = "assessmentItem";
	static final String CARDINALITY = "cardinality";
	static final String RESPONSEDECLARATION = "responsedeclaration";
	static final String VALUE = "value";
	static final String CORRECTRESPONSE = "correctResponse";
	static final String OUTCOMEDECLARATION = "outcomeDeclaration";
	static final String SHUFFLE = "shuffle";
	static final String CHOICEINTERACTION = "choiceInteraction";
	static final String ITEMBODY = "itemBody";
	static final String PROMPT = "prompt";
	static final String SIMPLECHOICE = "simpleChoice";
	static final String FIXED = "fixed";
	static final String IDENTIFIER = "identifier";

		@SuppressWarnings({ "unchecked", "null" })
		public List<TrueFalseQuestion> readConfig(String configFile) {
			List<TrueFalseQuestion> questions = new ArrayList<TrueFalseQuestion>();
			try {
				// First create a new XMLInputFactory
				XMLInputFactory inputFactory = XMLInputFactory.newInstance();
				// Setup a new eventReader
				InputStream in = new FileInputStream(configFile);
				XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
				// Read the XML document
				TrueFalseQuestion question = null;
				question = new TrueFalseQuestion();
				Answer answer = new Answer();
				List<Answer> answers = new ArrayList<Answer>();
				List correct = new ArrayList();
				while (eventReader.hasNext()) {
					XMLEvent event = eventReader.nextEvent();

					if (event.isStartElement()) {
						StartElement startElement = event.asStartElement();
						// If we have a question element we create a new question
						if (startElement.getName().getLocalPart() == (ASSESSMENTITEM)) {
							// We read the attributes from this tag and add the title
							// attribute to our object
							Iterator<Attribute> attributes = startElement
									.getAttributes();
							while (attributes.hasNext()) {
								Attribute attribute = attributes.next();
								if (attribute.getName().toString().equals(TITLE)) {
									question.setName(attribute.getValue());
								}

							}
						}
						if (startElement.getName().getLocalPart() == (CHOICEINTERACTION)) {						
							Iterator<Attribute> attributes2 = startElement
									.getAttributes();
							while (attributes2.hasNext()) {
								Attribute attribute = attributes2.next();
								if (attribute.getName().toString().equals(SHUFFLE)) {
								  //question.setShuffle(Boolean.valueOf(attribute.getValue()));
								}

							}
						}

						if (event.asStartElement().getName().getLocalPart()
								.equals(VALUE)) {
							event = eventReader.nextEvent();
							correct.add(event.asCharacters().getData());
							continue;
						}
						
						if (event.asStartElement().getName().getLocalPart()
								.equals(PROMPT)) {
							event = eventReader.nextEvent();
							question.setQuestionText(event.asCharacters().getData());
							continue;
						}

						if (startElement.getName().getLocalPart() == (SIMPLECHOICE)) {						
							//System.out.println("hola");
							Iterator<Attribute> attributes3 = startElement
									.getAttributes();
							while (attributes3.hasNext()) {
								Attribute attribute = attributes3.next();
								//System.out.println("preguntas correctas: "+ correct.size());
								if (attribute.getName().toString().equals(IDENTIFIER)) {
									  int escorrecta = correct.indexOf(attribute.getValue());
									  //System.out.println("IDENTIFICADOR: " + attribute.getValue());
									  //System.out.println(escorrecta);
									  if (escorrecta >= 0)
									  {
										answer.setCorrect(true);  
									  } 
									}
								if (attribute.getName().toString().equals(FIXED)) {
								  answer.setFixed(Boolean.valueOf(attribute.getValue()));
								  //System.out.println("texto fija" + answer.getFixed());
								}

							}
						}

						if (event.asStartElement().getName().getLocalPart()
								.equals(SIMPLECHOICE)) {
							//System.out.println("hola2");
							event = eventReader.nextEvent();
							answer.setText(event.asCharacters().getData());
							//System.out.println("texto encuentro respuesta:" + answer.getText());
							continue;
						}
					}
					if (event.isEndElement()) {
						EndElement endElement = event.asEndElement();
						if (endElement.getName().getLocalPart() == (VALUE)) {
						}
					}
					// If we reach the end of an item element we add it to the list
					if (event.isEndElement()) {
						EndElement endElement = event.asEndElement();
						if (endElement.getName().getLocalPart() == (SIMPLECHOICE)) {
							//System.out.println("holafinalchoice y añado" + answer.getText()+ " " + answer.getFixed());
							answers.add(answer);
							answer = new Answer();
						}
					}
					if (event.isEndElement()) {
						EndElement endElement = event.asEndElement();
						if (endElement.getName().getLocalPart() == (ASSESSMENTITEM)) {
							//System.out.println("numero elementos" + answers.size());
							question.setAnswers(answers);
							questions.add(question);
						}
					}

				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
			return questions;
		}

	}

