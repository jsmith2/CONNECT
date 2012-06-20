/*
 * Copyright (c) 2012, United States Government, as represented by the Secretary of Health and Human Services. 
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
package gov.hhs.fha.nhinc.docretrieve.adapter.deferred.request.queue;

import gov.hhs.fha.nhinc.asyncmsgs.dao.AsyncMsgRecordDao;
import gov.hhs.fha.nhinc.asyncmsgs.model.AsyncMsgRecord;
import gov.hhs.fha.nhinc.common.nhinccommon.AssertionType;
import gov.hhs.fha.nhinc.common.nhinccommon.HomeCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunitiesType;
import gov.hhs.fha.nhinc.common.nhinccommon.NhinTargetCommunityType;
import gov.hhs.fha.nhinc.common.nhinccommonentity.RespondingGatewayCrossGatewayRetrieveRequestType;
import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author narendra.reddy
 */
import gov.hhs.fha.nhinc.docretrieve.adapter.deferred.request.queue.proxy.AdapterDocRetrieveDeferredRequestQueueProxyJavaImpl;
import gov.hhs.fha.nhinc.util.HomeCommunityMap;
import gov.hhs.healthit.nhin.DocRetrieveAcknowledgementType;
import ihe.iti.xds_b._2007.RetrieveDocumentSetRequestType;

public class AdapterDocRetrieveDeferredReqQueueProcessOrchImpl {

    private static Log log = LogFactory.getLog(AdapterDocRetrieveDeferredReqQueueProcessOrchImpl.class);

    public AdapterDocRetrieveDeferredReqQueueProcessOrchImpl() {
    }

    /**
     * processDocRetrieveDeferredReqQueue Orchestration method for processing request queues on responding gateway
     * 
     * @param messageId
     * @return DocRetrieveAcknowledgementType
     */
    public DocRetrieveAcknowledgementType processDocRetrieveDeferredReqQueue(String messageId) {
        log.info("Begin AdapterDocRetrieveDeferredReqQueueProcessOrchImpl.processDocRetrieveDeferredReqQueueWWW()....");
        // RetrieveDocumentSetResponseType ack = new RetrieveDocumentSetResponseType();
        DocRetrieveAcknowledgementType ack = new DocRetrieveAcknowledgementType();
        RetrieveDocumentSetRequestType retrieveDocumentSetRequestType = null;
        try {

            RespondingGatewayCrossGatewayRetrieveRequestType respondingGatewayCrossGatewayRetrieveRequestType = new RespondingGatewayCrossGatewayRetrieveRequestType();
            // Extract the Request from the DB for the given msgid.
            AsyncMsgRecord asyncMsgRecordFromDb = new AsyncMsgRecord();
            AsyncMsgRecordDao asyncDao = new AsyncMsgRecordDao();
            log.info("messageId: " + messageId);
            if ((messageId != null)) {
                List<AsyncMsgRecord> msgList = new ArrayList<AsyncMsgRecord>();
                msgList = asyncDao.queryByMessageIdAndDirection(messageId, AsyncMsgRecordDao.QUEUE_DIRECTION_INBOUND);
                if ((msgList != null) && (msgList.size() > 0)) {
                    log.info("msgList: " + msgList.size());
                    asyncMsgRecordFromDb = msgList.get(0);
                } else {
                    log.info("msgList: is null");
                }

            } else {
                log.info("messageId: is null");
            }

            log.info("AsyncMsgRecord - messageId: " + asyncMsgRecordFromDb.getMessageId());
            log.info("AsyncMsgRecord - serviceName: " + asyncMsgRecordFromDb.getServiceName());
            log.info("AsyncMsgRecord - creationTime: " + asyncMsgRecordFromDb.getCreationTime());

            AdapterDocRetrieveDeferredRequestQueueProxyJavaImpl adapterDocRetrieveDeferredRequestQueueProxyJavaImpl = new AdapterDocRetrieveDeferredRequestQueueProxyJavaImpl();

            if (asyncMsgRecordFromDb.getMsgData() != null) {
                respondingGatewayCrossGatewayRetrieveRequestType = extractRespondingGatewayRetrieveRequestType(asyncMsgRecordFromDb
                        .getMsgData());
            }
            if (respondingGatewayCrossGatewayRetrieveRequestType != null) {
                log.info("AsyncMsgRecord - messageId: "
                        + respondingGatewayCrossGatewayRetrieveRequestType.getAssertion().getMessageId());

                retrieveDocumentSetRequestType = respondingGatewayCrossGatewayRetrieveRequestType
                        .getRetrieveDocumentSetRequest();

                String senderTargetCommunityId = HomeCommunityMap
                        .getCommunityIdFromAssertion(respondingGatewayCrossGatewayRetrieveRequestType.getAssertion());

                // Set the Sender HomeCommunity Id in the NHINTargetCommunity to serve the Deferred Request.
                if (senderTargetCommunityId != null) {
                    log.info("SenderTargetCommunityId: " + senderTargetCommunityId);
                    NhinTargetCommunitiesType targetCommunities = new NhinTargetCommunitiesType();
                    NhinTargetCommunityType nhinTargetCommunityType = new NhinTargetCommunityType();
                    HomeCommunityType homeCommunityType = new HomeCommunityType();
                    homeCommunityType.setHomeCommunityId(senderTargetCommunityId);
                    nhinTargetCommunityType.setHomeCommunity(homeCommunityType);
                    targetCommunities.getNhinTargetCommunity().add(nhinTargetCommunityType);

                    // Generate new request queue assertion from original request message assertion
                    AssertionType assertion = respondingGatewayCrossGatewayRetrieveRequestType.getAssertion();

                    ack = adapterDocRetrieveDeferredRequestQueueProxyJavaImpl.crossGatewayRetrieveResponse(
                            retrieveDocumentSetRequestType, assertion, targetCommunities);

                } else {
                    log.error("Sender home is null - Unable to extract target community hcid from sender home");
                }
            }
        } catch (Exception e) {
            log.error("ERROR: Failed in accessing the async msg from repository.", e);
        }

        return ack;
    }

    /**
     * 
     * @param msgData
     * @return RespondingGatewayCrossGatewayRetrieveRequestType
     */
    private RespondingGatewayCrossGatewayRetrieveRequestType extractRespondingGatewayRetrieveRequestType(Blob msgData) {
        log.debug("Begin AdapterDocRetrieveDeferredReqQueueProcessOrchImpl.extractRespondingGatewayRetrieveRequestType()..");
        RespondingGatewayCrossGatewayRetrieveRequestType respondingGatewayCrossGatewayRetrieveRequestType = new RespondingGatewayCrossGatewayRetrieveRequestType();
        try {
            byte[] msgBytes = null;
            if (msgData != null) {
                msgBytes = msgData.getBytes(1, (int) msgData.length());
                ByteArrayInputStream xmlContentBytes = new ByteArrayInputStream(msgBytes);
                JAXBContext context = JAXBContext.newInstance("gov.hhs.fha.nhinc.common.nhinccommonentity");
                Unmarshaller u = context.createUnmarshaller();
                JAXBElement<RespondingGatewayCrossGatewayRetrieveRequestType> root = (JAXBElement<RespondingGatewayCrossGatewayRetrieveRequestType>) u
                        .unmarshal(xmlContentBytes);
                respondingGatewayCrossGatewayRetrieveRequestType = root.getValue();
                log.debug("End AdapterDocRetrieveDeferredReqQueueProcessOrchImpl.extractRespondingGatewayQueryRequestType()..");
            }
        } catch (Exception e) {
            log.error("Exception during Blob conversion :" + e.getMessage());
            e.printStackTrace();
        }
        return respondingGatewayCrossGatewayRetrieveRequestType;
    }

}