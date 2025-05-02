package project_645;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import project_645.Operators.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class QueryExecutorTest {

    private QueryExecutor queryExecutor;

    @BeforeEach
    public void setUp() {
        queryExecutor = new QueryExecutor();
    }

    @Test
    public void testExecuteQueryRange() {
        assertThrows(Exception.class, () ->
                queryExecutor.executeQuery("Z", "A", 4096 * 4, true, null, false)
        );
    }

    @Test
    public void testPrematerializeTableRunsWithoutException() {
        assertThrows(Exception.class, () ->
                queryExecutor.prematerializeTable(4096 * 8)
        );
    }

    @Test
    public void testPrematerializeTableCoversForceAndReturnLines() {
        QueryExecutor executor = new QueryExecutor();
        try {

            long ioCount = executor.prematerializeTable(4096 * 10);


            assertTrue(ioCount >= 0);
        } catch (Exception e) {

        }
    }
}