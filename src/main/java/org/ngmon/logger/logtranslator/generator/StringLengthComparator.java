package org.ngmon.logger.logtranslator.generator;

import java.util.Comparator;

/**
 * Class StringLengthComparator
 * compares two strings based on their length.
 */
public class StringLengthComparator implements Comparator<String> {

    public StringLengthComparator(){}

    /**
     * Compares two strings on length. If o1 is longer, -1 is returned.
     * Method returns +1 if o2 is longer. Zero, when they have equal length.
     * @param o1 first string
     * @param o2 second string to compare
     * @return -1 if o1, 0 is equals, +1 if o2 is longer
     */
    @Override
    public int compare(String o1, String o2) {
        if (o1.length() == o2.length()) {
            return 0;
        }

        if (o1.length() > o2.length()) {
            return -1;
        } else {
            return +1;
        }
    }
}