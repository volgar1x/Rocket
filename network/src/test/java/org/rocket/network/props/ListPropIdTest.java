package org.rocket.network.props;

import com.google.inject.name.Names;
import org.junit.Test;
import org.rocket.network.PropId;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ListPropIdTest {

    @Test
    public void testIterator() throws Exception {
        ListPropId pid = (ListPropId) PropIds.of(
            PropIds.annotation(Names.named("foo")),
            PropIds.annotation(Names.named("bar")));

        Iterator<PropId> it = pid.iterator();
        assertTrue("first iteration", it.hasNext());
        it.next();
        assertTrue("second iteration", it.hasNext());
        it.next();
        assertFalse("third iteration", it.hasNext());
    }

    @Test
    public void testUnwrapSingleton() throws Exception {
        PropId pid = PropIds.annotation(Names.named("foo"));
        PropId newPid = ListPropId.cons(pid, ListPropId.Nil).unwrapSingleton();
        PropId otherPid = ListPropId.cons(pid, ListPropId.cons(pid, ListPropId.Nil)).unwrapSingleton();

        assertTrue("unwrapped singleton", pid.equals(newPid));
        assertFalse("other unwrapper singleton", pid.equals(otherPid));
    }


}