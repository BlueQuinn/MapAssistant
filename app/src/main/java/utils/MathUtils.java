/*
 * Copyright 2013 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils;

import com.google.android.gms.maps.model.LatLng;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.lang.Math.*;

/**
 * Utility functions that are used my both PolyUtil and SphericalUtil.
 */
class MathUtils
{

    public static LatLng[] getIntersection(LatLng pointA, LatLng pointB, LatLng center, double radius)
    {
        double baX = pointB.latitude - pointA.latitude;
        double baY = pointB.longitude - pointA.longitude;
        double caX = center.latitude - pointA.latitude;
        double caY = center.longitude - pointA.longitude;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0)
        {
            return null;
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        LatLng p1 = new LatLng(pointA.latitude - baX * abScalingFactor1,
                pointA.longitude - baY * abScalingFactor1);
        if (disc == 0)
        { // abScalingFactor1 == abScalingFactor2
            return new LatLng[]{p1};
        }
        LatLng p2 = new LatLng(pointA.latitude - baX * abScalingFactor2,
                pointA.longitude - baY * abScalingFactor2);
        return new LatLng[]{p1, p2};
    }


    /**
     * The earth's radius, in meters.
     * Mean radius as defined by IUGG.
     */
    static final double EARTH_RADIUS = 6371009;

    /**
     * Restrict x to the range [low, high].
     */
    static double clamp(double x, double low, double high)
    {
        return x < low ? low : (x > high ? high : x);
    }

    /**
     * Wraps the given value into the inclusive-exclusive interval between min and max.
     *
     * @param n   The value to wrap.
     * @param min The minimum.
     * @param max The maximum.
     */
    static double wrap(double n, double min, double max)
    {
        return (n >= min && n < max) ? n : (mod(n - min, max - min) + min);
    }

    /**
     * Returns the non-negative remainder of x / m.
     *
     * @param x The operand.
     * @param m The modulus.
     */
    static double mod(double x, double m)
    {
        return ((x % m) + m) % m;
    }

    /**
     * Returns mercator Y corresponding to latitude.
     * See http://en.wikipedia.org/wiki/Mercator_projection .
     */
    static double mercator(double lat)
    {
        return log(tan(lat * 0.5 + PI / 4));
    }

    /**
     * Returns latitude from mercator Y.
     */
    static double inverseMercator(double y)
    {
        return 2 * atan(exp(y)) - PI / 2;
    }

    /**
     * Returns haversine(angle-in-radians).
     * hav(x) == (1 - cos(x)) / 2 == sin(x / 2)^2.
     */
    static double hav(double x)
    {
        double sinHalf = sin(x * 0.5);
        return sinHalf * sinHalf;
    }

    /**
     * Computes inverse haversine. Has good numerical stability around 0.
     * arcHav(x) == acos(1 - 2 * x) == 2 * asin(sqrt(x)).
     * The argument must be in [0, 1], and the result is positive.
     */
    static double arcHav(double x)
    {
        return 2 * asin(sqrt(x));
    }

    // Given h==hav(x), returns sin(abs(x)).
    static double sinFromHav(double h)
    {
        return 2 * sqrt(h * (1 - h));
    }

    // Returns hav(asin(x)).
    static double havFromSin(double x)
    {
        double x2 = x * x;
        return x2 / (1 + sqrt(1 - x2)) * .5;
    }

    // Returns sin(arcHav(x) + arcHav(y)).
    static double sinSumFromHav(double x, double y)
    {
        double a = sqrt(x * (1 - x));
        double b = sqrt(y * (1 - y));
        return 2 * (a + b - 2 * (a * y + b * x));
    }

    /**
     * Returns hav() of distance from (lat1, lng1) to (lat2, lng2) on the unit sphere.
     */
    static double havDistance(double lat1, double lat2, double dLng)
    {
        return hav(lat1 - lat2) + hav(dLng) * cos(lat1) * cos(lat2);
    }
}