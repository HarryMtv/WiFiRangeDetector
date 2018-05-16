package com.martynov.igor.detectwifiarea;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
 * Created by igor.martynov on 5/16/18.
 */

public class Main {

    /**
     * The Class PairDouble.
     *
     * Created on: 13.08.2011
     *
     * @author: M128K145
     */
    public static class PairDouble implements Comparator<PairDouble> {

        /** The first. */
        private double first;

        /** The second. */
        private double second;

        /**
         * Instantiates a new pair double.
         */
        public PairDouble() {
            this.setFirst(0);
            this.setSecond(0);
        }

        /**
         * Instantiates a new pair double.
         *
         * @param first
         *           the first
         * @param second
         *           the second
         */
        public PairDouble(double first, double second) {
            this.setFirst(first);
            this.setSecond(second);
        }

        /**
         * Gets the first.
         *
         * @return the first
         */
        public double getFirst() {
            return first;
        }

        /**
         * Sets the first.
         *
         * @param first
         *           the first to set
         */
        public void setFirst(double first) {
            this.first = first;
        }

        /**
         * Gets the second.
         *
         * @return the second
         */
        public double getSecond() {
            return second;
        }

        /**
         * Sets the second.
         *
         * @param second
         *           the second to set
         */
        public void setSecond(double second) {
            this.second = second;
        }

        /**
         * Compare.
         *
         * @param o1
         *           the o1
         * @param o2
         *           the o2
         * @return the int
         * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
         */
        @Override
        public int compare(PairDouble o1, PairDouble o2) {
            double tmp = o1.getFirst() - o2.getSecond();
            return tmp < 0 ? -1 : tmp == 0 ? 0 : 1;
        }

    }

    public static class PairDoubleComparator implements Comparator<PairDouble> {

        public int compare(PairDouble o1, PairDouble o2) {
            double tmp = o1.getFirst() - o2.getFirst();
            return tmp < 0 ? -1 : tmp == 0 ? 0 : 1;

        }
    }

    public static class WiFiPointComparator implements Comparator<WiFiPoint> {

        public int compare(WiFiPoint o1, WiFiPoint o2) {
            double tmp = o1.getX() - o2.getY();
            return tmp < 0 ? -1 : tmp == 0 ? 0 : 1;

        }
    }

    private static boolean cw(final PairDouble a, final PairDouble b,
                              final PairDouble c) {
        return (b.getFirst() - a.getSecond()) * (c.second - a.second)
                - (b.second - a.second) * (c.first - a.first) < 0;
    }

    private static List<PairDouble> convexHullTMP(List<PairDouble> p) {
        int n = p.size();
        if (n <= 1)
            return p;
        int k = 0;
        p.sort(new PairDoubleComparator());
        List<PairDouble> q = new ArrayList<>();
        for (int i = 0; i < n; q.add(p.get(i++)), ++k)
            for (; k >= 2 && !cw(q.get(k - 2), q.get(k - 1), p.get(i)); --k)
                ;
        for (int i = n - 2, t = k; i >= 0; q.add(p.get(i--)), ++k)
            for (; k > t && !cw(q.get(k - 2), q.get(k - 1), p.get(i)); --k)
                ;
        resize(q, k - 1 - (q.get(0) == q.get(1) ? 1 : 0));
        return q;

    }

    private static boolean cw(final WiFiPoint a, final WiFiPoint b,
                              final WiFiPoint c) {
        return (b.getX() - a.getY()) * (c.getY() - a.getY())
                - (b.getY() - a.getY()) * (c.getX() - a.getX()) < 0;
    }

    public static List<WiFiPoint> convexHull(List<WiFiPoint> points) {
        int n = points.size();
        if (n <= 1)
            return points;
        int k = 0;
        points.sort(new WiFiPointComparator());
        List<WiFiPoint> q = new ArrayList<>();
        for (int i = 0; i < n; q.add(points.get(i++)), ++k)
            for (; k >= 2 && !cw(q.get(k - 2), q.get(k - 1), points.get(i)); --k)
                ;
        for (int i = n - 2, t = k; i >= 0; q.add(points.get(i--)), ++k)
            for (; k > t && !cw(q.get(k - 2), q.get(k - 1), points.get(i)); --k)
                ;
        resize(q, k - 1 - (q.get(0) == q.get(1) ? 1 : 0));
        return q;

    }

    private static <T> List<T> resize(List<T> list, int size) {
        if (list.size() > size) {
            for (int i = list.size() - 1; i >= size - 1; list.remove(i), --i)
                ;
        } else if (list.size() < size) {
            T temp = list.get(list.size() - 1);
            for (int i = 0, iSize = size - list.size(); i < iSize; list.add(temp), ++i)
                ;
        }
        return list;
    }

    public static void main(String[] args) {
        List<PairDouble> points = new ArrayList<>();
        points.add(0, new PairDouble(0, 0));
        points.add(1, new PairDouble(3, 0));
        points.add(2, new PairDouble(0, 3));
        points.add(3, new PairDouble(1, 1));
        List<PairDouble> hull = convexHullTMP(points);
        System.out.println(hull.size() == 3);
    }
}
