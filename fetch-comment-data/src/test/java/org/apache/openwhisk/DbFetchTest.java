package org.apache.openwhisk;

import com.fasterxml.jackson.databind.JsonNode;
import io.quarkus.test.junit.QuarkusTest;
import org.apache.openwhisk.sample.DbFetch;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class DbFetchTest {

    @Inject
    DbFetch main;

    @Test
    public void testMain() throws Exception {
        String data = "{}\n";
        JsonNode node = OWJSONHelper.parseJSON(data);
        JsonNode resultNode =  main.run(OWJSONHelper.getValueNode(node));
        String expectedResult = "{\"params\": {\"include_docs\": true,\"descending\": true}}\n";
        JsonNode expectedNode = OWJSONHelper.parseJSON(expectedResult);
        Assertions.assertEquals(expectedNode, resultNode);
    }
}
