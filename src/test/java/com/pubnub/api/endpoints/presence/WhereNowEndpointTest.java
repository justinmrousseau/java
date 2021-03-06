package com.pubnub.api.endpoints.presence;


import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import com.jayway.awaitility.Awaitility;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.WhereNowCallback;
import com.pubnub.api.PubNubException;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.consumer.presence.PNWhereNowResult;
import com.pubnub.api.endpoints.TestHarness;
import org.junit.Before;
import org.junit.Rule;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class WhereNowEndpointTest extends TestHarness {
    private PubNub pubnub;
    private WhereNow partialWhereNow;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule();

    @Before
    public void beforeEach() throws IOException {
        pubnub = this.createPubNubInstance(8080);
        partialWhereNow = pubnub.whereNow();
    }

    @org.junit.Test
    public void testSyncSuccess() throws IOException, PubNubException, InterruptedException {
        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        PNWhereNowResult response = partialWhereNow.sync();
        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @org.junit.Test
    public void testSyncSuccessCustomUUID() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/customUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        PNWhereNowResult response = partialWhereNow.uuid("customUUID").sync();
        assertThat(response.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testSyncBrokenWithString() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [zimp]}, \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testSyncBrokenWithoutJSON() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": zimp}, \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testSyncBrokenWithout200() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        partialWhereNow.sync();
    }

    @org.junit.Test
    public void testAsyncSuccess() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback(){

            @Override
            public void onResponse(PNWhereNowResult result, PNStatus status) {
                assertThat(result.getChannels(), org.hamcrest.Matchers.contains("a", "b"));
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));
    }

    @org.junit.Test
    public void testAsyncBrokenWithString() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [zimp]}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback(){

            @Override
            public void onResponse(PNWhereNowResult result, PNStatus status) {
                atomic.incrementAndGet();
            }

        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @org.junit.Test
    public void testAsyncBrokenWithoutJSON() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": zimp}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback(){

            @Override
            public void onResponse(PNWhereNowResult result, PNStatus status) {
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @org.junit.Test
    public void testAsyncBrokenWithout200() throws IOException, PubNubException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        final AtomicInteger atomic = new AtomicInteger(0);
        partialWhereNow.async(new WhereNowCallback(){

            @Override
            public void onResponse(PNWhereNowResult result, PNStatus status) {
                atomic.incrementAndGet();
            }
        });

        Awaitility.await().atMost(5, TimeUnit.SECONDS)
                .untilAtomic(atomic, org.hamcrest.core.IsEqual.equalTo(1));

    }

    @org.junit.Test
    public void testIsAuthRequiredSuccessSync() throws IOException, PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setAuthKey("myKey");
        PNWhereNowResult response = partialWhereNow.sync();

        List<LoggedRequest> requests = findAll(getRequestedFor(urlMatching("/.*")));
        assertEquals(1, requests.size());
        assertEquals("myKey", requests.get(0).queryParameter("auth").firstValue());
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testNullSubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey(null);
        PNWhereNowResult response = partialWhereNow.sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testEmptySubKeySync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"payload\": {\"channels\": [\"a\",\"b\"]}, \"service\": \"Presence\"}")));

        pubnub.getConfiguration().setSubscribeKey("");
        PNWhereNowResult response = partialWhereNow.sync();
    }

    @org.junit.Test(expected=PubNubException.class)
    public void testNullPayloadSync() throws PubNubException, InterruptedException {

        stubFor(get(urlPathEqualTo("/v2/presence/sub-key/mySubscribeKey/uuid/myUUID"))
                .willReturn(aResponse().withBody("{\"status\": 200, \"message\": \"OK\", \"service\": \"Presence\"}")));

        PNWhereNowResult response = partialWhereNow.sync();
    }

}
