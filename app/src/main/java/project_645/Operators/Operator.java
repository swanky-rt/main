package project_645.Operators;

import project_645.File;
import project_645.Record;

import java.io.IOException;

public interface Operator {
    void open() throws Exception;

    boolean hasNext() throws IOException;

    Record next() throws Exception;
    void close();

    File getRelation();

    public void makeResetOperatorTrue() throws Exception;
}
