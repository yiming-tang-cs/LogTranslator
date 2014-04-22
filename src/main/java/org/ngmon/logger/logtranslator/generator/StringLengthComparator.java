package org.ngmon.logger.logtranslator.generator;

import java.util.Comparator;

public class StringLengthComparator implements Comparator<String> {

    public StringLengthComparator(){}

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