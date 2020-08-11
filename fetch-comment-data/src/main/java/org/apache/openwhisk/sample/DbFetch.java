package org.apache.openwhisk.sample;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.openwhisk.OWMainInterface;

@ApplicationScoped
public class DbFetch implements OWMainInterface {

    @Inject
    GreetingService service;

    @Override
    public JsonNode run(JsonNode rootNode) throws Exception {
        ObjectNode paramsNode = JsonNodeFactory.instance.objectNode();
        paramsNode.put("include_docs", true);
        paramsNode.put("descending", true);
        ObjectNode response = JsonNodeFactory.instance.objectNode();
        response.set("params", paramsNode);
        return response;
    }


}
