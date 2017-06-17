
package verification.biometric.nadra;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the verification.biometric.nadra package. 
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

    private final static QName _GetLastVerificationResultsResponseGetLastVerificationResultsResult_QNAME = new QName("http://NADRA.Biometric.Verification", "GetLastVerificationResultsResult");
    private final static QName _SubmitBankAccountDetailsFranchizeID_QNAME = new QName("http://NADRA.Biometric.Verification", "franchizeID");
    private final static QName _SubmitBankAccountDetailsXmlRequestData_QNAME = new QName("http://NADRA.Biometric.Verification", "xml_request_data");
    private final static QName _SubmitBankAccountDetailsResponseSubmitBankAccountDetailsResult_QNAME = new QName("http://NADRA.Biometric.Verification", "SubmitBankAccountDetailsResult");
    private final static QName _TestServiceResponseTestServiceResult_QNAME = new QName("http://NADRA.Biometric.Verification", "TestServiceResult");
    private final static QName _VerifyFingerPrintsResponseVerifyFingerPrintsResult_QNAME = new QName("http://NADRA.Biometric.Verification", "VerifyFingerPrintsResult");
    private final static QName _TestServiceTempType_QNAME = new QName("http://NADRA.Biometric.Verification", "tempType");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: verification.biometric.nadra
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetLastVerificationResults }
     * 
     */
    public GetLastVerificationResults createGetLastVerificationResults() {
        return new GetLastVerificationResults();
    }

    /**
     * Create an instance of {@link VerifyFingerPrintsResponse }
     * 
     */
    public VerifyFingerPrintsResponse createVerifyFingerPrintsResponse() {
        return new VerifyFingerPrintsResponse();
    }

    /**
     * Create an instance of {@link SubmitBankAccountDetails }
     * 
     */
    public SubmitBankAccountDetails createSubmitBankAccountDetails() {
        return new SubmitBankAccountDetails();
    }

    /**
     * Create an instance of {@link TestService }
     * 
     */
    public TestService createTestService() {
        return new TestService();
    }

    /**
     * Create an instance of {@link TestServiceResponse }
     * 
     */
    public TestServiceResponse createTestServiceResponse() {
        return new TestServiceResponse();
    }

    /**
     * Create an instance of {@link SubmitBankAccountDetailsResponse }
     * 
     */
    public SubmitBankAccountDetailsResponse createSubmitBankAccountDetailsResponse() {
        return new SubmitBankAccountDetailsResponse();
    }

    /**
     * Create an instance of {@link GetLastVerificationResultsResponse }
     * 
     */
    public GetLastVerificationResultsResponse createGetLastVerificationResultsResponse() {
        return new GetLastVerificationResultsResponse();
    }

    /**
     * Create an instance of {@link VerifyFingerPrints }
     * 
     */
    public VerifyFingerPrints createVerifyFingerPrints() {
        return new VerifyFingerPrints();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "GetLastVerificationResultsResult", scope = GetLastVerificationResultsResponse.class)
    public JAXBElement<String> createGetLastVerificationResultsResponseGetLastVerificationResultsResult(String value) {
        return new JAXBElement<String>(_GetLastVerificationResultsResponseGetLastVerificationResultsResult_QNAME, String.class, GetLastVerificationResultsResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "franchizeID", scope = SubmitBankAccountDetails.class)
    public JAXBElement<String> createSubmitBankAccountDetailsFranchizeID(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsFranchizeID_QNAME, String.class, SubmitBankAccountDetails.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "xml_request_data", scope = SubmitBankAccountDetails.class)
    public JAXBElement<String> createSubmitBankAccountDetailsXmlRequestData(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsXmlRequestData_QNAME, String.class, SubmitBankAccountDetails.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "SubmitBankAccountDetailsResult", scope = SubmitBankAccountDetailsResponse.class)
    public JAXBElement<String> createSubmitBankAccountDetailsResponseSubmitBankAccountDetailsResult(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsResponseSubmitBankAccountDetailsResult_QNAME, String.class, SubmitBankAccountDetailsResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "franchizeID", scope = GetLastVerificationResults.class)
    public JAXBElement<String> createGetLastVerificationResultsFranchizeID(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsFranchizeID_QNAME, String.class, GetLastVerificationResults.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "xml_request_data", scope = GetLastVerificationResults.class)
    public JAXBElement<String> createGetLastVerificationResultsXmlRequestData(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsXmlRequestData_QNAME, String.class, GetLastVerificationResults.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "TestServiceResult", scope = TestServiceResponse.class)
    public JAXBElement<String> createTestServiceResponseTestServiceResult(String value) {
        return new JAXBElement<String>(_TestServiceResponseTestServiceResult_QNAME, String.class, TestServiceResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "franchizeID", scope = VerifyFingerPrints.class)
    public JAXBElement<String> createVerifyFingerPrintsFranchizeID(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsFranchizeID_QNAME, String.class, VerifyFingerPrints.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "xml_request_data", scope = VerifyFingerPrints.class)
    public JAXBElement<String> createVerifyFingerPrintsXmlRequestData(String value) {
        return new JAXBElement<String>(_SubmitBankAccountDetailsXmlRequestData_QNAME, String.class, VerifyFingerPrints.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "VerifyFingerPrintsResult", scope = VerifyFingerPrintsResponse.class)
    public JAXBElement<String> createVerifyFingerPrintsResponseVerifyFingerPrintsResult(String value) {
        return new JAXBElement<String>(_VerifyFingerPrintsResponseVerifyFingerPrintsResult_QNAME, String.class, VerifyFingerPrintsResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://NADRA.Biometric.Verification", name = "tempType", scope = TestService.class)
    public JAXBElement<String> createTestServiceTempType(String value) {
        return new JAXBElement<String>(_TestServiceTempType_QNAME, String.class, TestService.class, value);
    }

}
