package io.storj.libstorj;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class StorjTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public StorjTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( StorjTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testTimestamp()
    {
        assert(Storj.getTimestamp() > 0);
    }
}
