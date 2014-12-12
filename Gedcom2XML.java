package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.StringTokenizer;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Gedcom2XML {
	
	private static Element racine = new Element("gedcom");
	private static Document document = new Document(racine);

	public static void main(String[] args) {
		File gedcomFile = new File(args[0]);
		if(gedcomFile.exists()) {
			String file = args[0].substring(0, args[0].lastIndexOf("."));
			File xmlFile = new File(file+".xml");
			convert(gedcomFile, xmlFile);
		}
		else {
			System.out.println("Gedcom file does not exist");
		}
	}
	
	public static void convert(File gedcomFile, File xmlFile) {
		//Lecture du fichier Gedcom
		LinkedList<String> listLines = readGedcom(gedcomFile);
		
		//Si lecture OK
		if (listLines != null) {
			
			//Ajout Doctype
			document.setDocType(new DocType("gedcom", "gedcom.dtd"));
			
			//Ajout Stylesheet
			HashMap<String, String> piMap = new HashMap<String, String>(2);
			piMap.put("type", "text/xsl");
			piMap.put("href", "xml2html.xsl");		
			ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet", piMap);
			document.getContent().add(0, pi);
			
			//Construction de l'arbre XML
			makeTree(listLines);
			
			//Ecriture du fichier XML
			boolean writed = writeXML(xmlFile);
			
			//Si ecriture OK
			if(writed) {
				System.out.println("Converted!");
			}
			else {
				System.out.println("Not converted!");
			}
		}
		else {
			System.out.println("Gedcom file not readed");
		}
	}
	
	private static LinkedList<String> readGedcom(File gedcomFile) {
		LinkedList<String> listLines = null;
		
		try {
			FileReader fr = new FileReader(gedcomFile);
			BufferedReader br = new BufferedReader(fr);
			
			String line = null;
			listLines = new LinkedList<String>();
			while ((line = br.readLine()) != null) {
				listLines.addLast(line);
			}
			
			br.close();
			fr.close();
		}
		catch (IOException e) { System.out.println(e.getMessage()); }
		
		return listLines;
	}
	
	private static boolean writeXML(File xmlFile) {
		boolean converted = false;
		
		try {
			XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
		    xop.output(document, new FileOutputStream(xmlFile));
		    converted = true;
		}
		catch (IOException e) { System.out.println(e.getMessage()); }
		 
		return converted;
	}

	private static void makeTree(LinkedList<String> listLines) {
		StringTokenizer read = null;
		
		Element element = null;
		Stack<Element> stackElements = new Stack<Element>();
		stackElements.push(racine);
		
		List<String> tags = Arrays.asList(
				"INDI", "FAM",
				"NAME", "SEX", "FAMC", "FAMS", "BIRT", "CHR", "DEAT", "BURI",
				"HUSB", "WIFE", "CHIL", "MARR", "DIV",
				"DATE", "PLAC",
				"OBJE", "FORM", "TITL", "FILE"
				);
		List<String> tagsIndi = Arrays.asList("HUSB", "WIFE", "CHIL");
		List<String> tagsFam = Arrays.asList("FAMC", "FAMS");
		List<String> tagsEvent = Arrays.asList("BIRT", "CHR", "DEAT", "BURI", "MARR", "DIV");
		
		int lastIndex = -1, index = -1, buffer = -1;
		String type = null, value = null, ident = null;
		
		while(!listLines.isEmpty()) {
			read = new StringTokenizer(listLines.removeFirst());
			
			if (read.countTokens() > 0) {
				index = Integer.parseInt(read.nextToken());
				type = read.nextToken();
				value = "";
				if (read.hasMoreTokens()) {
					value = read.nextToken();
				}
				
				//Si Fin fichier Gedcom
				if (type.contains("TRLR")) {
					break;
				}
				
				//Contenus retenus
				else if(tags.contains(type) || (type.startsWith("@") && tags.contains(value))) {
					
					//Construction de la chaine value
					while (read.hasMoreTokens()) {
						value += " "+read.nextToken();
					}
					
					//Depile les elements a fermer, parent en tete de pile
					if(index <= lastIndex) {
						for(int i=0; i<=lastIndex-index; i++) {
							stackElements.pop();
						}					
					}
					
					if(type.startsWith("@")) {
						ident = type.substring(1, type.lastIndexOf("@"));
						type = (new StringTokenizer(value)).nextToken();
						element = new Element(type.toLowerCase());
						element.setAttribute("id", "id-"+ident);
						stackElements.peek().addContent(element);
					}
					else {
						if(type.contains("SEX")) {
							stackElements.peek().setAttribute("sex", value);
						}
						else if (tagsIndi.contains(type)) {
							element = new Element("indix");
							ident = value.substring(1, value.lastIndexOf("@"));
							element.setAttribute("ref", "id-"+ident);
							element.setAttribute("type", type);
							stackElements.peek().addContent(element);
						}
						else if (tagsFam.contains(type)) {
							element = new Element("famx");
							ident = value.substring(1, value.lastIndexOf("@"));
							element.setAttribute("ref", "id-"+ident);
							element.setAttribute("type", type);
							stackElements.peek().addContent(element);
						}
						else if (tagsEvent.contains(type)) {
							element = new Element("event");
							element.setAttribute("type", type);
							stackElements.peek().addContent(element);
							if(!value.isEmpty()) {
								element.setText(value);
							}
						}
						else {
							element = new Element(type.toLowerCase());
							if(value.startsWith("@")) {
								ident = value.substring(1, value.lastIndexOf("@"));
								element.setName(type.toLowerCase()+"x");
								element.setAttribute("ref", "id-"+ident);
							}
							else if(!value.isEmpty()) {
								element.setText(value);
							}
							stackElements.peek().addContent(element);
						}
					}
					lastIndex = index;
					stackElements.push(element);
				}
				
				//Contenus ignores
				else {
					buffer = -1;
					do {
						read = new StringTokenizer(listLines.getFirst());
						if (read.countTokens() > 0) {
							buffer = Integer.parseInt(read.nextToken());
							if (buffer <= index) break;
						}
						listLines.removeFirst();
					}
					while (!listLines.isEmpty());
				}
			}
		}
	}
}