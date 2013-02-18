package mock;

import junit.framework.TestCase;

import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.junit.runner.RunWith;

@SuppressWarnings("deprecation")
@RunWith(JMock.class)
public class NomicAgentMockTest extends TestCase {
	Mockery context = new JUnit4Mockery();
}
