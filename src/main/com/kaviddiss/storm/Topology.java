package com.kaviddiss.storm;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.topology.TopologyBuilder;

/**
 * Topology class that sets up the Storm topology for this sample.
 * Please note that Twitter credentials have to be provided as VM args, otherwise you'll get an Unauthorized error.
 *
 * @link http://twitter4j.org/en/configuration.html#systempropertyconfiguration
 */
public class Topology {

    static final String TOPOLOGY_NAME = "storm-twitter-word-count";

    public static void main(String[] args) {
        Config config = new Config();
        config.setMessageTimeoutSecs(120);

        TopologyBuilder b = new TopologyBuilder();
        b.setSpout("TwitterSampleSpout", new TwitterSampleSpout());
        b.setBolt("WordSplitterBolt", new WordSplitterBolt(5)).shuffleGrouping("TwitterSampleSpout");
        b.setBolt("WordsBolt", new WordsBolt()).shuffleGrouping("WordSplitterBolt");
        b.setBolt("WordCounterBolt", new WordCounterBolt(10, 5 * 60, 50)).shuffleGrouping("WordsBolt");

        final LocalCluster cluster = new LocalCluster();
        cluster.submitTopology(TOPOLOGY_NAME, config, b.createTopology());

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                cluster.killTopology(TOPOLOGY_NAME);
                cluster.shutdown();
            }
        });

    }

}
