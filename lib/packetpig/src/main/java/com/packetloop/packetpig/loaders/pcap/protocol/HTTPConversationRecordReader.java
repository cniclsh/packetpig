package com.packetloop.packetpig.loaders.pcap.protocol;

import com.packetloop.packetpig.loaders.pcap.StreamingPcapRecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.pig.data.TupleFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class HTTPConversationRecordReader extends StreamingPcapRecordReader {
    protected BufferedReader reader;
    private String field;
    private String pathToTcp;

    public HTTPConversationRecordReader(String pathToTcp, String field) {
        this.pathToTcp = pathToTcp;
        this.field = field;
    }

    @Override
    public void initialize(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {
        super.initialize(split, context);
        streamingProcess(pathToTcp + " -r /dev/stdin -om http_headers -of tsv -o ", path, true);
        reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    }

    @Override
    public boolean nextKeyValue() throws IOException, InterruptedException {
        String line;

        while ((line = reader.readLine()) != null) {
            String parts[] = line.split("\t");
            key = (long)Double.parseDouble(parts[0]);
            tuple = TupleFactory.getInstance().newTuple();

            // s, src, sport, dst, dport
            int i;
            for (i = 1; i < 5; i++)
                tuple.append(parts[i]);

            while (i > 0 && i < parts.length) {
                if (parts[i].equals(field)) {
                    tuple.append(parts[i + 1]);
                    return true;
                }
                i++;
            }
        }

        return false;
    }
}
