/*
 * Copyright (c) 2009-2015, United States Government, as represented by the Secretary of Health and Human Services.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above
 *       copyright notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *     * Neither the name of the United States Government nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE UNITED STATES GOVERNMENT BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package gov.hhs.fha.nhinc.docretrieve.audit.transform;

import com.services.nhinc.schema.auditmessage.AuditMessageType.ActiveParticipant;
import com.services.nhinc.schema.auditmessage.ParticipantObjectIdentificationType;
import gov.hhs.fha.nhinc.audit.AuditTransformsConstants;
import gov.hhs.fha.nhinc.audit.transform.AuditTransforms;
import gov.hhs.fha.nhinc.audit.transform.AuditTransformsTest;
import gov.hhs.fha.nhinc.common.auditlog.LogEventRequestType;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetSystemType;
import gov.hhs.fha.nhinc.connectmgr.ConnectionManagerException;
import gov.hhs.fha.nhinc.docretrieve.audit.DocRetrieveAuditTransformsConstants;
import gov.hhs.fha.nhinc.nhinclib.NhincConstants;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType.DocumentRequest;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetResponseType.DocumentResponse;
import java.lang.management.ManagementFactory;
import java.net.UnknownHostException;
import java.util.Properties;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class is designed to test Retrieve Document Audit Transforms.
 *
 * @author vimehta
 */
public class DocRetrieveAuditTransformsTest
    extends AuditTransformsTest<RetrieveDocumentSetRequestType, RetrieveDocumentSetResponseType> {

    public DocRetrieveAuditTransformsTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Test
    public void transformRequestToAuditMsg() throws ConnectionManagerException, UnknownHostException {
        final String localIp = "10.10.10.10";
        final String remoteIp = "16.14.13.12";
        final String remoteObjectUrl = "http://" + remoteIp + ":9090/source/AuditService";
        final String wsRequestUrl = "http://" + remoteIp + ":9090/AuditService";

        Properties webContextProperties = new Properties();
        webContextProperties.setProperty(NhincConstants.WEB_SERVICE_REQUEST_URL, wsRequestUrl);
        webContextProperties.setProperty(NhincConstants.REMOTE_HOST_ADDRESS, remoteIp);

        DocRetrieveAuditTransforms transforms = new DocRetrieveAuditTransforms() {
            @Override
            protected String getLocalHostAddress() {
                return localIp;
            }

            @Override
            protected String getRemoteHostAddress(Properties webContextProperties) {
                if (webContextProperties != null && !webContextProperties.isEmpty() && webContextProperties
                    .getProperty(NhincConstants.REMOTE_HOST_ADDRESS) != null) {

                    return webContextProperties.getProperty(NhincConstants.REMOTE_HOST_ADDRESS);
                }
                return AuditTransformsConstants.ACTIVE_PARTICIPANT_UNKNOWN_IP_ADDRESS;
            }

            @Override
            protected String getWebServiceUrlFromRemoteObject(NhinTargetSystemType target, String serviceName) {
                return remoteObjectUrl;
            }
        };

        AssertionType assertion = createAssertion();
        LogEventRequestType auditRequest = transforms.transformRequestToAuditMsg(createRetrieveDocumentSetRequest(),
            assertion, createNhinTarget(), NhincConstants.AUDIT_LOG_INBOUND_DIRECTION,
            NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, Boolean.TRUE, webContextProperties,
            NhincConstants.DOC_RETRIEVE_SERVICE_NAME);

        testGetEventIdentificationType(auditRequest, NhincConstants.DOC_RETRIEVE_SERVICE_NAME, Boolean.TRUE);
        testGetActiveParticipantSource(auditRequest, Boolean.FALSE, webContextProperties, remoteObjectUrl);
        testGetActiveParticipantDestination(auditRequest, Boolean.FALSE, webContextProperties, localIp);
        testCreateActiveParticipantFromUser(auditRequest, Boolean.TRUE, assertion);
        assertParticipantObjectIdentification(auditRequest, assertion);
    }

    @Test
    public void transformResponseToAuditMsg() throws ConnectionManagerException, UnknownHostException {
        final String localIp = "10.10.10.10";
        final String remoteIp = "16.14.13.12";
        final String remoteObjectUrl = "http://" + remoteIp + ":9090/source/AuditService";
        final String wsRequestUrl = "http://" + remoteIp + ":9090/AuditService";

        Properties webContextProperties = new Properties();
        webContextProperties.setProperty(NhincConstants.WEB_SERVICE_REQUEST_URL, wsRequestUrl);
        webContextProperties.setProperty(NhincConstants.REMOTE_HOST_ADDRESS, remoteIp);

        DocRetrieveAuditTransforms transforms = new DocRetrieveAuditTransforms() {
            @Override
            protected String getLocalHostAddress() {
                return localIp;
            }

            @Override
            protected String getRemoteHostAddress(Properties webContextProperties) {
                if (webContextProperties != null && !webContextProperties.isEmpty() && webContextProperties
                    .getProperty(NhincConstants.REMOTE_HOST_ADDRESS) != null) {

                    return webContextProperties.getProperty(NhincConstants.REMOTE_HOST_ADDRESS);
                }
                return AuditTransformsConstants.ACTIVE_PARTICIPANT_UNKNOWN_IP_ADDRESS;
            }

            @Override
            protected String getWebServiceUrlFromRemoteObject(NhinTargetSystemType target, String serviceName) {
                return remoteObjectUrl;
            }
        };

        AssertionType assertion = createAssertion();
        LogEventRequestType auditResponse = transforms.transformResponseToAuditMsg(createRetrieveDocumentSetRequest(),
            createRetrieveDocumentSetResponse(), assertion, createNhinTarget(),
            NhincConstants.AUDIT_LOG_INBOUND_DIRECTION, NhincConstants.AUDIT_LOG_ENTITY_INTERFACE, Boolean.TRUE,
            webContextProperties, NhincConstants.DOC_RETRIEVE_SERVICE_NAME);

        testGetEventIdentificationType(auditResponse, NhincConstants.DOC_RETRIEVE_SERVICE_NAME, Boolean.TRUE);
        testGetActiveParticipantSource(auditResponse, Boolean.FALSE, webContextProperties, remoteObjectUrl);
        testGetActiveParticipantDestination(auditResponse, Boolean.FALSE, webContextProperties, localIp);
        testCreateActiveParticipantFromUser(auditResponse, Boolean.TRUE, assertion);
        assertParticipantObjectIdentification(auditResponse, assertion);
    }

    private void assertParticipantObjectIdentification(LogEventRequestType auditRequest, AssertionType assertion) {
        assertNotNull("auditRequest is null", auditRequest);
        assertNotNull("AuditMessage is null", auditRequest.getAuditMessage());
        assertNotNull("ParticipantObjectIdentification is null", auditRequest.getAuditMessage()
            .getParticipantObjectIdentification());
        assertFalse("ParticipantObjectIdentification list is empty", auditRequest.getAuditMessage()
            .getParticipantObjectIdentification().isEmpty());

        ParticipantObjectIdentificationType participantPatientObject = auditRequest.getAuditMessage()
            .getParticipantObjectIdentification().get(0);

        if (assertion.getUniquePatientId() != null && !assertion.getUniquePatientId().isEmpty()
            && assertion.getUniquePatientId().get(0) != null && !assertion.getUniquePatientId().get(0).isEmpty()) {

            assertParticipantPatientObjectIdentification(participantPatientObject);

            // Test for Participant Document Object Identification
            assertFalse("ParticipantObjectIdentification list is missing test values", auditRequest.getAuditMessage()
                .getParticipantObjectIdentification().size() < 2);

            assertParticipantDocumentParticipantObjectIdentification(auditRequest.getAuditMessage()
                .getParticipantObjectIdentification().get(1));
        } else {
            assertParticipantDocumentParticipantObjectIdentification(participantPatientObject);
        }
    }

    private void assertParticipantDocumentParticipantObjectIdentification(
        ParticipantObjectIdentificationType participantObject) {

        assertSame(DocRetrieveAuditTransformsConstants.PARTICIPANT_DOCUMENT_OBJ_TYPE_CODE,
            participantObject.getParticipantObjectTypeCode());
        assertSame(DocRetrieveAuditTransformsConstants.PARTICIPANT_DOCUMENT_OBJ_TYPE_CODE_ROLE,
            participantObject.getParticipantObjectTypeCodeRole());
        assertEquals(DocRetrieveAuditTransformsConstants.PARTICIPANT_DOCUMENT_OBJ_ID_TYPE_CODE,
            participantObject.getParticipantObjectIDTypeCode().getCode());
        assertEquals(DocRetrieveAuditTransformsConstants.PARTICIPANT_DOCUMENT_OBJ_ID_TYPE_CODE_SYSTEM,
            participantObject.getParticipantObjectIDTypeCode().getCodeSystemName());
        assertEquals(DocRetrieveAuditTransformsConstants.PARTICIPANT_DOCUMENT_OBJ_ID_TYPE_DISPLAY_NAME,
            participantObject.getParticipantObjectIDTypeCode().getDisplayName());
    }

    private void assertParticipantPatientObjectIdentification(ParticipantObjectIdentificationType participantObject) {
        assertEquals("ParticipantPatient.ParticipantObjectID mismatch", "D123401",
            participantObject.getParticipantObjectID());

        // TODO: assertSame vs assertEquals consistency when returning constants
        assertSame("ParticipantPatient.ParticipantObjectTypeCode object reference mismatch",
            DocRetrieveAuditTransformsConstants.PARTICIPANT_PATIENT_OBJ_TYPE_CODE,
            participantObject.getParticipantObjectTypeCode());
        assertSame("ParticipantPatient.ParticipantObjectTypeCodeRole object reference mismatch",
            DocRetrieveAuditTransformsConstants.PARTICIPANT_PATIENT_OBJ_TYPE_CODE_ROLE,
            participantObject.getParticipantObjectTypeCodeRole());
        assertEquals("ParticipantPatient.ParticipantObjectIDTypeCode.Code mismatch",
            DocRetrieveAuditTransformsConstants.PARTICIPANT_PATIENT_OBJ_ID_TYPE_CODE,
            participantObject.getParticipantObjectIDTypeCode().getCode());
        assertEquals("ParticipantPatient.ParticipantObjectIDTypeCode.CodeSystemName mismatch",
            DocRetrieveAuditTransformsConstants.PARTICIPANT_PATIENT_OBJ_ID_TYPE_CODE_SYSTEM,
            participantObject.getParticipantObjectIDTypeCode().getCodeSystemName());
        assertEquals("ParticipantPatient.ParticipantObjectIDTypeCode.DisplayName mismatch",
            DocRetrieveAuditTransformsConstants.PARTICIPANT_PATIENT_OBJ_ID_TYPE_DISPLAY_NAME,
            participantObject.getParticipantObjectIDTypeCode().getDisplayName());
    }

    private RetrieveDocumentSetRequestType createRetrieveDocumentSetRequest() {
        RetrieveDocumentSetRequestType request = new RetrieveDocumentSetRequestType();
        DocumentRequest docRequest = new DocumentRequest();
        docRequest.setDocumentUniqueId("D123401");
        docRequest.setHomeCommunityId("ihe:homeCommunityID");
        docRequest.setRepositoryUniqueId("ihe:RepositoryUniqueId");
        request.getDocumentRequest().add(docRequest);
        return request;
    }

    private RetrieveDocumentSetResponseType createRetrieveDocumentSetResponse() {
        RetrieveDocumentSetResponseType response = new RetrieveDocumentSetResponseType();
        DocumentResponse docResponse = new DocumentResponse();
        docResponse.setDocumentUniqueId("D123401");
        docResponse.setHomeCommunityId("ihe:homeCommunityID");
        docResponse.setRepositoryUniqueId("ihe:RepositoryUniqueId");
        response.getDocumentResponse().add(docResponse);
        return response;
    }

    @Override
    protected AuditTransforms<RetrieveDocumentSetRequestType, RetrieveDocumentSetResponseType> getAuditTransforms() {
        return new DocRetrieveAuditTransforms();
    }

    /**
     * This method is override in this test class in order to check RD specific testing
     *
     * @param request
     * @param isRequesting
     * @param webContextProperties
     * @param remoteObjectUrl
     * @throws UnknownHostException
     */
    @Override
    protected void testGetActiveParticipantSource(LogEventRequestType request, Boolean isRequesting,
        Properties webContextProperties, String remoteObjectUrl) throws UnknownHostException {

        String ipOrHost;
        ActiveParticipant sourceActiveParticipant = null;
        for (ActiveParticipant item : request.getAuditMessage().getActiveParticipant()) {
            if (item.getRoleIDCode().get(0).getDisplayName() != null && item.getRoleIDCode().get(0).getDisplayName()
                .equals(AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_SOURCE_DISPLAY_NAME)) {

                sourceActiveParticipant = item;
                break;
            }
        }

        assertNotNull("sourceActiveParticipant is null", sourceActiveParticipant);

        if (isRequesting) {
            assertEquals("AlternativeUserId mismatch", ManagementFactory.getRuntimeMXBean().getName(),
                sourceActiveParticipant.getAlternativeUserID());
        }

        ipOrHost = webContextProperties.getProperty(NhincConstants.REMOTE_HOST_ADDRESS);

        assertEquals("UserId mismatch", remoteObjectUrl, sourceActiveParticipant.getUserID());
        assertEquals("NetworkAccessPointID mismatch", ipOrHost, sourceActiveParticipant.getNetworkAccessPointID());
        assertEquals("SourceActiveParticipant requestor flag mismatch", isRequesting,
            sourceActiveParticipant.isUserIsRequestor());
        assertEquals("NetworkAccessPointTypeCode mismatch",
            AuditTransformsConstants.NETWORK_ACCESSOR_PT_TYPE_CODE_IP,
            sourceActiveParticipant.getNetworkAccessPointTypeCode());
        assertEquals("RoleIDCode.Code mistmatch", AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_SOURCE,
            sourceActiveParticipant.getRoleIDCode().get(0).getCode());
        assertEquals("RoleIDCode.CodeSystemName mismatch", AuditTransformsConstants.ACTIVE_PARTICIPANT_CODE_SYSTEM_NAME,
            sourceActiveParticipant.getRoleIDCode().get(0).getCodeSystemName());
        assertEquals("RoleIDCode.DisplayName mismatch",
            AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_SOURCE_DISPLAY_NAME,
            sourceActiveParticipant.getRoleIDCode().get(0).getDisplayName());
    }

    /**
     * This method is override in this test class in order to check RD specific testing
     *
     * @param request
     * @param isRequesting
     * @param webContextProperties
     * @param localIp
     */
    @Override
    protected void testGetActiveParticipantDestination(LogEventRequestType request, Boolean isRequesting,
        Properties webContextProperties, String localIp) {

        String userId;
        ActiveParticipant destinationActiveParticipant = null;
        for (ActiveParticipant item : request.getAuditMessage().getActiveParticipant()) {
            if (item.getRoleIDCode().get(0).getDisplayName() != null && item.getRoleIDCode().get(0).getDisplayName()
                .equals(AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_DESTINATION_DISPLAY_NAME)) {

                destinationActiveParticipant = item;
                break;
            }
        }

        assertNotNull("destinationActiveParticipant is null", destinationActiveParticipant);

        userId = NhincConstants.WSA_REPLY_TO;

        assertEquals("UserID mismatch", userId, destinationActiveParticipant.getUserID());
        assertEquals("DestinationActiveParticipant requestor flag mismatch", !isRequesting,
            destinationActiveParticipant.isUserIsRequestor());
        assertEquals("NetworkAccessPointID mismatch", localIp, destinationActiveParticipant.getNetworkAccessPointID());
        assertEquals("NetworkAccessPointTypeCode mismatch", AuditTransformsConstants.NETWORK_ACCESSOR_PT_TYPE_CODE_IP,
            destinationActiveParticipant.getNetworkAccessPointTypeCode());
        assertEquals("RoleIDCode.Code mismatch", AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_DEST,
            destinationActiveParticipant.getRoleIDCode().get(0).getCode());
        assertEquals("RoleIDCode.CodeSystemName mismatch", AuditTransformsConstants.ACTIVE_PARTICIPANT_CODE_SYSTEM_NAME,
            destinationActiveParticipant.getRoleIDCode().get(0).getCodeSystemName());
        assertEquals("RoleIDCode.DisplayName mismatch",
            AuditTransformsConstants.ACTIVE_PARTICIPANT_ROLE_CODE_DESTINATION_DISPLAY_NAME,
            destinationActiveParticipant.getRoleIDCode().get(0).getDisplayName());
    }
}
