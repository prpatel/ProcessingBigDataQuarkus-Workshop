package org.apache.openwhisk.sample;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.openwhisk.OWMainInterface;

import java.util.Iterator;
import java.util.Map;

@ApplicationScoped
public class DbTriggerAction implements OWMainInterface {

    @Inject
    GreetingService service;

    @Override
    public JsonNode run(JsonNode rootNode) throws Exception {
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        JsonNode docNode = rootNode.at("/doc");
        JsonNode ratings = docNode.at("/ratings");

        if ( ratings.isEmpty()) {
            // validation failed, in OpenWhisk we return a top level error with the message
            ObjectNode statusNode = JsonNodeFactory.instance.objectNode();
            statusNode.put("status", 412);
            statusNode.put("body", "No Ratings");
            response.set("error", statusNode);
        } else {
            // if everything is OK, send back the original data
            Iterator<java.util.Map.Entry<String, JsonNode>> ratingsNodes = ratings.fields();
            float total = 0;
            int count = 0;
            while(ratingsNodes.hasNext()) {
                java.util.Map.Entry<String, JsonNode> node = (java.util.Map.Entry<String, JsonNode>)ratingsNodes.next();
                count++;
                total += (float)node.getValue().asDouble();
            }
            System.out.println("Average Rating: "+total/count);
            ((ObjectNode)docNode).put("averageRating", total/count);
            response.set("doc", docNode);
        }
        return response;
    }


}
