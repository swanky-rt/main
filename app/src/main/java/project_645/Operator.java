package project_645;

public interface Operator {
    void open();

    boolean hasNext();

    Record next();
    void close();
}
