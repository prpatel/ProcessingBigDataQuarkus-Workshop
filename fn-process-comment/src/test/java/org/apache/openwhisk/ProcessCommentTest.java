package org.apache.openwhisk;

import com.fasterxml.jackson.databind.JsonNode;
import com.ibm.openwhisk.ProcessComment;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

@QuarkusTest
public class ProcessCommentTest {

    @Inject
    ProcessComment main;

    @Test
    public void testMain() throws Exception {
        String data = "{\"value\":{\"doc\":{\"text\":\"hello\"}, \"__ow_debug\":true}}\n";
        JsonNode node = OWJSONHelper.parseJSON(data);
        JsonNode resultNode =  main.run(OWJSONHelper.getValueNode(node));

         String expectedResult = "{\"doc\":{\"text\":\"hello\"}}\n";
//        String expectedResult = "{\"text\":\"hello\"}\n";
        JsonNode expectedNode = OWJSONHelper.parseJSON(expectedResult);
        Assertions.assertEquals(expectedNode, resultNode);
    }
}
