package org.rocket.network.props;

import org.rocket.network.PropId;

import java.util.Iterator;

interface ListPropId extends PropId, Iterable<PropId> {
    PropId head();
    ListPropId tail();

    @Override
    default Iterator<PropId> iterator() {
        return new Itr(this);
    }

    public static ListPropId cons(PropId head, ListPropId tail) {
        return new ConsPropId(head, tail);
    }

    public static final ListPropId Nil = new ListPropId() {
        @Override
        public PropId head() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ListPropId tail() {
            return this;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object obj) {
            return Nil == obj;
        }

        @Override
        public String toString() {
            return "Nil";
        }
    };

    static final class ConsPropId implements ListPropId {
        final PropId head;
        final ListPropId tail;

        public ConsPropId(PropId head, ListPropId tail) {
            this.head = head;
            this.tail = tail;
        }

        @Override
        public PropId head() {
            return head;
        }

        @Override
        public ListPropId tail() {
            return tail;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ConsPropId that = (ConsPropId) o;
            return head.equals(that.head) && tail.equals(that.tail);
        }

        @Override
        public int hashCode() {
            int result = head.hashCode();
            result = 31 * result + tail.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return head.toString() + " :: " + tail.toString();
        }
    }

    static final class Itr implements Iterator<PropId> {
        ListPropId cur;

        Itr(ListPropId cur) {
            this.cur = cur;
        }

        @Override
        public boolean hasNext() {
            return cur != Nil;
        }

        @Override
        public PropId next() {
            cur = cur.tail();
            return cur.head();
        }
    }
}
