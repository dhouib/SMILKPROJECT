
package fr.ho2s.holmes.ws;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the fr.ho2s.holmes.ws package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Analyze_QNAME = new QName("http://ws.holmes.ho2s.fr/", "analyze");
    private final static QName _AnalyzeResponse_QNAME = new QName("http://ws.holmes.ho2s.fr/", "analyzeResponse");
    private final static QName _HolmesOutput_QNAME = new QName("http://ws.holmes.ho2s.fr/", "holmesOutput");
    private final static QName _Parse_QNAME = new QName("http://ws.holmes.ho2s.fr/", "parse");
    private final static QName _ParseResponse_QNAME = new QName("http://ws.holmes.ho2s.fr/", "parseResponse");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: fr.ho2s.holmes.ws
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link SemanticGraph }
     * 
     */
    public SemanticGraph createSemanticGraph() {
        return new SemanticGraph();
    }

    /**
     * Create an instance of {@link Sentence }
     * 
     */
    public Sentence createSentence() {
        return new Sentence();
    }

    /**
     * Create an instance of {@link HolmesOutput }
     * 
     */
    public HolmesOutput createHolmesOutput() {
        return new HolmesOutput();
    }

    /**
     * Create an instance of {@link Analyze }
     * 
     */
    public Analyze createAnalyze() {
        return new Analyze();
    }

    /**
     * Create an instance of {@link AnalyzeResponse }
     * 
     */
    public AnalyzeResponse createAnalyzeResponse() {
        return new AnalyzeResponse();
    }

    /**
     * Create an instance of {@link Parse }
     * 
     */
    public Parse createParse() {
        return new Parse();
    }

    /**
     * Create an instance of {@link ParseResponse }
     * 
     */
    public ParseResponse createParseResponse() {
        return new ParseResponse();
    }

    /**
     * Create an instance of {@link Token }
     * 
     */
    public Token createToken() {
        return new Token();
    }

    /**
     * Create an instance of {@link Features }
     * 
     */
    public Features createFeatures() {
        return new Features();
    }

    /**
     * Create an instance of {@link Feature }
     * 
     */
    public Feature createFeature() {
        return new Feature();
    }

    /**
     * Create an instance of {@link BasicNode }
     * 
     */
    public BasicNode createBasicNode() {
        return new BasicNode();
    }

    /**
     * Create an instance of {@link Edge }
     * 
     */
    public Edge createEdge() {
        return new Edge();
    }

    /**
     * Create an instance of {@link SemanticGraph.Nodes }
     * 
     */
    public SemanticGraph.Nodes createSemanticGraphNodes() {
        return new SemanticGraph.Nodes();
    }

    /**
     * Create an instance of {@link SemanticGraph.Edges }
     * 
     */
    public SemanticGraph.Edges createSemanticGraphEdges() {
        return new SemanticGraph.Edges();
    }

    /**
     * Create an instance of {@link Sentence.Tokens }
     * 
     */
    public Sentence.Tokens createSentenceTokens() {
        return new Sentence.Tokens();
    }

    /**
     * Create an instance of {@link Sentence.Semgraphs }
     * 
     */
    public Sentence.Semgraphs createSentenceSemgraphs() {
        return new Sentence.Semgraphs();
    }

    /**
     * Create an instance of {@link HolmesOutput.Sentences }
     * 
     */
    public HolmesOutput.Sentences createHolmesOutputSentences() {
        return new HolmesOutput.Sentences();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Analyze }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.holmes.ho2s.fr/", name = "analyze")
    public JAXBElement<Analyze> createAnalyze(Analyze value) {
        return new JAXBElement<Analyze>(_Analyze_QNAME, Analyze.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link AnalyzeResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.holmes.ho2s.fr/", name = "analyzeResponse")
    public JAXBElement<AnalyzeResponse> createAnalyzeResponse(AnalyzeResponse value) {
        return new JAXBElement<AnalyzeResponse>(_AnalyzeResponse_QNAME, AnalyzeResponse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link HolmesOutput }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.holmes.ho2s.fr/", name = "holmesOutput")
    public JAXBElement<HolmesOutput> createHolmesOutput(HolmesOutput value) {
        return new JAXBElement<HolmesOutput>(_HolmesOutput_QNAME, HolmesOutput.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Parse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.holmes.ho2s.fr/", name = "parse")
    public JAXBElement<Parse> createParse(Parse value) {
        return new JAXBElement<Parse>(_Parse_QNAME, Parse.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ParseResponse }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://ws.holmes.ho2s.fr/", name = "parseResponse")
    public JAXBElement<ParseResponse> createParseResponse(ParseResponse value) {
        return new JAXBElement<ParseResponse>(_ParseResponse_QNAME, ParseResponse.class, null, value);
    }

}
