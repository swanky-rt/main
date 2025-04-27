package project_645.Operators;

import project_645.Record;

import java.io.IOException;

public interface Operator {
    void open();

    boolean hasNext() throws IOException;

    Record next() throws Exception;
    void close();
}
