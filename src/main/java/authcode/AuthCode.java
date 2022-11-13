package authcode;

import redis.clients.jedis.Jedis;

import java.util.Calendar;
import java.util.Scanner;

/**
 * @author swamp
 * @version 1.0
 * @description
 * @date 2022/11/13 下午4:08
 */
public class AuthCode {
    private Jedis jedis = new Jedis("127.0.0.1", 6379);
    private final static String COUNT_LIMIT = "countLimit";

    public static void main(String[] args) {
        AuthCode authCode = new AuthCode();
        while (true) {
            Scanner scanner = new Scanner(System.in);
            System.out.print("Please input your phone number: ");
            String phoneNum = scanner.nextLine();
            if (!authCode.sendAuthCode(phoneNum)) {
                System.out.println("The number has run out of times today.");
                continue;
            }
            System.out.println("The verification code has been sent to your mobile phone.");
            while (true) {
                System.out.print("Please input auth code: ");
                String code = scanner.nextLine();
                if (authCode.verify(phoneNum, code)) {
                    System.out.println("The verification code is correct.");
                    break;
                } else {
                    System.out.println("Incorrect verification code.");
                    String ifTryAgain = "";
                    while (true) {
                        System.out.println("Try again? yes or no.");

                        ifTryAgain = scanner.nextLine();
                        if (ifTryAgain.equals("yes") || ifTryAgain.equals("no")) {
                            break;
                        }
                    }
                    if (ifTryAgain.equals("no")) {
                        break;
                    }
                }
            }
        }
    }

    public boolean sendAuthCode(String phoneNum) {
        if (tryGetAuthCode(phoneNum)) {
            String authCode = getAuthCode();
            jedis.set(phoneNum, authCode);
            jedis.expire(phoneNum, 2 * 60);
            // send
            System.out.println("Auth Code: " + authCode);
            return true;
        } else {
            return false;
        }
    }

    public boolean verify(String phoneNum, String authCode) {
        return jedis.get(phoneNum).equals(authCode);
    }

    /**
     * Determine whether the sending times exceed the limit
     *
     * @param phoneNum
     * @return
     */
    private boolean tryGetAuthCode(String phoneNum) {
        if (jedis.hget(COUNT_LIMIT, phoneNum) == null) {
            jedis.hset(COUNT_LIMIT, phoneNum, "1");
            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 23, 59, 59);
            long endTime = calendar.getTime().getTime() / 1000;
            jedis.expireAt(COUNT_LIMIT, endTime);
            return true;
        } else if (Integer.parseInt(jedis.hget(COUNT_LIMIT, phoneNum)) < 3) {
            jedis.hincrBy(COUNT_LIMIT, phoneNum, 1);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return random verification code
     */
    private String getAuthCode() {
        StringBuilder authCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            authCode.append((int) (Math.random() * 10));
        }
        return new String(authCode);
    }
}