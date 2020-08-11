package com.ibm.openwhisk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.openwhisk.OWMainInterface;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProcessComment implements OWMainInterface {
    @Override
    public JsonNode run(JsonNode rootNode) throws Exception {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        JsonNode docNode = rootNode.at("/doc");
        String reviewText = docNode.at("/text").asText();

        if (reviewText.isBlank() || reviewText.isEmpty()) {
            // validation failed, in OpenWhisk we return a top level error with the message
            ObjectNode statusNode = JsonNodeFactory.instance.objectNode();
            statusNode.put("status", 412);
            statusNode.put("body", "Invalid review, missing review text");
            response.set("error", statusNode);
        } else {
            // if everything is OK, send back the original data
            response.set("doc", docNode);
        }

        return response;
    }
}
