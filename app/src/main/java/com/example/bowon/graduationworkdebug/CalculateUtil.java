package com.example.bowon.graduationworkdebug;

/**
 * Created by bowon on 2017-03-16.
 */

public class CalculateUtil {
    public static String parseAction(String action) {
        return (action.substring(action.indexOf(':') + 1, action.length()))
                .trim();
    }

    // 거리의 단위 변환
    public static String formatDist(float meters) {
        if (meters < 1000) {
            return ((int) meters) + "m";
        } else if (meters < 10000) {
            return formatDec(meters / 1000f, 1) + "km";
        } else {
            return ((int) (meters / 1000f)) + "km";
        }
    }

    // 수치의 변환. 소수점 한자리 까지만 수용한다
    static String formatDec(float val, int dec) {
        int factor = (int) Math.pow(10, dec);

        int front = (int) (val );
        int back = (int) Math.abs(val * (factor) ) % factor;

        return front + "." + back;
    }

    // 점이 지정된 공간 내에 있는지 판별한다
    public static boolean pointInside(float P_x, float P_y, float r_x,
                                      float r_y, float r_w, float r_h) {
        return (P_x > r_x && P_x < r_x + r_w && P_y > r_y && P_y < r_y + r_h);
    }

    // 중점과 점 사이의 각도를 구한다
    public static float getAngle(float center_x, float center_y, float post_x,
                                 float post_y) {
        float tmpv_x = post_x - center_x;
        float tmpv_y = post_y - center_y;
        float d = (float) Math.sqrt(tmpv_x * tmpv_x + tmpv_y * tmpv_y);
        float cos = tmpv_x / d;
        float angle = (float) Math.toDegrees(Math.acos(cos));

        angle = (tmpv_y < 0) ? angle * -1 : angle;
        return angle;
    }
}
