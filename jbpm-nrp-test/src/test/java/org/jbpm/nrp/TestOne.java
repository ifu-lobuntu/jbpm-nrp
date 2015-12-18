package org.jbpm.nrp;

import org.jbpm.nrp.cfa.AbstractConversationForActionTest;
import org.junit.Test;

public class TestOne extends AbstractConversationForActionTest {
    public TestOne() {
        super();
    }
    @Test
    public void testIt() throws Exception{
        createConversationService();
    }
}
