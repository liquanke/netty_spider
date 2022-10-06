package kfaka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.log4j.BasicConfigurator;

import java.util.Properties;

public class Producer {
    private  KafkaProducer<String, String> producer;
    private final static String TOPIC = "adienTest2";
    public Producer(){
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        //设置分区类,根据key进行数据分区
        producer = new KafkaProducer<String, String>(props);
    }
    public void produce(String data,String url){
        producer.send(new ProducerRecord<String, String>(TOPIC,url,data));
        System.out.println(data);
        producer.close();
    }

}
