package com.flint.si;

import java.util.HashMap;
import java.util.Map;

import com.flint.si.bolts.JsonRequestProcessingBolt;
import com.flint.si.context.EnvironmentContext;
import com.flint.si.spouts.SpoutFactory;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.generated.AlreadyAliveException;
import org.apache.storm.generated.AuthorizationException;
import org.apache.storm.generated.InvalidTopologyException;
import org.apache.storm.generated.StormTopology;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

public class NodeAdministratorTopology {
    private static final int TEN_MINUTES = 600000;
    private static final boolean IS_LOCAL_CLUSTER = false;

    public static void main(String[] args) {
        /*
         * We will set system env for hostname and port and docker secrets for passwords
         * or both set all with docker secrets! Credentials credentials = null;
         * DeviceInfo deviceInfo = null; String hostname; Integer port;
         * 
         * try { // we get the hostname and port from env hostname =
         * System.getenv("HOSTNAME"); port = Integer.parseInt(System.getenv("PORT"));
         * deviceInfo = new DeviceInfo(hostname, port);
         * 
         * // we get the credentials from file (docker secrets) // for now only password
         * is fetched, username(admin) is hardcoded byte[] readPass =
         * Files.readAllBytes(Paths.get("/run/secrets/..something")); // fix the path!
         * byte[] encodedBytes = Base64.getEncoder().encode(readPass); credentials = new
         * Credentials(password); } catch (IOException e) { e.printStackTrace(); }
         */

        TopologyBuilder builder = new TopologyBuilder();
        String bootstrapServers = EnvironmentContext.getInstance().getProperty("kafka.bootstrapServers");
        builder.setSpout("kilda-request-spout", SpoutFactory.getKafkaSpout(bootstrapServers),1);

        builder.setBolt("request-processing-bolt", new JsonRequestProcessingBolt(bootstrapServers),1).shuffleGrouping("kilda-request-spout", "node-administrator-request");

        Config config = new Config();
        config.setDebug(true);
        config.setNumWorkers(2);
        config.setMaxSpoutPending(5000);

        Map<String, Object> env = new HashMap<>();
        env.put("kafkaBootstrapServers", bootstrapServers);
        config.setEnvironment(env);

        StormTopology topology = builder.createTopology();

        if (IS_LOCAL_CLUSTER) {
            LocalCluster cluster;
            try {
                cluster = new LocalCluster();
                cluster.submitTopology("orchestrator-topology", config, topology);
                Utils.sleep(TEN_MINUTES);
                cluster.killTopology("orchestrator-topology");
                cluster.shutdown();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {
                StormSubmitter.submitTopology("mytopology", config, topology);
            } catch (AlreadyAliveException e) {
                // TODO print error to the user and write it in log
                e.printStackTrace();
            } catch (InvalidTopologyException e) {
                // TODO Print error to the user to review code
                e.printStackTrace();
            } catch (AuthorizationException e) {
                // TODO Log error and print it
                e.printStackTrace();
            }
        }

    }
}