package org.rocket.network.event.acara;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        RocketDispatcherLookupTest.class,
        RocketDispatcherTest.class,
        RocketListenerMetadataLookupTest.class,
        RocketEventMetadataLookupTest.class
})
public class RocketAcaraTestSuite {
}
