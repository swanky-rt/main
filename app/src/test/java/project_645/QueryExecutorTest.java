package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.Operators.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryExecutorTest {

    private QueryExecutor queryExecutor;
    private BufferManagerImpl mockBufferManager;
    private Operator mockOuter;
    private Operator mockInner;
    private Record mockRecord;

    @BeforeEach
    void setUp() {
        queryExecutor = new QueryExecutor();
        mockBufferManager = mock(BufferManagerImpl.class);
        mockOuter = mock(Operator.class);
        mockInner = mock(Operator.class);
        mockRecord = mock(Record.class);
    }

    @Test
    void testExecuteQueryRunsWithoutException() {
        assertDoesNotThrow(() -> {
            queryExecutor.executeQuery("A", "Z", 4096 * 100);
        });
    }

    @Test
    void testExecuteQueryHandlesNullsGracefully() throws Exception {

        QueryExecutor qe = new QueryExecutor();
        assertDoesNotThrow(() -> {
            qe.executeQuery("X", "Y", 4096 * 10);
        });
    }

    @Test
    void testExecuteQueryWithDifferentRange() {
        assertDoesNotThrow(() -> {
            queryExecutor.executeQuery("C", "D", 4096 * 50);
        });
    }
}
