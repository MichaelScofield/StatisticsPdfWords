package me.lfc;

import redis.clients.jedis.Jedis;

import java.util.concurrent.Callable;

/**
 * User: luofucong
 * Date: 13-1-1
 */
public abstract class AbstractTranslator implements Callable<String> {

    protected Jedis jedis;

    public void setJedis(Jedis jedis) {
        this.jedis = jedis;
    }
}
