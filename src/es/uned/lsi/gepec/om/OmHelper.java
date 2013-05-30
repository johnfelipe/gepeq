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
package es.uned.lsi.gepec.om;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import es.uned.lsi.gepec.model.entities.Answer;
import es.uned.lsi.gepec.model.entities.Feedback;
import es.uned.lsi.gepec.model.entities.Question;
import es.uned.lsi.gepec.model.entities.QuestionResource;
import es.uned.lsi.gepec.model.entities.Resource;

/**
 * Clase de ayuda para la exportación de preguntas y 
 * pruebas al sistema OM
 * 
 * @author Víctor Manuel Alonso Rodríguez
 * @since  12/2011
 */
public class OmHelper
{
	/**
	 * Escape sequence character
	 */
	private final static char SEQUENCE_ESCAPE_CHARACTER='\\';
	
	/** 
	 * Character used to open a selector
	 */
	private static final char SELECTOR_OPEN='['; 
	
	/** 
	 * Character used to close a selector
	 */
	private static final char SELECTOR_CLOSE=']'; 
	
	/** Symbol used as OR operator in answer property: ',' */
	private static final char ANSWER_OR_SYMBOL=',';
	
	/** Symbol used as AND operator in answer property: '+' (UNICODE: '\u002B') */
	private static final char ANSWER_AND_SYMBOL='+';
	
	/** Symbol used as NOT operator in answer property: '!' */
	private static final char ANSWER_NOT_SYMBOL='!';
	
	public OmHelper()
	{
	}
	
	/**
	 * @param answer Answer's feedback attribute
	 * @return List of groups of answers separated by , (the OR operator)
	 */
	public static List<String> getOrAnswers(String answer)
	{
		List<String> orAnswers=new ArrayList<String>();
		if (answer!=null)
		{
			orAnswers=splitButNotInsideSelectors(answer,ANSWER_OR_SYMBOL,getEscapeSequences());
		}
		return orAnswers;
	}
	
	
	/**
	 * @param orAnswer One of the groups of answers returned by 'getOrAnswers'
	 * @return List of answers separated by + (the AND operator)
	 */
	public static List<String> getAndAnswers(String orAnswer)
	{
		List<String> andAnswers=new ArrayList<String>();
		if (orAnswer!=null)
		{
			andAnswers=splitButNotInsideSelectors(orAnswer,ANSWER_AND_SYMBOL,getEscapeSequences());
		}
		return andAnswers; 
	}
	
	/**
	 * @param andAnswer One of the answers returned by 'getAndAnswers'
	 * @return true if the answer is negated by ! (the NOT operator)
	 */
	public static boolean isAnswerFlagNot(String andAnswer)
	{
		return andAnswer!=null && andAnswer.length()>0 && andAnswer.charAt(0)==ANSWER_NOT_SYMBOL;
	}
	
	
	/**
	 * Decrement all OM answer components identifiers above given position inside the string received.
	 * <br/><br/>
	 * It is useful when we delete an answer to update anwers's feedbacks attributes because we decrement
	 * positions of some answers components identifiers and we need to apply these changes also to them.
	 * @param answer Answer's feedback attribute
	 * @param position Removed position
	 * @return Same string with all OM answer components identifiers above given position decremented
	 */
	public static String decrementOmIdsOfFeedbackAnswer(String answer,int position)
	{
		StringBuffer decrementedAnswer=null;
		if (answer!=null)
		{
			decrementedAnswer=new StringBuffer();
			List<String> orAnswers=splitButNotInsideSelectors(answer,ANSWER_OR_SYMBOL,getEscapeSequences());
			boolean insertOrSymbol=false;
			for (String orAnswer:orAnswers)
			{
				if (insertOrSymbol)
				{
					decrementedAnswer.append(ANSWER_OR_SYMBOL);
				}
				else
				{
					insertOrSymbol=true;
				}
				List<String> andAnswers=
					splitButNotInsideSelectors(orAnswer,ANSWER_AND_SYMBOL,getEscapeSequences());
				boolean insertAndSymbol=false;
				for (String andAnswer:andAnswers)
				{
					if (insertAndSymbol)
					{
						decrementedAnswer.append(ANSWER_AND_SYMBOL);
					}
					else
					{
						insertAndSymbol=true;
					}
					boolean notTesting=andAnswer.length()>0 && andAnswer.charAt(0)==ANSWER_NOT_SYMBOL;
					if (notTesting)
					{
						decrementedAnswer.append(ANSWER_NOT_SYMBOL);
					}
					StringBuffer decrementedId=new StringBuffer();
					String onlyIdAnswer=getOnlyId(andAnswer);
					if (onlyIdAnswer.startsWith("answer"))
					{
						try
						{
							int positionId=Integer.parseInt(onlyIdAnswer.substring("answer".length()));
							if (positionId>position)
							{
								decrementedId.append("answer");
								decrementedId.append(positionId-1);
							}
							else
							{
								decrementedId.append(onlyIdAnswer);
							}
						}
						catch (NumberFormatException e)
						{
							decrementedId.append(onlyIdAnswer);
						}
					}
					else
					{
						decrementedId.append(onlyIdAnswer);
					}
					decrementedAnswer.append(decrementedId);
					String answerSelector=getAnswerSelector(andAnswer);
					if (answerSelector!=null)
					{
						decrementedAnswer.append(SELECTOR_OPEN);
						decrementedAnswer.append(answerSelector);
						decrementedAnswer.append(SELECTOR_CLOSE);
					}
				}
			}
		}
		return decrementedAnswer==null?null:decrementedAnswer.toString();
	}
	
	/**
	 * Split a string by a separator character but without splitting selectors, even if they contain
	 * separator characters.<br/><br/>
	 * It also takes care of escape sequences.
	 * @param s String
	 * @param separator Separator character
	 * @param escapeSequences Map with the escape sequences
	 * @return List of splitted strings
	 */
	public static List<String> splitButNotInsideSelectors(String s,char separator,
			Map<Character,String> escapeSequences)
	{
		List<String> splitted=new ArrayList<String>();
		StringBuffer token=new StringBuffer();
		boolean selectorOpened=false;
		boolean selectorClosed=false;
		for (int i=0;i<s.length();i++)
		{
			char c=s.charAt(i);
			if (c==SELECTOR_OPEN)
			{
				if (!selectorClosed)
				{
					selectorOpened=true;
				}
				token.append(SELECTOR_OPEN);
			}
			else if (c==SELECTOR_CLOSE)
			{
				if (selectorOpened)
				{
					selectorClosed=true;
				}
				selectorOpened=false;
				token.append(SELECTOR_CLOSE);
			}
			else if (c==separator && !endWithSequenceEscapeCharacter(s.substring(0,i),escapeSequences))
			{
				if (!selectorOpened)
				{
					splitted.add(token.toString());
					token=new StringBuffer();
				}
				else
				{
					token.append(c);
				}
			}
			else
			{
				token.append(c);
			}
		}
		splitted.add(token.toString());
		return splitted;
	}
	
	/**
	 * Checks if the last character of the string is the escape sequence character, taking account other
	 * possible sequence escapes.
	 * @param s String
	 * @param escapeSequences Map with the escape sequences
	 * @return true if the last character of the string is the escape sequence character, false otherwise
	 */
	public static boolean endWithSequenceEscapeCharacter(String s,Map<Character,String> escapeSequences)
	{
		boolean ok=false;
		int i=0;
		while (i<s.length())
		{
			char c=s.charAt(i);
			if (c==SEQUENCE_ESCAPE_CHARACTER)
			{
				if (i+1<s.length())
				{
					Character c2=new Character(s.charAt(i+1));
					if (escapeSequences.containsKey(c2))
					{
						i++;
					}
				}
				else
				{
					ok=true;
				}
			}
			i++;
		}
		return ok;
	}
	
	/**
	 * @param idAnswer Identifier of component selected for answer (including answer selector if exists)
	 * @return Same identifier but without selector 
	 */
	public static String getOnlyId(String idAnswer)
	{
		String onlyId=idAnswer;
		if (getAnswerSelector(idAnswer)!=null)
		{
			Map<Character,String> escapeSequences=getEscapeSequences();
			int iStartSelector=indexOfCharacter(idAnswer,SELECTOR_OPEN,escapeSequences);
			if (iStartSelector!=-1)
			{
				onlyId=idAnswer.substring(0,iStartSelector);
			}
		}
		if (onlyId.length()>0 && onlyId.charAt(0)==ANSWER_NOT_SYMBOL)
		{
			if (onlyId.length()>1)
			{
				onlyId=onlyId.substring(1);
			}
			else
			{
				onlyId="";
			}
		}
		return onlyId;
	}
	
	/**
	 * Gets attribute's selector if defined, otherwise return null.<br/><br/>
	 * Note that there are availabe some escape sequences in the attribute's selector:<br/>
	 * <table border="1">
	 * <tr><th>Escape sequence</th><th>Replaced character</th></tr>
	 * <tr><td>\,</td><td>,</td></tr>
	 * <tr><td>\?</td><td>?</td></tr>
	 * <tr><td>\:</td><td>:</td></tr>
	 * <tr><td>\[</td><td>[</td></tr>
	 * <tr><td>\]</td><td>]</td></tr>
	 * <tr><td>\n</td><td>New line character</td></tr>
	 * <tr><td>\(</td><td>(</td></tr>
	 * <tr><td>\)</td><td>)</td></tr>
	 * <tr><td>\{</td><td>{</td></tr>
	 * <tr><td>\}</td><td>}</td></tr>
	 * <tr><td>\!</td><td>!</td></tr>
	 * <tr><td>\#</td><td>#</td></tr>
	 * <tr><td>\&</td><td>&</td></tr>
	 * <tr><td>\"</td><td>"</td></tr>
	 * <tr><td>\\</td><td>\</td></tr>
	 * </table>
	 * @return Attribute's selector if defined, null otherwise
	 */
	public static String getAnswerSelector(String idAnswer)
	{
		String answerSelector=null;
		Map<Character,String> escapeSequences=getEscapeSequences();
		if (idAnswer!=null)
		{
			int iStartSelector=indexOfCharacter(idAnswer,SELECTOR_OPEN,escapeSequences);
			if (iStartSelector!=-1 && iStartSelector+1<idAnswer.length())
			{
				iStartSelector++;
				int iEndSelector=
					indexOfCharacter(idAnswer,SELECTOR_CLOSE,iStartSelector,escapeSequences);
				if (iEndSelector!=-1)
				{
					answerSelector=idAnswer.substring(iStartSelector,iEndSelector);
				}
			}
		}
		return answerSelector;
	}
	
	/**
	 * Get the index of a character in the string taking care of escape sequences.
	 * @param s String
	 * @param ch Character to search
	 * @param escapeSequences  Map with the escape sequences
	 * @return Index of a character in the string taking care of escape sequences or -1 if it is not found
	 */
	public static int indexOfCharacter(String s,char ch,Map<Character,String> escapeSequences)
	{
		return indexOfCharacter(s,ch,0,escapeSequences);
	}
	
	/**
	 * Get the index of a character in the string from the indicated index taking care of escape sequences.
	 * @param s String
	 * @param ch Character to search
	 * @param fromIndex Index of character in the string from which we start the search
	 * @param escapeSequences  Map with the escape sequences
	 * @return Index of a character in the string from the indicated index taking care of escape sequences
	 * or -1 if it is not found
	 */
	public static int indexOfCharacter(String s,char ch,int fromIndex,Map<Character,String> escapeSequences)
	{
		int index=-1;
		if (fromIndex<s.length())
		{
			boolean searchIndex=escapeSequences.containsKey(new Character(ch));
			index=s.indexOf(ch,fromIndex);
			while (index!=-1  && searchIndex)
			{
				if (index>0 && endWithSequenceEscapeCharacter(s.substring(0,index),escapeSequences))
				{
					if (index+1<s.length())
					{
						index=s.indexOf(ch,index+1);
					}
					else
					{
						index=-1;
					}
				}
				else
				{
					searchIndex=false;
				}
			}
		}
		return index;
	}
	
	/**
	 * Get a map with the following escape sequences:<br/>
	 * <table border="1">
	 * <tr><th>Escape sequence</th><th>Replaced character</th></tr>
	 * <tr><td>\,</td><td>,</td></tr>
	 * <tr><td>\.</td><td>.</td></tr>
	 * <tr><td>\?</td><td>?</td></tr>
	 * <tr><td>\:</td><td>:</td></tr>
	 * <tr><td>\[</td><td>[</td></tr>
	 * <tr><td>\]</td><td>]</td></tr>
	 * <tr><td>\n</td><td>New line character</td></tr>
	 * <tr><td>\(</td><td>(</td></tr>
	 * <tr><td>\)</td><td>)</td></tr>
	 * <tr><td>\{</td><td>{</td></tr>
	 * <tr><td>\}</td><td>}</td></tr>
	 * <tr><td>\!</td><td>!</td></tr>
	 * <tr><td>\#</td><td>#</td></tr>
	 * <tr><td>\&amp;</td><td>&amp;</td></tr>
	 * <tr><td>\"</td><td>"</td></tr>
	 * <tr><td>\\</td><td>\</td></tr>
	 * </table>
	 * @return Map with escape sequences
	 */
	public static Map<Character,String> getEscapeSequences()
	{
		Map<Character,String> escapeSequences=new HashMap<Character, String>();
		escapeSequences.put(new Character(','),",");
		escapeSequences.put(new Character('.'),".");
		escapeSequences.put(new Character('?'),"?");
		escapeSequences.put(new Character(':'),":");
		escapeSequences.put(new Character('['),"[");
		escapeSequences.put(new Character(']'),"]");
		escapeSequences.put(new Character('n'),"\n");
		escapeSequences.put(new Character('('),"(");
		escapeSequences.put(new Character(')'),")");
		escapeSequences.put(new Character('{'),"{");
		escapeSequences.put(new Character('}'),"}");
		escapeSequences.put(new Character('!'),"!");
		escapeSequences.put(new Character('#'),"#");
		escapeSequences.put(new Character('&'),"&");
		escapeSequences.put(new Character('\"'),"\"");
		escapeSequences.put(new Character('\\'),"\\");
		return escapeSequences;
	}
	
	// Comprueba si una pregunta contiene ficheros de recursos y los copia a la carpeta indicada
	/**
	 * Copy all resources needed by a question to the folder that corresponds to the indicated path and 
	 * package's name of the question.
	 * @param question Question
	 * @param resourcesPath Resource's path
	 * @param path Root destination's path
	 * @throws Exception 
	 */
	public static void copyResources(Question question,String resourcesPath,String path) throws Exception
	{
		List<Long> resourcesCopied=new ArrayList<Long>();
		String packageName=question.getPackage();
		
		// First we copy resource to be presented at question's statement if it exists
		Resource resource=question.getResource();
		if (resource!=null)
		{
			QuestionGenerator.createFullPathDirectory(packageName,path);
			String resourceName=resource.getFileName();
			resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
			copyResource(resourceName,resourcesPath,packageName,path);
			resourcesCopied.add(new Long(resource.getId()));
		}
		
		// Next we copy answers resources if they exist and still we have not copied them to destination
		for (Answer answer:question.getAnswers())
		{
			resource=answer.getResource();
			if (resource!=null && !resourcesCopied.contains(new Long(resource.getId())))
			{
				QuestionGenerator.createFullPathDirectory(packageName,path);
				String resourceName=resource.getFileName();
				resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
				copyResource(resourceName,resourcesPath,packageName,path);
				resourcesCopied.add(new Long(resource.getId()));
			}
		}
		
		// Next we copy additional resources if thay exist and still we have not copied them to destination
		for (QuestionResource questionResource:question.getQuestionResources())
		{
			resource=questionResource.getResource();
			if (resource!=null && !resourcesCopied.contains(new Long(resource.getId())))
			{
				QuestionGenerator.createFullPathDirectory(packageName,path);
				String resourceName=resource.getFileName();
				resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
				copyResource(resourceName,resourcesPath,packageName,path);
				resourcesCopied.add(new Long(resource.getId()));
			}
		}
		
		// Next we copy feedbacks resources if they exist and still we have not copied them to destination
		for (Feedback feedback:question.getFeedbacks())
		{
			resource=feedback.getResource();
			if (resource!=null && !resourcesCopied.contains(new Long(resource.getId())))
			{
				QuestionGenerator.createFullPathDirectory(packageName,path);
				String resourceName=resource.getFileName();
				resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
				copyResource(resourceName,resourcesPath,packageName,path);
				resourcesCopied.add(new Long(resource.getId()));
			}
		}
		
		// Next we copy resource to be presented as correct feedback if it exists
		Resource correctFeedbackResource=question.getCorrectFeedbackResource();
		if (correctFeedbackResource!=null && !resourcesCopied.contains(new Long(correctFeedbackResource.getId())))
		{
			QuestionGenerator.createFullPathDirectory(packageName,path);
			String resourceName=correctFeedbackResource.getFileName();
			resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
			copyResource(resourceName,resourcesPath,packageName,path);
			resourcesCopied.add(new Long(correctFeedbackResource.getId()));
		}
		
		// Next we copy resource to be presented as incorrect feedback if it exists
		Resource incorrectFeedbackResource=question.getIncorrectFeedbackResource();
		if (incorrectFeedbackResource!=null && !resourcesCopied.contains(new Long(incorrectFeedbackResource.getId())))
		{
			QuestionGenerator.createFullPathDirectory(packageName,path);
			String resourceName=incorrectFeedbackResource.getFileName();
			resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
			copyResource(resourceName,resourcesPath,packageName,path);
			resourcesCopied.add(new Long(incorrectFeedbackResource.getId()));
		}
		
		// Next we copy resource to be presented as pass feedback if it exists
		Resource passFeedbackResource=question.getPassFeedbackResource();
		if (passFeedbackResource!=null && !resourcesCopied.contains(new Long(passFeedbackResource.getId())))
		{
			QuestionGenerator.createFullPathDirectory(packageName,path);
			String resourceName=passFeedbackResource.getFileName();
			resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
			copyResource(resourceName,resourcesPath,packageName,path);
			resourcesCopied.add(new Long(passFeedbackResource.getId()));
		}
		
		// Next we copy resource to be presented as final feedback if it exists
		Resource finalFeedbackResource=question.getFinalFeedbackResource();
		if (finalFeedbackResource!=null && !resourcesCopied.contains(new Long(finalFeedbackResource.getId())))
		{
			QuestionGenerator.createFullPathDirectory(packageName,path);
			String resourceName=finalFeedbackResource.getFileName();
			resourceName=resourceName.substring(resourceName.lastIndexOf('/')+1);
			copyResource(resourceName,resourcesPath,packageName,path);
			resourcesCopied.add(new Long(finalFeedbackResource.getId()));
		}
	}
	
	/**
	 * Copy a resource from resources folder to the folder that corresponds to the indicated path and 
	 * package's name.
	 * @param resourceName Resource's name
	 * @param resourcesPath Resource's path
	 * @param packageName Package's name
	 * @param path Root destination's path
	 * @throws Exception
	 */
	private static void copyResource(String resourceName,String resourcesPath,String packageName,String path)
		throws Exception
	{
		StringBuffer inFilePath=new StringBuffer(resourcesPath);
		inFilePath.append(File.separatorChar);
		inFilePath.append(resourceName);
		StringBuffer outFilePath=new StringBuffer(QuestionGenerator.getFullPath(packageName,path));
		outFilePath.append(File.separatorChar);
		outFilePath.append(resourceName);
		
		File inFile = new File(inFilePath.toString());
		File outFile = new File(outFilePath.toString());

		FileInputStream in = new FileInputStream(inFile);
		FileOutputStream out = new FileOutputStream(outFile);

		byte[] buf = new byte[8192];
		int len;
	 
		while ((len = in.read(buf)) > 0)
		{
			out.write(buf, 0, len);
		}

		in.close();
		out.close();
	}
}