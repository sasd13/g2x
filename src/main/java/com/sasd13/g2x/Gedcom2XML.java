package com.sasd13.g2x;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

public class Gedcom2XML {

	private static final List<String> TAGS = Arrays.asList("INDI", "FAM", "NAME", "SEX", "FAMC", "FAMS", "BIRT", "CHR", "DEAT", "BURI", "HUSB", "WIFE", "CHIL", "MARR", "DIV", "DATE", "PLAC", "OBJE", "FORM", "TITL", "FILE");
	private static final List<String> TAGS_INDI = Arrays.asList("HUSB", "WIFE", "CHIL");
	private static final List<String> TAGS_FAM = Arrays.asList("FAMC", "FAMS");
	private static final List<String> TAGS_EVENT = Arrays.asList("BIRT", "CHR", "DEAT", "BURI", "MARR", "DIV");

	public static void main(String[] args) {
		String path = args[0];
		File file = new File(path);

		if (!file.exists()) {
			System.out.println("Gedcom file not exists");
		} else {
			Document document = buildXMLDocument(readGedcomFile(file));

			writeXMLFile(new File(file.getName() + ".xml"), document);
		}
	}

	private static LinkedList<String> readGedcomFile(File file) {
		LinkedList<String> lines = new LinkedList<String>();

		FileReader fr = null;
		BufferedReader br = null;

		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);
			String line = null;

			while ((line = br.readLine()) != null) {
				lines.addLast(line);
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
		} finally {
			try {
				if (br != null) {
					br.close();
				}

				if (fr != null) {
					fr.close();
				}
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}

		return lines;
	}

	private static Document buildXMLDocument(LinkedList<String> lines) {
		Document document = getDocumentInstance();

		Stack<Element> stack = new Stack<Element>();
		StringTokenizer tokenizer = null;
		int index = -1, lastIndex = -1;
		String type = null, value = null;

		stack.push(document.getRootElement());

		while (!lines.isEmpty()) {
			tokenizer = new StringTokenizer(lines.removeFirst());

			if (tokenizer.countTokens() <= 0) {
				break;
			}

			type = tokenizer.nextToken();
			index = Integer.parseInt(tokenizer.nextToken());
			value = tokenizer.hasMoreTokens() ? tokenizer.nextToken() : "";

			if (type.contains("TRLR")) {
				break;
			} else if (TAGS.contains(type) || (type.startsWith("@") && TAGS.contains(value))) {
				cleanStack(stack, index, lastIndex);
				pushElement(stack, type, getValue(tokenizer, value));

				lastIndex = index;
			} else {
				removeLines(lines, index);
			}
		}

		return document;
	}

	private static Document getDocumentInstance() {
		Document document = new Document(new Element("gedcom"));

		document.setDocType(new DocType("gedcom", "gedcom.dtd"));
		document.getContent().add(0, new ProcessingInstruction("xml-stylesheet", getHeaders()));

		return document;
	}

	private static Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();

		headers.put("type", "text/xsl");
		headers.put("href", "xml2html.xsl");

		return headers;
	}

	private static void cleanStack(Stack<Element> stack, int index, int lastIndex) {
		if (index <= lastIndex) {
			for (int i = 0; i <= lastIndex - index; i++) {
				stack.pop();
			}
		}
	}

	private static String getValue(StringTokenizer tokenizer, String value) {
		StringBuilder builder = new StringBuilder();

		builder.append(value);

		while (tokenizer.hasMoreTokens()) {
			builder.append(" " + tokenizer.nextToken());
		}

		return builder.toString();
	}

	private static void pushElement(Stack<Element> stack, String type, String value) {
		Element element = null;

		if (type.startsWith("@")) {
			element = pushReference(stack, type);
		} else {
			if (type.contains("SEX")) {
				stack.peek().setAttribute("sex", value);
			} else if (TAGS_INDI.contains(type)) {
				element = pushPerson(stack, type, value);
			} else if (TAGS_FAM.contains(type)) {
				element = pushFamily(stack, type, value);
			} else if (TAGS_EVENT.contains(type)) {
				element = pushEvent(stack, type, value);
			} else {
				element = pushOther(stack, type, value);
			}
		}

		stack.push(element);
	}

	private static Element pushReference(Stack<Element> stack, String type) {
		Element element = new Element(type.toLowerCase());

		element.setAttribute("id", "id-" + type.substring(1, type.lastIndexOf("@")));
		stack.peek().addContent(element);

		return element;
	}

	private static Element pushPerson(Stack<Element> stack, String type, String value) {
		Element element = new Element("indix");

		element.setAttribute("ref", "id-" + value.substring(1, value.lastIndexOf("@")));
		element.setAttribute("type", type);
		stack.peek().addContent(element);

		return element;
	}

	private static Element pushFamily(Stack<Element> stack, String type, String value) {
		Element element = new Element("famx");

		element.setAttribute("ref", "id-" + value.substring(1, value.lastIndexOf("@")));
		element.setAttribute("type", type);
		stack.peek().addContent(element);

		return element;
	}

	private static Element pushEvent(Stack<Element> stack, String type, String value) {
		Element element = new Element("event");

		element.setAttribute("type", type);
		stack.peek().addContent(element);

		if (!value.isEmpty()) {
			element.setText(value);
		}

		return element;
	}

	private static Element pushOther(Stack<Element> stack, String type, String value) {
		Element element = new Element(type.toLowerCase());

		if (value.startsWith("@")) {
			element.setName(type.toLowerCase() + "x");
			element.setAttribute("ref", "id-" + value.substring(1, value.lastIndexOf("@")));
		} else if (!value.isEmpty()) {
			element.setText(value);
		}

		stack.peek().addContent(element);

		return element;
	}

	private static void removeLines(LinkedList<String> lines, int index) {
		StringTokenizer tokenizer;

		do {
			tokenizer = new StringTokenizer(lines.getFirst());

			if (tokenizer.countTokens() > 0 && Integer.parseInt(tokenizer.nextToken()) <= index) {
				break;
			}

			lines.removeFirst();
		} while (!lines.isEmpty());
	}

	private static void writeXMLFile(File xmlFile, Document document) {
		XMLOutputter xop = new XMLOutputter(Format.getPrettyFormat());
		FileOutputStream fos = null;

		try {
			System.out.println("Writing XML file...");

			xop.output(document, new FileOutputStream(xmlFile));

			System.out.println("XML generated !");
		} catch (IOException e) {
			System.out.println("XML not generated");
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					System.out.println(e.getMessage());
				}
			}
		}
	}
}