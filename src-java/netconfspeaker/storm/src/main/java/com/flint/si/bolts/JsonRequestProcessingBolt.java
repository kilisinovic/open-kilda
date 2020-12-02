package com.flint.si.bolts;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import com.flint.si.client.DatabaseClient;
import com.flint.si.context.EnvironmentContext;
import com.flint.si.context.KildaNamespaceContext;
import com.flint.si.models.yang_wrappers.ContainerWrapper;
import com.flint.si.models.yang_wrappers.ListEntryWrapper;
import com.google.gson.stream.JsonReader;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Tuple;
import org.flint.si.kilda.models.device.DeviceInfo;
import org.flint.si.kilda.models.messages.NetconfSpeakerRequest;
import org.flint.si.kilda.models.serialization.NetconfSpeakerRequestSerializer;
import org.opendaylight.yangtools.util.xml.UntrustedXML;
import org.opendaylight.yangtools.yang.data.api.schema.ContainerNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeWriter;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactory;
import org.opendaylight.yangtools.yang.data.codec.gson.JSONCodecFactorySupplier;
import org.opendaylight.yangtools.yang.data.codec.gson.JsonParserStream;
import org.opendaylight.yangtools.yang.data.codec.xml.XMLStreamNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.NormalizedNodeResult;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.test.util.YangParserTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class JsonRequestProcessingBolt extends BaseRichBolt {
    private static final String NETCONFSPEAKER_REQUEST_TOPIC = "netconfspeaker-request";

    Logger log = LoggerFactory.getLogger(JsonRequestProcessingBolt.class.getName());

    OutputCollector collector;
    EnvironmentContext envContext;
    EffectiveModelContext SCHEMA_CONTEXT;
    JSONCodecFactory lhotkaCodecFactory;
    KafkaProducer<String, NetconfSpeakerRequest> producer;
    private String bootstrapServers;

    public JsonRequestProcessingBolt(String bootstrapServers) {
        this.bootstrapServers = bootstrapServers;
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // we send result to kilda so no need to output anything here
    }

    @Override
    public void execute(Tuple input) {
        final String inputJson = input.getStringByField("value");
        ContainerWrapper request = new ContainerWrapper((ContainerNode) parseJsonAsNormalizedNode(inputJson));
        // ContainerWrapper devices = fetchDevicesDataFromDB();

        NetconfSpeakerRequest netconfRequest = null;
        log.info("OPERATION: " + request.leaf("operation").getValue());

        switch (request.leaf("operation").getValue()) {
            case "get-config":
                processGetConfig(input, request);
                break;
            case "edit-config":
                processEditConfig(input, request);
                break;
        }
    }

    private boolean sendNetconfSpeakerRequest(NetconfSpeakerRequest request) {
        ProducerRecord<String, NetconfSpeakerRequest> record = new ProducerRecord<>(NETCONFSPEAKER_REQUEST_TOPIC,
                request);
        AtomicBoolean result = new AtomicBoolean(false);
        producer.send(record, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                if (e == null) {
                    log.info("SENT MESSAGE TO KAFKA SUCCESSFULLY");
                    result.set(true);
                } else {
                    log.info("FAILED SENDING MESSAGE TO KAFKA");
                    result.set(false);
                }
            }
        });
        return result.get();
    }

    private ContainerWrapper fetchDataFromDB(String filter) throws IOException, SAXException {
        log.info("FETCH DATA");
        log.info("FILTER" + filter);
        DatabaseClient client = DatabaseClient.getInstance();
        String response = client.execute("get-config", filter, null, null);

        ContainerWrapper node = null;
        //TODO node = new ContainerWrapper((ContainerNode) parseJsonAsNormalizedNode(response));

        log.info("FETCH DATA: " + response);
        //log.info(node.unwrap().getNodeType().getLocalName());

        return node;
    }

    private ContainerWrapper fetchDevicesDataFromDB() throws IOException, SAXException {
        log.info("FETCH DEVICES");
        String filter = "<devices><authgroups/><device><name/><authgroup/><address/><port/></device></devices>";
        return fetchDataFromDB(filter);
    }

    private void processEditConfig(Tuple input, ContainerWrapper data) {

        boolean isDeviceConfig = checkIfIsDeviceConfig(data);
        if (isDeviceConfig) {
            NetconfSpeakerRequest request = prepareNetconfSpeakerRequest(data);
            boolean success = false;
            if (request != null) {
                success = sendNetconfSpeakerRequest(request);
            }
            if (success) {
                collector.ack(input);
            } else {
                collector.fail(input);
            }
        } else {
            DatabaseClient client = DatabaseClient.getInstance();
            String filter = generateXmlFromNode(data, "/request:request/dev3:devices");
            log.info("FILTER EDIT_CONFIG" + filter);
            String response = "";
            try {
                response = client.execute("edit-config", filter, "running", "merge");
                log.info("SOMETHING: " + response);
                collector.ack(input);
            } catch (IOException e) {
                log.info("IOException happened in edit-config while communicating with device");
                collector.fail(input);
            } catch (SAXException e) {
                log.info("SAXException happened in edit-config while communicating with device");
                collector.fail(input);
            }
            // TODO send FINAL response to kafka to say that action went through or that it
            // failed
        }
    }

    private NetconfSpeakerRequest prepareNetconfSpeakerRequest(ContainerWrapper data) {
        NetconfSpeakerRequest request = new NetconfSpeakerRequest();
        try {
            request.setAction(data.leaf("operation").getValue());
            request.setTransactionID(Integer.parseInt(data.leaf("transactionId").getValue()));
            ContainerWrapper devicesData = fetchDevicesDataFromDB();

            List<DeviceInfo> devices = new ArrayList<>();
            for (ListEntryWrapper deviceEntry : data.container("devices").list("device")) {
                String name = deviceEntry.leaf("name").getValue();
                ListEntryWrapper deviceData = devicesData.list("device").getEntry("name", name);

                String tmp = deviceData.leaf("address").getValue();
                String address = tmp == null ? deviceEntry.leaf("address").getValue() : tmp;

                tmp = deviceData.leaf("port").getValue();
                String port = tmp == null ? deviceEntry.leaf("port").getValue() : tmp;

                String payload = generateXmlFromNode(data, "/request:request/dev3:devices/dev3:device[dev3:name="+ name + "]/dev3:config");
                log.info("device " + name + " payload: " + payload);

                // TODO credentials
                DeviceInfo deviceInfo = new DeviceInfo(name, address, Integer.parseInt(port), "admin", "admin",
                        payload); // TODO get user and pass from netopeer
                devices.add(deviceInfo);
            }
            request.setDevices(devices);
            return request;
        } catch (Exception e) { // not good
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            log.info(sw.toString());
            return null; // also not good - try to figure out which part caused exception and log that
                         // shizzle
        }
    }

    private boolean checkIfIsDeviceConfig(ContainerWrapper data) {
        for (ListEntryWrapper deviceEntry : data.container("devices").list("device")) {
            ContainerWrapper config = deviceEntry.container("config");
            if (config != null)
                return true;
            // TODO : this is not valid check. one more check is needed to see if config has
            // child elements

        }

        return false;
    }

    public String 
    
    
    
    
    generateXmlFromNode(ContainerWrapper container, String xpathExpression) {
        final DOMResult domResult = new DOMResult(UntrustedXML.newDocumentBuilder().newDocument());
        XMLStreamWriter xmlStreamWriter;
        try {
            xmlStreamWriter = (XMLOutputFactory.newFactory()).createXMLStreamWriter(domResult);
            final NormalizedNodeStreamWriter xmlNormalizedNodeStreamWriter = XMLStreamNormalizedNodeStreamWriter
                    .create(xmlStreamWriter, SCHEMA_CONTEXT);
            final NormalizedNodeWriter normalizedNodeWriter = NormalizedNodeWriter
                    .forStreamWriter(xmlNormalizedNodeStreamWriter);
            normalizedNodeWriter.write(container.unwrap());

            StringWriter writer = new StringWriter();
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            if(xpathExpression != null){
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(new KildaNamespaceContext());
                
                Node node = (Node)xpath.evaluate(xpathExpression, domResult.getNode(), XPathConstants.NODE);
                
                DOMSource s = new DOMSource(node);
                transformer.transform(s, new StreamResult(writer));
                
            } else {
                transformer.transform(new DOMSource(domResult.getNode()), new StreamResult(writer));
            }
            String xml = writer.toString();
            return xml;

        } catch (XMLStreamException | FactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerFactoryConfigurationError e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (XPathExpressionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null; 
    }

    private void processGetConfig(Tuple input, ContainerWrapper data) {
        log.info("GET_CONFIG");
        try {
            //TODO log.info(generateXmlFromNode(fetchDevicesDataFromDB()));
            String xml = generateXmlFromNode(data, "/request:request/dev3:devices");
            log.info("GET REQUEST FILTER " + xml);
            fetchDataFromDB(xml);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        collector.ack(input);
    }

    private NormalizedNode<?, ?> parseJsonAsNormalizedNode(String inputJson) {
        final NormalizedNodeResult result = new NormalizedNodeResult();
        final NormalizedNodeStreamWriter streamWriter = ImmutableNormalizedNodeStreamWriter.from(result);

        final JsonParserStream jsonParser = JsonParserStream.create(streamWriter, lhotkaCodecFactory);
        jsonParser.parse(new JsonReader(new StringReader(inputJson)));

        return result.getResult();
    }

    @Override
    public void prepare(Map<String, Object> topoConf, TopologyContext context, OutputCollector collector) {
        this.collector = collector;
        this.envContext = EnvironmentContext.getInstance();

        
        this.SCHEMA_CONTEXT = YangParserTestUtils.parseYangResourceDirectory("/yang");
        lhotkaCodecFactory = JSONCodecFactorySupplier.DRAFT_LHOTKA_NETMOD_YANG_JSON_02
                .getShared(SCHEMA_CONTEXT);

        // configure producer properties
        Properties properties = new Properties();
        //topoConf.get(Config.TOPOLOGY_ENVIRONMENT)
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, NetconfSpeakerRequestSerializer.class.getName());

        producer = new KafkaProducer<>(properties);
    }
}
