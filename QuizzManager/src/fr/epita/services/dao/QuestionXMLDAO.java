package fr.epita.services.dao;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.xml.sax.SAXException;

import fr.epita.datamodel.Question;

public class QuestionXMLDAO {

	private static final String XML_FILENAME = "questions.xml";


	public List<Question> getAllQuestions() throws SAXException, IOException, ParserConfigurationException {
		Document doc = parseFile();

		List<Question> listQuestions = new ArrayList<>();
		NodeList list = doc.getElementsByTagName("question");
		for (int i = 0; i < list.getLength(); i++) {
			Question question = new Question();
			Element questionXML = (Element) list.item(i);
			int id = Integer.valueOf(questionXML.getAttribute("id")); // getting id attribute from question
																			// element
			String label = questionXML.getElementsByTagName("label").item(0).getTextContent(); // getting label element
																								// (then text content)
																								// from question element
			int difficulty = Integer.valueOf(questionXML.getElementsByTagName("difficulty").item(0).getTextContent()); // getting
																														// question
			question.setId(id);																											// element
			question.setQuestion(label);
			question.setDifficulty(difficulty);

			String[] topics = getAllTopicsFromQuestion(questionXML);
			question.setTopics(topics);

			listQuestions.add(question);
		}

		return listQuestions;
	}

	
	public List<String> getAllQuestionLabels() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
		List<String> labelsToReturn = new ArrayList<>();
		
		Document doc = parseFile();
		
		//init xpath parser
		XPathFactory xpathFactory = XPathFactory.newInstance();
		XPath xpath = xpathFactory.newXPath();
		
		//create expression to get labels
		XPathExpression expression = xpath.compile("//label");
		NodeList listElements = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);
		//loop through elements to get labels
		for (int i=0; i<listElements.getLength(); i++) {
			labelsToReturn.add(listElements.item(i).getTextContent());
		}
			
		return labelsToReturn;
	}
	
	private String[] getAllTopicsFromQuestion(Element question) {
		// get element of tag name "topics"
		Element topicsElement = (Element) question.getElementsByTagName("topics").item(0);

		// extract all elements of tag name "topic" within the last
		NodeList topicsList = topicsElement.getElementsByTagName("topic");

		// init array size with lenght of list of topics
		String[] result = new String[topicsList.getLength()];

		// loop on each topic
		for (int i = 0; i < topicsList.getLength(); i++) {
			Element topic = (Element) topicsList.item(i);
			result[i] = topic.getTextContent(); // add topic text content to array
		}
		return result;
	}

	public void create(Question question) throws ParserConfigurationException, SAXException, IOException,
			TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		// TODO parse xml file into Document obj
		Document doc = parseFile();

		Element newQuestion = doc.createElement("question");
		// newQuestion.setAttribute("id", String.valueOf(question.getId()));

		Element label = doc.createElement("label");
		label.setTextContent(question.getQuestion());
		newQuestion.appendChild(label);

		Element difficulty = doc.createElement("difficulty");
		difficulty.setTextContent(String.valueOf(question.getDifficulty()));
		newQuestion.appendChild(difficulty);

		Element topics = doc.createElement("topics"); // creation of a single "topics" element
		// which will contain all the "topic" elements
		for (String topic : question.getTopics()) { // for each topic in the java object
			Element xmlTopic = doc.createElement("topic"); // creation of an xml element "topic"
			xmlTopic.setTextContent(topic); // setting text content
			topics.appendChild(xmlTopic); // .. and append to the "topics" element
		}
		newQuestion.appendChild(topics);
		doc.getDocumentElement().appendChild(newQuestion);

		transformXMLFile(doc);
	}

	private Document parseFile() throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document doc = builder.parse(new File(XML_FILENAME));
		return doc;
	}

	private void transformXMLFile(Document doc)
			throws TransformerFactoryConfigurationError, TransformerConfigurationException, TransformerException {

		TransformerFactory fact = TransformerFactory.newInstance();
		Transformer transformer = fact.newTransformer();
		transformer.transform(new DOMSource(doc), new StreamResult(XML_FILENAME));
	}

	public void update(Question question) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		//TODO parse file
		Document doc = parseFile();
		
		//TODO get question element in xml file through question id
		NodeList listQuestions = doc.getElementsByTagName("question");
		for (int i=0; i<listQuestions.getLength(); i++) {
			Element questionXML = (Element) listQuestions.item(i);
			int idXML = Integer.valueOf(questionXML.getAttribute("id")); // !! Assuming id is in the attributes
			if (question.getId() == idXML) { //TODO add id to the question object
				//label modification
				Element label = (Element) questionXML.getElementsByTagName("label").item(0);
				label.setTextContent(question.getQuestion());
				
				//difficulty modification
				Element difficulty = (Element) questionXML.getElementsByTagName("difficulty").item(0);
				difficulty.setTextContent(String.valueOf(question.getDifficulty()));
				
				//topics modification
				Element topics = (Element) questionXML.getElementsByTagName("topics").item(0);
				NodeList topicList = topics.getElementsByTagName("topic");
				
				//loop on the old topic list of the xml element to delete them
				for (int j=0; j<topicList.getLength(); j++) {
					topics.removeChild(topicList.item(j));
				}
				
				//loop on the new topic list of the java object to add them to the xml
				for (String topic : question.getTopics()) {
					Element xmlTopic = doc.createElement("topic"); // creation of an xml element "topic"
					xmlTopic.setTextContent(topic);    //setting text content 
					topics.appendChild(xmlTopic);      // .. and append to the "topics" element					
				}
			}
			
		}
		
		transformXMLFile(doc);
		
	
	}
	
	public void delete(Question question) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
		//TODO parse file
		Document doc = parseFile();
		//TODO get question to delete with id
		NodeList listQuestions = doc.getElementsByTagName("question");
		for (int i=0; i<listQuestions.getLength(); i++) {
			Element questionXML = (Element) listQuestions.item(i);
			int idXML = Integer.valueOf(questionXML.getAttribute("id")); // !! Assuming id is in the attributes
			if (question.getId() == idXML) { //TODO add id to the question object
				doc.getDocumentElement().removeChild(questionXML);
			}
			
		}
		
		transformXMLFile(doc);
	}

}
