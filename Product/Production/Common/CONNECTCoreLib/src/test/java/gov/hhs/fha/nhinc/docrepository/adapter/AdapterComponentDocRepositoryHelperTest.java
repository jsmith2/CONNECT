package gov.hhs.fha.nhinc.docrepository.adapter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Matchers.any;

import gov.hhs.fha.nhinc.docrepository.adapter.model.Document;
import gov.hhs.fha.nhinc.docrepository.adapter.model.DocumentQueryParams;
import gov.hhs.fha.nhinc.docrepository.adapter.service.DocumentService;
import gov.hhs.fha.nhinc.largefile.LargeFileUtils;
import ihe.iti.xds_b._2007.ProvideAndRegisterDocumentSetRequestType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.activation.DataHandler;

import oasis.names.tc.ebxml_regrep.xsd.rim._3.ClassificationType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.InternationalStringType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.LocalizedStringType;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1;
import oasis.names.tc.ebxml_regrep.xsd.rim._3.ValueListType;

import org.junit.Test;

public class AdapterComponentDocRepositoryHelperTest {
	
	@Test
    public void getDocumentMapTest() throws IOException {
    	AdapterComponentDocRepositoryHelper docRepoHelper = new AdapterComponentDocRepositoryHelper();
    	ProvideAndRegisterDocumentSetRequestType body = 
    			mock(ProvideAndRegisterDocumentSetRequestType.class);
    	List<ProvideAndRegisterDocumentSetRequestType.Document> docList= 
    			new ArrayList<ProvideAndRegisterDocumentSetRequestType.Document>();
    	ProvideAndRegisterDocumentSetRequestType.Document doc = new ProvideAndRegisterDocumentSetRequestType.Document();
    	final String ID = "MOCK_ID";
    	final String VALUE = "MOCK_VALUE";
    	DataHandler dataHandler = LargeFileUtils.getInstance().convertToDataHandler(VALUE);
    	doc.setId(ID);
    	doc.setValue(dataHandler);
    	
    	docList.add(doc);
    	
    	when(body.getDocument()).thenReturn(docList);
    	
    	HashMap<String, DataHandler> docMap = docRepoHelper.getDocumentMap(body);
    	
    	assertEquals(docMap.get(ID), dataHandler);
    }
    
	/**
	 * Test for extractClassifcationMetadata(List<ClassificationType, String, String, int).
	 */
	@Test
	public void extractClassificationMetadataTest(){
		final String CLASS_VALUE = "Classfication value";
		final String CLASS_SCHEME_NAME = "classificationSchemeName";
		AdapterComponentDocRepositoryHelper docRepoHelper = new AdapterComponentDocRepositoryHelper(){
			String extractMetadataFromSlots(List<oasis.names.tc.ebxml_regrep.xsd.rim._3.SlotType1> documentSlots,
		            String slotName, int valueIndex) {
				return CLASS_VALUE;
			}
		};
		List<ClassificationType> classifications = new ArrayList<ClassificationType>();
		ClassificationType classType = mock(ClassificationType.class);
		
		classifications.add(classType);
		
		when(classType.getClassificationScheme()).thenReturn(CLASS_SCHEME_NAME);
		
		String result = docRepoHelper.extractClassificationMetadata(classifications, CLASS_SCHEME_NAME, "slotName", 0);
		assertEquals(result, CLASS_VALUE);	
	}
	
	@Test
	public void extractPatientInfoTest(){
		final String PATIENT_NAME = "John Doe";
		final String SLOT_PATIENT_NAME = "John Doe <Patient Information>";
		final String EXPECTED_RESULT = "<Patient Information>";
		List<SlotType1> documentSlots = new ArrayList<SlotType1>();
		SlotType1 slot = mock(SlotType1.class);
		documentSlots.add(slot);
		ValueListType valueListType = mock(ValueListType.class);
		List<String> valueList = new ArrayList<String>();
		valueList.add(SLOT_PATIENT_NAME);
		
		when(slot.getName()).thenReturn(DocRepoConstants.XDS_SOURCE_PATIENT_INFO_SLOT);
		when(slot.getValueList()).thenReturn(valueListType);
		when(valueListType.getValue()).thenReturn(valueList);
		
		AdapterComponentDocRepositoryHelper docRepoHelper = new AdapterComponentDocRepositoryHelper();
		
		String result = docRepoHelper.extractPatientInfo(documentSlots, PATIENT_NAME);
		
		assertEquals(result, EXPECTED_RESULT);
	}
	
	/**
	 * Test for extractClassificationMetadata(List<ClassificationType>, String, String).
	 */
	@Test
	public void extractClassificationMetadata2Test(){
		AdapterComponentDocRepositoryHelper docRepoHelper = new AdapterComponentDocRepositoryHelper();
		String result = null;
		List<ClassificationType> classifications = new ArrayList();
		ClassificationType classificationType = mock(ClassificationType.class);
		classifications.add(classificationType);
		InternationalStringType internationalString = mock(InternationalStringType.class);
		LocalizedStringType localizedString = new LocalizedStringType();
		final String VALUE = "classification Value";
		localizedString.setValue(VALUE);
		List<LocalizedStringType> localizedList = new ArrayList();
		localizedList.add(localizedString);
		final String CLASSIFICATION_SCHEME = "classificationScheme";
		
		when(classificationType.getClassificationScheme()).thenReturn(CLASSIFICATION_SCHEME);
		when(classificationType.getName()).thenReturn(internationalString);
		when(internationalString.getLocalizedString()).thenReturn(localizedList);
		
		result = docRepoHelper.extractClassificationMetadata(classifications, CLASSIFICATION_SCHEME, DocRepoConstants.XDS_NAME);
		assertEquals(result, VALUE);
		
		when(classificationType.getNodeRepresentation()).thenReturn(VALUE);
		result = docRepoHelper.extractClassificationMetadata(classifications, CLASSIFICATION_SCHEME, DocRepoConstants.XDS_NODE_REPRESENTATION);
		assertEquals(result, VALUE);
		
		when(classificationType.getClassifiedObject()).thenReturn(VALUE);
		result = docRepoHelper.extractClassificationMetadata(classifications, CLASSIFICATION_SCHEME, DocRepoConstants.XDS_CLASSIFIED_OBJECT);
		assertEquals(result, VALUE);
		
		result = docRepoHelper.extractClassificationMetadata(classifications, CLASSIFICATION_SCHEME, DocRepoConstants.XDS_CLASSIFICATION_ID);
		assertEquals(result, VALUE);
	}
	
	@Test
	public void queryRepositoryByPatientIdTest(){
		AdapterComponentDocRepositoryHelper docRepoHelper = new AdapterComponentDocRepositoryHelper();
		final String SPATID = "sPatId";
		final String SDOCID = "Doc ID 1";
		final String SCLASSCODE = "sClassCode";
		final String ESTATUS = "sStatus";
		final String DOC_UNIQUE_ID = "Doc ID 1";
		final long DOC_ID = 12345;
		DocumentService docService = mock(DocumentService.class);
		List<Document> documents = new ArrayList<Document>();
		Document doc = mock(Document.class);
		documents.add(doc);
		
		when(docService.documentQuery(any(DocumentQueryParams.class))).thenReturn(documents);
		when(doc.getDocumentUniqueId()).thenReturn(DOC_UNIQUE_ID);
		when(doc.getDocumentid()).thenReturn(DOC_ID);
		
		long result = docRepoHelper.queryRepositoryByPatientId(SPATID, SDOCID, SCLASSCODE, ESTATUS, docService);
		
		assertEquals(result, DOC_ID);
	}
	
}